package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.annotation.LoginRequired;
import com.aatroxc.wecommunity.exception.NotFoundException;
import com.aatroxc.wecommunity.model.dto.Page;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.enums.Topic;
import com.aatroxc.wecommunity.model.enums.ValueEnum;
import com.aatroxc.wecommunity.model.params.MessageParam;
import com.aatroxc.wecommunity.model.support.BaseResponse;
import com.aatroxc.wecommunity.model.support.CommunityConstant;
import com.aatroxc.wecommunity.model.support.UserHolder;
import com.aatroxc.wecommunity.model.support.UserInfo;
import com.aatroxc.wecommunity.model.vo.LatestNoticeVO;
import com.aatroxc.wecommunity.model.vo.LetterVo;
import com.aatroxc.wecommunity.model.vo.NoticeVO;
import com.aatroxc.wecommunity.service.UserService;
import com.aatroxc.wecommunity.utils.JsonUtils;
import com.github.pagehelper.PageInfo;
import com.aatroxc.wecommunity.model.entity.Message;
import com.aatroxc.wecommunity.model.enums.MessageStatus;
import com.aatroxc.wecommunity.model.vo.ConversationVo;
import com.aatroxc.wecommunity.service.MessageService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mafei007
 * @date 2020/4/18 17:05
 */

@Controller
@Slf4j
public class MessageController {

	private final MessageService messageService;
	private final UserService userService;

	public MessageController(MessageService messageService, UserService userService) {
		this.messageService = messageService;
		this.userService = userService;
	}

	/**
	 * 私信列表
	 *
	 * @return
	 */
	@LoginRequired
	@GetMapping("letter/list")
	@ApiOperation("私信列表")
	public String getLetterList(Model model, Page page) {
		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();

		//分页信息
		page.setLimit(5);
		page.setPath("/letter/list");

		// 会话列表
		List<Message> conversationList = messageService.findConversations(userId, page);

		// page.setRows(messageService.findConversationCount(userInfo.getId()));
		// 使用 PageHelper 来获取分页信息
		PageInfo<Message> pageInfo = new PageInfo<>(conversationList);
		page.setRows((int) pageInfo.getTotal());

		// 每个会话需要的都放在 ConversationVo
		List<ConversationVo> conversationVos = new ArrayList<>();
		if (conversationList != null) {
			for (Message message : conversationList) {
				ConversationVo conversationVo = new ConversationVo();
				String conversationId = message.getConversationId();
				conversationVo.setConversation(message);
				conversationVo.setLetterCount(messageService.findLetterCount(conversationId, userId));
				conversationVo.setUnreadCount(messageService.findLetterUnreadCount(userId, conversationId));

				// 私信用户id，需要查出用户名和头像
				// 有可能此 Message 是当前用户发给别人，那目标id就是 to_id
				// 有可能此 Message 是别人发给当前用户，那目标id就是 from_id
				int targetId = userId.equals(message.getFromId()) ? message.getToId() : message.getFromId();
				conversationVo.setTarget(userService.findUserById(targetId));

				conversationVos.add(conversationVo);
			}
		}

		// 总未读数量
		int allLetterUnreadCount = messageService.findLetterUnreadCount(userId, null);

		// 查询系统通知未读消息数量
		int allNoticeUnreadCount = messageService.findAllNoticeUnreadCount(userId);
		model.addAttribute("allNoticeUnreadCount", allNoticeUnreadCount);

		model.addAttribute("page", page);
		model.addAttribute("allLetterUnreadCount", allLetterUnreadCount);
		model.addAttribute("conversations", conversationVos);
		return "site/letter";
	}

	@LoginRequired
	@GetMapping("letter/{conversationId}")
	@ApiOperation("查看私信详情，并且将未读的消息设为已读")
	public String getLetterDetail(@PathVariable String conversationId, Page page, Model model) {
		isOwner(conversationId);

		// 私信目标
		User target = getLetterTarget(conversationId);
		if (target == null) {
			return "error/404";
		}
		model.addAttribute("target", target);

		//分页信息
		page.setLimit(5);
		page.setPath("/letter/" + conversationId);

		// 私信列表
		List<Message> letterList = messageService.findLetters(conversationId, page, UserHolder.get().getId());

		// page.setRows(messageService.findLetterCount(conversationId));
		// 使用 PageHelper 来获取分页信息
		PageInfo<Message> pageInfo = new PageInfo<>(letterList);
		page.setRows((int) pageInfo.getTotal());

		List<LetterVo> letterVoList = new ArrayList<>();
		// 需要 from_user
		if (letterList != null) {
			for (Message message : letterList) {
				LetterVo letterVo = new LetterVo();
				letterVo.setLetter(message);
				letterVo.setFromUser(userService.findUserById(message.getFromId()));
				letterVoList.add(letterVo);
			}
		}

		// 将未读的消息设为已读
		List<Integer> ids = getUnreadLetterIds(letterList);
		messageService.readMessage(ids);

		model.addAttribute("letterVoList", letterVoList);
		return "site/letter-detail";
	}


