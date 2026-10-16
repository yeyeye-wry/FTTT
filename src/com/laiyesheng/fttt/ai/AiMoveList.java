package com.laiyesheng.fttt.ai;

import org.w3c.dom.traversal.NodeIterator;

import java.util.Iterator;

//单链表使用迭代器 foreach
//拒绝for getdix这种遍历
//能让链表的遍历到O(1)

public class AiMoveList implements Iterable<AIMove> {
    private AIMoveNode head;
    private AIMoveNode tail;//增加尾指针
    private int count;


    public AiMoveList() {
        this.head = null;
        this.tail = null;
        this.count = 0;
    }

    public void add(AIMove aiMove) {
        AIMoveNode newNode = new AIMoveNode(aiMove);
        if (this.head == null) {
            this.head = newNode;
            this.tail = newNode;
        } else {
            this.tail.setnext(newNode);
            this.tail = newNode;
        }

        this.count++;
    }

    public AIMove getAiMoveidx(int idx) {
        if (idx < 0 || idx >= this.count || this.head == null) return null;
        AIMoveNode current = this.head;
        for (int i = 0; i < idx; i++) {
            current = current.getnext();
        }
        return current.getAimove();
    }

    //虽然不知道有没有用
    public AIMove RemoveAt(int idx) {
        if (idx < 0 || idx >= this.count || this.head == null) {
            return null;
        }
        AIMove returnAIMove;
        if (idx == 0) {
            returnAIMove = this.head.getAimove();
            this.head = this.head.getnext();
        } else {       //头无前驱处理 也可以用一个空头
            AIMoveNode prev = this.head;
            for (int i = 0; i < idx - 1; i++) {
                prev = prev.getnext();
            }
            AIMoveNode target = prev.getnext();
            returnAIMove = target.getAimove();
            prev.setnext(target.getnext());
        }
        return returnAIMove;
    }


    public int size() {
        return this.count;
    }

    //clear
    void clear() {
        this.head = null;
        this.count = 0;
    }


    //实现Iterable for(AiMove m:list)
    @Override
    public Iterator<AIMove> iterator(){
        return new NodeIterator(this.head);
    }

    private  static class NodeIterator implements Iterator<AIMove>{
        private  AIMoveNode current;
        NodeIterator(AIMoveNode head){this.current=head;}
        @Override public  boolean hasNext(){return current!=null;}
        @Override public  AIMove next(){
            if(current==null) throw  new  java.util.NoSuchElementException();
            AIMove v=current.getAimove();
            current=current.getnext();
            return v;
        }
    }



}




