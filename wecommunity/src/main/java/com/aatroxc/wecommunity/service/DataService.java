package com.aatroxc.wecommunity.service;

import com.aatroxc.wecommunity.utils.DateUtils;
import com.aatroxc.wecommunity.utils.RedisKeyUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mafei007
 * @date 2020/5/17 19:31
 */

@Service
public class DataService {

	private final RedisTemplate redisTemplate;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	public DataService(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 将指定的IP计入UV    Unique Visitor 独立访客
	 * <p>
	 * 没有登陆也算，将 IP 存入 HyperLogLog 数据结构中
	 * <p>
	 * 只要有用户访问，每天都会存入 redis
	 *    k  -->  v
	 *    uv:yyyyMMdd  -->  ....
	 *
	 * @param ip
	 */
	public void recordUV(String ip) {
		String redisKey = RedisKeyUtils.getUVKey(DateUtils.now(formatter));
		redisTemplate.opsForHyperLogLog().add(redisKey, ip);
	}

	/**
	 * 统计指定日期范围内的UV
	 * 比如统计 20200515 至 20200520 之间的 UV
	 * 包含 15 日，不包含 20 日
	 * 将这 5 天的 uv 合并成一个新的 redis 键值对，数据结构还是 HyperLogLog
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public long calculateUV(LocalDate start, LocalDate end) {
		Assert.notNull(start, "开始日期不能为空");
		Assert.notNull(end, "截至日期不能为空");

		// start 变量在下面的逻辑中会发送改变，先提前格式化String
		String startDate = start.format(formatter);

		// 整理日期范围内的 key
		List<String> keyList = new ArrayList<>();
		while (!start.isAfter(end)) {
			String key = RedisKeyUtils.getUVKey(start.format(formatter));
			keyList.add(key);
			start = start.plusDays(1);
		}

		// 合并这些数据
		String redisKey = RedisKeyUtils.getUVKey(startDate, end.format(formatter));
		// 运算的结果还是要存到 redis 中的
		redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

		// 返回统计的结果
		return redisTemplate.opsForHyperLogLog().size(redisKey);
	}

	/**
	 * 单日活跃用户 DAU   Daily Active User
	 * 将指定用户 id 计入 DAU
	 * 只统计登陆的，访问过一次就认为是活跃用户
	 * <p>
	 * redis 数据结构采用： Bitmap，实际上就是 redis 字符串，
	 *                   支持按位存取数据，可以看出 byte[] 数组，
	 *                   适合存储大量的连续的布尔值，默认没有设置的 bit 为 0，也就是false
	 * 用 userId 作为 byte[] 数组的索引，将下标对应的 userId 下标设置为 true
	 *     k --> v
	 *     dau:yyyyMMdd  -->  [0,0,0,0,1,1,1,1,0,1,0,0,.....]
	 *
	 * @param userId
	 */
	public void recordDAU(Integer userId) {
		Assert.notNull(userId, "userId不能为空");

		String redisKey = RedisKeyUtils.getDAUKey(DateUtils.now(formatter));
		redisTemplate.opsForValue().setBit(redisKey, userId, true);
	}

	/**
	 * 统计指定日期范围内的 DAU
	 * 如果要统计今天的活跃用户，只要你今天访问过一次，就算是活跃的
	 * 如果以 7 天为单位统计活跃用户，那么在 7 天内的任何一天访问过，就算是活跃
	 * <p>
	 * 拿到 n 天内的访问数据，做个 OR 运算即可
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public long calculateDAU(LocalDate start, LocalDate end) {
		Assert.notNull(start, "开始日期不能为空");
		Assert.notNull(end, "截至日期不能为空");

		// start 变量在下面的逻辑中会发送改变，先提前格式化String
		String startDate = start.format(formatter);

		// 整理日期范围内的 key， 要通过 byte[] 数组去访问 Bitmap
		List<byte[]> keyList = new ArrayList<>();
		while (!start.isAfter(end)) {
			String key = RedisKeyUtils.getDAUKey(start.format(formatter));
			keyList.add(key.getBytes());
			start = start.plusDays(1);
		}

		// 进行 OR 运算
		Object obj = redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				// 运算的结果还是要存到 redis 中的
				String redisKey = RedisKeyUtils.getDAUKey(startDate, end.format(formatter));
				connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), keyList.toArray(new byte[0][0]));
				// 统计 bit 为 1 的数量
				return connection.bitCount(redisKey.getBytes());
			}
		});
		return (long) obj;
	}

}
