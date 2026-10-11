package com.laiyesheng.fttt.list;

public class Hand {
    private PieceNode head;
    private  int count;
    public Hand(){
    this.head=null;
    this.count=0;
    }

    public void add(int level){
        PieceNode newNode=new PieceNode(level);
        if(this.head==null){
            this.head=newNode;
            this.count++;
        }else{
            PieceNode node=this.head;
            while(node.getNext()!=null){
                node=node.getNext();
            }

            node.setNext(newNode);
            this.count++;

        }
    }//add 表尾入队

    //remove 删除 第index个手牌 //返回等级
    public int removeAt(int index) {

        if (this.head == null || index < 0 || index >= this.count) {
            return -1;//-1越界
        }
        int removedLevel;
        if (index == 0) {
            removedLevel = this.head.getLevel();
            this.head = this.head.getNext();
        } else {
            PieceNode prev = this.head;
            for (int i = 0; i < index - 1; i++) {
                prev = prev.getNext();
            }
            PieceNode target = prev.getNext();
            removedLevel = target.getLevel();
            prev.setNext(target.getNext());

        }
        this.count--;
        return removedLevel;

    }

    public  void clear(){
        this.head=null;
        this.count=0;
    }//clear

    public int size(){
        return this.count;
    }


    //显示手牌等级用 取出index 的level
    public int getLevelAt(int index){
        if(index<0||index>=this.count)return -1;
        PieceNode cur=this.head;
        for(int i =0;i<index;i++){
            cur=cur.getNext();
        }
        return cur.getLevel();
    }

    //
    public boolean isEmpty(){
        return this.head==null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hand[");
        PieceNode cur = head;
        while (cur != null) {
            sb.append(cur.getLevel());
            cur = cur.getNext();
            if (cur != null) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }


}
