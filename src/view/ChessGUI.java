package view;

import controller.Game;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class ChessGUI extends JFrame {

    private final Game game;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;

    // Seleção atual e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // --- ALTERAÇÃO: Flag para mostrar o diálogo de fim de jogo apenas uma vez ---
    private boolean gameOverDialogShown = false;

    // Bordas para destacar seleção e destinos
    private static final Border BORDER_SELECTED = BorderFactory.createLineBorder(Color.BLUE, 3);
    private static final Border BORDER_LEGAL = BorderFactory.createLineBorder(new Color(0, 128, 0), 3);

    public ChessGUI() {
        super("ChessGame");
        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Painel do tabuleiro (8x8)
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
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f)); // fallback com Unicode
                b.addActionListener(e -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        status = new JLabel("Vez: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Histórico
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
        setVisible(true);

        refresh();
    }

    
    private void handleClick(Position clicked) {
        // --- ALTERAÇÃO: Impede qualquer movimento se o jogo já terminou ---
        if (game.isGameOver()) {
            return;
        }

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
            String chk = ""; 
            status.setText("Vez: " + side + chk);
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
            gameOverDialogShown = true; // Marca que o diálogo foi exibido
            showGameOverDialog();
        }
    }
    
    // --- ALTERAÇÃO: Novo método para exibir o diálogo de fim de jogo ---
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

    // --- ALTERAÇÃO: Novo método para reiniciar o jogo ---
    private void resetGame() {
        game.reset();
        selected = null;
        legalForSelected.clear();
        gameOverDialogShown = false; // Reinicia a flag para a próxima partida
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
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}

