package com.aatroxc.wecommunity.service;

import com.aatroxc.wecommunity.utils.MailClient;
import com.github.pagehelper.PageHelper;
import com.aatroxc.wecommunity.dao.CommentMapper;
import com.aatroxc.wecommunity.model.dto.Page;
import com.aatroxc.wecommunity.model.entity.Comment;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.utils.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.util.HtmlUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author mafei007
 * @date 2020/4/6 19:17
 */

@Slf4j
@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final SensitiveFilter sensitiveFilter;
    private final DiscussPostService discussPostService;
    private final MailClient mailClient;

    @Value("${spring.mail.username}")
    private String systemEmail;

    public CommentService(CommentMapper commentMapper, SensitiveFilter sensitiveFilter, DiscussPostService discussPostService, MailClient mailClient) {
        this.commentMapper = commentMapper;
        this.sensitiveFilter = sensitiveFilter;
        this.discussPostService = discussPostService;
        this.mailClient = mailClient;
    }


    /**
     * 查询评论，根据创建时间升序
     * @param entityType 帖子评论、回复评论、课程评论...
     * @param entityId entityType对应的 id
     * @param page 第几页，每页几个，如果为null, 就查询全部
     * @return
     */
    public List<Comment> findCommentsByEntity(CommentEntityType entityType, Integer entityId, Page page) {
        if (page == null) {
            // pageSize 为 0 时查询所有
            // 第三个参数表示是否查询count(*) totalCount, 用于计算 totalPage
            // 当前场景下不需要，因为post表中存有count
            PageHelper.startPage(1, 0, false);
        } else {
            PageHelper.startPage(page.getCurrent(), page.getLimit(), false);
        }
        return commentMapper.findCommentsByEntity(entityType, entityId);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int addComment(Comment comment){
        Assert.notNull(comment, "参数不能为null");

        // 过滤敏感词
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));

        String originContent = comment.getContent();
        String filterContent = sensitiveFilter.filter(originContent);
        comment.setContent(filterContent);

        int rows = commentMapper.insertSelective(comment);
        if (!originContent.equals(filterContent)) {
            Integer postId = findPostIdOfComment(comment);
            String warnMsg = String.format("用户【id=%s】发布含有敏感词的评论【commentId=%s, postId=%s】！",
                    comment.getUserId(), comment.getId(), postId);
            log.warn(warnMsg);
            mailClient.sendMail(systemEmail, "发现敏感词", warnMsg);
        }

        // 更新帖子评论数量
        if (comment.getEntityType() == CommentEntityType.POST) {
            // 查询数量
            Example example = new Example(Comment.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("entityType", comment.getEntityType());
            criteria.andEqualTo("entityId", comment.getEntityId());
            int count = commentMapper.selectCountByExample(example);
            // 更新
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    public Comment findCommentById(Integer id) {
        return commentMapper.selectByPrimaryKey(id);
    }

    /**
     * select * from comment
     * where user_id = #{userId}
     * and status = 0
     * order by create_time desc
     * limit offset, limit
     *
     * @param userId
     * @param page
     * @return
     */
    public List<Comment> findCommentsByUser(Integer userId, Page page) {
        PageHelper.startPage(page.getCurrent(), page.getLimit(), "create_time desc");

        Example example = new Example(Comment.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("status", 0);
        return commentMapper.selectByExample(example);
    }

    private Integer findPostIdOfComment(Comment comment) {
        // 只有这个评论是给帖子进行评论时，对应的 entityId 才是帖子的 id
        while (comment.getEntityType() != CommentEntityType.POST) {
            // 回复的评论id
            Integer replyCommentId = comment.getEntityId();
            comment = this.findCommentById(replyCommentId);
        }
        return comment.getEntityId();
    }

}
