package com.laiyesheng.fttt.ai;

public class AiMoveList {
    private AIMoveNode head;
    private int count;


    public AiMoveList(){
        this.head=null;
        this.count=0;
    }

    public void add(AIMove aiMove){
        AIMoveNode newnode=new AIMoveNode(aiMove);
        if(this.head==null){
            this.head=newnode;
        }else{
            AIMoveNode current=this.head;
            while(current.getnext()!=null){
                current=current.getnext();
            }
            current.setnext(newnode);
        }

        this.count++;
    }

    public AIMove getAiMoveidx(int idx){
        if(idx<0||idx>=this.count||this.head==null) return null;
        AIMoveNode current=this.head;
        for(int i=0;i<idx;i++){
            current=current.getnext();
        }
        return current.getAimove();
    }

    //虽然不知道有没有用
    public AIMove RemoveAt(int idx){
        if(idx<0||idx>=this.count||this.head==null){
            return null;
        }
        AIMove returnAIMove;
        if(idx==0){
            returnAIMove=this.head.getAimove();
            this.head=this.head.getnext();
        } else {       //头无前驱处理 也可以用一个空头
            AIMoveNode prev = this.head;
            for (int i = 0; i < idx - 1; i++) {
                prev = prev.getnext();
            }
            AIMoveNode target = prev.getnext();
            returnAIMove=target.getAimove();
            prev.setnext(target.getnext());
        }
        return returnAIMove;
    }



    public int size(){
        return this.count;
    }

    //clear
    void clear(){
        this.head=null;
        this.count=0;
    }
}
