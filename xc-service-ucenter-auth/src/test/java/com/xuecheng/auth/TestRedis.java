package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Test
    public void testRedis() {
        // 定义 key
        String key = "user_token:56b78a01-bd93-4094-8204-bbe1e3ece1cf";
        // 定义 map
        Map<String, String> mapValue = new HashMap<>();
        mapValue.put("id", "101");
        mapValue.put("username", "itcast");
        String value = JSON.toJSONString(mapValue);
        // 向 redis 中存入值
        redisTemplate.boundValueOps(key).set(value, 60, TimeUnit.SECONDS);
        // 读取key 的过期时间，已过期返回 -2
        long expire = redisTemplate.getExpire(key);
        System.out.println(expire);
        // 根据key 获取 value
        String one = redisTemplate.opsForValue().get(key);
        System.out.println(one);
    }
}
