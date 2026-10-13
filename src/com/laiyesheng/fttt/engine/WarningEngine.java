package com.laiyesheng.fttt.engine;

import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.map.Cell;

import java.util.Random;

public class WarningEngine {
    public  static void check_waring(GameState game,Owner player){
        if(game.getPhase()!=1)return;
        if(game.getPlayer(player).isWarningUsedThisTurn())return;
        Owner opp=player==Owner.BLACK?Owner.WHITE:Owner.BLACK;
        //opp check对象
        int boardSize=game.getBoardSize();
        int[][] DIRS={{1,0},{0,1},{1,1},{1,-1}};
        for(int d=0;d<4;d++){
            int dx=DIRS[d][0], dy=DIRS[d][1];
            for(int r=0;r<boardSize;r++){
                for(int c=0;c<boardSize;c++){
                    int er=r+dx*4,ec=c+dy*4;
                    if(er<0||er>=boardSize||ec<0||ec>=boardSize){
                        continue;
                    }
                    int oppCount=0;
                    int emptyIdx=-1;
                    int oc=0;
                    int inVis=0;
                    int[][] oppCells=new int[4][2];
                    for(int k=0;k<5;k++){
                        int rr=r+dx*k,cc=c+dy*k;
                        Cell cell =game.getCell(rr,cc);
                        if(cell.getOwner()==opp){
                            oppCount++;
                            oppCells[oc][0]=rr;
                            oppCells[oc][1]=cc;
                            oc++;
                            //记录四连点
                            if(VisionEngine.inVision(game,player,rr,cc)){
                                inVis=1;
                            }

                        }else if(cell.getOwner()==Owner.EMPTY){
                            emptyIdx=k;
                        }

                        if(oppCount==4&&emptyIdx>=0&&inVis==1){
                            Random rand=new Random();
                            int pick=rand.nextInt(oc);//oc=4
                            int pr=oppCells[pick][0];
                            int pc=oppCells[pick][1];

                            //揭示 完成waring
                            game.getCell(pr,pc).setRevealed(true);
                            game.getPlayer(player).setWarningUsedThisTurn(true);
                            //!
                            return;

                        }//连城四子，其中至少一子可见
                    }

                }
            }
        }



    }

}
