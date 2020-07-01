package com.aatroxc.wecommunity.model.dto;

import com.aatroxc.wecommunity.model.enums.LikeStatus;
import lombok.Data;

/**
 * @author mafei007
 * @date 2020/4/20 21:16
 */

@Data
public class LikeDTO {

	private long likeCount;
	private LikeStatus likeStatus;

}
