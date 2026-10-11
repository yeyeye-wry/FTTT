package com.laiyesheng.fttt.list;

public class Log {
    LogNode head;
    int count;

    public Log(){
        this.head=null;
        this.count=0;
    }

    public void add(String event){
        LogNode newNode=new LogNode(event);
        if(this.head==null){
            this.head=newNode;
            this.count++;
        }else{
            LogNode node=this.head;
            while(node.getNext()!=null){
                node=node.getNext();
            }
            node.setNext(newNode);
            this.count++;
        }

    }//add 表尾入队

    //后面后悔步的时候可以用到
    public String removeAt(int index){
        if(this.head==null||index<0||index>=this.count){
            return null;
        }else {
            String removedEvent=null;
            if (index == 0) {
                removedEvent=this.head.getEvent();
                this.head=this.head.getNext();
            } else {

                LogNode prev = this.head;
                for (int i = 0; i < index - 1; i++) {
                    prev=prev.getNext();
                }

                LogNode target = prev.getNext();
                removedEvent = target.getEvent();
                prev.setNext(target.getNext());
            }
            this.count--;
            return removedEvent;
        }
    }//removeAt

    public String removeLast(){
        return this.removeAt(this.count-1);
    }//removen 最后一个

    //取出日志的事件
    public String getEventAt(int index){
        if(this.head==null||index<0||index>=this.count)return null;
        LogNode cur=this.head;
        for(int i =0;i<index;i++){
            cur=cur.getNext();
        }
        return cur.getEvent();

    }

    public int size(){
        return this.count;
    }

    public boolean isEmpty(){
        return this.head==null;
    }

    public  void clear(){
        this.head=null;
        count=0;
    }

    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("Log[");
        LogNode cur=head;
        while(cur!=null){
            sb.append(cur.getEvent());
            cur=cur.getNext();
            if(cur!=null){
                sb.append(",");
            }

        }
        sb.append("]");
        return sb.toString();
    }//覆写toString
}