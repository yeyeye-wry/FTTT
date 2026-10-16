package com.laiyesheng.fttt.state;

import com.laiyesheng.fttt.map.Cell;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.list.Log;

//或许加个手牌 还是我之前忘记写了？
public class GameState {
    static final int BOARD_N = 20;//忘记加public 那就不加了
//11->20 速度明显降低 v3:还为合并maxrun 和linepotential
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
    private boolean blackFirstDone=false;//黑第一回合do了吗

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
        this.phase=1;//v1:没有在这里初始话phase
        this.turn=1;//从1开始计数

    }

    //mode
    public int getMode(){
        return this.mode;
    }
    public void setMode(int mode){
        this.mode=mode;
    }

    //difficulty
    public int getDifficulty(){
        return this.difficulty;
    }
    public void setDifficulty(int difficulty){
        this.difficulty=difficulty;
    }
    //winScore
    public  int getWinScore(){
        return this.winScore;
    }
    public void setWinScore(int winScore){
        this.winScore=winScore;
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

    public Cell[][] getBoard(){
        return this.board;
    }

    public PlayerState getPlayer(Owner owner){
        return this.players[this.pidx(owner)];
    }

    //getPlayers


    public PlayerState[] getPlayers() {
        return this.players;
    }

    public PlayerState getCurrentPlayer(){
        return this.getPlayer(this.getCurrent());
    }

    public PlayerState getHerPlayer(){
        int idx = pidx(this.getCurrent());
        return this.players[(idx+1)%2];
    }

    public boolean isAdjacent(int r1,int c1,int r2,int c2){
        int dr=Math.abs(r1-r2);
        int dc=Math.abs(c1-c2);
        return (dr<=1&&dc<=1&&!(dr==0&&dc==0));//相邻 //以及排除的同格子了
    }

    //blackFirstDone
    public boolean isBlackFirstDone(){
        return this.blackFirstDone;
    }
    public void setBlackFirstDone(boolean done){
        this.blackFirstDone=done;
    }

    public GameState GameStateCopy(){
        //深拷贝一个Game

        GameState newgame=new GameState(this.mode,this.difficulty,this.winScore);
        // phase等 可以直接赋值
        newgame.setPhase(this.getPhase());
        newgame.setWinner(this.getWinner());
        newgame.setCurrent(this.getCurrent());
        newgame.setTurn(this.getTurn());
        newgame.setBlackFirstDone(this.isBlackFirstDone());
        //board

        int boardSize=this.getBoardSize();
        for(int r=0;r<boardSize;r++){
            for(int c=0;c<boardSize;c++){
                newgame.getBoard()[r][c]=this.getCell(r,c).copyCell();
                //给board[][] copy出来
            }
        }

        //Player 0 1 //hand Player.hand

        newgame.getPlayers()[this.pidx(Owner.BLACK)]=this.getPlayer(Owner.BLACK).playerCopy();
        newgame.getPlayers()[this.pidx(Owner.WHITE)]=this.getPlayer(Owner.WHITE).playerCopy();




        return newgame;
        //ok!
    }
}
