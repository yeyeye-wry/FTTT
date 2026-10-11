package com.laiyesheng.fttt.list;
//Hand手牌的一个节点 也就是一个棋子
//属性 ： level 手牌棋子的等级
public class PieceNode {
    private int level=0;
    private PieceNode  next=null;

    public PieceNode(int level){
        this.level=level;
        this.next=null;
    }

    public PieceNode getNext(){
        return this.next;
    }

    public void setNext(PieceNode next){
        this.next=next;
    }

    public int getLevel(){
        return this.level;
    }

}
