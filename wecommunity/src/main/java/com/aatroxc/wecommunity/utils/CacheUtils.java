package com.aatroxc.wecommunity.utils;

import com.aatroxc.wecommunity.dao.DiscussPostMapper;
import com.aatroxc.wecommunity.model.entity.DiscussPost;
import com.aatroxc.wecommunity.model.enums.OrderMode;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author mafei007
 * @date 2020/5/21 0:09
 */

@Slf4j
@Component
public class CacheUtils {


	@Value("${caffeine.posts.max-size}")
	private int maxSize;

	@Value("${caffeine.posts.expire-seconds}")
	private int expireSeconds;

	private final DiscussPostMapper discussPostMapper;

	// Caffeine核心接口： Cache, 实现：LoadingCache, AsyncLoadingCache
	/**
	 * 帖子列表缓存
	 */
	public static LoadingCache<String, List<DiscussPost>> POST_LIST_CACHE;

	/**
	 * 帖子总数缓存
	 */
	public static LoadingCache<Integer, Integer> POST_ROWS_CACHE;

	@PostConstruct
	public void init() {
		// 初始化帖子列表缓存
		POST_LIST_CACHE = Caffeine.newBuilder()
				.maximumSize(maxSize)
				.expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
				// Caffeine 从缓存中取数据时，有就返回，没有的话，要告诉它怎么去查这个数据，然后装到缓存里
				// 所以要给它一个查询数据，得到数据的办法，就是这个 CacheLoader.load()
				.build(new CacheLoader<String, List<DiscussPost>>() {
					@Nullable
					@Override
					public List<DiscussPost> load(@NonNull String key) throws Exception {
						if (StringUtils.isBlank(key)) {
							throw new IllegalArgumentException("缓存参数 key 错误！");
						}
						String[] params = key.split(":");
						if (params.length != 2) {
							throw new IllegalArgumentException("缓存参数 key 错误: " + key);
						}
						int offset = Integer.parseInt(params[0]);
						int limit = Integer.parseInt(params[1]);

						// 在这里可以先去访问二级缓存 redis，没有命中再去 mysql
						// 二级缓存： Redis -> mysql

						log.debug("load post list from DB.");
						return discussPostMapper.selectDiscussPosts(0, offset, limit, OrderMode.HEAT);
					}
				});

		// 初始化帖子总数缓存，因为帖子总数量只有一个，这里 maxSize 只需要设置 1 其实就可以了
		POST_ROWS_CACHE = Caffeine.newBuilder()
				.maximumSize(1)
				.expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
				.build(new CacheLoader<Integer, Integer>() {
					@Nullable
					@Override
					public Integer load(@NonNull Integer key) throws Exception {
						log.debug("load post rows from DB.");
						return discussPostMapper.selectDiscussPostRows(key);
					}
				});
	}

	public CacheUtils(DiscussPostMapper discussPostMapper) {
		this.discussPostMapper = discussPostMapper;
	}
}
