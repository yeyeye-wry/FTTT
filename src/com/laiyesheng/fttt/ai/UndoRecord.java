package com.laiyesheng.fttt.ai;

import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.state.PlayerState;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.map.Cell;
import com.laiyesheng.fttt.ai.AI;
//UndoRecord：
public final class UndoRecord {
    final int boardSize;
    final int[] boardSnap;          // 整盘紧凑快照：每格 [owner序, level, revealed]
    final PlayerState savedMe;      // 当前方玩家状态（含手牌）拷贝
    final PlayerState savedOpp;     // 对方玩家状态拷贝
    //一些game所具有的标量
    final int savedTurn;
    final int savedPhase;
    final int savedBlackFirstDone;
    final Owner savedWinner;
    final Owner savedCurrent;


    UndoRecord(int boardSize, PlayerState me, PlayerState opp, int turn, int phase,
               int blackFirstDone, Owner winner, Owner current) {
        this.boardSize = boardSize;
        this.boardSnap = new int[boardSize * boardSize * 3];   // 只建数组；内容稍后在 makeMove 里填
        this.savedMe = me;
        this.savedOpp = opp;
        this.savedTurn = turn;
        this.savedPhase = phase;
        this.savedBlackFirstDone = blackFirstDone;
        this.savedWinner = winner;
        this.savedCurrent = current;
    }


    //make
    public static UndoRecord makeMove(GameState game, AIMove move, Owner player) {
        int boardSize = game.getBoardSize();
        Owner opp = (player == Owner.BLACK) ? Owner.WHITE : Owner.BLACK;
        UndoRecord ur = new UndoRecord(
                boardSize,
                game.getPlayer(player).playerCopy(),
                game.getPlayer(opp).playerCopy(),
                game.getTurn(),
                game.getPhase(),
                game.isBlackFirstDone() ? 1 : 0,
                game.getWinner(),
                game.getCurrent()
        );
        //棋盘快照 boardSnap
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Cell cell = game.getCell(r, c);
                int i = (r * boardSize + c) * 3;//二维坐标一维数组扁平映射
                ur.boardSnap[i] = cell.getOwner().ordinal();
                ur.boardSnap[i + 1] = cell.getLevel();
                ur.boardSnap[i + 2] = cell.getRevealed() ? 1 : 0;
            }
        }//forfor
        boolean ok = AI.applyMoveTo(game, move, player);
        if (!ok) {
            undoMove(game, ur);//非法行为 弹出
            return null;
        }
        return ur;
    }


    public static void undoMove(GameState game,UndoRecord ur){
        int boardSize=ur.boardSize;
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                int i = (r * boardSize + c) * 3;
                Cell cell = game.getCell(r, c);
                cell.setOwner(Owner.values()[ur.boardSnap[i]]);
                cell.setLevel(ur.boardSnap[i+1]);
                cell.setRevealed(ur.boardSnap[i+2]==1);
            }
        }
        game.getPlayers()[game.pidx(ur.savedMe.getOwner())]   = ur.savedMe;
        game.getPlayers()[game.pidx(ur.savedOpp.getOwner())]  = ur.savedOpp;
        game.setTurn(ur.savedTurn);
        game.setPhase(ur.savedPhase);
        game.setBlackFirstDone(ur.savedBlackFirstDone == 1);
        game.setWinner(ur.savedWinner);
        game.setCurrent(ur.savedCurrent);
    }
}