	@LoginRequired
	@PostMapping("letter/send")
	@ResponseBody
	public BaseResponse sendLetter(@Valid MessageParam messageParam) {
		if (UserHolder.get().getUsername().equals(messageParam.getToName())) {
			return new BaseResponse(400, "不能给自己私信", null);
		}
		User target = userService.findUserByUsername(messageParam.getToName());
		if (target == null) {
			throw new NotFoundException("要发送私信的用户不存在");
		}

		Message message = messageParam.convertTo(target);
		messageService.addMessage(message);

		return BaseResponse.ok("发送私信成功！");
	}

	/**
	 * 删除会话中的某一条私信
	 * @param letterId
	 * @return
	 */
	@LoginRequired
	@DeleteMapping("letter")
	@ResponseBody
	@ApiOperation("删除会话中的某一条私信")
	public BaseResponse deleteLetter(@RequestParam Integer letterId) {
		Message letter = messageService.findMessageById(letterId);
		if (letter == null) {
			return new BaseResponse(400, "删除失败，消息不存在", null);
		}

		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();

		// 如果是系统通知类消息不给删除
		if (letter.getFromId().equals(CommunityConstant.SYSTEM_USER_ID)) {
			return new BaseResponse(403, "forbidden", null);
		}
		// 脚本执行才会出现
		if (!userId.equals(letter.getFromId()) && !userId.equals(letter.getToId())) {
			log.warn(String.format("尝试删除非自己的会话消息，userId: %s, letterId: %s", userId, letterId));
			return new BaseResponse(403, "forbidden", null);
		}
		// 更新
		// 如果消息的发送方要删除自己发的私信，这条私信对发送方来说肯定是已读的，此时允许发送方删除
		if (letter.getFromId().equals(userId)) {
			messageService.deleteLetter(letter, userId);
			return BaseResponse.ok("删除成功！");
		}
		//如果 status 为0未读状态，说明用户没有点开详情页，也就是使用脚本进行 delete msg ，此时就不允许 delete
		if (letter.getStatus() == MessageStatus.UNREAD) {
			log.warn(String.format("尝试删除自己的未读会话消息，userId: %s, letterId: %s", userId, letterId));
			return new BaseResponse(403, "forbidden", null);
		}

		messageService.deleteLetter(letter, userId);
		log.warn(String.format("用户【%s，id=%s】 删除了私信, id: %d", userInfo.getUsername(), userId, letterId));

		return BaseResponse.ok("删除成功！");
	}

	@LoginRequired
	@DeleteMapping("notice")
	@ResponseBody
	@ApiOperation("删除某一条系统通知")
	public BaseResponse deleteNotice(@RequestParam Integer noticeId) {
		Message notice = messageService.findMessageById(noticeId);
		if (notice == null) {
			return new BaseResponse(400, "删除失败，消息不存在", null);
		}
		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();

		// 删除非系统通知
		if (!notice.getFromId().equals(CommunityConstant.SYSTEM_USER_ID)) {
			return new BaseResponse(403, "forbidden", null);
		}

		if (!userId.equals(notice.getToId())) {
			log.warn(String.format("尝试删除非自己的系统通知，userId: %s, noticeId: %s", userId, noticeId));
			return new BaseResponse(403, "forbidden", null);
		}

		//如果 status 为0未读状态，说明用户没有点开通知详情页，也就是使用脚本进行 delete，此时就不允许 delete
		if (notice.getStatus() == MessageStatus.UNREAD) {
			log.warn(String.format("尝试删除自己的未读系统通知，userId: %s, noticeId: %s", userId, noticeId));
			return new BaseResponse(403, "forbidden", null);
		}

		messageService.deleteNotice(notice);
		log.warn(String.format("用户【%s，id=%s】 删除了系统通知, id: %d", userInfo.getUsername(), userId, noticeId));

		return BaseResponse.ok("删除成功！");
	}


	private List<Integer> getUnreadLetterIds(List<Message> letterList) {
		// 当前用户是消息的接收者才是进行读的操作
		// 如果是发送者，那不算已读，只有接收者才算已读
		if (letterList != null) {
			return letterList.stream()
					.filter(message -> UserHolder.get().getId().equals(message.getToId()) && message.getStatus() == MessageStatus.UNREAD)
					.map(Message::getId)
					.collect(Collectors.toList());
		}

		return null;
	}

	private User getLetterTarget(String conversationId) {
		String[] ids = conversationId.split("_");
		int id0 = Integer.parseInt(ids[0]);
		int id1 = Integer.parseInt(ids[1]);
		if (UserHolder.get().getId().equals(id0)) {
			return userService.findUserById(id1);
		}
		return userService.findUserById(id0);
	}

