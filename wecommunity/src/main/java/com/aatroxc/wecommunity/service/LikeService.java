package com.aatroxc.wecommunity.service;

import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import com.aatroxc.wecommunity.model.enums.LikeStatus;
import com.aatroxc.wecommunity.utils.RedisKeyUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author mafei007
 * @date 2020/4/19 22:28
 */

@Service
public class LikeService {

	private final RedisTemplate<String, Object> redisTemplate;

	public LikeService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}


	/**
	 * 点赞
	 * redis 中的结构：
	 * 		k  -> v
	 * 		like:entity:entityType:entityId -> set(userId)
	 *
	 * 被赞的用户结构：
	 * 		like:user:entityUserId -> int
	 * 		存的是被赞数
	 *
	 * 当前用户没点过赞，那就执行点赞
	 * 如果点过赞，那就是取消点赞
	 *
	 * @param userId 谁点的赞
	 * @param entityType 点赞的实体类型
	 * @param entityId 具体的实体
	 * @param entityUserId 被赞的用户，可能是帖子被赞、评论被赞
	 */
	public void like(Integer userId, CommentEntityType entityType, Integer entityId, Integer entityUserId){
		Assert.notNull(userId, "点赞的用户id不能为空");
		Assert.notNull(entityType, "点赞的实体类型不能为空");
		Assert.notNull(entityId, "点赞的实体id不能为空");
		Assert.notNull(entityUserId, "被赞的用户id不能为空");

		redisTemplate.execute(new SessionCallback<Object>() {
			@Override
			public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
				String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
				String userLikeKey = RedisKeyUtils.getUserLikeKey(entityUserId);

				// 查询要放在事务之外
				// 判断有没有点过赞，如果点过那就是取消点赞
				Boolean isMember = operations.opsForSet().isMember((K) entityLikeKey, userId);

				// 开启事务
				operations.multi();

				if (isMember) {
					// 移除点赞者，就是当前的用户
					operations.opsForSet().remove((K) entityLikeKey, userId);
					// 被赞者的被赞数 -1
					operations.opsForValue().decrement((K) userLikeKey);
				} else {
					// 增加点赞者，就是当前的用户
					operations.opsForSet().add((K) entityLikeKey, (V) userId);
					// 被赞者的被赞数 +1
					operations.opsForValue().increment((K) userLikeKey);
				}

				// 执行事务
				return operations.exec();
			}
		});
	}

	/**
	 * 查询实体点赞的数量
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public long findEntityLikeCount(CommentEntityType entityType, Integer entityId){
		String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
		Long size = redisTemplate.opsForSet().size(entityLikeKey);
		return size == null ? 0 : size;
	}

	/**
	 * 查询某人对某实体的点赞状态，就是有没有点过赞
	 * 返回 boolean 就行，但这里为了扩展，有可能点的是踩，返回一个枚举，来表示3中状态
	 * @param userId
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public LikeStatus findEntityLikeStatus(Integer userId, CommentEntityType entityType, Integer entityId){
		Assert.notNull(userId, "点赞的用户id不能为空");
		Assert.notNull(entityType, "点赞的实体类型不能为空");
		Assert.notNull(entityId, "点赞的实体id不能为空");

		String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
		Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
		return isMember ? LikeStatus.LIKE : LikeStatus.NONE;
	}

	/**
	 * 查询某个用户获得的赞的个数
	 * @param userId
	 * @return
	 */
	public int findUserLikeCount(Integer userId){
		String userLikeKey = RedisKeyUtils.getUserLikeKey(userId);
		Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
		return count == null ? 0 : count;
	}


}
