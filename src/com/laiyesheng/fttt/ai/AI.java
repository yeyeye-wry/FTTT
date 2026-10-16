package com.laiyesheng.fttt.ai;

import com.laiyesheng.fttt.engine.ActionEngine;
import com.laiyesheng.fttt.engine.VisionEngine;
import com.laiyesheng.fttt.map.Cell;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.state.PlayerState;
import com.laiyesheng.fttt.list.Hand;


public class AI {
    public static final int MAX_LEVEL=10;
    private static final int[][] DIRS = {{1,0},{0,1},{1,1},{1,-1}};
    public static final int  DIFF_EASY=0;
    public static final int NORML=1;
    public static final int HARD=2;

    public static AiMoveList genMoves(GameState game,Owner player){
        Owner opp=(player ==Owner.BLACK)?Owner.WHITE:Owner.BLACK;//对方
        //int ap =game.getPlayer(player).getAp();
        boolean hasMine=HasMyPiece(game,player);
        AiMoveList list =new AiMoveList();
        PlayerState playerNow=game.getPlayer(player);//己方对象
        int Ap=playerNow.getAp();
        Hand playerhand=playerNow.getHand();
        if(hasMine){//棋盘上有棋子
            int boardSize=game.getBoardSize();
            boolean[][] Vision=new boolean[boardSize][boardSize];
            VisionEngine.computeVision(game,player,Vision);
            //computeVision 得到可见格子 一次 以后可以直接查表

            outer:for(int r=0;r<boardSize;r++){
                for(int c=0;c<boardSize;c++){
                    Cell thisCell=game.getCell(r,c);
                    //boolean isVision=VisionEngine.inVision(game,player,r,c);
                    //V1：一个个试一试显然脱时间 不能放再这里
                    boolean isVision=Vision[r][c];

                    boolean placeOk=thisCell.isEmpty()&&isVision&&adjacentToMine(game,r,c,player);
                    if(placeOk){

                        if(Ap>= ActionEngine.COST_DROP){
                            if(addCapped(list,AIMove.dropNew(r,c))){
                                break outer;
                            }
                        }
                        if(Ap>=ActionEngine.COST_DROP&&okDropHand(playerhand,Ap)){
                            for(int idx=0;idx<playerhand.size();idx++){
                                if(addCapped(list,AIMove.dropHand(r,c,idx))){
                                    break outer;//Move-DropHand
                                }
                            }

                        }
                    }//placeOk


                //SYNTH

                  if(thisCell.getOwner()==player&&thisCell.getLevel()<MAX_LEVEL){
                      for(int k=0;k<4;k++){
                          int nr=r+DIRS[k][0];
                          int nc=c+DIRS[k][1];
                          if(nr<0||nr>=boardSize||nc<0||nc>=boardSize)continue;
                          Cell thatCell=game.getCell(nr,nc);
                          if(thatCell.getOwner()==player&&thatCell.getLevel()== thisCell.getLevel()){
                              if(addCapped(list,AIMove.synth(r,c,nr,nc))){
                                  break outer;
                              }
                          }
                      }
                  }//SYNTH 合成 目标未相邻的自己子



                    //DEVOUR
                    if(thisCell.getOwner()==opp&&isVision){
                        if(Ap>=ActionEngine.COST_DEVOUR&&addCapped(list,AIMove.devour(r,c,-1)))break outer;
                        if(okDropHand(playerhand,Ap)){
                            for(int idx=0;idx<playerhand.size();idx++){
                                if(addCapped(list,AIMove.devour(r,c,idx)))break outer;
                            }
                        }
                    }//DEVOUR 目标为敌格子 用新棋子或者手牌

                    //RECYCLE
                    if(thisCell.getOwner()==player&&!playerNow.isRecycleThisTurn()&&Ap>=ActionEngine.COST_RECYCLE){
                        if(addCapped(list,AIMove.recycle(r,c))){
                            break outer;
                        }
                    }//RECYCLE 目标为自己格子 未回收过

                    //VIEW 目标未地方格子并且没有被揭示的
                    if(thisCell.getOwner()==opp&&isVision&&!thisCell.getRevealed()&&Ap>=ActionEngine.COST_VIEW){
                        if(addCapped(list,AIMove.view(r,c)))break outer;
                    }
                }
            }







        }else{ //棋盘上无子
            int boardsize=game.getBoardSize();

            outer1:
            for(int r=0;r<boardsize;r++){
                for(int c=0;c<boardsize;c++){
                    //ai只会落空格 以防在对方格子上下
                    if(!game.getCell(r,c).isEmpty())continue;
                    if(Ap>=ActionEngine.COST_DROP){
                        if(addCapped(list,AIMove.dropNew(r,c))){
                            break outer1;
                        }
                    }
                    if(okDropHand(playerhand,Ap)){
                        for(int idx=0;idx<playerhand.size();idx++){
                          if(addCapped(list,AIMove.dropHand(r,c,idx))){
                              break outer1;
                          }
                        }
                    }
                }
            }//for

        }







        //return 之前添加END
        list.add(AIMove.end());
        return list;
    }


