package com.laiyesheng.fttt.state;

import com.laiyesheng.fttt.map.Cell;
import com.laiyesheng.fttt.map.Owner;


public class GameState {
    private Cell[][] board;
    private PlayerState[] players;
    private Owner current=Owner.BLACK;//当前行动方
    private  int turn;//回合数
    private  int phase;//0=开局布置 1=对局中 /2=结束
    private Owner winner;
    private int winScore=50;
    private  int difficulty;//人机模式选择难度 0/1/2
    private int mode;//0=双人 1=人机


}
