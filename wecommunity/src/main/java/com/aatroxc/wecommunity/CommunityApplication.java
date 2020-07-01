package com.aatroxc.wecommunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

import javax.annotation.PostConstruct;


/**
 * @author mafei007
 * @date 2020/3/28 22:52
 */

@SpringBootApplication
@MapperScan("com.aatroxc.wecommunity.dao")
@EnableAsync
@EnableScheduling
public class CommunityApplication {

    @PostConstruct
    public void init(){
        // es redis
        // 解决 netty 启动冲突问题， 不设置会出错...
        // @see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