	private void isOwner(String conversationId) {
		String[] ids = conversationId.split("_");
		Arrays.stream(ids)
				.filter(id -> UserHolder.get().getId().toString().equals(id))
				.findAny()
				.orElseThrow(() -> new RuntimeException("没有权限访问非自己的消息！"));
	}

	@LoginRequired
	@GetMapping("notice/list")
	@ApiOperation("查询用户最新一条的（点赞、评论、关注）通知")
	public String getNoticeList(Model model) {
		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();

		// 查询评论类通知
		LatestNoticeVO commentNoticeVO = getLatestMessageVo(userId, Topic.Comment);
		model.addAttribute("commentNotice", commentNoticeVO);

		// 查询点赞类通知
		LatestNoticeVO likeNoticeVO = getLatestMessageVo(userId, Topic.Like);
		model.addAttribute("likeNotice", likeNoticeVO);

		// 查询关注类通知
		LatestNoticeVO followNoticeVO = getLatestMessageVo(userId, Topic.Follow);
		model.addAttribute("followNotice", followNoticeVO);

		// 查询系统通知未读消息数量
		int allNoticeUnreadCount = messageService.findAllNoticeUnreadCount(userId);
		model.addAttribute("allNoticeUnreadCount", allNoticeUnreadCount);

		// 用户私信总未读消息数量
		int allLetterUnreadCount = messageService.findLetterUnreadCount(userId, null);
		model.addAttribute("allLetterUnreadCount", allLetterUnreadCount);

		return "site/notice";
	}

	/**
	 * 查询用户不同类型的最新一条通知
	 * @param userId
	 * @param topic 点赞、评论、关注
	 * @return
	 */
	private LatestNoticeVO getLatestMessageVo(Integer userId, Topic topic) {
		Message latestTopicNotice = messageService.findLatestNotice(userId, topic);
		// message vo
		LatestNoticeVO noticeVO = new LatestNoticeVO();
		if (latestTopicNotice != null) {
			noticeVO.setMessage(latestTopicNotice);

			String jsonContent = HtmlUtils.htmlUnescape(latestTopicNotice.getContent());
			Map<String, Object> content = JsonUtils.jsonToMap(jsonContent, String.class, Object.class);

			noticeVO.setUser(userService.findUserById((Integer) content.get("userId")));

			CommentEntityType entityType = ValueEnum.valueToEnum(CommentEntityType.class, (Integer) content.get("entityType"));
			noticeVO.setEntityType(entityType);

			int count = messageService.findNoticeCount(userId, topic);
			noticeVO.setCount(count);

			int unread = messageService.findNoticeUnreadCount(userId, topic);
			noticeVO.setUnread(unread);
		}
		return noticeVO;
	}

	@LoginRequired
	@GetMapping("notice/detail/{topic}")
	public String getNoticeDetail(@PathVariable Topic topic, Page page, Model model) {
		UserInfo userInfo = UserHolder.get();

		page.setLimit(5);
		page.setPath("/notice/detail/" + topic.getValue());

		List<Message> notices = messageService.findNotices(userInfo.getId(), topic, page);
		// 使用 PageHelper 来获取分页信息，总行数
		PageInfo<Message> pageInfo = new PageInfo<>(notices);
		page.setRows((int) pageInfo.getTotal());

		List<NoticeVO> noticeVoList = notices.stream()
				.map(notice -> {
					NoticeVO noticeVo = new NoticeVO();
					noticeVo.setNotice(notice);

					String jsonContent = HtmlUtils.htmlUnescape(notice.getContent());
					Map<String, Object> content = JsonUtils.jsonToMap(jsonContent, String.class, Object.class);

					noticeVo.setUser(userService.findUserById((Integer) content.get("userId")));

					CommentEntityType entityType = ValueEnum.valueToEnum(CommentEntityType.class, (Integer) content.get("entityType"));
					noticeVo.setEntityType(entityType);
					noticeVo.setEntityId((Integer) content.get("entityId"));

					// 关注类通知不需要 postId 参数
					if (topic != Topic.Follow) {
						noticeVo.setPostId((Integer) content.get("postId"));
					}
					// 通知者，就是 id 为 1 的系统管理员
					noticeVo.setFromUser(userService.findUserById(notice.getFromId()));

					return noticeVo;
				}).collect(Collectors.toList());

		model.addAttribute("notices", noticeVoList);
		model.addAttribute("page", page);
		model.addAttribute("topicType", topic);

		// 设置已读
		List<Integer> unreadIds = getUnreadLetterIds(notices);
		if (!CollectionUtils.isEmpty(unreadIds)) {
			messageService.readMessage(unreadIds);
		}

		return "site/notice-detail";
	}

}
