### 使用 keytool 工具与 OpenSSL 生成密钥

- keytool 是 java 提供的一个证书管理工具，使用 RSA 算法生成的证书包含一组公钥/私钥
- openSSL 是 一个加解密工具包，这里使用 openssl 导出公钥

```shell
# 生成密钥证书的方式 ----- 测试时使用当前命令无法完成下面的步骤，将密钥的访问密码删除后可以正常使用下面的流程 ------

  keytool -genkeypair -alias xckey -keyalg RSA -keypass xuecheng -keystore xc.keystore -storepass xuechengkeystore
  
  # 实际测试使用的语句
  keytool -genkeypair -keyalg RSA -keystore xc.keystore -storepass xuechengkeystore

# -genkeypair 生成密钥对
# -alias      指定密钥的别名
# -keyalg     指定使用的hash算法
# -keypass    密钥的访问密码
# -keystore   密钥库文件名，这个文件保存了生成的密钥
# -storepass  密钥库的访问密码
```
- 查询生成的证书信
```shell
  # 查询证书信息：
  
  keytool -list -keystore xc.keystore
  
```
- 删除证书别名
```shell
  # 删除别名 (可在生成时不指定)
  
  keytool -delete -alias xckey -keystore xc.keystore

```
-------------------------------------------------------
##### 导出公钥

- 使用 openssl 导出公钥
    + 首先需要讲 openssl 的 bin 目录配置在环境变量中
    + 进入要存储密钥证书的文件夹内
    + 执行下面的命令，来导出公钥
    + 将导出的公钥复制保存在一个文件中

```shell
    
    keytool -list -rfc --keystore xc.keystore | openssl x509 -inform pem -pubkey
    
```

