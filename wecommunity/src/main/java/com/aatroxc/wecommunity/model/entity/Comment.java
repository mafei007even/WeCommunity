package com.aatroxc.wecommunity.model.entity;

import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer userId;

	/**
	 * 帖子的评论
	 * 评论的评论
	 * 课程的评论
	 * ....
	 */
	private CommentEntityType entityType;

	/**
	 * 如果这条评论是帖子的直接评论，评论对应帖子或课程的 id
	 * <p>
	 * 如果这条评论是评论的评论（不管是不是回复），那么entityId 就对应评论的id (最顶级评论的id)
	 */
	private Integer entityId;

	/**
	 * 如果这条评论是帖子的直接评论，targetId = 0
	 * <p>
	 * 如果是评论的评论，非回复，   targetId = 0
	 * <p>
	 * 如果是评论的评论，且为回复， targetId = 回复的用户 userId
	 */
	private Integer targetId;
	private String content;
	private Integer status;
	private Date createTime;

}