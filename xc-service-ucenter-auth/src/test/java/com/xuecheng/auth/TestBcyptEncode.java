package com.xuecheng.auth;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcyptEncode {


    /**
     *
     * BcyptPasswordEncoder 方法进行加密的时候，加入了随机的盐值，虽然是同一种密码，但是每次
     * 加密之后密文都不一样，更加安全，不易破解
     * ------------------------------------------------------------------------
     */
    @Test
    public void testBcyptPasswordEncode() {
        // 原始密码
        String password = "111111";

        // 构建一个BcyptPasswordEncoder
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i < 10; i++) {
            String encode = passwordEncoder.encode(password);
            System.out.println(encode);

            // 校验密码
            boolean matches = passwordEncoder.matches(password, encode);
            System.out.println(matches);
        }
    }
}
