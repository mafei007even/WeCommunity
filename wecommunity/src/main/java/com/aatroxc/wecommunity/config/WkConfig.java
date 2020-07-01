package com.aatroxc.wecommunity.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地程序 wkhtmltoimage命令配置，启动时创建目录
 *
 * @author mafei007
 * @date 2020/5/19 15:37
 */

@Slf4j
@Configuration
public class WkConfig {

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@PostConstruct
	public void init() throws IOException {
		// 创建 wk 图片目录
		Path path = Paths.get(wkImageStorage);
		if (!Files.isDirectory(path)) {
			Files.createDirectories(path);
			log.info("创建WK图片目录：" + wkImageStorage);
		}
	}

}
