package com.aatroxc.wecommunity.elasticsearch.repo;

import com.aatroxc.wecommunity.elasticsearch.model.EsDiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author mafei007
 * @date 2020/5/10 17:52
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<EsDiscussPost, Integer> {
}
