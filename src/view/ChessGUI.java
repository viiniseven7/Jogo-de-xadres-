// ========================= src/view/ChessGUI.java (COM DESTAQUE DE XEQUE) =========================
package view;

import controller.AIPlayer;
import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.King;
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

public class ChessGUI extends JFrame {

    private static final Color COR_CASA_CLARA = new Color(240, 217, 181);
    private static final Color COR_CASA_ESCURA = new Color(181, 136, 99);
    private static final Color COR_DESTAQUE_SELECIONADA = new Color(30, 144, 255, 200);
    private static final Color COR_DESTAQUE_LEGAL = new Color(0, 128, 0, 150);
    private static final Color COR_DESTAQUE_ULTIMA = new Color(255, 255, 0, 100);
    // NOVO: Cor para o destaque de xeque
    private static final Color COR_DESTAQUE_XEQUE = new Color(255, 0, 0, 120); // Vermelho translúcido
    private static final Font FONTE_HISTORICO = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private static final Border BORDA_SELECIONADA = BorderFactory.createLineBorder(COR_DESTAQUE_SELECIONADA, 4);
    private static final Border BORDA_LEGAL = BorderFactory.createLineBorder(COR_DESTAQUE_LEGAL, 3);
    private static final Border BORDA_ULTIMA_JOGADA = BorderFactory.createLineBorder(COR_DESTAQUE_ULTIMA, 3);
    // NOVO: Borda para o destaque de xeque
    private static final Border BORDA_XEQUE = BorderFactory.createLineBorder(COR_DESTAQUE_XEQUE, 4);

    private final Game game;
    private final AIPlayer aiPlayer;
    private final GameMode gameMode;
    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];
    private final JLabel statusLabel;
    private final JTextArea historyTextArea;

    private Position selectedPosition = null;
    private List<Position> legalMovesForSelected = new ArrayList<>();
    private boolean gameOverDialogShown = false;

    public ChessGUI(GameMode mode, AIDifficulty difficulty) {
        // ... (o construtor continua exatamente igual ao da versão anterior)
        super("ChessGame");
        this.game = new Game();
        this.gameMode = mode;

        if (this.gameMode == GameMode.JOGADOR_vs_IA) {
            this.aiPlayer = new AIPlayer(difficulty);
            setTitle("ChessGame - Jogador vs IA (" + difficulty + ")");
        } else {
            this.aiPlayer = null;
            setTitle("ChessGame - Jogador vs Jogador");
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        boardPanel = new JPanel(new GridLayout(8, 8));
        initializeBoardButtons();
        
        this.historyTextArea = new JTextArea(10, 20);
        
        JPanel rightPanel = createRightPanel();

        statusLabel = new JLabel("Vez das Brancas");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
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
    
    // ... (métodos initializeBoardButtons, createRightPanel, handleSquareClick, etc. continuam iguais)
    
    // ALTERADO: Apenas a chamada para o novo método de destaque do rei
    private void updateBoardAppearance() {
        Move lastMove = game.getLastMove();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean isLight = (r + c) % 2 == 0;
                squares[r][c].setBackground(isLight ? COR_CASA_CLARA : COR_CASA_ESCURA);
                squares[r][c].setBorder(null);

                if (lastMove != null && (new Position(r, c).equals(lastMove.getFrom()) || new Position(r, c).equals(lastMove.getTo()))) {
                    squares[r][c].setBorder(BORDA_ULTIMA_JOGADA);
                }
            }
        }
        
        if (selectedPosition != null) {
            squares[selectedPosition.getRow()][selectedPosition.getColumn()].setBorder(BORDA_SELECIONADA);
            for (Position pos : legalMovesForSelected) {
                squares[pos.getRow()][pos.getColumn()].setBorder(BORDA_LEGAL);
            }
        }

        // NOVO: Chama o método para destacar o rei em xeque
        highlightKingInCheck();
    }

    // NOVO: Lógica para destacar o rei
    private void highlightKingInCheck() {
        if (game.inCheck(game.whiteToMove())) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Piece p = game.board().get(new Position(r, c));
                    if (p instanceof King && p.isWhite() == game.whiteToMove()) {
                        squares[r][c].setBorder(BORDA_XEQUE);
                        return; // Encontrou o rei, pode sair
                    }
                }
            }
        }
    }
    
    // ALTERADO: Adiciona a palavra "Xeque!" ao status
    // Dentro de src/view/ChessGUI.java

