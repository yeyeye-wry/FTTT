package com.laiyesheng.fttt.engine;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.map.Owner;
public class VisionEngine {
    public static final int VIS_R=2;//视野半径
    // r-2 r+2 c-2 c+2 构成5*5视野

    /**
     * 计算玩家视野，填充vis二维boolean数组 能否看见
     * @param game GameState 游戏完整的状态
     * @param player 当前要计算视野的玩家
     * @param vis 输出的数组，初始值为false
     */


    public static void computeVision(GameState game,Owner player,boolean[][] vis) {
        int n= game.getBoardSize();

        for(int r=0;r< n;r++){
            for(int c=0;c<n;c++){
                if(game.getCell(r,c).getOwner()==player){
                    for(int dr=-VIS_R;dr<=VIS_R;dr++){
                        for(int dc=-VIS_R;dc<=VIS_R;dr++){
                            int nr=r+dr;
                            int nc=c+dc;
                            if(nr>=0&&nr<n&&nc>=0&&nc<n){//注意数组越界
                                vis[nr][nc]=true;//true->可以看见
                            }
                        }
                    }
                }
            }
        }//迷雾迷漫中
    }//computeVision

    /**
     * 判断坐标（r,c）格子，是否在该玩家视野内
     * @param g 游戏完整状态
     * @param player 玩家
     * @param r 目标行
     * @param c 目标列
     * @return true=在视野中，false=迷雾看不见
     */

    public static boolean inVision(GameState g,Owner player,int r,int c){
        int n=g.getBoardSize();

        if(r<0||r>=n||c<0||c>=n){
            return false;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (g.getCell(i, j).getOwner() == player) {
                    if (Math.abs(i - r) <= VIS_R && Math.abs(j - c) <= VIS_R) {
                        return true;   // 有己方子在 (r,c) 的 5x5 内 → 可见
                    }
                }
            }
        }
        return false;
    }
}
