package com.aatroxc.wecommunity.model.vo;

import com.aatroxc.wecommunity.elasticsearch.model.EsDiscussPost;
import com.aatroxc.wecommunity.model.entity.User;
import lombok.Data;

/**
 * 搜索结果
 *
 * @author mafei007
 * @date 2020/5/11 0:13
 */

@Data
public class SearchResultVo {

	/**
	 * 帖子
	 */
	private EsDiscussPost post;

	/**
	 * 发帖者
	 */
	private User user;

	/**
	 * 帖子点赞数
	 */
	private int likeCount;

}
