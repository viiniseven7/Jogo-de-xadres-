package view;

import controller.AIPlayer;
import controller.Game;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import controller.AIDifficulty;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;

public class ChessGUI extends JFrame {

    private final Game game;
    private final AIPlayer aiPlayer;
    private final GameMode gameMode;
    private AIDifficulty aiDifficulty = null;
    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];
    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();
    private boolean gameOverDialogShown = false;
    private static final Border BORDER_SELECTED = BorderFactory.createLineBorder(Color.BLUE, 3);
    private static final Border BORDER_LEGAL = BorderFactory.createLineBorder(new Color(0, 128, 0), 3);

    public ChessGUI(GameMode mode, AIDifficulty difficulty) {
        super("ChessGame");
        this.game = new Game();
        this.gameMode = mode;
        this.aiDifficulty = difficulty;

        if (this.gameMode == GameMode.JOGADOR_vs_IA) {
            this.aiPlayer = new AIPlayer(this.aiDifficulty);
            setTitle("ChessGame - Jogador vs IA (" + this.aiDifficulty + ")");
        } else {
            this.aiPlayer = null;
            setTitle("ChessGame - Jogador vs Jogador");
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(Color.DARK_GRAY);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f));
                b.addActionListener(_ -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        status = new JLabel("Vez: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        history = new JTextArea(10, 20);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyScroll = new JScrollPane(history);
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.add(new JLabel("Histórico de lances:"), BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);
        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh();
            }
        });

        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        refresh();
    }

    private void handleClick(Position clicked) {
        if (game.isGameOver()) return;

        Piece p = game.board().get(clicked);
        if (selected == null) {
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            if (game.legalMovesFrom(selected).contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving != null && moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                game.move(selected, clicked, promo);
                selected = null;
                legalForSelected.clear();
                refresh();

                if (!game.isGameOver() && gameMode == GameMode.JOGADOR_vs_IA && !game.whiteToMove()) {
                    makeAIMove();
                }
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            } else {
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    private void makeAIMove() {
        boardPanel.setEnabled(false);
        status.setText("Vez: Pretas (IA a pensar...)");

        SwingWorker<Position[], Void> worker = new SwingWorker<>() {
            @Override
            protected Position[] doInBackground() throws Exception {
                return aiPlayer.findBestMove(game);
            }

            @Override
            protected void done() {
                try {
                    Position[] bestMove = get();
                    if (bestMove != null && bestMove[0] != null) {
                        game.move(bestMove[0], bestMove[1], null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    boardPanel.setEnabled(true);
                    refresh();
                }
            }
        };
        worker.execute();
    }
    
    // O resto dos seus métodos (askPromotion, refresh, etc.) continua aqui...
    private Character askPromotion() {
        String[] opts = {"Rainha", "Torre", "Bispo", "Cavalo"};
        int ch = JOptionPane.showOptionDialog(
                this, "Escolha a peça para promoção:", "Promoção",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    private void refresh() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? new Color(240, 217, 181) : new Color(181, 136, 99);
                squares[r][c].setBackground(base);
                squares[r][c].setBorder(null);
            }
        }

        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];
                if (p == null) {
                    b.setIcon(null); b.setText(""); continue;
                }
                char sym = p.getSymbol().charAt(0);
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon); b.setText("");
                } else {
                    b.setIcon(null); b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }

        if (game.isGameOver()) {
            status.setText("FIM DE JOGO! Vitória das " + game.getWinner() + ".");
        } else {
            String side = game.whiteToMove() ? "Brancas" : "Pretas";
            status.setText("Vez: " + side);
        }
        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0) sb.append((i / 2) + 1).append(". ");
            sb.append(hist.get(i)).append(" ");
            if (i % 2 == 1) sb.append("\n");
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());

        if (game.isGameOver() && !gameOverDialogShown) {
            gameOverDialogShown = true;
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        String winner = game.getWinner();
        String message = "FIM DE JOGO!\nO jogador de peças " + winner + " venceu.";
        String[] options = {"Jogar Novamente", "Sair"};

        int choice = JOptionPane.showOptionDialog(
            this, message, "Vitória!",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void resetGame() {
        game.reset();
        selected = null;
        legalForSelected.clear();
        gameOverDialogShown = false;
        refresh();
    }

    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1) return 64;
        return Math.max(24, side - 6);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Jogador vs Jogador", "Jogador vs IA"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Escolha o modo de jogo:",
                    "Menu Principal",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            if (choice == JOptionPane.CLOSED_OPTION) {
                System.exit(0);
            }

            GameMode selectedMode = (choice == 0) ? GameMode.JOGADOR_vs_JOGADOR : GameMode.JOGADOR_vs_IA;
            AIDifficulty selectedDifficulty = null;

            // CORREÇÃO: Usar '==' para comparação
            if (selectedMode == GameMode.JOGADOR_vs_IA) {
                // CORREÇÃO: 'String' com 'S' maiúsculo
                String[] difficultyOptions = {"Fácil", "Médio", "Difícil"};
                int difficultyChoice = JOptionPane.showOptionDialog(null, "Escolha a dificuldade da IA:",
                        "Dificuldade", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, difficultyOptions, difficultyOptions[1]);

                if (difficultyChoice == JOptionPane.CLOSED_OPTION) {
                    System.exit(0);
                }
                
                // CORREÇÃO: 'default' escrito corretamente
                selectedDifficulty = switch (difficultyChoice) {
                    case 0 -> AIDifficulty.EASY;
                    case 2 -> AIDifficulty.HARD;
                    default -> AIDifficulty.MEDIUM;
                };
            }
            
            // CORREÇÃO: Passar ambos os argumentos para o construtor
            ChessGUI gui = new ChessGUI(selectedMode, selectedDifficulty);
            gui.setVisible(true);
        });
    }
}