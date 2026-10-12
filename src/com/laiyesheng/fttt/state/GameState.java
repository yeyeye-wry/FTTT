package com.laiyesheng.fttt.state;

import com.laiyesheng.fttt.map.Cell;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.list.Log;

public class GameState {
    static final int BOARD_N = 11;//忘记加public 那就不加了

    private Cell[][] board;
    private PlayerState[] players;
    private Owner current=Owner.BLACK;//当前行动方
    private  int turn;//回合数
    private  int phase;//0=开局布置 1=对局中 /2=结束
    private Owner winner;
    private int winScore=50;
    private  int difficulty;//人机模式选择难度 0/1/2
    private int mode;//0=双人 1=人机
    private Log log;//日志

    public GameState(int mode,int difficulty,int winScore){
        this.board=new Cell[BOARD_N][BOARD_N];//11*11 的cell //注意此时是121个

        for(int r=0;r<BOARD_N;r++) {
            for(int c =0;c<BOARD_N;c++){
                this.board[r][c]=new Cell();
            }
        }
        this.players=new PlayerState[2];
        this.players[0]=new PlayerState(Owner.BLACK);
        this.players[1]=new PlayerState(Owner.WHITE);
        this.current=Owner.BLACK;
        this.winner=Owner.EMPTY;
        this.mode=mode;
        this.difficulty=difficulty;
        this.winScore=winScore;
        this.log=new Log();

    }

    //turn
    public int getTurn(){
        return this.turn;
    }
    public void setTurn(int turn){
        if(turn<0) throw new IllegalArgumentException("turn must>=0");
        this.turn=turn;
    }

    //phase
    public int getPhase(){
        return this.phase;
    }
    public void setPhase(int phase){
        if(phase<0||phase>2)throw new IllegalArgumentException("phase error");
        this.phase=phase;
    }

    //current
    public  void setCurrent(Owner o){
        this.current=o;
    }
    public Owner getCurrent(){
        return this.current;
    }

    //winner
    public void setWinner(Owner winner){
        this.winner=winner;
    }
    public Owner getWinner(){
        return this.winner;
    }


    //返回对应下标
    public int pidx(Owner o){
        if(o==Owner.EMPTY) throw new IllegalArgumentException("not Empty");
        return o.ordinal()-1;
    }

    public Cell getCell(int r,int c){
        if(r<0||r>BOARD_N-1){
            return null;
        }
        if(c<0||c>BOARD_N-1){
            return null;
        }
        return board[r][c];//(r,c)读取一个cell
    }//getCell

    public int getBoardSize(){
        return BOARD_N;
    }

    public PlayerState getPlayer(Owner owner){
        return this.players[this.pidx(owner)];
    }

    public PlayerState getCurrentPlayer(){
        return this.getPlayer(this.getCurrent());
    }

    public PlayerState getHerPlayer(){
        int idx = pidx(this.getCurrent());
        return this.players[idx+1%2];
    }



}
