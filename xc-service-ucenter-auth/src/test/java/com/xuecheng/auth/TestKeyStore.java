package com.xuecheng.auth;


import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class TestKeyStore {


    /* 生成一个jwt */
    @Test
    public void testCreateJwt() {
        // 证书文件
        String key_location = "xc.keystore";
        // 密钥库密码
        String keystore_password = "xuechengkeystore";
        // 访问证书路径
        ClassPathResource resource = new ClassPathResource(key_location);
        // 密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, keystore_password.toCharArray());
        // 密钥的密码 -- 此密码要与别名相匹配
        String keypassword = "xuecheng";
        // 密钥的别名
        String alias = "xckey";
        // 密钥对 (公钥和私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypassword.toCharArray());
        // 私钥
        RSAPrivateKey aprivate = (RSAPrivateKey) keyPair.getPrivate();

        // 定义 payload   信息
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "123");
        tokenMap.put("name", "webapp");
        tokenMap.put("roles", "role1,role2");
        tokenMap.put("ext", "ee");

        // 生成 jwt 令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(aprivate));
        // 取出 jwt 令牌
        String token = jwt.getEncoded();
        System.out.println("<<token>>::" + token);
    }

    /* 校验Token */
    @Test
    public void testValidateToken() {

        // jwt 令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJVbmljb3JuIiwidXR5cGUiOiIxMDEwMDIiLCJpZCI6IjQ5IiwiZXhwIjoxNjI4Mzg1MjUyLCJhdXRob3JpdGllcyI6WyJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2Jhc2UiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2RlbCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfbGlzdCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcGxhbiIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2UiLCJjb3Vyc2VfZmluZF9saXN0IiwieGNfdGVhY2htYW5hZ2VyIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9tYXJrZXQiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX3B1Ymxpc2giLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2FkZCJdLCJqdGkiOiJmMGFmNzk1Yy1hZTRmLTQzMWYtOWE0OC1kNmE5OWFjNjk4MWQiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.JnK2tBjqHGuMYz7wd7qv-AvN8wyCUt2rnUpy641nGQMmrbQXZwpjSMLhTx3VycL0qpEagJw2brYXo-2_YeXBnH0Ec-L-TyROymEOlHWqeZB4r6ftBF1wf9MdNUu9GvluXGSwzyF4dass_lo_GHr_U-f3KCAHo1NZAupqx6zh7h5zK1yKeOj_RClYUjSWeUAS9Evy4tKjhnQPLUavexlQ2SyGfRC4DDk1uLhaFyuWT6iiadhSrytbDXQo-gikTlCLCC_5pFl1Ikv0tuyHwcER24yhoBMixcob1Agxbmzawn-AymqHHp_BUjI9N7kwdLKCdYKyaXBX06RnCvIdkQEbrw";

        // 公钥
        String pub_key = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";

        // 校验 jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(pub_key));
        // 获取 jwt 的原始 内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //  jwt 令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

}