// ALTERADO: Adiciona a lógica para mudar a cor do texto em caso de xeque
private void updateSidePanel() {
    String statusText;
    // NOVO: Define a cor padrão do texto
    statusLabel.setForeground(Color.BLACK); 

    if (game.isGameOver()) {
        statusText = "XEQUE-MATE! Vitória das " + game.getWinner() + ".";
        // NOVO: Deixa o texto de vitória em uma cor destacada
        statusLabel.setForeground(new Color(0, 128, 0)); // Verde escuro
    } else {
        String side = game.whiteToMove() ? "Brancas" : "Pretas";
        statusText = "Vez: " + side;
        if (game.inCheck(game.whiteToMove())) {
            statusText += " (Xeque!)";
            // NOVO: Muda a cor do texto para vermelho
            statusLabel.setForeground(Color.RED); 
        }
    }
    statusLabel.setText(statusText);

    // O código para atualizar o histórico continua o mesmo
    StringBuilder sb = new StringBuilder();
    List<String> hist = game.history();
    for (int i = 0; i < hist.size(); i++) {
        if (i % 2 == 0) {
            sb.append((i / 2) + 1).append(". ");
        }
        sb.append(hist.get(i)).append(" ");
        if (i % 2 == 1) {
            sb.append("\n");
        }
    }
    historyTextArea.setText(sb.toString());
    historyTextArea.setCaretPosition(historyTextArea.getDocument().getLength());
}

    // ... (O restante da classe ChessGUI.java continua exatamente igual)
    private void initializeBoardButtons() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int finalR = r;
                final int finalC = c;
                JButton button = new JButton();
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setFocusPainted(false);
                button.addActionListener(_ -> handleSquareClick(new Position(finalR, finalC)));
                squares[r][c] = button;
                boardPanel.add(button);
            }
        }
    }

    private JPanel createRightPanel() {
        historyTextArea.setEditable(false);
        historyTextArea.setFont(FONTE_HISTORICO);
        JScrollPane historyScroll = new JScrollPane(historyTextArea);
        historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        
        JLabel historyLabel = new JLabel("Histórico de Lances:");
        historyLabel.setFont(historyLabel.getFont().deriveFont(Font.BOLD));
        historyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        rightPanel.add(historyLabel, BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);
        return rightPanel;
    }

    private void handleSquareClick(Position clickedPos) {
        if (game.isGameOver()) return;

        Piece clickedPiece = game.board().get(clickedPos);

        if (selectedPosition == null) {
            if (clickedPiece != null && clickedPiece.isWhite() == game.whiteToMove()) {
                selectedPosition = clickedPos;
                legalMovesForSelected = game.legalMovesFrom(selectedPosition);
            }
        }
        else {
            if (legalMovesForSelected.contains(clickedPos)) {
                Character promo = null;
                Piece movingPiece = game.board().get(selectedPosition);
                if (movingPiece instanceof Pawn && game.isPromotion(selectedPosition, clickedPos)) {
                    promo = askForPromotionPiece();
                }
                game.move(selectedPosition, clickedPos, promo);
                clearSelection();
                
                if (!game.isGameOver() && gameMode == GameMode.JOGADOR_vs_IA && !game.whiteToMove()) {
                    makeAIMove();
                }
            } 
            else if (clickedPiece != null && clickedPiece.isWhite() == game.whiteToMove()) {
                selectedPosition = clickedPos;
                legalMovesForSelected = game.legalMovesFrom(selectedPosition);
            } 
            else {
                clearSelection();
            }
        }
        refresh();
    }
    
    private void clearSelection() {
        selectedPosition = null;
        legalMovesForSelected.clear();
    }

    private void makeAIMove() {
        setBoardEnabled(false);
        statusLabel.setText("Vez das Pretas (IA pensando...)");

        SwingWorker<Position[], Void> worker = new SwingWorker<>() {
            @Override
            protected Position[] doInBackground() {
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
                    setBoardEnabled(true);
                    refresh();
                }
            }
        };
        worker.execute();
    }
    
    private void setBoardEnabled(boolean enabled) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                squares[r][c].setEnabled(enabled);
            }
        }
    }
    
    private Character askForPromotionPiece() {
        String[] opts = {"Rainha", "Torre", "Bispo", "Cavalo"};
        int choice = JOptionPane.showOptionDialog(
                this, "Escolha a peça para promoção:", "Promoção de Peão",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        return switch (choice) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    private void refresh() {
        updateBoardAppearance();
        updatePieceIcons();
        updateSidePanel();
        
        if (game.isGameOver() && !gameOverDialogShown) {
            gameOverDialogShown = true;
            showGameOverDialog();
        }
    }

    private void updatePieceIcons() {
        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton button = squares[r][c];
                if (p != null) {
                    ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), p.getSymbol().charAt(0), iconSize);
                    button.setIcon(icon);
                } else {
                    button.setIcon(null);
                }
            }
        }
    }

    private void showGameOverDialog() {
        String winner = game.getWinner();
        String message = "XEQUE-MATE!\nO jogador de peças " + winner + " venceu.";
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
        clearSelection();
        gameOverDialogShown = false;
        refresh();
    }

    private int computeSquareIconSize() {
        int w = boardPanel.getWidth() / 8;
        int h = boardPanel.getHeight() / 8;
        int side = Math.min(w, h);
        return Math.max(24, side - 8);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

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

            if (selectedMode == GameMode.JOGADOR_vs_IA) {
                String[] difficultyOptions = {"Fácil", "Médio", "Difícil"};
                int difficultyChoice = JOptionPane.showOptionDialog(null, "Escolha a dificuldade da IA:",
                        "Dificuldade", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, difficultyOptions, difficultyOptions[1]);

                if (difficultyChoice == JOptionPane.CLOSED_OPTION) {
                    System.exit(0);
                }
                
                selectedDifficulty = switch (difficultyChoice) {
                    case 0 -> AIDifficulty.EASY;
                    case 2 -> AIDifficulty.HARD;
                    default -> AIDifficulty.MEDIUM;
                };
            }
            
            ChessGUI gui = new ChessGUI(selectedMode, selectedDifficulty);
            gui.setVisible(true);
        });
    }
}
