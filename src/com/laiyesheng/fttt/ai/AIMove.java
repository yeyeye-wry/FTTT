package com.laiyesheng.fttt.ai;

public class AIMove {
    public  MoveType type;
    public int r,c;//主坐标
    public int r2,c2;//次坐标 合成/吞噬
    public int idx;//手牌索引

    public  AIMove(MoveType type,int r,int c,int r2,int c2 ,int idx){
        this.type=type;
        if(r!=-1)this.r=r;
        if(c!=-1)this.c=c;
        if(r2!=-1)this.r2=r2;
        if(c2!=-1)this.c2=c2;
        if(idx!=-1)this.idx=idx;
    }//全参数构造器

    public  AIMove(MoveType type,int r,int c){
        this(type,r,c,-1,-1,-1);
    }

    //合成  //这是合成的 和吞噬所需要的参数不同
    public AIMove(MoveType type,int r,int c,int r2,int c2){
        this(type,r,c,r2,c2,-1);
    }
    //落手牌
    public  AIMove(MoveType type,int r,int c,int idx){
        this(type,r,c,-1,-1,idx);
    }

    //结束回合 无参
    public static AIMove end(){
        return new AIMove(MoveType.END,-1,-1);
    }

}
