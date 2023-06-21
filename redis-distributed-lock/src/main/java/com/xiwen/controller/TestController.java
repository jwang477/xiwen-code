package com.xiwen.controller;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String prefix = "product:";
    private static final String lock = "lock";
    @Autowired
    private RedissonClient redissonClient;
    @GetMapping("buy1")
    public String buy1(String username, Long productId) {
        synchronized (TestController.class) {
            String key = prefix + productId;
            String value = redisTemplate.opsForValue().get(key);
            if (StringUtils.isBlank(value)) {
                return "商品不存在";
            }
            Long stock = Long.valueOf(value);
            if (stock > 0) {
                System.out.println("用户：" + username + "购买了第" + stock + "件商品");
                stock = stock - 1;
                redisTemplate.opsForValue().set(key, stock.toString());
                return "ok";
            } else {
                return "库存不足";
            }
        }
    }

    @GetMapping("buy2")
    public String buy2(String username, Long productId) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(lock, "1");
        if (flag){
            try {
                String key = prefix + productId;
                String value = redisTemplate.opsForValue().get(key);
                if (StringUtils.isBlank(value)) {
                    return "商品不存在";
                }
                Long stock = Long.valueOf(value);
                if (stock > 0) {
                    System.out.println("用户：" + username + "购买了第" + stock + "件商品");
                    stock = stock - 1;
                    redisTemplate.opsForValue().set(key, stock.toString());
                    return "ok";
                } else {
                    return "库存不足";
                }
            }finally {
                redisTemplate.delete(lock);
            }
        }else{
            return "当前访问量过大，请重试";
        }
    }

    @GetMapping("buy3")
    public String buy3(String username, Long productId) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(lock, "1",1l, TimeUnit.SECONDS);
        if (flag){
            try {
                String key = prefix + productId;
                String value = redisTemplate.opsForValue().get(key);
                if (StringUtils.isBlank(value)) {
                    return "商品不存在";
                }
                Long stock = Long.valueOf(value);
                if (stock > 0) {
                    System.out.println("用户：" + username + "购买了第" + stock + "件商品");
                    stock = stock - 1;
                    redisTemplate.opsForValue().set(key, stock.toString());
                    return "ok";
                } else {
                    return "库存不足";
                }
            }finally {
                redisTemplate.delete(lock);
            }
        }else{
            return "当前访问量过大，请重试";
        }
    }

    @GetMapping("buy4")
    public String buy4(String username, Long productId) throws InterruptedException {
        RLock lock1 = redissonClient.getLock(lock);
        boolean flag = lock1.tryLock(5, TimeUnit.SECONDS);
        if (flag){
            try {
                String key = prefix + productId;
                String value = redisTemplate.opsForValue().get(key);
                if (StringUtils.isBlank(value)) {
                    return "商品不存在";
                }
                Long stock = Long.valueOf(value);
                if (stock > 0) {
                    System.out.println("用户：" + username + "购买了第" + stock + "件商品");
                    stock = stock - 1;
                    redisTemplate.opsForValue().set(key, stock.toString());
                    return "ok";
                } else {
                    return "库存不足";
                }
            }finally {
                lock1.unlock();
            }
        }else{
            return "当前访问量过大，请重试";
        }
    }

}
