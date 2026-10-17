package com.laiyesheng.fttt.user;


//这里id不用nextid的方案 用数据库的AUTO_INCREMENT
public class User {
    private long userId;
    private String username;
   private  byte[] pwdHash;
   private  byte[] salt;
   //存入哈希和盐
    private long regTime;//register
    private  int wins,losses,bestScore;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long user_id) {
        this.userId = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(byte[] pwdHash) {
        this.pwdHash = pwdHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }
}
