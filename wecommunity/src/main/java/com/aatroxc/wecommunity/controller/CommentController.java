package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.event.EventProducer;
import com.aatroxc.wecommunity.model.enums.Topic;
import com.aatroxc.wecommunity.model.event.Event;
import com.aatroxc.wecommunity.model.params.CommentParam;
import com.aatroxc.wecommunity.utils.RedisKeyUtils;
import com.aatroxc.wecommunity.annotation.LoginRequired;
import com.aatroxc.wecommunity.model.entity.Comment;
import com.aatroxc.wecommunity.model.entity.DiscussPost;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.support.UserHolder;
import com.aatroxc.wecommunity.model.support.UserInfo;
import com.aatroxc.wecommunity.service.CommentService;
import com.aatroxc.wecommunity.service.DiscussPostService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * @author mafei007
 * @date 2020/4/11 20:40
 */

@Slf4j
@Controller
@RequestMapping("comment")
public class CommentController {

	private final CommentService commentService;
	private final EventProducer eventProducer;
	private final DiscussPostService discussPostService;
	private final RedisTemplate redisTemplate;

	public CommentController(CommentService commentService, EventProducer eventProducer, DiscussPostService discussPostService, RedisTemplate redisTemplate) {
		this.commentService = commentService;
		this.eventProducer = eventProducer;
		this.discussPostService = discussPostService;
		this.redisTemplate = redisTemplate;
	}

	@LoginRequired
	@PostMapping("add/{discussPostId}")
	@ApiOperation("添加评论")
	public String addComment(@PathVariable Integer discussPostId, @Valid CommentParam commentParam) {
		Comment comment = commentParam.convertTo();
		commentService.addComment(comment);

		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();
		/**
		 触发事件对应的用户，就是要通知的用户
		 1. 如果这个评论是直接给帖子进行评论，那要通知的就是发帖者
		 2. 如果这个评论是给评论进行直接评论，非在评论下进行回复，那通知的就是原评论者
		 3. 如果这个评论是给评论进行评论，且评论为回复，那通知的就是评论下的被回复者
		 */
		Integer entityUserId = null;
		switch (comment.getEntityType()) {
			case POST:
				DiscussPost targetPost = discussPostService.findDiscussPostById(comment.getEntityId());
				entityUserId = targetPost.getUserId();
				break;
			case COMMENT:
				// targetId 就是 userId，不为0说明是回复操作
				if (comment.getTargetId() != 0) {
					entityUserId = comment.getTargetId();
					break;
				}
				Comment targetComment = commentService.findCommentById(comment.getEntityId());
				entityUserId = targetComment.getUserId();
				break;
			default:
		}

		// 如果是对自己进行以上 3 种评论操作，那就不用再发通知自己通知自己了
		if (!userId.equals(entityUserId)) {
			// 触发评论事件
			Event event = Event.builder()
					.topic(Topic.Comment)
					.userId(userId)
					.entityType(comment.getEntityType())
					.entityId(comment.getEntityId())
					.entityUserId(entityUserId)
					.build()
					// postId可以让被评论者点开通知时可以链接到帖子详情页面
					.setData("postId", discussPostId);
			// 发送消息
			eventProducer.fireEvent(event);
		}

		// 触发发帖事件，只有是给帖子进行直接评论才触发
		// 并没有在评论字段中进行搜索
		// 主要是为了更新索引库中帖子的评论数量，让搜索出来的评论数正常
		if (comment.getEntityType() == CommentEntityType.POST) {
			// 触发发帖事件，发帖事件没有 entityUserId 要通知的用户
			Event postEvent = Event.builder()
					.topic(Topic.Publish)
					.userId(userId)
					.entityType(CommentEntityType.POST)
					.entityId(discussPostId)
					.build();
			eventProducer.fireEvent(postEvent);

			// 给帖子进行直接评论时更新权重
			String redisKey = RedisKeyUtils.getPostScoreKey();
			redisTemplate.opsForSet().add(redisKey, discussPostId);
		}
		log.info(String.format("用户【%s，id=%s】 【评论/回复评论】了帖子: %d", userInfo.getUsername(), userId, discussPostId));
		return "redirect:/discuss/" + discussPostId;
	}

}
