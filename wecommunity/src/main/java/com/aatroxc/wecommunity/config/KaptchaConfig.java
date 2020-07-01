package com.aatroxc.wecommunity.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author mafei007
 * @date 2020/4/2 17:31
 */

@Configuration
public class KaptchaConfig {


    @Bean
    public Producer kaptchaProducer(){
        DefaultKaptcha kaptcha = new DefaultKaptcha();

        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        // 生成的字符
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMLNOPQRSTUVWXYZ");
        // 生成字符的个数
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 图片的干扰
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");


        Config config = new Config(properties);
        kaptcha.setConfig(config);

        return kaptcha;
    }

}
