package com.laiyesheng.fttt.ai;

public enum MoveType {
    DROP_NEW, //落新子 用r,c
    DROP_HAND, //落手牌 r c inx
    RECYCLE,//回收 r c 回收自己场上的(r,c)棋子
    VIEW, //侦察 r,c
    DEVUOR,//吞噬 r,c,r2,cw (r,c吃r2,c2)
    SYNTH,//合成 r,c r2,c2 r,c与r2,c2合成
    END //结束本回合
}//7action
