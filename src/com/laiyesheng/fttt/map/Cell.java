package com.laiyesheng.fttt.map;

//这是map的一个格子

public class Cell {

    private Owner owner=Owner.EMPTY;//枚举类型
    private int level=0; // level 如果有棋子上面就具有等级 1~10
    private boolean revealed=false; //在迷雾之下，是否被揭示/预警 ->true

    public Cell(){
    }

    public void setOwner(Owner owner){
        this.owner=owner;
    }//Owmer 是是枚举类型

    public Owner getOwner(){
        return this.owner;
    }

    public void setLevel(int level){
    if(level <0||level>10) throw new IllegalArgumentException("Cell.level 0~10");
    this.level=level;
    }
    public int getLevel(){
        return this.level;
    }


    public void setRevealed(boolean revealed){
            this.revealed=revealed;
    }
    public boolean getRevealed(){
        return this.revealed;
    }

    //引擎：游戏的主控逻辑
    //引擎需要clear 的方法
    public void clear(){
        this.owner=Owner.EMPTY;
        this.level=0;
        this.revealed=false;
    }

    //isEmpty
    public  boolean isEmpty() {
    return this.owner==Owner.EMPTY;
    }

    @Override
    public String toString() {
        // owner：EMPTY / BLACK / WHITE，level等级，revealed是否揭开
        return "Cell{" +
                "owner=" + owner +
                ", level=" + level +
                ", revealed=" + revealed +
                '}';
    }


}
