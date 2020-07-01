package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.event.EventProducer;
import com.aatroxc.wecommunity.model.enums.Topic;
import com.aatroxc.wecommunity.model.event.Event;
import com.aatroxc.wecommunity.service.FollowService;
import com.aatroxc.wecommunity.annotation.LoginRequired;
import com.aatroxc.wecommunity.model.dto.Page;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.support.BaseResponse;
import com.aatroxc.wecommunity.model.support.UserHolder;
import com.aatroxc.wecommunity.model.support.UserInfo;
import com.aatroxc.wecommunity.model.vo.Followee;
import com.aatroxc.wecommunity.model.vo.Follower;
import com.aatroxc.wecommunity.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mafei007
 * @date 2020/5/2 22:39
 */

@Slf4j
@Controller
public class FollowController {

	private final FollowService followService;
	private final UserService userService;
	private final EventProducer eventProducer;

	public FollowController(FollowService followService, UserService userService, EventProducer eventProducer) {
		this.followService = followService;
		this.userService = userService;
		this.eventProducer = eventProducer;
	}

	@LoginRequired
	@PostMapping("follow")
	@ResponseBody
	public BaseResponse<Object> follow(CommentEntityType entityType, Integer entityId) {
		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();
		if (userId.equals(entityId)) {
			return new BaseResponse<>(400, "不能自己关注自己！", null);
		}
		User targetUserId = userService.findUserById(entityId);
		if (targetUserId == null) {
			return new BaseResponse<>(400, "关注用户不存在！", null);
		}
		followService.follow(userId, entityType, entityId);

		// 触发关注事件
		Event event = Event.builder()
				.topic(Topic.Follow)
				.userId(userId)
				.entityType(entityType)
				.entityId(entityId)
				// 现在功能只能关注用户，entityId 就是要通知的用户id
				// 如果可以关注帖子，那entityId就是帖子id，还需要根据帖子id查到发帖者id，发帖者id才是要通知的userId
				.entityUserId(entityId)
				.build();

		// 发送通知
		eventProducer.fireEvent(event);
		log.info(String.format("用户【%s，id=%s】 关注了用户【%s，id=%s】",
				userInfo.getUsername(), userId, targetUserId.getUsername(), targetUserId));

		return BaseResponse.ok("已关注！");
	}

	@LoginRequired
	@PostMapping("unfollow")
	@ResponseBody
	public BaseResponse<Void> unfollow(CommentEntityType entityType, Integer entityId) {
		UserInfo userInfo = UserHolder.get();
		User targetUserId = userService.findUserById(entityId);
		if (targetUserId == null) {
			return new BaseResponse<>(400, "取消关注用户不存在！", null);
		}
		followService.unfollow(userInfo.getId(), entityType, entityId);
		log.info(String.format("用户【%s，id=%s】 取消关注了用户【%s， id=%s】",
				userInfo.getUsername(), userInfo.getId(), targetUserId.getUsername(), targetUserId));
		return BaseResponse.ok("已取消关注！");
	}

	@GetMapping("followees/{userId}")
	@ApiOperation("根据userId查询该用户关注的人")
	public String getFollowees(@PathVariable Integer userId, Page page, Model model) {
		User user = userService.findUserById(userId);
		// 用户不存在
		if (user == null) {
			return "error/404";
		}

		// 设置分页数据
		page.setLimit(5);
		page.setPath("/followees/" + userId);
		page.setRows((int) followService.findFolloweeCount(userId, CommentEntityType.USER));

		List<Followee> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
		// 判断当前用户是否关注了查询用户的某些关注者
		if (!CollectionUtils.isEmpty(followees)) {
			followees = followees.stream()
					.peek(followee -> followee.setHasFollowed(hasFollowed(followee.getUser().getId())))
					.collect(Collectors.toList());
		}

		model.addAttribute("user", user);
		model.addAttribute("followees", followees);
		return "site/followee";
	}

	@GetMapping("followers/{userId}")
	@ApiOperation("根据userId查询该用户的粉丝")
	public String getFollowers(@PathVariable Integer userId, Page page, Model model) {
		User user = userService.findUserById(userId);
		// 用户不存在
		if (user == null) {
			return "error/404";
		}

		// 设置分页数据
		page.setLimit(5);
		page.setPath("/followers/" + userId);
		page.setRows((int) followService.findFollowerCount(CommentEntityType.USER, userId));

		List<Follower> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
		// 判断当前用户是否关注了查询用户的某些粉丝
		if (!CollectionUtils.isEmpty(followers)) {
			followers = followers.stream()
					.peek(follower -> follower.setHasFollowed(hasFollowed(follower.getUser().getId())))
					.collect(Collectors.toList());
		}

		model.addAttribute("user", user);
		model.addAttribute("followers", followers);
		return "site/follower";
	}

	/**
	 * 判断当前用户有没有关注指定的user
	 *
	 * @param userId
	 * @return
	 */
	private boolean hasFollowed(Integer userId) {
		if (UserHolder.get() == null) {
			return false;
		}
		return followService.hasFollowed(UserHolder.get().getId(), CommentEntityType.USER, userId);
	}

}
