//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.laiyesheng.fttt.list.Log;
import com.laiyesheng.fttt.engine.VisionEngine;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.map.Owner;
public class Main {
    public static void main(String[] args) {
        //Log 成功运行
        //Gamestate 初步运行没有问题

        GameState game = new GameState(1, 1, 1);
        game.getCell(5, 5).setOwner(Owner.BLACK);
        System.out.println(VisionEngine.inVision(game, Owner.BLACK, 7, 7));
        System.out.println(VisionEngine.inVision(game, Owner.BLACK, 7, 8));

    }
}