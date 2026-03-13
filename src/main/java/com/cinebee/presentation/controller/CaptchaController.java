package com.cinebee.presentation.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.code.kaptcha.Producer;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {
    @Autowired
    private Producer captchaProducer;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/captcha")
    public Map<String, String> getCaptcha() throws Exception {
        String capText = captchaProducer.createText();
        String key = UUID.randomUUID().toString();
        // LÆ°u captcha vÃ o Redis vá»›i TTL 3 phÃºt
        redisTemplate.opsForValue().set("captcha:" + key, capText, 3, TimeUnit.MINUTES);

        BufferedImage image = captchaProducer.createImage(capText);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        String base64Img = Base64.getEncoder().encodeToString(baos.toByteArray());

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImg", "data:image/jpeg;base64," + base64Img);
        return result;
    }
}