    private static boolean okDropHand(Hand hand,int Ap){
        return !hand.isEmpty()&&Ap>=ActionEngine.COST_DROP;
    }

    private static boolean HasMyPiece(GameState game,Owner current){
        Cell[][] board=game.getBoard();
        int boardSize=game.getBoardSize();
        for(int x=0;x<boardSize;x++){
            for(int y=0;y<boardSize;y++){
                Cell cell=board[x][y];
                if(cell!=null&&cell.getOwner()==current){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean adjacentToMine(GameState game,int r,int c,Owner player){
        int boardSize=game.getBoardSize();
        for(int dr=-1;dr<=1;dr++){
            for(int dc =-1;dc<=1;dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr < 0 || nr >= boardSize || nc < 0 || nc >= boardSize) continue;
                Cell cell = game.getCell(nr, nc);
                if (cell != null && cell.getOwner() == player) return true;//在8领域有自己的棋子
            }
        }
        return false;
    }//可以多次多用isAdjacent()实现 v1:上面没有调用这个 少了代码复用后面才发现nr>= 的=漏掉了


    //助手 在添加之后 自动判断是不是list.size()>=400
    private static boolean addCapped(AiMoveList list,AIMove move){
        list.add(move);
        return list.size()>=100;

    }

    //v1 直接拷贝game运行 //v2:使用helper:applyMoveTo
    public static GameState applyMove(GameState game, AIMove move, Owner player) {
        GameState gameCopy = game.GameStateCopy();
        boolean ok = applyMoveTo(gameCopy, move, player);
        return ok ? gameCopy : null;          // 失败返回 null（安全闸）
    }
//
//    public static GameState applyMove(GameState game,AIMove move,Owner player){
//        GameState gameCopy=game.GameStateCopy();
//        gameCopy.setCurrent(player);
//        ActionEngine actionEngine =new ActionEngine();
//        int rc=0;//返回code
//        switch (move.type()){ //record 类型中自带的访问器访问AiMoveType type
//            case DROP_NEW ->rc=actionEngine.actDropNew(gameCopy, move.r(),move.c());
//            case DROP_HAND ->rc=actionEngine.actDropHand(gameCopy,move.r(),move.c(),move.idx());
//            case RECYCLE ->rc=actionEngine.actRecycle(gameCopy,move.r(),move.c());
//            case VIEW -> rc=actionEngine.actView(gameCopy,move.r(),move.c());
//            case DEVUOR ->rc=actionEngine.actDevour(gameCopy,move.r(),move.c(), move.idx());
//            case SYNTH -> rc=actionEngine.actSynthesize(gameCopy,move.r(),move.c(),move.r2(),move.c2());
//            case END -> actionEngine.actEndTurn(gameCopy);
//        }
//        return rc==0?gameCopy:null;//在外需要考虑一下null的情况
//        //虽然经过genmove过滤不会触发执行失败 但是以防万一
//    }//apply一个AiMove


    public static double evaluateState(GameState game,Owner me){
        Owner opp =(me==Owner.BLACK)?Owner.WHITE:Owner.BLACK;

        //制导因素 分数差  *1000.0 v1:gei *20.0 会让ai一直四连不收
        double score=(game.getPlayer(me).getScore()-game.getPlayer(opp).getScore())*1000.0;
        score +=linePotential(game,me)*1.0;//连线潜力
        score +=(sumLevels(game,me)-sumLevels(game,opp))*0.5;//偏好高等级 收分快
        int myMax=maxRun(game,me);
        int oppMax=maxRun(game,opp);
        if(myMax>=4)score+=50.0; //4子连我 getscore
        if(oppMax>=4)score -=200.0;//对方四子 谨慎
        score +=(visionCells(game,me)-visionCells(game,opp))*0.1;//视野
        score +=(game.getPlayer(me).getAp()-game.getPlayer(opp).getAp())*0.02;
        score+=game.getPlayer(me).getHand().size()*1.0;

        return score;
    }

    //扫描整张棋盘，计算指定玩家在横、竖、两条斜方向上最长的一条连续棋子数。
    private static int maxRun(GameState game,Owner player){
        int best=0;
        int boardSize=game.getBoardSize();
        for(int d=0;d<4;d++){
            int dx=DIRS[d][0];
            int dy=DIRS[d][1];
            for(int r=0;r<boardSize;r++){
                for(int c=0;c<boardSize;c++){
                if(game.getCell(r,c).getOwner()!=player)continue;//己格
                int pr=r-dx;
                int pc=c-dy;
                if(pr>=0&&pr<boardSize&&pc>=0&&pc<boardSize&&game.getCell(pr,pc).getOwner()==player){
                    continue;
                }//前导格子 直到（pr pc）不是己格

                    //前导完成 开始数格子
                    int len=0;
                    int rr=r;
                    int cc=c;
                    while (rr>=0&&rr<boardSize&&cc>=0&&cc<boardSize&&game.getCell(rr,cc).getOwner()==player){
                        len++;
                        rr+=dx;
                        cc+=dy;
                        if(len>best)best=len;
                    }
                }

            }
        }//forfor
        return best;
    }

    //得到棋子总的等级
    private static int sumLevels(GameState game,Owner player){
        int score=0;
        int boardSize=game.getBoardSize();
        for(int r=0;r<boardSize;r++){
            for(int c=0;c<boardSize;c++){
                if(game.getCell(r,c).getOwner()==player){
                    score+=game.getCell(r,c).getLevel();

                }
            }
        }//for-for
        return score;
    }

    //视野大小 vis boolena[][].size
    private static int visionCells(GameState game,Owner player){
        int boardSize=game.getBoardSize();
        boolean[][] vis=new boolean[boardSize][boardSize];
        VisionEngine.computeVision(game,player,vis);
        int size=0;
        for(int r=0;r<boardSize;r++){
            for(int c=0;c<boardSize;c++)
            if (vis[r][c]) {
                size++;
            }
        }
        return size;
    }

    private  static int linePotential(GameState game,Owner player){
        int total=0;
        int boardSize=game.getBoardSize();
        for(int d=0;d<4;d++){
            int dx=DIRS[d][0];
            int dy=DIRS[d][1];
            for(int r=0;r<boardSize;r++){
                for(int c=0;c<boardSize;c++){

                       if(game.getCell(r,c).getOwner()!=player)continue;
                       int pr=r-dx;
                       int pc=c-dy;
                       if(pr>=0&&pr<boardSize&&pc>=0&&pc<boardSize&&game.getCell(pr,pc).getOwner()==player)continue;
                       int len=0;
                       int rr=r;
                       int cc=c;
                       while(rr>=0&&rr<boardSize&&cc>=0&&cc<boardSize&&game.getCell(rr,cc).getOwner()==player){
                           len++;
                           rr+=dx;
                           cc+=dy;
                       }
                       int openEnds=0;//端点是否open
                        //（sr sc）为（r,c)前端
                        //(rr,cc)在以上算完连子后在（r,c）的后端
                       int sr=r-dx;
                       int sc=c-dy;
                       if(sr>=0&&sr<boardSize&&sc>=0&&sc<boardSize && game.getCell(sr,sc).getOwner()==Owner.EMPTY) openEnds++;
                       if(rr>=0&&rr<boardSize&&cc>=0&&cc<boardSize && game.getCell(rr,cc).getOwner()==Owner.EMPTY) openEnds++;
                       int w=0;

                       switch (len){
                           case 1:w=1;break;
                           case 2:w=5;break;
                           case 3:w=30;break;
                           case 4:w=300;break;
                           default:w=1000;break;//ai预算中 6连甚至5连 大权重
                           //为什么是1000 因为5连以上即使两边堵死 *0.3 也有300权重
                       }
                       //openEnds ==2 活棋 openEnds==0 死棋
                       if(openEnds==2)w*=2;
                       else if(openEnds==0){
                           w=(int)(w*0.3);
                       }
                        total+=w;


                }
            }//for-for
        }
        return total;
    }
//v2：normal对局时间测试 1分55秒
    //v2:增加了make unmake 撤销栈帧  （栈用java自带的） 不用gamecopy 每次都要完全复制一边game
    public static AIMove aiChooseMove(GameState game,Owner player){
        AiMoveList moves=genMoves(game,player);
        AIMove best=AIMove.end();//找到“最佳”一步
        int tries=0;
        int hardLevel= game.getDifficulty();
        if(hardLevel==DIFF_EASY) { //EASY:随机
            java.util.Random rand = new java.util.Random();
            do {//随机抽取
                best = moves.getAiMoveidx(rand.nextInt(moves.size()));
                //随机抽取就使用getidx了 //v3：normal 对局时间测试：1分42秒 没有根本性缩短时间
                tries++;
            } while (best.type() == MoveType.END && moves.size() > 1 && tries < 10);

            return best;
        }

        double bestEval=-1e9;
        boolean hard =(hardLevel==HARD);//hard depth==1

        //有索引查询 改为迭代器 for—each遍历
            for (AIMove move:moves) {
                //AIMove move = moves.getAiMoveidx(i);
                //V1:我将moveList写成了链表 getidx在每个虚幻中一共要m*n次

                //GameState next=applyMove(game,move,player);
                UndoRecord ur = UndoRecord.makeMove(game, move, player);
                if (ur == null) continue;//安全检测：执行失败 跳过 非法move 已经自带弹出了
                double eval;

                if(hard){//如果是hard
                    //考虑对手的一步 最坏（对手最好）
                    Owner opp = (player == Owner.BLACK) ? Owner.WHITE : Owner.BLACK;
                    AiMoveList opponentMoves = genMoves(game, opp);
                    double worst = 1e9;
                    //内存循环 一样由索引查询(每次都要n次 改为) for_each遍历
                    for (AIMove oppMove:opponentMoves) {
                        UndoRecord urOpp = UndoRecord.makeMove(game, oppMove, opp);
                        if (urOpp == null) continue;//执行失败 自带Pop

                        double e = evaluateState(game, player);//评估

                        UndoRecord.undoMove(game, urOpp); //Pop
                        if (e < worst) worst = e;//考虑对手出了最有利于对手的一步 也就是我方e最小的情况
                    }//for moveopp
                    eval = (opponentMoves.size() > 0) ? worst : evaluateState(game, player);
                }
                else{
                    eval = evaluateState(game, player);
                }

                UndoRecord.undoMove(game, ur);//撤回
                if (eval > bestEval) {
                    bestEval = eval;
                    best = move;
                }

            }//for moveme

        return best;
    }


    //落地ai行为
    public   static boolean applyMoveTo(GameState game,AIMove move,Owner player){
        game.setCurrent(player);
        ActionEngine engine =new ActionEngine();
        int rc=0;//retuen code
        switch (move.type()){
            case DROP_NEW -> rc = engine.actDropNew(game, move.r(), move.c());
            case DROP_HAND -> rc = engine.actDropHand(game, move.r(), move.c(), move.idx());
            case RECYCLE  -> rc = engine.actRecycle(game, move.r(), move.c());
            case VIEW     -> rc = engine.actView(game, move.r(), move.c());
            case DEVUOR   -> rc = engine.actDevour(game, move.r(), move.c(), move.idx());
            case SYNTH    -> rc = engine.actSynthesize(game, move.r(), move.c(), move.r2(), move.c2());
            case END      -> engine.actEndTurn(game);
        }
        return rc == 0;
    }

    //aiTakeTurn 回合
    public  static void aiTakeTurn(GameState game){
        ActionEngine engine =new ActionEngine();
        int guard =0; //防止卡死 一直下 单回合最多60步 或许还可以减小，如果一直超过
        //有必要减少合成奖励分 5->3/4?
        while(game.getPhase()==1&&guard<60){
            guard++;
            AIMove move=aiChooseMove(game,game.getCurrent());
            if(move.type()==MoveType.END){
                engine.actEndTurn(game);
                break;//结束
            }

            boolean ok=applyMoveTo(game,move,game.getCurrent());
            if(!ok){   //
                engine.actEndTurn(game);
                break;
            }

            if(game.getPlayer(game.getCurrent()).getAp()<1){
                //ap不足
                engine.actEndTurn(game);
                break;

            }

        }



    }


}


