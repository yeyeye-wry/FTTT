package com.laiyesheng.fttt.ai;

import java.util.Iterator;

//为了方便替换AiMoveList 也
public class MoveArrayList implements  Iterable<AIMove> {
    private  AIMove[] data;
    private  int size;
    private  static final int INIT_CAP=64;

    public  MoveArrayList(){
        data=new AIMove[INIT_CAP];
        size=0;
    }
    public  void add(AIMove move){
        if(this.size==data.length) grow();
        data[this.size++]=move;
    }
    private  void grow(){
        AIMove[] newarray=new AIMove[data.length*2];
        System.arraycopy(data,0,newarray,0,size);
        data=newarray;
    }

    //实现随机访问 O(1)
    public  AIMove get(int i){return data[i];}
    public  int size(){
        return size;
    }

    @Override public Iterator<AIMove> iterator(){
        return new Iter();
    }
    private  class Iter implements Iterator<AIMove> {
        int i=0;
        @Override public  boolean hasNext(){
            return i<size;
        }
        @Override public  AIMove next(){
            return data[i++];
        }
    }

    void clear(){this.size=0;}
}
