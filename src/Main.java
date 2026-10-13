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
        GameState game=new GameState(1,1,50);
        ActionEngine action=new ActionEngine();
        game.getCurrentPlayer().setAp(25);
        int a=action.actDropNew(game,1,1);

        System.out.println(a);
        System.out.println(Owner.BLACK.ordinal());
        System.out.println(game.getCell(1,1).getOwner().ordinal());
        action.actDropNew(game,1,2);
        action.actDropNew(game,1,3);
        action.actDropNew(game,1,4);
        System.out.println(game.getCurrentPlayer().getScore());
        System.out.println(game.getCell(1,1).getOwner().ordinal());
        action.actDropNew(game,1,5);
        System.out.println(game.getCurrentPlayer().getScore());
        System.out.println(game.getCell(1,1).getOwner().ordinal());
        System.out.println(game.getCell(1,1).getOwner().ordinal());





    }
}
