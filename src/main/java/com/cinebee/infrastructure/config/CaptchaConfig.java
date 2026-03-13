package com.cinebee.infrastructure.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;

@Configuration
public class CaptchaConfig {
    @Bean
    public Producer captchaProducer() {
        Properties props = new Properties();
        props.put("kaptcha.image.width", "150");
        props.put("kaptcha.image.height", "50");
        props.put("kaptcha.textproducer.char.length", "5");
        props.put("kaptcha.textproducer.font.color", "black");
        Config config = new Config(props);
        return config.getProducerImpl();
    }
}

