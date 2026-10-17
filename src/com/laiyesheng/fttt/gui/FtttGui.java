package com.laiyesheng.fttt.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import com.laiyesheng.fttt.map.Owner;
import com.laiyesheng.fttt.map.Cell;
import com.laiyesheng.fttt.state.GameState;
import com.laiyesheng.fttt.state.PlayerState;
import com.laiyesheng.fttt.engine.ActionEngine;
import com.laiyesheng.fttt.engine.VisionEngine;
import com.laiyesheng.fttt.ai.AI;
import com.laiyesheng.fttt.ai.AIMove;
import com.laiyesheng.fttt.ai.MoveType;
import com.laiyesheng.fttt.ai.MoveArrayList;
import com.laiyesheng.fttt.user.UserService;
import com.laiyesheng.fttt.user.User;
import com.laiyesheng.fttt.persist.RankRow;
import com.laiyesheng.fttt.persist.MysqlRepository;
import com.laiyesheng.fttt.persist.ForumRepository;
import com.laiyesheng.fttt.persist.MysqlForumRepository;
import com.laiyesheng.fttt.forum.ForumService;

/**
 * FTTT（五子棋·吞噬等级版）Swing GUI —— 不改后端，只做菜单/渲染/交互。
 *
 * 补齐的「选择 / 合并 / 吞噬 / 提示」界面：
 *  1) 开局设置弹窗：模式(双人/人机) + 难度(简单/普通/困难) + 胜利分数
 *  2) 合法目标高亮：复用已有 AI.genMoves 算出当前动作的合法格子，在棋盘上高亮
 *  3) 上下文提示：状态栏实时说明「现在该点什么 / 有几个可选」
 *  4) AI 提示按钮：用 AI.aiChooseMove 给出建议着法并青色高亮
 */
public class FtttGui extends JFrame {

    private final ActionEngine engine = new ActionEngine();
    private GameState game;

    private final Owner humanSide = Owner.BLACK;
    private boolean vsAI = true;
    private int difficulty = AI.NORML;
    private int winScore = 50;
    private boolean aiThinking = false;
    private boolean gameOverShown = false;

    private final UserService userService = new UserService(new MysqlRepository());
    private final ForumService forumService = new ForumService(new MysqlForumRepository());
    private User currentUser = null;
    private JButton loginBtn;

    private final int MARGIN = 24;
    private int cellPx = 30;
    private boolean[][] vision;

    private ActionMode mode = ActionMode.DROP_NEW;
    private int selR = -1, selC = -1;
    private final Set<String> targets = new HashSet<>();   // 当前动作的合法目标 "r,c"
    private AIMove hintMove = null;                        // AI 提示的着法
    private final Deque<GameState> history = new ArrayDeque<>();

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final BoardPanel boardPanel = new BoardPanel();
    private final JLabel statusLabel = new JLabel(" ", JLabel.LEFT);
    private final JTextArea logArea = new JTextArea(3, 18);
    private final JLabel infoLabel = new JLabel(" ");
    private final ButtonGroup modeGroup = new ButtonGroup();
    private final JComboBox<String> pieceChoice = new JComboBox<>();  // 吞噬/落手牌时选「新子 or 手牌第几张」
    private boolean rebuildingChoice = false;   // 防止 rebuild 时触发 ActionEvent 递归

    // 各玩家「上一次有棋子时」的视野记忆。中途棋子清零时用它在限定范围内落子，
    // 有了新棋子后自动恢复正常视野机制；同时避免用报错暴露隐藏棋子的位置。
    private final java.util.Map<Owner, boolean[][]> lastVision = new java.util.EnumMap<>(Owner.class);

    enum ActionMode { DROP_NEW, DROP_HAND, DEVOUR, RECYCLE, VIEW, SYNTH }

