package com.laiyesheng.fttt.list;

public class LogNode {
    private  String event;
    private LogNode next;

    public LogNode(String event){
        this.event=event;
        this.next=null;
    }

    //event
    public String getEvent(){
        return this.event;
    }

    /*public void setEvent(String event){
        this.event=event;
    }*/

    //next
    public LogNode getNext(){
        return this.next;
    }
    public void setNext(LogNode next){
        this.next=next;
    }


}
