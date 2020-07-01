package com.aatroxc.wecommunity.event;

import com.aatroxc.wecommunity.model.event.Event;
import com.aatroxc.wecommunity.elasticsearch.model.EsDiscussPost;
import com.aatroxc.wecommunity.model.entity.DiscussPost;
import com.aatroxc.wecommunity.model.entity.Message;
import com.aatroxc.wecommunity.model.enums.MessageStatus;
import com.aatroxc.wecommunity.model.support.BaseResponse;
import com.aatroxc.wecommunity.model.support.CommunityConstant;
import com.aatroxc.wecommunity.service.DiscussPostService;
import com.aatroxc.wecommunity.service.ElasticsearchService;
import com.aatroxc.wecommunity.service.MessageService;
import com.aatroxc.wecommunity.utils.JsonUtils;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;


/**
 * @author mafei007
 * @date 2020/5/5 18:58
 */

@Component
@Slf4j
public class EventConsumer {

	private final MessageService messageService;
	private final DiscussPostService discussPostService;
	private final ElasticsearchService elasticsearchService;
	private final ThreadPoolTaskScheduler taskScheduler;

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@Value("${wk.image.command}")
	private String wkImageCommand;

	@Value("${qiniu.key.access}")
	private String accessKey;

	@Value("${qiniu.key.secret}")
	private String secretKey;

	@Value("${qiniu.bucket.share.name}")
	private String shareBucketName;

	private final String COMMENT = "comment";
	private final String LIKE = "like";
	private final String FOLLOW = "follow";
	private final String PUBLISH = "publish";
	private final String DELETE = "delete";
	private final String SHARE = "share";


	public EventConsumer(MessageService messageService, DiscussPostService discussPostService, ElasticsearchService elasticsearchService, ThreadPoolTaskScheduler taskScheduler) {
		this.messageService = messageService;
		this.discussPostService = discussPostService;
		this.elasticsearchService = elasticsearchService;
		this.taskScheduler = taskScheduler;
	}

