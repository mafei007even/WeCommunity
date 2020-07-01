package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.service.LikeService;
import com.aatroxc.wecommunity.model.dto.Page;
import com.aatroxc.wecommunity.model.entity.DiscussPost;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.enums.OrderMode;
import com.aatroxc.wecommunity.service.DiscussPostService;
import com.aatroxc.wecommunity.service.UserService;
import com.aatroxc.wecommunity.utils.MailClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mafei007
 * @date 2020/3/30 19:17
 */

@Controller
public class HomeController {

    private final DiscussPostService discussPostService;
    private final LikeService likeService;
    private final UserService userService;
    private final MailClient mailClient;
    private final TemplateEngine templateEngine;


    public HomeController(DiscussPostService discussPostService, LikeService likeService, UserService userService, MailClient mailClient, TemplateEngine templateEngine) {
        this.discussPostService = discussPostService;
        this.likeService = likeService;
        this.userService = userService;
        this.mailClient = mailClient;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/")
    public String root() {
        return "forward:index";
    }

    /**
     * 首页的帖子列表有两种排序方式，由 OrderMode 传递
     *   0-按日期排序
     *   1-按热度排序
     * @param model
     * @param page
     * @param orderMode
     * @return
     */
    @GetMapping("index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(required = false, defaultValue = "0") OrderMode orderMode) {

        // 设置Page回显的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode.getValue());
        model.addAttribute("page", page);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                // 帖子，帖子的评论数也在这里
                map.put("post", post);
                // 帖子的发帖者
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                // 帖子的点赞数
                long likeCount = likeService.findEntityLikeCount(CommentEntityType.POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "index";
    }


    @GetMapping("error")
    public String getErrorPage(){
        return "error/500";
    }

    @GetMapping("denied")
    public String getDeniedPage(){
        return "error/404";
    }

}
