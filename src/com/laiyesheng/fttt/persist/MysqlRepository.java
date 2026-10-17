package com.laiyesheng.fttt.persist;

import com.laiyesheng.fttt.user.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//
//MySQL实现
//JDBC Statement RETURN_GENERATED_KEYS返回自动生成的ID
public class MysqlRepository implements Repository {

    @Override
    public  void saveUser(User u){//在try()里面声明的资源 正常结束或者抛出异常之后会close()
        String sql="INSERT INTO users(username,pwd_hash,salt,reg_time) VALUES(?,?,?,NOW())";
        try(Connection c=Db.get();
            PreparedStatement ps = c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){

            ps.setString(1,u.getUsername());
            ps.setBytes(2,u.getPwdHash());
            ps.setBytes(3,u.getSalt());
            ps.executeUpdate();// 用来返回受到影响的行数
            //ps.getGeneratedKeys() 返回一个结果集，里面装着数据库刚为这行自动生成的主键
            try(ResultSet rs=ps.getGeneratedKeys()){
                //next 移动内部“游标” 进入下一行 一开始是在第一行之前的
                //得到该行的第一列 也就是UserID
                if(rs.next())u.setUserId(rs.getLong(1));//拿回在数据库中自增的主键
            }
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public User findUser(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = new User();
                u.setUserId(rs.getLong("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPwdHash(rs.getBytes("pwd_hash"));
                u.setSalt(rs.getBytes("salt"));
                u.setWins(rs.getInt("wins"));
                u.setLosses(rs.getInt("losses"));
                u.setBestScore(rs.getInt("best_score"));
                return u;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void saveMatchResult(long userId, int score) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO matches(user_id,score,played_at) VALUES(?,?,NOW())")) {
            ps.setLong(1, userId);
            ps.setInt(2, score);
            ps.executeUpdate();
            // 顺手刷新个人最高分
            try (PreparedStatement up = c.prepareStatement(
                    "UPDATE users SET best_score=GREATEST(best_score,?) WHERE user_id=?")) {
                up.setInt(1, score);
                up.setLong(2, userId);
                up.executeUpdate();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }


    }

    @Override
    public List<RankRow> topN(int k) {
        String sql = "SELECT u.username, m.score FROM matches m "
                + "JOIN users u ON u.user_id=m.user_id "
                + "ORDER BY m.score DESC LIMIT ?";
        List<RankRow> list = new ArrayList<>();
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new RankRow(rs.getString("username"), rs.getInt("score")));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }
}
