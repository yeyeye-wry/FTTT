package FTTT;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcTest {
    public static void main(String[] args) {
        // 数据库地址，3306是端口，test换成你的数据库名
        String url = "jdbc:mysql://localhost:3306/fttt?useSSL=false&serverTimezone=Asia/Shanghai";
        String user = "root";       //你的mysql用户名
        String pwd = "123456";    //你的mysql密码

        try(Connection conn = DriverManager.getConnection(url,user,pwd)){
            System.out.println("✅ 连接成功！jar包加载生效");
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("❌ 连接失败");
        }
    }
}//成功连接数据库
