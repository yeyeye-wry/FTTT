package com.laiyesheng.fttt.array;

public class LineSet {
    private long[] data;//set
    private int capacity=16;//当前容量
    private  int size;// 实际元素数量

    public LineSet(){
        this.data=new long[this.capacity];
        this.size=0;
    }

    public void add(long key){
        if(this.size==this.capacity){
            long[] newArr=new long[this.capacity*2];

            //copy
            System.arraycopy(this.data,0,newArr,0,size);
            this.capacity*=2;
            data=newArr;
        }//

        data[size]=key;
        size++;
    }//add

    //是否包含其中 查找.
    public boolean contains(long key){
        for(int i=0;i<size;i++){
            if(data[i]==key){
                return true;
            }
        }
        return false;
    }

    public void clear(){
        this.size=0;
    }//清空

    public int size(){
        return this.size;
    }

    public long get(int i){
        if(i<0||i>this.size-1)return -1;
        return this.data[i];
    }

}
