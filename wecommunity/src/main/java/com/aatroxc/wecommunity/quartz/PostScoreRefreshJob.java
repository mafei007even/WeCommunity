package com.aatroxc.wecommunity.quartz;

import com.aatroxc.wecommunity.model.entity.DiscussPost;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.enums.DiscussPostStatus;
import com.aatroxc.wecommunity.service.DiscussPostService;
import com.aatroxc.wecommunity.service.LikeService;
import com.aatroxc.wecommunity.utils.DateUtils;
import com.aatroxc.wecommunity.utils.RedisKeyUtils;
import com.aatroxc.wecommunity.elasticsearch.model.EsDiscussPost;
import com.aatroxc.wecommunity.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.Month;

/**
 * 刷新帖子的权重
 * 且要更新到 es 中
 * @author mafei007
 * @date 2020/5/18 21:14
 */

@Slf4j
public class PostScoreRefreshJob implements Job {

	private final RedisTemplate redisTemplate;
	private final DiscussPostService discussPostService;
	private final LikeService likeService;
	private final ElasticsearchService elasticsearchService;

	/**
	 * 社区创立的时间，用于计算权重
	 */
	private static final LocalDateTime epoch;

	static {
		epoch = LocalDateTime.of(2014, Month.AUGUST, 1, 0, 0, 0);
	}

	public PostScoreRefreshJob(RedisTemplate redisTemplate, DiscussPostService discussPostService, LikeService likeService, ElasticsearchService elasticsearchService) {
		this.redisTemplate = redisTemplate;
		this.discussPostService = discussPostService;
		this.likeService = likeService;
		this.elasticsearchService = elasticsearchService;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String redisKey = RedisKeyUtils.getPostScoreKey();
		BoundSetOperations ops = redisTemplate.boundSetOps(redisKey);

		if (ops.size() == 0) {
			log.info("[任务取消] 没有需要更新权重的帖子！");
			return;
		}
		log.info("[任务开始] 正在更新帖子权重, size: " + ops.size());
		while (ops.size() > 0) {
			refresh((Integer) ops.pop());
		}
		log.info("[任务结束] 帖子权重更新完毕！");
	}

	private void refresh(Integer postId) {
		DiscussPost post = discussPostService.findDiscussPostById(postId);
		// 没查到可能就是后面被管理员把帖子给删了
		if (post == null) {
			log.error("[更新权重中..] 帖子不存在, id: " + postId);
			return;
		}

		double score = getWeight(post);
		// 更新帖子权重
		discussPostService.updateScore(post.getId(), score);

		// 同步搜索数据
		post.setScore(score);
		elasticsearchService.saveDiscussPost(EsDiscussPost.convertTo(post));
	}

	/**
	 * 计算帖子的权重
	 *    log(精华分 + 评论数*10 + 点赞数*2 + 收藏数*2) + (发布时间 - 社区纪元)[天]
	 * @param post
	 * @return
	 */
	private double getWeight(DiscussPost post) {
		// 是否精华贴
		boolean wonderful = post.getStatus() == DiscussPostStatus.ESSENCE;
		// 评论数量
		Integer commentCount = post.getCommentCount();
		// 点赞数
		long likeCount = likeService.findEntityLikeCount(CommentEntityType.POST, post.getId());

		// 计算权重
		double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
		double score = Math.log10(Math.max(weight, 1))
				+ (DateUtils.date2LocalDate(post.getCreateTime()).toEpochDay() - epoch.toLocalDate().toEpochDay());
		return score;
	}

}
