package com.aatroxc.wecommunity.elasticsearch.model;

import cn.hutool.core.bean.BeanUtil;
import com.aatroxc.wecommunity.model.entity.DiscussPost;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.util.Assert;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import java.util.Date;

/**
 *
 * 用于 es 中的实体
 * 主要变更就是 枚举改成了 int ，方便搜索排序
 *
 * 在 es 中的 @Document type 字段，新版本中被废弃
 * @author mafei007
 * @date 2020/5/10 21:29
 */
@Data
@Table(name = "discuss_post")
@Document(indexName = "discusspost", type = "_doc", shards = 1, replicas = 0)
public class EsDiscussPost {

	/**
	 * @Id 是 es 中的
	 */
	@Id
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Field(type = FieldType.Integer)
	private Integer userId;

	/**
	 * FieldType ->  Text（分词）、keyword（不分词）
	 * <p>
	 * analyzer = "ik_max_word" 存储时的分词器，能拆出更多的词
	 * 		比如：互联网校招，就可以拆为：
	 * 			互联网、联网、网校、校招、互联网校招
	 * <p>
	 * searchAnalyzer = "ik_smart" 搜索时的分词器，拆分较小的词汇
	 */
	@Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
	private String title;

	@Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
	private String content;

	/**
	 * 枚举类型会映射为：
	 * 	type: "text",
	 * 	fields: {
	 * 			keyword: {
	 *	 			type: "keyword",
	 * 				ignore_above: 256
	 * 			}
	 * 	}
	 *
	 * 	保存的值为：
	 * 		type: "ORDINARY",
	 * 		status: "NORMAL",
	 *
	 * 	需要保存为 int
	 *
	 */
	@Field(type = FieldType.Integer)
	private Integer type;

	@Field(type = FieldType.Integer)
	private Integer status;

	@Field(type = FieldType.Date)
	private Date createTime;

	@Field(type = FieldType.Integer)
	private Integer commentCount;

	@Field(type = FieldType.Double)
	private Double score;

	public static EsDiscussPost convertTo(DiscussPost post) {
		Assert.notNull(post, "要转换的 post 不能为 null");

		EsDiscussPost esPost = new EsDiscussPost();
		BeanUtil.copyProperties(post, esPost, "type", "status");

		esPost.setType(post.getType().getValue());
		esPost.setStatus(post.getStatus().getValue());

		return esPost;
	}

}
