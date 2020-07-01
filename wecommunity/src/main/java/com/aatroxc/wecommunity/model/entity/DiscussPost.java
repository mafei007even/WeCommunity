package com.aatroxc.wecommunity.model.entity;

import com.aatroxc.wecommunity.model.enums.DiscussPostStatus;
import com.aatroxc.wecommunity.model.enums.DiscussPostType;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "discuss_post")
public class DiscussPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer userId;
	private String title;
	private String content;
	private DiscussPostType type;
	private DiscussPostStatus status;
	private Date createTime;
	private Integer commentCount;
	private Double score;

}
