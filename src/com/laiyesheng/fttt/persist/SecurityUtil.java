package com.laiyesheng.fttt.persist;

import java.nio.charset.StandardCharsets;//提供的标准字符集常量
import java.security.MessageDigest;//消息摘要（哈希算法）
import java.security.SecureRandom;
//salt 盐加密 密码加盐 SHA-256

public final class SecurityUtil {
    //生成随机盐 （一串随机字节）
    private  static final SecureRandom RAND =new SecureRandom();

    public  static byte[] newSalt(){
        byte[] s=new byte[16];// 16*8 位 salt足够长
        RAND.nextBytes(s);//nextBytes 讲生成的一串随机字节填入数组
        return s;
    }

    //hash
    //salt+密码 ->哈希  //这里先加盐在加密码
    public static byte[] hash(byte[] salt,String password) {
        try {
            //update 往message里面追加字符数据
            //拿到一个 SHA-256 哈希计算器实例
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);//getBytes(Charset 字符集)
            md.update(password.getBytes(StandardCharsets.UTF_8));
            return md.digest();//digest 摘要 即使计算出哈希值 输出哈希字节数组
            //注意 同时md 在digest之后会清空
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean equal(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int r = 0;
        //为什么不是一不一样旧返回false？用来防止计时攻击(timing attack)
        for (int i = 0; i < a.length; i++) r |= a[i] ^ b[i];
        return r == 0;//r==0 a与b字节相同
    }
}
