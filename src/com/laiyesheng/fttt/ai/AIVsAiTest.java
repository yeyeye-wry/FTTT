package com.laiyesheng.fttt.ai;

import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.state.GameState;

public class AIVsAiTest {
    public static void main(String[] args) {
        int difficulty = 1;                       // 先 1(NORMAL) 快速验证；再 2(HARD) 压测深搜
        GameState g = new GameState(1, difficulty, 50);
        int maxTurns = 300;
        int t = 0;
        while (g.getPhase() == 1 && t < maxTurns) {
            AI.aiTakeTurn(g);                      // 当前是谁就替谁走，走完自动换人
            t++;
            if (t % 20 == 0) {
                System.out.println("回合=" + t
                        + " 黑分=" + g.getPlayer(Owner.BLACK).getScore()
                        + " 白分=" + g.getPlayer(Owner.WHITE).getScore()
                        + " 当前=" + g.getCurrent());
            }
        }
        System.out.println("=== 结束 === phase=" + g.getPhase()
                + " 总回合=" + t
                + " 黑分=" + g.getPlayer(Owner.BLACK).getScore()
                + " 白分=" + g.getPlayer(Owner.WHITE).getScore()
                + " 胜者=" + g.getWinner());
    }
}

