package com.aatroxc.wecommunity.dao;

import com.aatroxc.wecommunity.model.enums.Topic;
import com.aatroxc.wecommunity.model.entity.Message;
import com.aatroxc.wecommunity.model.enums.MessageStatus;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 *
 * count 方法用来计算分页用
 *
 * @author mafei007
 * @date 2020/4/18 15:16
 */


public interface MessageMapper extends Mapper<Message> {


	/**
	 * 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
	 * 且要分页查,limit 使用 PageHelper
	 *
	 * 每个会话会有多条message，最大的messageId 的那条就是最新的消息
	 *
		 select * from message
		 where id in
		 (
			 select max(id) from message
			 where from_id != 1
			 and status != 2
			 and (from_id = 111 or to_id= 111)
			 GROUP BY conversation_id
		 )
		 ORDER BY create_time desc
		 limit x,x
	 *
	 * 这里 order by 排序使用 id 和 create_time都可以.
	 * 因为 id 是自增长的，后面发的消息 id 肯定就大，后面发的消息create_time肯定也大
	 * 所以用哪个来排序都行
	 *
	 * ----------------------2020年5月11日更新
	 * 新增 delete_by
	 * 		and (delete_by is null or delete_by not like '%A#{userId}A%')
	 *
	 * 	为什么加上前面加上 delete_by is null ？
	 * 		因为数据库中 delete_by 存储默认为 null，null 对象不能进行 like 比较，会查不到东西
	 * 		百度说 null 对象做运算得到的是 Unknown 对象，此对象代表 false
	 * 		所以 delete_by like 'xx' 或者 delete_by not like 'xx' 结果都是 false
	 *
	 * @param userId
	 * @return
	 */
	@Select("select * from message " +
			"where id in " +
			"(" +
				"select max(id) from message " +
				"where from_id != 1 " +
				"and status != 2 " +
				"and (from_id = #{userId} or to_id = #{userId}) " +
				"and (delete_by is null or delete_by not like concat('%A', #{userId}, 'A%')) " +
				"GROUP BY conversation_id " +
			") " +
			"ORDER BY create_time desc")
	List<Message> selectConversations(Integer userId);


	/**
	 * 查询当前用户的会话数量
	 * 跟 selectConversations() 方法差不多
	 *
	 * select count(m.maxid) from
	 * (
	 *    select max(id) as maxid from message
	 *    where from_id != 1
	 *    and status != 2
	 *    and (from_id = 111 or to_id= 111)
	 *    GROUP BY conversation_id
	 * ) as m
	 *
	 * @param userId
	 * @return
	 */
	@Select("select count(m.maxid) from " +
			"(" +
				"select max(id) as maxid from message " +
				"where from_id != 1 " +
				"and status != 2 " +
				"and (from_id = #{userId} or to_id = #{userId}) " +
				"and (delete_by is null or delete_by not like concat('%A', #{userId}, 'A%')) " +
				"GROUP BY conversation_id" +
			") as m")
	int selectConversationCount(Integer userId);


	/**
	 * 查询某个会话所包含的私信列表，需要分页
	 * @param conversationId
	 * @return
	 */
	@Select("select * from message " +
			"where status != 2 " +
			"and from_id != 1 " +
			"and conversation_id = #{conversationId} " +
			"and (delete_by is null or delete_by not like concat('%A', #{userId}, 'A%')) " +
			"order by id desc")
	List<Message> selectLetters(String conversationId, Integer userId);


	/**
	 * 查询某个会话所包含的私信数量，跟 selectLetters() 差不多
	 * @param conversationId
	 * @param userId 用来判断当前用户是否删除了此条消息
	 * @return
	 */
	@Select("select count(id) from message " +
			"where status != 2 " +
			"and from_id != 1 " +
			"and conversation_id = #{conversationId} " +
			"and (delete_by is null or delete_by not like concat('%A', #{userId}, 'A%'))")
	int selectLetterCount(String conversationId, Integer userId);

	/**
	 * 查询未读私信的数量
	 * 不会出现用户删除了某条私信但 status=0 未读的情况，已经在代码中做了控制
	 * 有两处要用到：
	 * 		1. 需要所有的未读会话数量
	 * 		2. 需要某一个会话的未读数量
	 * 	如果	conversationId 传了就拼上条件
	 *
	 *   select count(id)
	 *   from message
	 *   where status = 0
	 *   and from_id != 1
	 *   and to_id = #{userId}
	 *   <if test="conversationId != null">
	 *       and conversation_id = #{conversationId}
	 *   </if>
	 *
	 * @param userId
	 * @param conversationId
	 * @return
	 */
	int selectLetterUnreadCount(Integer userId, String conversationId);

	/**
	 * 更新消息状态
	 *
	 *    update message
	 *    set status = #{status}
	 *    where id in
	 *    <foreach collection="ids" item="id" open="(" close=")" separator=",">
	 *        #{id}
	 *    </foreach>
	 *
	 * @param ids
	 * @param status
	 * @return
	 */
	int updateStatus(List<Integer> ids, MessageStatus status);


	/**
	 * 查询用户某个系统通知（点赞、评论、关注）下最新的一条通知
	 *
	 * SELECT * FROM `message`
	 * WHERE from_id = 1 and to_id = 112 AND conversation_id = 'like' AND `status` != 2
	 * ORDER BY create_time desc
	 * LIMIT 1
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	@Select("select * from message " +
			"where from_id = 1 " +
			"and to_id = #{userId} " +
			"and conversation_id = #{topic} " +
			"and status != 2 " +
			"order by create_time desc " +
			"limit 1")
	Message selectLatestNotice(Integer userId, Topic topic);

	/**
	 * 查询用户某个系统通知（点赞、评论、关注）下的总通知数量
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	@Select("select count(*) from message " +
			"where from_id = 1 " +
			"and to_id = #{userId} " +
			"and conversation_id = #{topic} " +
			"and status != 2 ")
	int selectNoticeCount(Integer userId, Topic topic);

	/**
	 * 查询用户某个系统通知（点赞、评论、关注）未读通知的数量
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	@Select("select count(*) from message " +
			"where from_id = 1 " +
			"and to_id = #{userId} " +
			"and conversation_id = #{topic} " +
			"and status = 0 ")
	int selectNoticeUnreadCount(Integer userId, Topic topic);

	/**
	 * 查询用户所有系统通知（点赞、评论、关注）未读通知的总数量
	 * @param userId
	 * @return
	 */
	@Select("select count(*) from message " +
			"where from_id = 1 " +
			"and to_id = #{userId} " +
			"and status = 0 ")
	int selectAllNoticeUnreadCount(Integer userId);


	/**
	 * 查询用户某个系统通知（点赞、评论、关注）的列表
	 * 支持分页，在 service 中由 PageHelper 完成
	 * @param userId
	 * @param topic
	 * @return
	 */
	@Select("select * from message " +
			"where status != 2 " +
			"and from_id = 1 " +
			"and to_id = #{userId} " +
			"and conversation_id = #{topic} " +
			"order by create_time desc ")
	List<Message> selectNotices(Integer userId, Topic topic);

}
