package controller;

import model.board.Board;
import model.board.Position;
import model.pieces.Piece;
import java.util.List;
import java.util.Random;

public class AIPlayer {

    private final AIDifficulty difficulty;
    private static final int MINIMAX_DEPTH = 3; // Profundidade para o nível Difícil

    public AIPlayer(AIDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Position[] findBestMove(Game game) {
        return switch (difficulty) {
            case EASY -> findRandomMove(game);
            case MEDIUM -> findBestMoveOneStep(game);
            case HARD -> findBestMoveMinimax(game);
        };
    }

    // NÍVEL FÁCIL: Escolhe um movimento aleatório 
    private Position[] findRandomMove(Game game) {
        List<Piece> aiPieces = game.board().pieces(false);
        Random rand = new Random();
        
        while (!aiPieces.isEmpty()) {
            Piece randomPiece = aiPieces.remove(rand.nextInt(aiPieces.size()));
            List<Position> legalMoves = game.legalMovesFrom(randomPiece.getPosition());
            if (!legalMoves.isEmpty()) {
                Position from = randomPiece.getPosition();
                Position to = legalMoves.get(rand.nextInt(legalMoves.size()));
                return new Position[]{from, to};
            }
        }
        return new Position[]{null, null}; // Sem movimentos possíveis
    }

    // NÍVEL MÉDIO: Avaliação de 1 jogada à frente (código que já tínhamos)
    private Position[] findBestMoveOneStep(Game game) {
        Position bestMoveFrom = null;
        Position bestMoveTo = null;
        int bestValue = Integer.MAX_VALUE;
        Board board = game.board();
        List<Piece> aiPieces = board.pieces(false);

        for (Piece piece : aiPieces) {
            Position from = piece.getPosition();
            List<Position> legalMoves = game.legalMovesFrom(from);
            for (Position to : legalMoves) {
                Board simulatedBoard = board.copy();
                Piece movingPiece = simulatedBoard.get(from);
                if (movingPiece != null) {
                    simulatedBoard.set(to, movingPiece);
                    simulatedBoard.set(from, null);
                }
                int boardValue = evaluateBoard(simulatedBoard);
                if (boardValue < bestValue) {
                    bestValue = boardValue;
                    bestMoveFrom = from;
                    bestMoveTo = to;
                }
            }
        }
        return new Position[]{bestMoveFrom, bestMoveTo};
    }

    // NÍVEL DIFÍCIL: Algoritmo Minimax [cite: 379]
    private Position[] findBestMoveMinimax(Game game) {
        Position bestMoveFrom = null;
        Position bestMoveTo = null;
        int bestValue = Integer.MAX_VALUE;
        Board board = game.board();
        List<Piece> aiPieces = board.pieces(false);

        for (Piece piece : aiPieces) {
            Position from = piece.getPosition();
            List<Position> legalMoves = game.legalMovesFrom(from);
            for (Position to : legalMoves) {
                Board simulatedBoard = board.copy();
                Piece movingPiece = simulatedBoard.get(from);
                if (movingPiece != null) {
                    simulatedBoard.set(to, movingPiece);
                    simulatedBoard.set(from, null);
                }
                // Chama o minimax para o turno do oponente (maximizando)
                int boardValue = minimax(simulatedBoard, MINIMAX_DEPTH - 1, true, game);

                if (boardValue < bestValue) {
                    bestValue = boardValue;
                    bestMoveFrom = from;
                    bestMoveTo = to;
                }
            }
        }
        return new Position[]{bestMoveFrom, bestMoveTo};
    }

    private int minimax(Board board, int depth, boolean isMaximizingPlayer, Game game) {
        if (depth == 0) {
            return evaluateBoard(board);
        }

        List<Piece> pieces = board.pieces(isMaximizingPlayer);
        int bestValue;

        if (isMaximizingPlayer) { // Turno do Jogador (Brancas) - quer maximizar a pontuação
            bestValue = Integer.MIN_VALUE;
            for (Piece piece : pieces) {
                for (Position to : game.legalMovesFrom(piece.getPosition())) {
                    Board childBoard = board.copy();
                    Piece p = childBoard.get(piece.getPosition());
                    if (p != null) {
                        childBoard.set(to, p);
                        childBoard.set(piece.getPosition(), null);
                    }
                    bestValue = Math.max(bestValue, minimax(childBoard, depth - 1, false, game));
                }
            }
        } else { // Turno da IA (Pretas) - quer minimizar a pontuação
            bestValue = Integer.MAX_VALUE;
            for (Piece piece : pieces) {
                 for (Position to : game.legalMovesFrom(piece.getPosition())) {
                    Board childBoard = board.copy();
                     Piece p = childBoard.get(piece.getPosition());
                    if (p != null) {
                        childBoard.set(to, p);
                        childBoard.set(piece.getPosition(), null);
                    }
                    bestValue = Math.min(bestValue, minimax(childBoard, depth - 1, true, game));
                }
            }
        }
        return bestValue;
    }

    // --- FUNÇÕES DE AVALIAÇÃO (comuns a Médio e Difícil) ---
    private int evaluateBoard(Board board) {
        int totalScore = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board.get(new Position(row, col));
                if (p != null) {
                    totalScore += getPieceValue(p);
                }
            }
        }
        return totalScore;
    }

    private int getPieceValue(Piece piece) {
        int value = switch (piece.getSymbol().toUpperCase()) {
            case "P" -> 100;
            case "N" -> 320;
            case "B" -> 330;
            case "R" -> 500;
            case "Q" -> 900;
            case "K" -> 20000;
            default -> 0;
        };
        return piece.isWhite() ? value : -value;
    }
}