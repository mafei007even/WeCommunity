package com.aatroxc.wecommunity.actuator;

import com.aatroxc.wecommunity.model.support.BaseResponse;
import com.aatroxc.wecommunity.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义 actuator 端点(endpoint)
 * 查看数据库连接是否正常
 * 在访问此端点时尝试获取一个数据库连接，能取到就是正常，没取到就有问题
 *
 * @author mafei007
 * @date 2020/5/21 17:01
 */

@Component
@Endpoint(id = "database")
@Slf4j
public class DatabaseEndpoint {

	private final DataSource dataSource;

	public DatabaseEndpoint(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * ReadOperation 代表 get 请求
	 * WriteOperation 代表 post 或 put
	 * actuator 返回的都是 json 字符串
	 *
	 * @return
	 */
	@ReadOperation
	public String checkConnection() {
		try (Connection conn = dataSource.getConnection()) {
			return JsonUtils.objectToJson(BaseResponse.ok("获取连接成功！"));
		} catch (SQLException e) {
			log.error("获取连接失败：" + e.getMessage(), e);
			return JsonUtils.objectToJson(new BaseResponse<>(1, "获取连接失败！", null));
		}
	}

}
