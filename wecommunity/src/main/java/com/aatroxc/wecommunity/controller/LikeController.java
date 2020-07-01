package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.event.EventProducer;
import com.aatroxc.wecommunity.model.enums.Topic;
import com.aatroxc.wecommunity.model.event.Event;
import com.aatroxc.wecommunity.service.LikeService;
import com.aatroxc.wecommunity.utils.RedisKeyUtils;
import com.aatroxc.wecommunity.annotation.LoginRequired;
import com.aatroxc.wecommunity.model.dto.LikeDTO;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.enums.LikeStatus;
import com.aatroxc.wecommunity.model.support.BaseResponse;
import com.aatroxc.wecommunity.model.support.UserHolder;
import com.aatroxc.wecommunity.model.support.UserInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author mafei007
 * @date 2020/4/20 21:06
 */

@Controller
public class LikeController {

	private final LikeService likeService;
	private final EventProducer eventProducer;
	private final RedisTemplate redisTemplate;

	public LikeController(LikeService likeService, EventProducer eventProducer, RedisTemplate redisTemplate) {
		this.likeService = likeService;
		this.eventProducer = eventProducer;
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 点赞，点赞后还要发送消息，异步通知被赞者
	 *
	 * @param entityType   点赞类型：帖子、评论
	 * @param entityId     帖子或评论的 id
	 * @param entityUserId 被点赞的用户
	 * @param postId       给哪条帖子点的赞，或是在哪条帖子下给评论点的赞
	 * @return
	 */
	@LoginRequired
	@PostMapping("like")
	@ResponseBody
	public BaseResponse<LikeDTO> like(CommentEntityType entityType, Integer entityId, Integer entityUserId, Integer postId) {
		UserInfo userInfo = UserHolder.get();

		// 点赞
		likeService.like(userInfo.getId(), entityType, entityId, entityUserId);
		// 数量
		long likeCount = likeService.findEntityLikeCount(entityType, entityId);
		// 当前用户点赞状态
		LikeStatus likeStatus = likeService.findEntityLikeStatus(userInfo.getId(), entityType, entityId);
		// 返回的数据
		LikeDTO likeDTO = new LikeDTO();
		likeDTO.setLikeCount(likeCount);
		likeDTO.setLikeStatus(likeStatus);

		// 触发点赞事件，点赞发送通知，取消赞不发送通知，且自己给自己点赞就不用通知自己了
		if (likeStatus == LikeStatus.LIKE && !userInfo.getId().equals(entityUserId)) {
			Event event = Event.builder()
					.topic(Topic.Like)
					.userId(UserHolder.get().getId())
					.entityType(entityType)
					.entityId(entityId)
					.entityUserId(entityUserId)
					.build()
					// 不管是给帖子点赞还是帖子下的评论点赞，被赞者查看通知时都是跳到帖子页面
					.setData("postId", postId);
			//发送消息
			eventProducer.fireEvent(event);
		}


		// 触发发帖事件，只有给帖子点赞或取消点赞才会触发
		// 主要是为了更新索引库中帖子的点赞数量，让搜索出来的点赞数正常
		if (entityType == CommentEntityType.POST) {
			// 发帖事件没有 entityUserId 要通知的用户
			Event postEvent = Event.builder()
					.topic(Topic.Publish)
					.userId(userInfo.getId())
					.entityType(CommentEntityType.POST)
					.entityId(postId)
					.build();
			eventProducer.fireEvent(postEvent);

			// 只有对帖子点赞时才更新权重
			String redisKey = RedisKeyUtils.getPostScoreKey();
			redisTemplate.opsForSet().add(redisKey, postId);
		}

		return BaseResponse.ok(likeDTO);
	}

}
