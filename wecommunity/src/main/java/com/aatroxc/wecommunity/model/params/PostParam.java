package com.aatroxc.wecommunity.model.params;

import com.aatroxc.wecommunity.model.support.UserHolder;
import com.aatroxc.wecommunity.model.entity.DiscussPost;
import com.aatroxc.wecommunity.model.enums.DiscussPostStatus;
import com.aatroxc.wecommunity.model.enums.DiscussPostType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author mafei007
 * @date 2020/4/5 23:35
 */

@Data
public class PostParam {

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题字符长度不能超过{max}")
    private String title;


    @NotBlank(message = "帖子内容不能为空")
    private String content;


    public DiscussPost convertTo(){

        DiscussPost post = new DiscussPost();
        post.setUserId(UserHolder.get().getId());
        post.setTitle(title);
        post.setContent(content);
        post.setType(DiscussPostType.ORDINARY);
        post.setStatus(DiscussPostStatus.NORMAL);
        post.setCreateTime(new Date());
        post.setCommentCount(0);
        post.setScore(0d);

        return post;
    }

    public DiscussPost convertToForUpdate(Integer postId){
        DiscussPost post = new DiscussPost();
        post.setId(postId);
        post.setTitle(title);
        post.setContent(content);
        return post;
    }


}
