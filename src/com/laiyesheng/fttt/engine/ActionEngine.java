package com.laiyesheng.fttt.engine;
import com.laiyesheng.fttt.list.PieceNode;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.state.PlayerState;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.list.Hand;
import com.laiyesheng.fttt.map.Cell;

//actDropHand/actRecycle/actDevour 传了 hand 参数，
// 而 actDropNew/actView 不传——建议统一从 game.getCurrentPlayer().getHand() 内部取，
// 去掉参数，免得传错手牌。
//M1 hasPiece(GameState, Owner)：配合 B4，扫棋盘看该玩家还有没有子。


//吞噬没处理 handIdx<0（用新 Lv1 发起）：devourAlgorthm 直接 hand.removeAt(index)，handIdx<0 时
// removeAt(-1) 返回 -1 → 等级算错。应：handIdx<0 时 mylevel=1 且不碰手牌；
// 否则 mylevel=hand.removeAt(handIdx)。

//我记得我一个null没有做防备

public class ActionEngine {

    //ap
    public static final int AP_PER_TURN=10;//每回合行动点
    public static final int BLACK_FIRST_AP=5;//黑方首回合
    public  static final int COST_DROP=5;// 落新子/手牌落子
    public  static final int COST_RECYCLE=5;//回收消耗
    public  static final int COST_DEVOUR=5;//吞噬消耗
    public static final int COST_VIEW=1;//查看1等级
    public static final int COST_SYNTH=0;//合成，合成成功ap+5
    public static final int ADD_SYNTH=5;//合成成功的奖励点数

    //失败操作返回值
    public static final int OK = 0;// 动作成功
    public static final int ERR_AP_NOT_ENOUGH = -1;// AP不足，ap<5（actDropNew专用）
    public static final int ERR_OUT_VISION = -2;// 不在视野内（例外：场上自己0子的时候不受这条限制）
    public static final int ERR_NOT_EMPTY = -3;// 格子已有棋子，不是空格
    public static final int ERR_HAND_INDEX = -4; // 手牌idx越界（actDropHand专用）
    public static final int ERR_NOT_OWNER = -5; // 改格子没有自己的棋子 无法回收 /不是自己的格子
    public static final int ERR_LOOK_SHE=-6;//该格子不是对方的格子
    public static final int ERR_LOOK_REV=-7;//配合SHE使用 虽然这个是对方的格子 但是已经揭示了，不能重复揭示
    public static final int ERR_NOT_ADJA=-8;//因为不相邻无法合成
    public static final int ERR_SYN_FAIL=-8;//因为等级不同无法合成
    public static final int ERR_OTHER=-10;//其他错误

