package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.elasticsearch.model.EsDiscussPost;
import com.aatroxc.wecommunity.model.dto.Page;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.service.LikeService;
import com.aatroxc.wecommunity.service.UserService;
import com.aatroxc.wecommunity.model.vo.SearchResultVo;
import com.aatroxc.wecommunity.service.ElasticsearchService;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mafei007
 * @date 2020/5/11 0:09
 */

@Controller
public class SearchController {

	private final ElasticsearchService elasticsearchService;
	private final UserService userService;
	private final LikeService likeService;

	public SearchController(ElasticsearchService elasticsearchService, UserService userService, LikeService likeService) {
		this.elasticsearchService = elasticsearchService;
		this.userService = userService;
		this.likeService = likeService;
	}

	/**
	 * search?keyword=xxx
	 *
	 * @param keyword
	 * @param page
	 * @param model
	 * @return
	 */
	@GetMapping("search")
	public String search(String keyword, Page page, Model model) {
		// 搜索帖子
		AggregatedPage<EsDiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

		// 聚合数据
		List<SearchResultVo> searchResultVoList = null;
		if (searchResult != null) {
			searchResultVoList = searchResult.stream()
					.map(esPost -> {
						SearchResultVo vo = new SearchResultVo();
						vo.setPost(esPost);
						vo.setUser(userService.findUserById(esPost.getUserId()));
						vo.setLikeCount((int) likeService.findEntityLikeCount(CommentEntityType.POST, esPost.getId()));

						return vo;
					})
					.collect(Collectors.toList());
		}
		model.addAttribute("discussPosts", searchResultVoList);
		model.addAttribute("keyword", keyword);

		// 分页信息
		page.setPath("/search?keyword=" + keyword);
		page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());
		model.addAttribute("page", page);

		return "site/search";
	}

}
