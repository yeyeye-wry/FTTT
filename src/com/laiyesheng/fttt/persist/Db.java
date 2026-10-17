package com.laiyesheng.fttt.persist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//连接管理
public class Db {

    private static final String URL =
            "jdbc:mysql://localhost:3306/fttt?useSSL=false&serverTimezone==UTC&characterEncoding=utf8mb4";
    private static final String USER = "root";
    private static final String PASS = "123456";

    private  static Connection conn;//通信对象

    //throws+异常名
//throws：放在声明后面 声明这个方法可能抛出异常（SQL异常）
    public static Connection get() throws  SQLException{
        if(conn==null||conn.isClosed()){//conn是空的
            conn=DriverManager.getConnection(URL,USER,PASS);
        }
        return conn;//get()  得到Connection对象
    }//只建立一次 并且只在程序结束前close 懒加载

    //null 空指针 ！conn.isClosed 多余调用close
    public  static void close() throws SQLException{
        if(conn!=null&&!conn.isClosed()) conn.close();
    }
}

