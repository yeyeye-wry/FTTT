package com.laiyesheng.fttt.persist;
import com.laiyesheng.fttt.user.User;



public class Test {
        public  static void main(String[] args){

            Repository repo = new MysqlRepository();

// —— 注册 ——
            User u = new User();
            u.setUsername("alice5");
            byte[] salt = SecurityUtil.newSalt();
            u.setSalt(salt);
            u.setPwdHash(SecurityUtil.hash(salt, "mypassword"));
            if(repo.saveUser(u)){
                System.out.println("创建成功");
            }else {
                System.out.println("重名");
            }

// —— 登录 ——
            User db = repo.findUser("alice2");
            boolean ok = db != null
                    && SecurityUtil.equal(db.getPwdHash(), SecurityUtil.hash(db.getSalt(),"mypassword" ));
// ok == true 才算登录成功
            if(ok)System.out.println("ok");
            else System.out.println("not");

// —— 一局结束（在 GameOver 逻辑里调）——
            repo.saveMatchResult(db.getUserId(), 50);

// —— 排行榜 ——
            for (RankRow r : repo.topN(100)) System.out.println(r.username + " " + r.score);

        }
}
