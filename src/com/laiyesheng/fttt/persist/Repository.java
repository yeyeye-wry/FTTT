package com.laiyesheng.fttt.persist;

import com.laiyesheng.fttt.user.User;
import java.util.List;//懒得再写一个list了
//接口
public interface Repository {
    boolean saveUser(User u);
    User findUser(String username);
    void saveMatchResult(long userId,int score);//一局结束写入战绩
    List<RankRow> topN(int k);//排行榜前K
    void recordResult(long userId,int score,boolean won);
}