	/**
	 * 将点赞、评论、关注这些系统通知 相关信息存入 message 表中
	 * COMMENT, LIKE, FOLLOW 这些 topic 要事先在 kafka 中创建！
	 *
	 * @param record
	 */
	@KafkaListener(topics = {COMMENT, LIKE, FOLLOW})
	public void handleCommentMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空");
			return;
		}

		Event event = JsonUtils.jsonToPojo(record.value().toString(), Event.class);
		if (event == null) {
			log.error("消息格式错误！");
			return;
		}

		// 发送站内通知
		Message message = new Message();
		// fromId 为 1 代表系统通知
		message.setFromId(CommunityConstant.SYSTEM_USER_ID);
		message.setToId(event.getEntityUserId());
		message.setConversationId(event.getTopic().getValue());
		message.setStatus(MessageStatus.UNREAD);
		message.setCreateTime(new Date());

		// 构建 content
		Map<String, Object> content = new HashMap<>();
		content.put("userId", event.getUserId());
		content.put("entityType", event.getEntityType().getValue());
		content.put("entityId", event.getEntityId());
		content.putAll(event.getData());
		message.setContent(JsonUtils.objectToJson(content));
		// 保存到数据库
		messageService.addMessage(message);
	}

	/**
	 * 消费发帖事件，将贴子添加到 es 索引库
	 * @param record
	 */
	@KafkaListener(topics = {PUBLISH})
	public void hadlePublishMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空");
			return;
		}

		Event event = JsonUtils.jsonToPojo(record.value().toString(), Event.class);
		if (event == null) {
			log.error("消息格式错误！");
			return;
		}

		DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
		EsDiscussPost esDiscussPost = EsDiscussPost.convertTo(post);
		elasticsearchService.saveDiscussPost(esDiscussPost);
	}

	/**
	 * 消费删帖事件，将贴子从 es 索引库 删除
	 * @param record
	 */
	@KafkaListener(topics = {DELETE})
	public void hadleDeleteMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空");
			return;
		}

		Event event = JsonUtils.jsonToPojo(record.value().toString(), Event.class);
		if (event == null) {
			log.error("消息格式错误！");
			return;
		}

		elasticsearchService.deleteDiscussPost(event.getEntityId());
	}

	/**
	 * 生成长图事件，利用 wkhtmltoimage 本地命令去生成图片
	 * @param record
	 */
	@KafkaListener(topics = {SHARE})
	public void hadleShareMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空");
			return;
		}

		Event event = JsonUtils.jsonToPojo(record.value().toString(), Event.class);
		if (event == null) {
			log.error("消息格式错误！");
			return;
		}

		String htmlUrl = event.getData().get("htmlUrl").toString();
		String fileName = event.getData().get("fileName").toString();
		String suffix = event.getData().get("suffix").toString();

		String localPath = wkImageStorage + "/" + fileName + suffix;
		String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " " + localPath;

		try {
			/**
			 * 调用操作系统本地的命令是异步的
			 * 也就是说下面这行代码会立即返回，往下执行，就有可能操作系统还没有执行完这个命令
			 *
			 */
			Runtime.getRuntime().exec(cmd);
			log.info("生成分享图成功：" + cmd);
		} catch (IOException e) {
			log.info("生成分享图失败：" + cmd, e);
		}

		// 上传七牛云
		// 启动定时器，监视图片是否生成
		// 这个定时器是 kafka 消费者的，会部署多台，谁抢到了这个消息，谁就开启定时
		UploadTask task = new UploadTask(fileName, suffix);
		// future 封装了任务的状态，还能够停止定时器
		// 因为指定了定时器间隔 500 毫秒后执行，所以下一行代码立刻把返回值 Future 设置给 Task，赶在任务执行之前就行
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(task, 500);
		task.setFuture(future);

	}

	class UploadTask implements Runnable {

		// 文件名称
		private String fileName;
		// 文件后缀
		private String suffix;
		// 启动任务的返回值，可以停止定时器
		private Future future;
		// 开始时间
		private long startTime;
		// 上传次数
		private int uploadTimes;

		public UploadTask(String fileName, String suffix) {
			this.fileName = fileName;
			this.suffix = suffix;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public void run() {
			String localPath = wkImageStorage + "/" + fileName + suffix;
			// 距离生成图片命令已经过了30秒就认为失败
			if (System.currentTimeMillis() - startTime > 30000) {
				log.error("执行时间过长，终止任务：" + localPath);
				// 取消定时任务
				future.cancel(true);
				return;
			}
			// 上传了3次还没成功 上传失败
			if (uploadTimes >= 3) {
				log.error("上传次数过多，终止任务：" + localPath);
				future.cancel(true);
				return;
			}

			Path path = Paths.get(localPath);
			if (Files.exists(path)) {
				log.info(String.format("开始第 %d 次上传【%s】.", ++uploadTimes, localPath));

				StringMap policy = new StringMap();
				policy.put("returnBody", JsonUtils.objectToJson(BaseResponse.ok("success")));
				Auth auth = Auth.create(accessKey, secretKey);
				String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);

				// 指定上传机房
				UploadManager manager = new UploadManager(new Configuration(Region.huadong()));
				try {
					// 开始上传图片
					Response response = manager.put(
							localPath, fileName, uploadToken, null, "image/" + suffix, false);
					// 处理响应结果
					Map<String, Object> json = JsonUtils.jsonToMap(response.bodyString(), String.class, Object.class);
					if (json == null || json.get("status") == null || !json.get("status").toString().equals("200")) {
						log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
					} else {
						log.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
						future.cancel(true);
					}
				} catch (QiniuException e) {
					log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
				}
			} else{
				log.info("等待图片生成：" + localPath);
			}

		}

		public void setFuture(Future future) {
			this.future = future;
		}
	}

}
