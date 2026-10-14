//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.laiyesheng.fttt.list.Log;
import com.laiyesheng.fttt.engine.VisionEngine;
import com.laiyesheng.fttt.engine.ActionEngine;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.map.Owner;
public class Main {
    public static void main(String[] args) {
        //Log 成功运行
        //Gamestate 初步运行没有问题
        //五子连成消除没有问题 score

            // ===== GameStateCopy 自检 =====
            GameState game = new GameState(1, 1, 50);

            // 1) 给原件造点状态
            game.getPlayer(Owner.BLACK).setAp(25);
            game.getPlayer(Owner.BLACK).getHand().add(5);   // 黑手牌放两颗
            game.getPlayer(Owner.BLACK).getHand().add(3);
            int originalHandSize = game.getPlayer(Owner.BLACK).getHand().size(); // = 2
            game.getCell(2, 2).setOwner(Owner.BLACK);
            game.getCell(2, 2).setLevel(4);
            game.getPlayer(Owner.WHITE).setScore(123);

            // 2) 克隆
            GameState copy = game.GameStateCopy();

            // 3) 先验证「克隆体确实拷对了」（改动之前）
            boolean ok = true;
            if (copy.getPlayer(Owner.BLACK).getHand().size() != originalHandSize) {
                System.out.println("FAIL: 克隆体手牌为空/不对 —— playerCopy() 没拷 hand"); ok = false;
            }
            if (copy.getPlayer(Owner.WHITE).getScore() != 123) {
                System.out.println("FAIL: 克隆体分数没拷对"); ok = false;
            }
            if (copy.getCell(2, 2).getLevel() != 4) {
                System.out.println("FAIL: 克隆体棋盘没拷对"); ok = false;
            }

            // 4) 再尽情改副本，原件不该受影响
            copy.getPlayer(Owner.BLACK).getHand().add(7);
            copy.getCell(3, 4).setLevel(9);
            copy.getPlayer(Owner.WHITE).setScore(999);

            // 5) 原件不受影响
            if (game.getPlayer(Owner.BLACK).getHand().size() != originalHandSize) {
                System.out.println("FAIL: 原件手牌被副本带跑"); ok = false;
            }
            if (game.getCell(3, 4).getLevel() == 9) {
                System.out.println("FAIL: 原件棋盘被改"); ok = false;
            }
            if (game.getPlayer(Owner.WHITE).getScore() == 999) {
                System.out.println("FAIL: 原件分数被改"); ok = false;
            }

            System.out.println(ok ? "ALL PASS ✅ deepCopy 正确" : "有 FAIL ❌ 见上面");




    }
}
