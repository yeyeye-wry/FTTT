package com.laiyesheng.fttt.ai;

public class AIMoveNode {
    private AIMove aimove;
    private AIMoveNode next;


    public AIMoveNode(AIMove aiMove){
        this.aimove=aiMove;
        this.next=null;
    }

    public AIMove getAimove() {
        return this.aimove;
    }

    public AIMoveNode getnext(){
        return this.next;
    }

    public void setnext(AIMoveNode next){
        this.next=next;
    }





}
