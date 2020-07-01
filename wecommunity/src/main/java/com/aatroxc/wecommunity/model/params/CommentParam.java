package com.aatroxc.wecommunity.model.params;

import com.aatroxc.wecommunity.model.entity.Comment;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.support.UserHolder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author mafei007
 * @date 2020/4/17 19:06
 */

@Data
public class CommentParam {

	@NotNull(message = "缺少entityType")
	private CommentEntityType entityType;

	@NotNull(message = "缺少entityId")
	private Integer entityId;

	private Integer targetId;

	@NotBlank(message = "评论内容不能为空")
	private String content;

	public Comment convertTo() {
		Comment comment = new Comment();
		comment.setEntityType(entityType);
		comment.setEntityId(entityId);
		comment.setTargetId(targetId == null ? 0 : targetId);
		comment.setContent(content);
		comment.setUserId(UserHolder.get().getId());
		comment.setStatus(0);
		comment.setCreateTime(new Date());
		return comment;
	}

}