    /**
     *
     * @param game 游戏完整状态
     * @param r 行
     * @param c 列
     * @return 0/1
     */
    public int actDropNew(GameState game,int r,int c){
        boolean valueInVision=VisionEngine.inVision(game,game.getCurrent(),r,c);//这个方法两次运行，尽量减少调用；
        if(this.isEnoughAp(game,COST_DROP)&&valueInVision&&this.isEmptyCell(game,r,c)){
            modifyCellOwner(game,game.getCurrent(),r,c);
            modifyCellLevel(game,r,c,1);
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_DROP);
            return 0;
        }
        if(!this.isEnoughAp(game,COST_DROP))return ERR_AP_NOT_ENOUGH;
        if(!valueInVision)return ERR_OUT_VISION;
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
        boolean valueInVision=VisionEngine.inVision(game,game.getCurrent(),r,c);//这个方法两次运行，尽量减少调用；
        if(Hand.aviailableindex(index)&&this.isEnoughAp(game,COST_DROP)&&valueInVision&&this.isEmptyCell(game,r,c)){
            modifyCellOwner(game,game.getCurrent(),r,c);
            modifyCellLevel(game,r,c,hand.removeAt(index));//removeAt返回-1为越界，但是还是在这里保障吧
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_DROP);
            return 0;
        }//下棋！
        if(!Hand.aviailableindex(index))return ERR_HAND_INDEX;
        if(!this.isEnoughAp(game,COST_DROP))return ERR_AP_NOT_ENOUGH;
        if(!valueInVision)return ERR_OUT_VISION;
        if(!this.isEmptyCell(game,r,c))return ERR_NOT_EMPTY;
        return ERR_OTHER;
    }

    /**
     * 回收棋子 变为手牌
     * @param game 游戏
     * @param hand 手牌
     * @param r 行
     * @param c 列
     * @return 错误类型
     */
    public int actRecycle(GameState game,Hand hand,int r,int c){
        boolean valueInVision=VisionEngine.inVision(game,game.getCurrent(),r,c);//这个方法两次运行，尽量减少调用；
        if(this.isEnoughAp(game,COST_RECYCLE)&&valueInVision&&this.isMyCell(game,r,c)&&!game.getCurrentPlayer().isRecycleThisTurn()){
            hand.add(game.getCell(r,c).getLevel());
            game.getCell(r,c).clear();//回收 this.owner=Owner.EMPTY;this.level=0;this.revealed=false;
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_RECYCLE);
            return 0;
        }//回收

        if(!this.isEnoughAp(game,COST_RECYCLE))return ERR_AP_NOT_ENOUGH;
        if(!valueInVision)return ERR_OUT_VISION;
        if(!isMyCell(game,r,c))return ERR_NOT_OWNER;//-6

        return ERR_OTHER;//-10
    }

    /**
     * View 查看对方棋子的等级
     * @param game 游戏整体
     * @param r 行
     * @param c 列
     * @return 错误类型
     */
    public int actView(GameState game,int r,int c){
        boolean valueInVision=VisionEngine.inVision(game,game.getCurrent(),r,c);//这个方法两次运行，尽量减少调用；
        if(this.isEnoughAp(game,COST_VIEW)&&valueInVision&&!game.getCell(r,c).getRevealed()&&this.isHerCell(game,r,c)){
            game.getCell(r,c).setRevealed(true);
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_VIEW);
            return 0;
        }
        if(!isEnoughAp(game,COST_VIEW))return ERR_AP_NOT_ENOUGH;//没有点数最优先
        if(!valueInVision)return ERR_OUT_VISION;//没有视野
        if(!isHerCell(game,r,c))return ERR_LOOK_SHE;//这个不是对方棋子 没有查看的意义
        if(game.getCell(r,c).getRevealed())return ERR_LOOK_REV;//-6 已经揭示了 没有查看意义
        return ERR_OTHER;
    }

    public int  actDevour(GameState game,Hand hand,int r,int c,int handIdx){

        boolean valueInVision=VisionEngine.inVision(game,game.getCurrent(),r,c);
        if(this.isEnoughAp(game,COST_DEVOUR)&&valueInVision&&this.isHerCell(game,r,c)){
            this.devourAlgorthm(game,hand,handIdx,game.getCell(r,c));//decour 完成吞噬操作
            this.modifyPlayerAp(game.getPlayer(game.getCurrent()),-COST_DEVOUR);
            return 0;
        }
        if(!this.isEnoughAp(game,COST_DEVOUR))return ERR_AP_NOT_ENOUGH;
        if(!valueInVision)return ERR_OUT_VISION;
        if(!this.isHerCell(game,r,c))return ERR_LOOK_SHE;
        return ERR_OTHER;
    }

    private void devourAlgorthm(GameState game,Hand hand,int index, Cell herCell){

        int mylevel=hand.removeAt(index);
        int herlevel=herCell.getLevel();
        if(mylevel==herlevel) {
            //herCell.setOwner(Owner.EMPTY);//抵消
            //调用Cell.clear即可
            herCell.clear();
            //我的手牌也要丢弃 注意我方棋子也已经地下
            //这里不处理即可
        }else if(mylevel>herlevel){

            if(mylevel-herlevel==1){//相邻升级 //这里没必要用abs
                //int level=Math.max(mylevel,herlevel)+1; 也没有必要max 显然mylevel是大的
                mylevel++;
                if(mylevel>10)mylevel=10;//在外面要防止吞噬10级 或者用10级吞并 10级不能乱动 这里保障升级不会超过10级 先不弄这个
                herCell.clear();//清空
                herCell.setOwner(game.getCurrent());
                herCell.setLevel(mylevel);//“升级”后的棋子完成放置
            }else{
                herCell.clear();
                herCell.setOwner(game.getCurrent());
                herCell.setLevel(mylevel);
            }//>> 远远大于啊，这是胜利 不升级 保持myLevel

        }else{//吞噬失败/养肥对方玩法
                //不变 无影响？ 都是养肥了要不还是加点玩法吧 分两档升级 加一分或者两分
                herlevel+=this.addlevel(herlevel,mylevel);
                herCell.setLevel(herlevel);//全新养肥玩法，简洁了不少
        }
    }//这里不做对等级的保障

    //上方计算加分的小算法

    /**
     * 根据diff判断加等级 如果diff是1就加已 如果diff在2到4之间就加2 如果大于4 为防止养肥过于容易所以不加
     * @param herlevel
     * @param mylevel
     * @return
     */
    private  int addlevel(int herlevel,int mylevel){

        int diff=herlevel-mylevel;
        if(diff==1){
            return 1;
        }else if(diff>=2&&diff<=4){
            return 2;
        }else{
            return 0;
        }
    }

    /**
     * 合成同等级类型的棋子 合成
     * @param game 游戏
     * @param r1 对象1行
     * @param c1 对象1列
     * @param r2 对象2行
     * @param c2 对象2列
     * @return 错误类型
     */
    public int actSynthesize(GameState game,int r1,int c1,int r2,int c2){
        //COST_SYNTH 是0
        if(this.isMyCell(game,r1, c1)&&this.isMyCell(game,r2,c2)&&game.isAdjacent(r1, c1, r2, c2)) {
            int  res= this.synthesizeAlgorithm(game.getCell(r1,c1),game.getCell(r2,c2));
            PlayerState player=game.getCurrentPlayer();
            player.setAp(player.getAp()+ADD_SYNTH);
            return res;
        }
        if(!this.isMyCell(game,r1,c1)||!this.isMyCell(game,r2,c2))return ERR_NOT_OWNER;
        if(!game.isAdjacent(r1,c1,r2,c2))return ERR_NOT_ADJA;
        return ERR_OTHER;
    }

    private int synthesizeAlgorithm(Cell cellOne,Cell cellTwo){
        int levelOne=cellOne.getLevel();
        int levelTwo=cellTwo.getLevel();
        if(levelOne==levelTwo){ //满足同等级 跨级升级
            int level=levelOne+2;//是+2 不是*2
            if(level>10) level=10;
            cellTwo.clear();
            return 0;
        }else{
         //不同等级
            return ERR_SYN_FAIL;
        }
    }//synthesizeAlgorthm

    // 结束回合 turn+1 清空ap 给对方回ap 切换current 重置回收/预警的boolean
    public void actEndTurn(GameState game){//这里的trun是双方总共的trun 如果不修改的话
        game.setTurn(game.getTurn()+1);//从0开始 0 作为空局 用来给双方布下第一个棋子 第一个棋子不消耗ap 在第0轮实现
        PlayerState myplayer=game.getCurrentPlayer();
        myplayer.setAp(0);

        //重置flag 换current
        myplayer.resetTurnFlags();//重置两个flag //1 BLACK 2 WHITE
        Owner next=game.getCurrent().ordinal()==1?Owner.WHITE:Owner.BLACK;
        game.setCurrent(next);

        int ap = (next==Owner.BLACK && !game.isBlackFirstDone()) ? BLACK_FIRST_AP : AP_PER_TURN;
        game.getPlayer(next).setAp(ap);
        if(next==Owner.BLACK) game.setBlackFirstDone(true);
        //默认黑棋为先首方 为平衡首回合ap为


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

    /**
     * 该位置是不是对方棋子位置
     * @param game 游戏
     * @param r 行
     * @param c 列
     * @return
     */
    private boolean isHerCell(GameState game,int r,int c){
        //return !isEmptyCell(game,r,c)&&!isMyCell(game,r,c);//不是空的 不是我的 也就是对方的
        //有没有更好的算法？
        //有的有的！虽然效率可能差不多？
        //return game.getCell(r,c).getOwner().ordinal()==game.getCurrent().ordinal()%2+1;//这个有个0的肯 如果是Empty没有考虑进去
        //展开的写法！
        Owner owner=game.getCell(r,c).getOwner();
        if(owner==Owner.EMPTY){
            return false;
        }
        return owner!=game.getCurrent();
    }




}