    public FtttGui() {
        super("Gomoku Devour Level Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        root.add(buildMenu(), "MENU");
        root.add(buildGame(), "GAME");
        add(root);
        setSize(980, 780);
        setLocationRelativeTo(null);
        cards.show(root, "MENU");
        setVisible(true);
    }

    // ==================================================== 菜单屏

    private JPanel buildMenu() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(58, 38, 20),
                                              0, getHeight(), new Color(96, 62, 32)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(70, 46, 24, 90));
                for (int y = 0; y < getHeight(); y += 26) g2.fillRect(0, y, getWidth(), 2);
            }
        };
        JLabel title = new JLabel("五子棋", SwingConstants.CENTER);
        title.setFont(titleFont(92));
        title.setForeground(new Color(226, 188, 78));
        JLabel sub = new JLabel("吞 噬 等 级 版", SwingConstants.CENTER);
        sub.setFont(titleFont(26));
        sub.setForeground(new Color(214, 186, 120));
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
        top.add(title); top.add(sub);
        p.add(top, BorderLayout.CENTER);

        JPanel btns = new JPanel(new GridLayout(0, 1, 12, 12));
        btns.setOpaque(false);
        btns.setBorder(BorderFactory.createEmptyBorder(0, 330, 80, 330));
        btns.add(menuBtn("双人模式", e -> openSetup(false)));
        btns.add(menuBtn("人机模式", e -> openSetup(true)));
        btns.add(menuBtn("帮  助", e -> showHelp()));
        loginBtn = menuBtn("登录 / 注册", e -> openLogin());
        btns.add(loginBtn);
        btns.add(menuBtn("排 行 榜", e -> showRank()));
        btns.add(menuBtn("论  坛", e -> openForum()));
        btns.add(menuBtn("退出游戏", e -> System.exit(0)));
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private JButton menuBtn(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(uiFont(Font.BOLD, 24));
        b.setBackground(new Color(112, 74, 40));
        b.setForeground(new Color(243, 226, 184));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(214, 186, 120), 2));
        b.setPreferredSize(new Dimension(300, 54));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this,
            "落子 / 吞噬 / 回收 各 5 AP；侦查 1 AP；合成 0 AP 且成功 +5 AP。\n" +
            "每回合 10 AP，黑方首回合 5 AP。\n" +
            "同等级相邻己方可合成 → 等级 +2；五连计分后消子；先到胜利分数者胜。\n" +
            "战争迷雾：只看见己方棋子周围 5×5；用「侦查」揭示敌方等级。\n\n" +
            "操作：先选右侧动作 → 棋盘上「绿色高亮格」就是可以点的目标 → 点击执行。",
            "帮助", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRank() {
        var rows = userService.topN(10);
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暂无战绩数据。",
                "排行榜", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("排行榜（前 10 名）\n\n");
        int rank = 1;
        for (RankRow r : rows) {
            sb.append(rank++).append(".  ").append(r.username)
              .append("   最高分 ").append(r.score).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(),
            "排行榜", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================================================== 登录 / 注册

    private void openLogin() {
        if (currentUser != null) {                 // 已登录 → 提供退出
            int r = JOptionPane.showConfirmDialog(this,
                "当前已登录：" + currentUser.getUsername() + "\n确定退出登录？",
                "账户", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                currentUser = null;
                refreshLoginUI();
            }
            return;
        }

        // 登录页
        JTextField lu = new JTextField(12);
        JPasswordField lp = new JPasswordField(12);
        JPanel loginP = new JPanel(new GridLayout(0, 2, 6, 6));
        loginP.add(new JLabel("用户名：")); loginP.add(lu);
        loginP.add(new JLabel("密码："));   loginP.add(lp);

        // 注册页
        JTextField ru = new JTextField(12);
        JPasswordField rp = new JPasswordField(12);
        JPanel regP = new JPanel(new GridLayout(0, 2, 6, 6));
        regP.add(new JLabel("用户名：")); regP.add(ru);
        regP.add(new JLabel("密码："));   regP.add(rp);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("登录", loginP);
        tabs.add("注册", regP);

        int res = JOptionPane.showConfirmDialog(this, tabs, "登录 / 注册",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        if (tabs.getSelectedIndex() == 1) {        // 注册
            String name = ru.getText().trim();
            String pwd = new String(rp.getPassword());
            if (name.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名和密码不能为空。");
                return;
            }
            if (!userService.register(name, pwd)) {
                JOptionPane.showMessageDialog(this, "用户名已存在。");
                return;
            }
            JOptionPane.showMessageDialog(this, "注册成功，请切到登录页登录。");
        } else {                                    // 登录
            String name = lu.getText().trim();
            String pwd = new String(lp.getPassword());
            User u = userService.login(name, pwd);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "用户名或密码错误。");
                return;
            }
            currentUser = u;
            refreshLoginUI();
            JOptionPane.showMessageDialog(this, "欢迎，" + u.getUsername() + "！");
        }
    }

    private void refreshLoginUI() {
        if (loginBtn != null)
            loginBtn.setText(currentUser == null ? "登录 / 注册" : "已登录：" + currentUser.getUsername());
    }

    // ==================================================== 论坛

    private void openForum() {
        new ForumWindow(this, forumService);   // 非模态：打开论坛时仍可返回主菜单
    }

    /** 供 ForumWindow 读当前登录用户（发帖需先登录） */
    public User getLoggedInUser() { return currentUser; }

    // ==================================================== 开局设置弹窗

    private void openSetup(boolean defaultVsAI) {
        if (aiThinking) return;
        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"双人模式 (PvP)", "人机模式 (PvAI)"});
        modeCombo.setSelectedIndex(defaultVsAI ? 1 : 0);
        JComboBox<String> diffCombo = new JComboBox<>(new String[]{"简单 (EASY)", "普通 (NORMAL)", "困难 (HARD)"});
        diffCombo.setSelectedIndex(difficulty == AI.DIFF_EASY ? 0 : (difficulty == AI.NORML ? 1 : 2));
        JSpinner scoreSpin = new JSpinner(new SpinnerNumberModel(50, 10, 500, 10));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("模式："));     panel.add(modeCombo);
        panel.add(new JLabel("难度："));     panel.add(diffCombo);
        panel.add(new JLabel("胜利分数：")); panel.add(scoreSpin);

        int res = JOptionPane.showConfirmDialog(this, panel, "开局设置",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        vsAI = modeCombo.getSelectedIndex() == 1;
        difficulty = diffCombo.getSelectedIndex() == 0 ? AI.DIFF_EASY
                   : (diffCombo.getSelectedIndex() == 1 ? AI.NORML : AI.HARD);
        winScore = (Integer) scoreSpin.getValue();
        cards.show(root, "GAME");
        newGame();
    }

    // ==================================================== 对局屏

    private JPanel buildGame() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(new Color(40, 30, 20));

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setBackground(new Color(112, 74, 40));
        bar.add(toolBtn("开局设置", e -> openSetup(vsAI)));
        bar.add(toolBtn("悔  棋", e -> undo()));
        bar.add(toolBtn("认  输", e -> resign()));
        bar.add(toolBtn("AI提示", e -> askHint()));
        bar.add(Box.createHorizontalGlue());
        bar.add(toolBtn("返回菜单", e -> { if (aiThinking) return; cards.show(root, "MENU"); }));

        statusLabel.setForeground(new Color(255, 232, 160));
        statusLabel.setFont(uiFont(Font.BOLD, 15));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.NORTH);
        top.add(statusLabel, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        boardPanel.setBackground(new Color(222, 184, 135));
        JScrollPane sc = new JScrollPane(boardPanel);
        sc.getViewport().setBackground(new Color(222, 184, 135));
        p.add(sc, BorderLayout.CENTER);

        JPanel side = new JPanel(new BorderLayout(6, 6));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(260, 10));
        side.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        infoLabel.setForeground(new Color(245, 232, 195));
        infoLabel.setFont(uiFont(Font.PLAIN, 14));
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        side.add(infoLabel, BorderLayout.NORTH);

        JPanel act = new JPanel(new GridLayout(0, 1, 3, 3));
        act.setOpaque(false);
        act.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(214, 186, 120)),
                "选择动作", TitledBorder.LEFT, TitledBorder.TOP,
                uiFont(Font.BOLD, 13), new Color(226, 188, 78)));
        for (ActionMode m : ActionMode.values()) {
            JRadioButton rb = new JRadioButton(labelOf(m), m == mode);
            rb.setForeground(new Color(245, 232, 195));
            rb.setOpaque(false);
            rb.setFont(uiFont(Font.PLAIN, 13));
            rb.addActionListener(e -> { mode = m; selR = selC = -1; hintMove = null; refreshTargets(); });
            modeGroup.add(rb);
            act.add(rb);
        }
        side.add(act, BorderLayout.CENTER);

        // 「使用棋子」选择：吞噬时可选 新子(Lv1) / 手牌第 i 张；落手牌时选手牌第 i 张
        JPanel choosePanel = new JPanel(new BorderLayout(4, 4));
        choosePanel.setOpaque(false);
        JLabel cbLabel = new JLabel("使用棋子：");
        cbLabel.setForeground(new Color(245, 232, 195));
        cbLabel.setFont(uiFont(Font.PLAIN, 13));
        pieceChoice.setFont(uiFont(Font.PLAIN, 13));
        pieceChoice.addActionListener(e -> { if (!rebuildingChoice) refreshTargets(); });
        choosePanel.add(cbLabel, BorderLayout.WEST);
        choosePanel.add(pieceChoice, BorderLayout.CENTER);

        JButton endBtn = new JButton("结束回合 →");
        endBtn.setFont(uiFont(Font.BOLD, 16));
        endBtn.addActionListener(e -> endTurn());

        JPanel bottom = new JPanel(new GridLayout(0, 1, 6, 6));
        bottom.setOpaque(false);
        bottom.add(choosePanel);
        bottom.add(endBtn);
        side.add(bottom, BorderLayout.SOUTH);
        p.add(side, BorderLayout.EAST);

        logArea.setEditable(false);
        logArea.setBackground(new Color(28, 20, 12));
        logArea.setForeground(new Color(200, 190, 160));
        logArea.setFont(uiFont(Font.PLAIN, 12));
        p.add(new JScrollPane(logArea), BorderLayout.SOUTH);
        return p;
    }

    private JButton toolBtn(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(uiFont(Font.BOLD, 14));
        b.setBackground(new Color(140, 96, 52));
        b.setForeground(new Color(245, 232, 195));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(214, 186, 120), 1));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    private static String labelOf(ActionMode m) {
        switch (m) {
            case DROP_NEW:  return "落子新子(Lv1) [5AP]";
            case DROP_HAND: return "落子手牌#0 [5AP]";
            case RECYCLE:   return "回收 [5AP]";
            case VIEW:      return "查看(敌) [1AP]";
            case DEVOUR:    return "吞噬(敌) [5AP]";
            case SYNTH:     return "合成(相邻同级) [0AP+5]";
        }
        return "";
    }

    // ==================================================== 流程控制

    private void newGame() {
        game = new GameState(vsAI ? 1 : 0, difficulty, winScore);
        game.getPlayer(game.getCurrent()).setAp(ActionEngine.BLACK_FIRST_AP);
        cellPx = 30;
        boardPanel.setPreferredSize(new Dimension(cellPx * game.getBoardSize() + 2 * MARGIN,
                                                  cellPx * game.getBoardSize() + 2 * MARGIN));
        boardPanel.revalidate();
        history.clear();
        gameOverShown = false;
        selR = selC = -1;
        hintMove = null;
        log("新对局：模式=" + (vsAI ? "人机" : "双人")
            + " 难度=" + (difficulty == AI.DIFF_EASY ? "简单" : difficulty == AI.NORML ? "普通" : "困难")
            + " 胜利分=" + winScore);
        refreshTargets();
    }

    private void endTurn() {
        if (aiThinking || game == null || game.getPhase() != 1) return;
        selR = selC = -1;
        hintMove = null;
        engine.actEndTurn(game);
        history.clear();
        if (game.getPhase() != 1) { repaintAll(); showGameOver(); return; }
        if (vsAI && game.getCurrent() != humanSide) runAI();
        else { repaintAll(); status("轮到 " + name(game.getCurrent()) + " 行动"); }
    }

    private void runAI() {
        aiThinking = true;
        status("AI 思考中…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { AI.aiTakeTurn(game); return null; }
            @Override protected void done() {
                aiThinking = false;
                history.clear();
                hintMove = null;
                if (game.getPhase() != 1) { repaintAll(); showGameOver(); return; }
                // 兜底：AI 若因 guard 上限没结束回合，这里补一次结束，避免卡死在 AI 方
                if (game.getCurrent() != humanSide) { engine.actEndTurn(game); history.clear(); }
                repaintAll();
                status("轮到你（黑）行动");
            }
        }.execute();
    }

    private void askHint() {
        if (aiThinking || game == null || game.getPhase() != 1) return;
        if (vsAI && game.getCurrent() != humanSide) { status("请等你自己的回合再要提示。"); return; }
        status("AI 计算提示中…");
        new SwingWorker<AIMove, Void>() {
            @Override protected AIMove doInBackground() {
                return AI.aiChooseMove(game, game.getCurrent());
            }
            @Override protected void done() {
                try {
                    hintMove = get();
                    if (hintMove == null || hintMove.type() == MoveType.END) {
                        status("提示：AI 认为本回合应结束。");
                    } else {
                        status("AI 提示：" + hintText(hintMove) + "（棋盘上青色圆圈）");
                        log("提示：" + hintText(hintMove));
                    }
                    boardPanel.repaint();
                } catch (Exception ex) {
                    status("提示计算失败：" + ex.getMessage());
                }
            }
        }.execute();
    }

    private String hintText(AIMove m) {
        String at = "(" + m.r() + "," + m.c() + ")";
        switch (m.type()) {
            case DROP_NEW:  return "在 " + at + " 落新子";
            case DROP_HAND: return "在 " + at + " 落手牌";
            case DEVUOR:    return "在 " + at + " 吞噬";
            case RECYCLE:   return "在 " + at + " 回收";
            case VIEW:      return "在 " + at + " 侦查";
            case SYNTH:     return "把 " + at + " 与 (" + m.r2() + "," + m.c2() + ") 合成";
            default:        return "结束回合";
        }
    }

    private void undo() {
        if (aiThinking || game == null || game.getPhase() != 1) return;
        if (vsAI && game.getCurrent() != humanSide) { status("只能悔自己的棋。"); return; }
        if (history.isEmpty()) { status("本回合没有可悔的棋。"); return; }
        game = history.pop();
        selR = selC = -1;
        hintMove = null;
        if (game.getPhase() == 1) gameOverShown = false;
        status("已悔棋。");
        refreshTargets();
    }

    private void resign() {
        if (aiThinking || game == null || game.getPhase() != 1) return;
        Owner loser = game.getCurrent();
        Owner winner = (loser == Owner.BLACK) ? Owner.WHITE : Owner.BLACK;
        int ok = JOptionPane.showConfirmDialog(this,
                name(loser) + "方 认输，确定吗？", "认输", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;
        game.setWinner(winner);
        game.setPhase(2);
        showGameOver(name(loser) + "方认输，" + name(winner) + "方获胜！");
    }

    private void showGameOver() {
        if (gameOverShown) return;
        showGameOver("先到 " + game.getWinScore() + " 分，" + name(game.getWinner()) + "方获胜！");
    }

    private void showGameOver(String msg) {
        gameOverShown = true;
        persistIfNeeded(game.getWinner());          // 登录用户才落库
        status("游戏结束：" + msg);
        repaintAll();
        JOptionPane.showMessageDialog(this, msg, "游戏结束", JOptionPane.PLAIN_MESSAGE);
    }

    // 对局结束：若已登录，把本局分数与胜负写回数据库
    private void persistIfNeeded(Owner winner) {
        if (currentUser == null || winner == null) return;   // 游客 / 胜负未定 → 不下分
        Owner me = humanSide;                        // 人机：人类恒为黑；双人简化记黑方视角
        int score = game.getPlayer(me).getScore();
        boolean won = (winner == me);
        userService.recordResult(currentUser.getUserId(), score, won);
    }

    // ==================================================== 合法目标高亮 + 提示文案

    private static String key(int r, int c) { return r + "," + c; }

    // 重建「使用棋子」下拉：吞噬 → 新子(Lv1) + 各手牌；落手牌 → 各手牌（带等级）
    private void rebuildPieceChoice() {
        if (rebuildingChoice) return;      // 递归保护
        rebuildingChoice = true;
        try {
            String prev = (String) pieceChoice.getSelectedItem();
            pieceChoice.removeAllItems();
            if (game != null && mode == ActionMode.DEVOUR) {
                pieceChoice.addItem("新子(Lv1)");
            }
            if (game != null && (mode == ActionMode.DEVOUR || mode == ActionMode.DROP_HAND)) {
                var hand = game.getCurrentPlayer().getHand();
                for (int i = 0; i < hand.size(); i++) {
                    pieceChoice.addItem("手牌#" + i + "(Lv" + hand.getLevelAt(i) + ")");
                }
            }
            boolean relevant = (mode == ActionMode.DEVOUR || mode == ActionMode.DROP_HAND);
            pieceChoice.setEnabled(relevant && pieceChoice.getItemCount() > 0);
            if (prev != null) {   // 尽量保持上一次的选择
                for (int i = 0; i < pieceChoice.getItemCount(); i++) {
                    if (prev.equals(pieceChoice.getItemAt(i))) { pieceChoice.setSelectedIndex(i); break; }
                }
            }
        } finally {
            rebuildingChoice = false;
        }
    }

    // -1 = 用新子(Lv1)；>=0 = 用手牌第 idx 张
    private int chosenPieceIdx() {
        String s = (String) pieceChoice.getSelectedItem();
        if (s == null || !s.startsWith("手牌#")) return -1;
        try { return Integer.parseInt(s.substring(3, s.indexOf('('))); }
        catch (Exception ex) { return -1; }
    }

    private String pieceName() {
        String s = (String) pieceChoice.getSelectedItem();
        return (s == null) ? "（无可选项）" : s;
    }

    // 有效视野：有子 → 按当前棋子实时算；无子（中途清零）→ 用「清零前记忆的视野」；
    // 从未有过棋子（开局首子）→ 返回空视野，由 isFreePlacement 单独处理为「全场自由落子」。
    private boolean[][] effectiveVision(Owner owner) {
        int n = game.getBoardSize();
        boolean[][] vis = new boolean[n][n];
        if (engine.ishasCell(game, owner)) {
            VisionEngine.computeVision(game, owner, vis);
            lastVision.put(owner, vis);           // 有子时刷新记忆
            return vis;
        }
        boolean[][] remembered = lastVision.get(owner);
        if (remembered != null && remembered.length == n) return remembered;
        return vis;                                // 从未有子 → 空
    }

    // 开局首子：场上无子且没有历史视野 → 全场自由落子（引擎 isNotCell 允许）
    private boolean isFreePlacement(Owner owner) {
        return !engine.ishasCell(game, owner) && lastVision.get(owner) == null;
    }

    private void refreshTargets() {
        rebuildPieceChoice();
        targets.clear();
        if (game == null) { repaintAll(); return; }

        if (game.getPhase() == 1 && (!vsAI || game.getCurrent() == humanSide) && !aiThinking) {
            Owner me = game.getCurrent();
            int n = game.getBoardSize();
            int ap = game.getCurrentPlayer().getAp();

            // 落子：按引擎 actDropNew / actDropHand 的真实条件算——
            // 只要「视野内（或场上无子）+ 空格 + AP 够」即可，
            // 不套用 AI.genMoves 里那条专门给 AI 加的「必须贴着自己棋子 adjacentToMine」限制。
            if (mode == ActionMode.DROP_NEW || mode == ActionMode.DROP_HAND) {
                boolean handOk = !game.getCurrentPlayer().getHand().isEmpty();
                if (ap >= ActionEngine.COST_DROP && (mode == ActionMode.DROP_NEW || handOk)) {
                    boolean[][] vis = effectiveVision(me);
                    boolean free = isFreePlacement(me);   // 开局首子：全场自由落子
                    for (int r = 0; r < n; r++) {
                        for (int c = 0; c < n; c++) {
                            // 自由落子时不按「是否为空」过滤，否则被排除的格子就等于暴露了敌方隐藏棋子的位置
                            if (free) { targets.add(key(r, c)); continue; }
                            if (!game.getCell(r, c).isEmpty()) continue;
                            if (!vis[r][c]) continue;
                            targets.add(key(r, c));
                        }
                    }
                }
                updatePrompt();
                repaintAll();
                return;
            }

            // 其余动作（吞噬/回收/查看/合成）本身不含「贴子」限制，继续用 AI.genMoves 筛
            MoveArrayList moves = AI.genMoves(game, me);
            for (AIMove m : moves) {
                boolean match;
                switch (mode) {
                    case DEVOUR:  match = m.type() == MoveType.DEVUOR; break;
                    case RECYCLE: match = m.type() == MoveType.RECYCLE; break;
                    case VIEW:    match = m.type() == MoveType.VIEW; break;
                    default:      match = m.type() == MoveType.SYNTH; break;   // SYNTH
                }
                if (!match) continue;
                if (mode == ActionMode.SYNTH) {
                    if (selR < 0) { targets.add(key(m.r(), m.c())); targets.add(key(m.r2(), m.c2())); }
                    else if (m.r() == selR && m.c() == selC) targets.add(key(m.r2(), m.c2()));
                    else if (m.r2() == selR && m.c2() == selC) targets.add(key(m.r(), m.c()));
                } else {
                    targets.add(key(m.r(), m.c()));
                }
            }
        }
        updatePrompt();
        repaintAll();
    }

    private void updatePrompt() {
        if (game == null) return;
        if (game.getPhase() != 1) { status("游戏结束：" + name(game.getWinner()) + "方获胜"); return; }
        if (aiThinking) return;
        if (vsAI && game.getCurrent() != humanSide) { status("AI 回合，请稍候…"); return; }

        Owner me = game.getCurrent();
        if (mode == ActionMode.DROP_NEW || mode == ActionMode.DROP_HAND) {
            if (isFreePlacement(me)) {
                status("开局首子：全场任意位置均可落子（不受视野限制）→ 点绿色高亮格");
                return;
            }
            if (!engine.ishasCell(game, me)) {
                status("场上已无己方棋子：只能在【保留视野】内落子（重新有子后自动恢复正常视野）");
                return;
            }
        }

        String what;
        switch (mode) {
            case DROP_NEW:  what = "点击「绿色高亮空格」（视野内任意空格）落新子(Lv1)"; break;
            case DROP_HAND: what = "点击「绿色高亮空格」（视野内任意空格）落【" + pieceName() + "】"; break;
            case DEVOUR:    what = "点击「绿色高亮的敌方棋子」，用【" + pieceName() + "】发起吞噬"; break;
            case RECYCLE:   what = "点击「绿色高亮的己方棋子」回收到手牌"; break;
            case VIEW:      what = "点击「绿色高亮的敌方棋子」侦查其等级"; break;
            default:        what = (selR < 0) ? "点击第一个「绿色高亮己方棋子」"
                                             : "已选 (" + selR + "," + selC + ")，再点相邻同级的己方棋子"; break;
        }
        if (targets.isEmpty()) {
            status("【" + labelOf(mode) + "】当前无合法目标（AP 不足或无可用格）→ 建议「结束回合」");
        } else {
            status("已选【" + labelOf(mode) + "】：" + what + "（共 " + targets.size() + " 个可选）");
        }
    }

    // ==================================================== 交互

    private void onBoardClick(MouseEvent e) {
        if (aiThinking || game == null || game.getPhase() != 1) return;
        if (vsAI && game.getCurrent() != humanSide) return;
        int N = game.getBoardSize();
        int c = (e.getX() - MARGIN) / cellPx;
        int r = (e.getY() - MARGIN) / cellPx;
        if (r < 0 || r >= N || c < 0 || c >= N) return;
        act(r, c);
    }

    private void act(int r, int c) {
        if (mode == ActionMode.SYNTH) {
            if (selR < 0) {
                if (!targets.contains(key(r, c))) { status("该格不能作为合成起点，请点高亮格子。"); return; }
                selR = r; selC = c;
                refreshTargets();
                return;
            }
            int rc = engine.actSynthesize(game, selR, selC, r, c);
            selR = selC = -1;
            feedback(rc);
            return;
        }
        if (!targets.isEmpty() && !targets.contains(key(r, c))) {
            status("该格不是当前动作的合法目标，请点绿色高亮格子。");
            return;
        }

        GameState snap = game.GameStateCopy();
        int rc;
        int idx = chosenPieceIdx();          // -1 = 新子；>=0 = 手牌第 idx 张
        switch (mode) {
            case DROP_NEW:  rc = engine.actDropNew(game, r, c); break;
            case DROP_HAND:
                if (idx < 0) { status("手牌为空，无法落手牌。"); return; }
                rc = engine.actDropHand(game, r, c, idx); break;
            case DEVOUR:    rc = engine.actDevour(game, r, c, idx); break;
            case RECYCLE:   rc = engine.actRecycle(game, r, c); break;
            case VIEW:      rc = engine.actView(game, r, c); break;
            default: return;
        }
        if (rc == ActionEngine.OK) history.push(snap);
        feedback(rc);
    }

    private void feedback(int rc) {
        hintMove = null;
        if (rc == ActionEngine.OK) status("操作成功。");
        else if (isFreePlacement(game.getCurrent())) {
            // 自由落子状态下统一提示：不能用「不在视野内 / 该格已有棋子」把隐藏棋子的位置泄露出去
            status("该位置无法落子。");
        } else {
            String msg = errMsg(rc);
            status("失败：" + msg);
            log(name(game.getCurrent()) + " 操作失败：" + msg);
        }
        if (game.getPhase() != 1) { showGameOver(); return; }
        refreshTargets();
    }

    private static String errMsg(int rc) {
        if (rc == ActionEngine.ERR_AP_NOT_ENOUGH) return "AP 不足";
        if (rc == ActionEngine.ERR_OUT_VISION)   return "不在视野内";
        if (rc == ActionEngine.ERR_NOT_EMPTY)    return "该格已有棋子";
        if (rc == ActionEngine.ERR_HAND_INDEX)   return "手牌为空/索引错误";
        if (rc == ActionEngine.ERR_NOT_OWNER)    return "不是己方棋子";
        if (rc == ActionEngine.ERR_LOOK_SHE)     return "不是对方棋子";
        if (rc == ActionEngine.ERR_LOOK_REV)     return "已揭示过";
        if (rc == ActionEngine.ERR_NOT_ADJA)     return "两格不相邻";
        if (rc == ActionEngine.ERR_SYN_FAIL)     return "等级不同无法合成";
        if (rc == ActionEngine.ERR_CELL)         return "坐标错误";
        return "其他错误(" + rc + ")";
    }

    // ==================================================== 渲染

    private void repaintAll() {
        boardPanel.repaint();
        updateInfo();
    }

    private void updateInfo() {
        if (game == null) return;
        PlayerState black = game.getPlayer(Owner.BLACK);
        PlayerState white = game.getPlayer(Owner.WHITE);
        PlayerState cur = game.getCurrentPlayer();
        infoLabel.setText("<html><b>当前：</b>" + name(game.getCurrent())
                + "（" + (game.getPhase() == 2 ? "结束" : "对局中") + "）<br>"
                + "<b>黑分 / 白分：</b>" + black.getScore() + " / " + white.getScore() + "<br>"
                + "<b>当前 AP：</b>" + cur.getAp() + "<br>"
                + "<b>黑手牌：</b>" + black.getHand() + "<br>"
                + "<b>白手牌：</b>" + white.getHand() + "<br>"
                + "<b>胜利分：</b>" + winScore
                + (game.getPhase() == 2 ? "<br><b>胜者：</b>" + name(game.getWinner()) : "")
                + "</html>");
    }

    private static String name(Owner o) {
        return o == Owner.BLACK ? "黑" : (o == Owner.WHITE ? "白" : "-");
    }

    private void status(String s) { statusLabel.setText(s); }
    private void log(String s)    { logArea.append(s + "\n"); }

    private class BoardPanel extends JPanel {
        BoardPanel() {
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { onBoardClick(e); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (game == null) return;
            Graphics2D g2 = (Graphics2D) g;
            int N = game.getBoardSize();
            if (cellPx <= 0) cellPx = 30;

            Owner side = game.getCurrent();
            vision = effectiveVision(side);   // 有子→当前视野；无子(清零)→保留清零前的视野

            int bx = MARGIN, by = MARGIN, boardPx = cellPx * N;

            g2.setColor(new Color(224, 186, 140));
            g2.fillRect(bx, by, boardPx, boardPx);
            g2.setColor(new Color(205, 166, 120, 90));
            for (int y = by; y < by + boardPx; y += 17) g2.fillRect(bx, y, boardPx, 1);
            for (int x = bx; x < bx + boardPx; x += 23) g2.fillRect(x, by, 1, boardPx);

            g2.setColor(new Color(110, 70, 32));
            for (int i = 0; i <= N; i++) {
                g2.drawLine(bx + i * cellPx, by, bx + i * cellPx, by + boardPx);
                g2.drawLine(bx, by + i * cellPx, bx + boardPx, by + i * cellPx);
            }
            g2.setColor(new Color(80, 48, 20));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(bx, by, boardPx, boardPx);

            g2.setFont(uiFont(Font.PLAIN, 11));
            g2.setColor(new Color(60, 40, 18));
            for (int i = 0; i < N; i++) {
                String col = String.valueOf((char) ('A' + i));
                String row = String.valueOf(i + 1);
                g2.drawString(col, bx + i * cellPx + (cellPx - g2.getFontMetrics().stringWidth(col)) / 2, by - 6);
                g2.drawString(row, bx - g2.getFontMetrics().stringWidth(row) - 4, by + i * cellPx + cellPx / 2 + 4);
            }

            g2.setFont(uiFont(Font.BOLD, Math.max(11, cellPx / 3)));
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    Cell cell = game.getCell(r, c);
                    Owner owner = cell.getOwner();
                    int x = bx + c * cellPx, y = by + r * cellPx;
                    boolean inVis = vision[r][c];

                    if (owner == side) {
                        drawPiece(g2, x, y, true);
                        drawLevel(g2, String.valueOf(cell.getLevel()), x, y, Color.WHITE);
                    } else if (owner != Owner.EMPTY && (inVis || cell.getRevealed())) {
                        drawPiece(g2, x, y, false);
                        drawLevel(g2, cell.getRevealed() ? String.valueOf(cell.getLevel()) : "?",
                                  x, y, new Color(60, 0, 0));
                    }

                    if (!inVis) {
                        g2.setColor(new Color(45, 32, 18, 150));
                        g2.fillRect(x, y, cellPx, cellPx);
                    }

                    if (targets.contains(key(r, c))) {            // 合法目标：绿色高亮
                        g2.setColor(new Color(60, 200, 90, 70));
                        g2.fillRect(x + 1, y + 1, cellPx - 2, cellPx - 2);
                        g2.setColor(new Color(40, 190, 80));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(x + 1, y + 1, cellPx - 2, cellPx - 2);
                    }
                    if (r == selR && c == selC) {                 // 合成第一格
                        g2.setColor(Color.YELLOW);
                        g2.setStroke(new BasicStroke(3));
                        g2.drawRect(x + 1, y + 1, cellPx - 2, cellPx - 2);
                    }
                    if (hintMove != null && isHintCell(hintMove, r, c)) {   // AI 提示
                        g2.setColor(Color.CYAN);
                        g2.setStroke(new BasicStroke(3));
                        g2.drawOval(x + 3, y + 3, cellPx - 6, cellPx - 6);
                    }
                }
            }
        }

        private boolean isHintCell(AIMove m, int r, int c) {
            return (m.r() == r && m.c() == c) || (m.r2() == r && m.c2() == c);
        }

        private void drawPiece(Graphics2D g2, int x, int y, boolean black) {
            int pad = Math.max(2, cellPx / 7);
            int d = cellPx - 2 * pad;
            int cx = x + cellPx / 2, cy = y + cellPx / 2, rad = d / 2;
            Color base = black ? new Color(18, 18, 18) : new Color(245, 245, 245);
            Color hi = black ? new Color(96, 96, 96) : new Color(255, 255, 255);
            g2.setPaint(new RadialGradientPaint(cx - rad / 3, cy - rad / 3, rad,
                    new float[]{0f, 1f}, new Color[]{hi, base}));
            g2.fillOval(x + pad, y + pad, d, d);
            g2.setColor(black ? Color.BLACK : new Color(150, 150, 150));
            g2.setStroke(new BasicStroke(1));
            g2.drawOval(x + pad, y + pad, d, d);
        }

        private void drawLevel(Graphics2D g2, String s, int x, int y, Color color) {
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(color);
            g2.drawString(s, x + (cellPx - fm.stringWidth(s)) / 2, y + cellPx / 2 + fm.getAscent() / 3);
        }
    }

    private static Font uiFont(int style, int size) { return new Font("Microsoft YaHei", style, size); }
    private static Font titleFont(int size) { return new Font("Microsoft YaHei", Font.BOLD, size); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FtttGui::new);
    }
}
