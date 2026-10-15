package com.laiyesheng.fttt.ai;

import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.map.Owner;

public class GenMovesSelfTest {
    public static void main(String[] args) {
        System.out.println("===== 场景1：对局中（黑方有子） =====");
        scenarioMidgame();

        System.out.println("\n===== 场景2：开局（无任何子） =====");
        scenarioOpening();
    }

    // 摆一个典型对局局面：黑方有子、白方在视野内、黑方有手牌
    static void scenarioMidgame(){
        GameState g = new GameState(1,1,50);
        g.setCurrent(Owner.BLACK);
        g.getPlayer(Owner.BLACK).setAp(10);

        // 黑方三子：其中 (5,5)(5,6) 同级相邻 → 应触发 SYNTH
        g.getCell(5,5).setOwner(Owner.BLACK); g.getCell(5,5).setLevel(2);
        g.getCell(5,6).setOwner(Owner.BLACK); g.getCell(5,6).setLevel(2);
        g.getCell(4,4).setOwner(Owner.BLACK); g.getCell(4,4).setLevel(1);

        // 白方两子，落在黑方 5x5 视野内且未揭示 → 应触发 DEVOUR + VIEW
        g.getCell(5,7).setOwner(Owner.WHITE); g.getCell(5,7).setLevel(3);
        g.getCell(5,8).setOwner(Owner.WHITE); g.getCell(5,8).setLevel(1);

        // 给黑方手牌，触发 DROP_HAND / DEVOUR 的手牌变体
        g.getPlayer(Owner.BLACK).getHand().add(2);
        g.getPlayer(Owner.BLACK).getHand().add(4);

        printHist("黑方着法", AI.genMoves(g, Owner.BLACK));
    }

    // 开局：棋盘空，验证不崩、且能落子
    static void scenarioOpening(){
        GameState g = new GameState(1,1,50);
        g.setCurrent(Owner.BLACK);
        g.getPlayer(Owner.BLACK).setAp(10);
        printHist("开局着法", AI.genMoves(g, Owner.BLACK));
    }

    static void printHist(String title, AiMoveList list){
        System.out.println(title + "  总数=" + list.size());
        for(MoveType mt : MoveType.values()){
            int n = 0;
            for(int i=0;i<list.size();i++)
                if(list.getAiMoveidx(i).type()==mt) n++;
            System.out.println("  " + mt + " : " + n);
        }
    }
}
