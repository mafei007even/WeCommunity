package com.aatroxc.wecommunity.dao;

import com.aatroxc.wecommunity.model.entity.DiscussPost;
import com.aatroxc.wecommunity.elasticsearch.model.EsDiscussPost;
import com.aatroxc.wecommunity.model.enums.OrderMode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author mafei007
 * @date 2020/3/30 18:23
 */


public interface DiscussPostMapper extends Mapper<DiscussPost> {


    List<DiscussPost> selectDiscussPosts(Integer userId, Integer offset, Integer limit, OrderMode orderMode);

    /**
     * 视频说如果只有一个参数，使用动态sql在 <if>中使用，必需使用别名@Param
     * @param userId
     * @return
     */
    Integer selectDiscussPostRows(@Param("userId") Integer userId);

    /**
     * 封装为 es 用的实体对象
     * @return
     */
    @Select("select * from discuss_post where status != 2")
    List<EsDiscussPost> selectAllDiscussPostsForEs();

}
