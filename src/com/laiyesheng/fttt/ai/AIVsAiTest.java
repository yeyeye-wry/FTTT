package com.laiyesheng.fttt.ai;

import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.state.GameState;

//v4: 将链表替换顺序表 一样使用迭代器接口 ？？
//以score 100 20*20测试
// 9.0s no debug
//8.0 9.0s
//8.25s
public class AIVsAiTest {

    public static void main(String[] args) {
        int difficulty = 2;                       // 先 1(NORMAL) 快速验证；再 2(HARD) 压测深搜
        GameState g = new GameState(1, difficulty, 100);
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

