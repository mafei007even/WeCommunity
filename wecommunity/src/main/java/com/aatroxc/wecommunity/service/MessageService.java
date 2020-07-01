package com.aatroxc.wecommunity.service;

import com.aatroxc.wecommunity.model.dto.Page;
import com.aatroxc.wecommunity.model.enums.Topic;
import com.aatroxc.wecommunity.utils.MailClient;
import com.aatroxc.wecommunity.utils.SensitiveFilter;
import com.github.pagehelper.PageHelper;
import com.aatroxc.wecommunity.dao.MessageMapper;
import com.aatroxc.wecommunity.model.entity.Message;
import com.aatroxc.wecommunity.model.enums.MessageStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author mafei007
 * @date 2020/4/18 16:58
 */

@Service
@Slf4j
public class MessageService {

	private final MessageMapper messageMapper;
	private final SensitiveFilter sensitiveFilter;
	private final MailClient mailClient;

	@Value("${spring.mail.username}")
	private String systemEmail;

	public MessageService(MessageMapper messageMapper, SensitiveFilter sensitiveFilter, MailClient mailClient) {
		this.messageMapper = messageMapper;
		this.sensitiveFilter = sensitiveFilter;
		this.mailClient = mailClient;
	}

	public Message findMessageById(Integer msgId) {
		return messageMapper.selectByPrimaryKey(msgId);
	}

	public List<Message> findConversations(Integer userId, Page page) {
		PageHelper.startPage(page.getCurrent(), page.getLimit());
		return messageMapper.selectConversations(userId);
	}

	/**
	 * 可以从 PageHelper 中取分页信息
	 * @param userId
	 * @return
	 */
	public int findConversationCount(Integer userId) {
		return messageMapper.selectConversationCount(userId);
	}

	/**
	 *
	 * @param conversationId
	 * @param page
	 * @param userId 用来判断当前用户是否删除了某条消息
	 * @return
	 */
	public List<Message> findLetters(String conversationId, Page page, Integer userId) {
		PageHelper.startPage(page.getCurrent(), page.getLimit());
		return messageMapper.selectLetters(conversationId, userId);
	}

	/**
	 * 可以从 PageHelper 中取分页信息
	 * @param conversationId
	 * @param userId 用来判断当前用户是否删除了此条消息
	 * @return
	 */
	public int findLetterCount(String conversationId, Integer userId) {
		return messageMapper.selectLetterCount(conversationId, userId);
	}

	public int findLetterUnreadCount(Integer userId, String conversationId) {
		return messageMapper.selectLetterUnreadCount(userId, conversationId);
	}

	public int addMessage(Message message){
		Assert.notNull(message, "消息不能为空！");

		message.setContent(HtmlUtils.htmlEscape(message.getContent()));
		String originContent = message.getContent();
		String filterContent = sensitiveFilter.filter(originContent);
		message.setContent(filterContent);
		int rows = messageMapper.insertSelective(message);

		// 在插入数据之后记录日志，这样才能拿到回显的 id
		if (!originContent.equals(filterContent)) {
			String warnMsg = String.format("用户【id=%s】发送含有敏感词的私信【messageId=%s, conversationId=%s】！",
					message.getFromId(), message.getId(), message.getConversationId());
			log.warn(warnMsg);
			// mailClient.sendMail(systemEmail, "发现敏感词", warnMsg);
		}
		return rows;
	}

	/**
	 * 更改消息状态为已读
	 * @param ids 要更改的消息id
	 * @return 更改的个数
	 */
	public int readMessage(List<Integer> ids){
		if (CollectionUtils.isEmpty(ids)){
			return 0;
		}
		return messageMapper.updateStatus(ids, MessageStatus.READED);
	}

	/**
	 * 查询用户某个系统通知（点赞、评论、关注）下最新的一条通知
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	public Message findLatestNotice(Integer userId, Topic topic) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		return messageMapper.selectLatestNotice(userId, topic);
	}

	/**
	 * 查询用户某个系统通知（点赞、评论、关注）下的总通知数量
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	public int findNoticeCount(Integer userId, Topic topic) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		return messageMapper.selectNoticeCount(userId, topic);
	}


	/**
	 * 查询用户某个系统通知（点赞、评论、关注）未读通知的数量
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	public int findNoticeUnreadCount(Integer userId, Topic topic) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		return messageMapper.selectNoticeUnreadCount(userId, topic);
	}

	/**
	 * 查询用户所有系统通知（点赞、评论、关注）未读通知的总数量
	 *
	 * @param userId
	 * @return
	 */
	public int findAllNoticeUnreadCount(Integer userId) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		return messageMapper.selectAllNoticeUnreadCount(userId);
	}

	/**
	 * 分页查询用户某个系统通知（点赞、评论、关注）的列表
	 * @param userId
	 * @param topic
	 * @param page
	 * @return
	 */
	public List<Message> findNotices(Integer userId, Topic topic, Page page) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		Assert.notNull(page, "分页参数不能为空");
		PageHelper.startPage(page.getCurrent(), page.getLimit());
		return messageMapper.selectNotices(userId, topic);
	}

	/**
	 * 删除私信的某一条消息
	 * @param letter
	 * @param userId
	 */
	public void deleteLetter(Message letter, Integer userId) {
		String deleteBy = letter.getDeleteBy();
		String wrap = "A" + userId + "A";
		// 包含说明已经删除过此条消息了
		if (StringUtils.contains(deleteBy, wrap)) {
			log.warn("疑是脚本要删除已经删除的会话消息，userId: " + userId);
			return;
		}
		deleteBy = StringUtils.isBlank(deleteBy) ? "" : deleteBy;
		letter.setDeleteBy(deleteBy + wrap);
		messageMapper.updateByPrimaryKeySelective(letter);
	}

	public void deleteNotice(Message notice) {
		// 已经删除
		if (notice.getStatus() == MessageStatus.DELETED) {
			return;
		}
		notice.setStatus(MessageStatus.DELETED);
		messageMapper.updateByPrimaryKeySelective(notice);
	}
}
