package com.aatroxc.wecommunity.service;

import com.aatroxc.wecommunity.elasticsearch.model.EsDiscussPost;
import com.aatroxc.wecommunity.elasticsearch.repo.DiscussPostRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author mafei007
 * @date 2020/5/10 23:24
 */

@Service
public class ElasticsearchService {

	private final DiscussPostRepository discussPostRepository;
	private final ElasticsearchTemplate elasticsearchTemplate;

	public ElasticsearchService(DiscussPostRepository discussPostRepository, ElasticsearchTemplate elasticsearchTemplate) {
		this.discussPostRepository = discussPostRepository;
		this.elasticsearchTemplate = elasticsearchTemplate;
	}

	public void saveDiscussPost(EsDiscussPost post) {
		discussPostRepository.save(post);
	}

	public void deleteDiscussPost(Integer id) {
		discussPostRepository.deleteById(id);
	}

	public AggregatedPage<EsDiscussPost> searchDiscussPost(String keyword, int current, int limit) {

		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
				.withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				// Elasticsearch中的分页是从第0页开始
				.withPageable(PageRequest.of(current, limit))
				.withHighlightFields(
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
				).build();

		// 从 es 中查到的结果会交给 SearchResultMapper 处理，通过SearchResponse得到查到的值
		AggregatedPage<EsDiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, EsDiscussPost.class, new SearchResultMapper() {

			@Override
			public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
				SearchHits hits = searchResponse.getHits();
				// 有没有命中数据，就是有没有查到
				if (hits.getTotalHits() <= 0) {
					return null;
				}

				// 封装结果集，主要是加上高亮
				List<EsDiscussPost> list = new ArrayList<>();

				for (SearchHit hit : hits) {
					EsDiscussPost post = new EsDiscussPost();

					// _source 就是存储的内容
					Map<String, Object> sourceMap = hit.getSourceAsMap();

					String id = sourceMap.get("id").toString();
					post.setId(Integer.parseInt(id));

					String userId = sourceMap.get("userId").toString();
					post.setUserId(Integer.parseInt(userId));

					String status = sourceMap.get("status").toString();
					post.setStatus(Integer.parseInt(status));

					String type = sourceMap.get("type").toString();
					post.setType(Integer.parseInt(type));

					String score = sourceMap.get("score").toString();
					post.setScore(Double.parseDouble(score));

					String commentCount = sourceMap.get("commentCount").toString();
					post.setCommentCount(Integer.parseInt(commentCount));

					// 有可能查询的关键字在 title 中不存在
					String title = sourceMap.get("title").toString();
					post.setTitle(title);

					// 有可能查询的关键字在 content 中不存在
					String content = sourceMap.get("content").toString();
					post.setContent(content);

					String createTime = sourceMap.get("createTime").toString();
					post.setCreateTime(new Date(Long.parseLong(createTime)));

					// 处理高亮显示
					// 如果存在就覆盖掉之前没有设置高亮的
					HighlightField titleField = hit.getHighlightFields().get("title");
					if (titleField != null) {
						// 搜互联网寒冬，有可能title中匹配了多个？？
						post.setTitle(titleField.getFragments()[0].toString());
					}

					HighlightField contentField = hit.getHighlightFields().get("content");
					if (contentField != null) {
						// 搜互联网寒冬，有可能content中匹配了多个？？
						post.setContent(contentField.getFragments()[0].toString());

						// Arrays.stream(contentField.getFragments()).forEach(System.out::println);
					}

					list.add(post);
				}

				return new AggregatedPageImpl(list, pageable, hits.getTotalHits(), searchResponse.getAggregations(),
						searchResponse.getScrollId(), hits.getMaxScore());
			}

			@Override
			public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
				return null;
			}
		});

		return page;
	}

}
