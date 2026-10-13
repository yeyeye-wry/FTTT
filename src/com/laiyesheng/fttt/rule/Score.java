package com.laiyesheng.fttt.rule;

import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.map.Cell;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.state.PlayerState;

public class Score {
    public static void awardScores(GameState game,Owner owner){
        int BoardSize=game.getBoardSize();
        int [][] DIRS={{1,0},{0,1},{1,1},{1,-1}};
        // 有 下 右下 右上 {1，0} {0，1} {1.1} {1，-1}

        for(int d=0;d<4;d++){
            int dx=DIRS[d][0],dy=DIRS[d][1];
            for(int r=0;r<BoardSize;r++){
                for(int c=0;c<BoardSize;c++){
                    int er=r+dx*4,ec=c+dy*4;
                    if(er<0||er>=BoardSize||ec<0||ec>=BoardSize)continue;//不会5子

                    boolean ok=true;//能不能连5
                    int sum=0;
                    for(int k=0;k<5;k++){
                        Cell cell =game.getCell(r+dx*k,c+dy*k);
                        if(cell.getOwner()!=owner){
                            ok=false;break;
                        }
                        sum+=cell.getLevel();
                    }
                    if(!ok)continue;

                    PlayerState ps=game.getPlayer(owner);
                    ps.setScore(ps.getScore()+sum);
                    for(int k=0;k<5;k++){
                        //清空
                        game.getCell(r+dx*k,c+dy*k).clear();
                    }
                    if(ps.getScore()>=game.getWinScore()){
                        game.setWinner(owner);
                        game.setPhase(2);//Phase=2 结束对局
                    }
                }
            }
        }
    }

}
