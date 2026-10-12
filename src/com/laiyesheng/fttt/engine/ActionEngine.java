package com.laiyesheng.fttt.engine;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.state.PlayerState;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.list.Hand;

public class ActionEngine {

    //ap
    public static final int AP_PER_TURN=10;//每回合行动点
    public static final int BLACK_FIRST_AP=5;//黑方首回合
    public  static final int COST_DROP=5;// 落新子/手牌落子
    public  static final int COST_RECYCLE=5;//回收消耗
    public  static final int COST_DEVOUR=5;//吞噬消耗
    public static final int COST_VIEW=1;//查看1等级
    public static final int COST_SYNTH=0;//合成，合成成功ap+5

    //失败操作返回值
    public static final int OK = 0;// 动作成功
    public static final int ERR_AP_NOT_ENOUGH = -1;// AP不足，ap<5（actDropNew专用）
    public static final int ERR_OUT_VISION = -2;// 不在视野内（例外：场上自己0子的时候不受这条限制）
    public static final int ERR_NOT_EMPTY = -3;// 格子已有棋子，不是空格
    public static final int ERR_HAND_INDEX = -4; // 手牌idx越界（actDropHand专用）
    public static final int ERR_NOT_OWNER = -4; // 改格子没有自己的棋子 无法回收
    public static final int ERR_LOOK_SHE=-5;//看的不是对方并且对方已经揭露了 此时无法再查看
    public static final int ERR_OTHER=-10;//其他错误

    /**
     *
     * @param game 游戏完整状态
     * @param r 行
     * @param c 列
     * @return 0/1
     */
    public int actDropNew(GameState game,int r,int c){
        if(this.isEnoughAp(game,COST_DROP)&&this.isEmptyCell(game,r,c)&&VisionEngine.inVision(game,game.getCurrent(),r,c)){
            modifyCellOwner(game,game.getCurrent(),r,c);
            modifyCellLevel(game,r,c,1);
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_DROP);
            return 0;
        }
        if(!this.isEnoughAp(game,COST_DROP))return ERR_AP_NOT_ENOUGH;
        if(!VisionEngine.inVision(game,game.getCurrent(),r,c))return ERR_OUT_VISION;
        if(!this.isEmptyCell(game,r,c))return ERR_NOT_EMPTY;
        return ERR_OTHER;
    }//actDropNew

    /**
     * act：下手牌
     * @param game 游戏整体
     * @param hand 手牌
     * @param r 行
     * @param c 列
     * @param index 手牌序号
     * @return int类型 错误类别
     */
    public int actDropHand(GameState game,Hand hand,int r,int c,int index){
        if(Hand.aviailableindex(index)&&this.isEnoughAp(game,COST_DROP)&&this.isEmptyCell(game,r,c)&&VisionEngine.inVision(game,game.getCurrent(),r,c)){
            modifyCellOwner(game,game.getCurrent(),r,c);
            modifyCellLevel(game,r,c,hand.removeAt(index));//removeAt返回-1为越界，但是还是在这里保障吧
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_DROP);
            return 0;
        }//下棋！
        if(!Hand.aviailableindex(index))return ERR_HAND_INDEX;
        if(!this.isEnoughAp(game,COST_DROP))return ERR_AP_NOT_ENOUGH;
        if(!VisionEngine.inVision(game,game.getCurrent(),r,c))return ERR_OUT_VISION;
        if(!this.isEmptyCell(game,r,c))return ERR_NOT_EMPTY;
        return ERR_OTHER;
    }

    /**
     * 回收棋子 变为手牌
     * @param game
     * @param hand
     * @param r
     * @param c
     * @return
     */
    public int actRecycle(GameState game,Hand hand,int r,int c){
        if(VisionEngine.inVision(game,game.getCurrent(),r,c)&&this.isMyCell(game,r,c)&&this.isEnoughAp(game,COST_RECYCLE)&&!game.getCurrentPlayer().isRecycleThisTurn()){
            hand.add(game.getCell(r,c).getLevel());
            game.getCell(r,c).clear();//回收 this.owner=Owner.EMPTY;this.level=0;this.revealed=false;
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_RECYCLE);
            return 0;
        }//回收

        if(!this.isEnoughAp(game,COST_DROP))return ERR_AP_NOT_ENOUGH;
        if(!VisionEngine.inVision(game,game.getCurrent(),r,c))return ERR_OUT_VISION;
        if(!isMyCell(game,r,c))return ERR_NOT_OWNER;//-5

        return ERR_OTHER;//-10
    }

    //
    public int actView(GameState game,int r,int c){
        if(this.isHerCell(game,r,c)&&this.isEnoughAp(game,COST_VIEW)&&VisionEngine.inVision(game,game.getCurrent(),r,c)){

        }
        if(!isEnoughAp(game,COST_VIEW))return ERR_AP_NOT_ENOUGH;
        if(!VisionEngine.inVision(game,game.getCurrent(),r,c))return ERR_OUT_VISION;
        if(!isHerCell(game,r,c)||)
    }





    //在本类中所需要的小方法 有部分方法在别的类中
    /**
     * 判断该格子是不是空的
     * @param game 游戏整体
     * @param r 棋盘行
     * @param c 棋盘列
     * @return 是否是空的格子
     */

    private  boolean isEmptyCell(GameState game,int r,int c){
        return game.getCell(r,c).isEmpty();
    }
    //在Cell类中已经有这个函数了

    /**
     * 判断己方的ap够不够COST_DROP
     * @param game 游戏完整状态
     * @param cost   所需要的ap
     * @return boolean类型
     */
    private  boolean isEnoughAp(GameState game,int cost){
        return game.getCurrentPlayer().getAp()>=cost;
    }

    /**
     * 改变玩家的Ap
     * @param player  玩家
     * @param modifyvalue 变化值
     */
    private void modifyPlayerAp(PlayerState player,int modifyvalue){
        player.setAp(player.getAp()+modifyvalue);
    }

    private void modifyCellOwner(GameState game,Owner o,int r,int c){
        game.getCell(r,c).setOwner(o);
    }

    private void modifyCellLevel(GameState game,int r,int c,int level){
        game.getCell(r,c).setLevel(level);
    }

    private boolean isMyCell(GameState game,int r,int c){
        return game.getCell(r,c).getOwner()==game.getCurrent();
    }

    private boolean isHerCell(GameState game,int r,int c){
        return !isEmptyCell(game,r,c)&&!isMyCell(game,r,c)
    }

}



