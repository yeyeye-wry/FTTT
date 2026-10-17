package com.laiyesheng.fttt.user;

import  com.laiyesheng.fttt.persist.Repository;
import com.laiyesheng.fttt.persist.SecurityUtil;
import com.laiyesheng.fttt.persist.RankRow;
import java.util.List;
//为Gui 提供的账号逻辑中枢

public class UserService {
    private  final Repository repo;
    public UserService(Repository repo){
        this.repo=repo;
    }

    //注册用户 如果username重复返回false
    public  boolean register(String username,String password){
        byte[] salt =SecurityUtil.newSalt();
        byte[] hash=SecurityUtil.hash(salt,password);
        User u=new User();
        u.setUsername(username);
        u.setPwdHash(hash);
        u.setSalt(salt);
        return repo.saveUser(u);
    }

        //成功返回user 失败（用户不存在或者密码错误）返回null
    public  User login(String username,String password){
        User u=repo.findUser(username);
        if(u==null)return null;
        byte[] expect =SecurityUtil.hash(u.getSalt(),password);
        return SecurityUtil.equal(expect,u.getPwdHash())?u:null;

    }

    // 透传给 Repository：记录一局结果（分数 + 最高分 + 胜负场次）
    public void recordResult(long userId, int score, boolean won) {
        repo.recordResult(userId, score, won);
    }

    // 透传给 Repository：排行榜前 k 名
    public List<RankRow> topN(int k) {
        return repo.topN(k);
    }

}
