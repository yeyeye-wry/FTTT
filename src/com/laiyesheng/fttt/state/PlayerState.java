package com.laiyesheng.fttt.state;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.list.Hand;
public class PlayerState {
    private Owner owner;//枚举 BLACK/WHITE
    private  int score;
    private int ap;
    private Hand hand;
    private boolean recycleThisTurn;//本回合是否已经回收
    private boolean warningUsedThisTurn;//本回合是否触发预警

    public PlayerState(Owner owner){
        this.owner=owner;
        this.score=0;
        this.hand=new Hand();
        this.recycleThisTurn=false;
        this.warningUsedThisTurn=false;
        //ap的取决于 黑白 和是否首个回合 暂时不表
    }

    //score
    public void setScore(int score){
        this.score=score;
    }
    public int getScore(){
        return this.score;
    }

    //ap
    public void setAp(int ap){
        this.ap=ap;
    }
    public int getAp(){
        return this.ap;
    }

    //owner

    /*public void setOwner(Owner owner){
        this.owner=owner;
    }*/

    public Owner getOwner(){
        return this.owner;
    }

    //Hand

    /*public void setHand(Hand hand){
        this.hand=hand;
    }*/

    public Hand getHand(){
        return this.hand;
    }

    //RecycleThisTurn
    public  boolean isRecycleThisTurn(){
        return this.recycleThisTurn;
    }
    public  void setRecycleThisTurn(boolean input){
        this.recycleThisTurn=input;
    }

    //WarningUsedThisTurn
    public boolean isWarningUsedThisTurn(){
        return this.warningUsedThisTurn;
    }
    public  void setWarningUsedThisTurn(boolean input){
        this.warningUsedThisTurn=input;
    }

    public void resetTurnFlags(){
        this.setWarningUsedThisTurn(false);//每回合只能预警一次 预警一个
        this.setRecycleThisTurn(false);//每回合只能回收一次
    }//回合结束reset


}
