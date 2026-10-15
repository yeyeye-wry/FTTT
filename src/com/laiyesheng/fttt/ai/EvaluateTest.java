package com.laiyesheng.fttt.ai;

import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.state.GameState;

public class EvaluateTest {
    public static void main(String[] args) {
        // 局面A：黑方 4 连（水平，放在第5行，故意绕开 row0，用来验证 maxRun 修没修好）
        GameState A = new GameState(1, 2, 50);
        for (int c = 3; c <= 6; c++) {
            A.getCell(5, c).setOwner(Owner.BLACK);
            A.getCell(5, c).setLevel(1);
        }

        // 局面B：黑方只有 1 个孤立子
        GameState B = new GameState(1, 2, 50);
        B.getCell(5, 5).setOwner(Owner.BLACK);
        B.getCell(5, 5).setLevel(1);

        double eA = AI.evaluateState(A, Owner.BLACK);
        double eB = AI.evaluateState(B, Owner.BLACK);
        System.out.println("A (4连) = " + eA);
        System.out.println("B (孤子) = " + eB);
        System.out.println("eA > eB ? " + (eA > eB));

        // 诊断 maxRun：同样的 4 连放在 row0 和 row5，修好后两者都应含 +50 且分数接近
        GameState A0 = new GameState(1, 2, 50);
        for (int c = 3; c <= 6; c++) {
            A0.getCell(0, c).setOwner(Owner.BLACK);
            A0.getCell(0, c).setLevel(1);
        }
        double eA0 = AI.evaluateState(A0, Owner.BLACK);
        System.out.println("A0 (row0 4连) = " + eA0);
        System.out.println("maxRun已修好(row5>=row0) ? " + (eA >= eA0));
        // 修好: eA≈eA0(行5视野略多, eA稍高) → true
        // 没修: row5漏掉+50, eA比eA0低约50 → false
    }
}
