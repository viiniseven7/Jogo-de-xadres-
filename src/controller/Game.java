// ========================= src/controller/Game.java (CORRIGIDO) =========================
package controller;

import model.board.Board;
import model.board.Move;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Game {

    private Board board;
    private boolean whiteToMove = true;
    private boolean gameOver = false;
    private Move lastMove = null;
    private final List<String> history = new ArrayList<>();

    public Game() {
        this.board = new Board();
        setupPieces();
    }

    // ==== API PÚBLICA ====

    public Board board() { return board; }
    public boolean whiteToMove() { return whiteToMove; }
    public List<String> history() { return Collections.unmodifiableList(history); }
    public boolean isGameOver() { return gameOver; }
    public Move getLastMove() { return lastMove; }

    public String getWinner() {
        if (!gameOver) return null;
        return whiteToMove ? "Pretas" : "Brancas";
    }

    public void reset() {
        this.board = new Board();
        setupPieces();
        whiteToMove = true;
        gameOver = false;
        lastMove = null;
        history.clear();
    }

    public List<Position> legalMovesFrom(Position from) {
        if (gameOver) return List.of();

        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return List.of();
        
        List<Position> pseudoLegalMoves = p.getPossibleMoves();
        
        return pseudoLegalMoves.stream()
                .filter(to -> !moveResultsInCheck(from, to))
                .collect(Collectors.toList());
    }

    public boolean isPromotion(Position from, Position to) {
        Piece p = board.get(from);
        return p instanceof Pawn && (p.isWhite() ? to.getRow() == 0 : to.getRow() == 7);
    }
    
    public void move(Position from, Position to, Character promotion) {
        if (gameOver) return;

        List<Position> legalMoves = legalMovesFrom(from);
        if (!legalMoves.contains(to)) {
            System.err.println("Movimento ilegal tentado: " + from + " para " + to);
            return;
        }

        Piece movingPiece = board.get(from);
        Piece capturedPiece = board.get(to);
        boolean isPawnPromotion = isPromotion(from, to);

        lastMove = new Move(from, to, movingPiece, capturedPiece, false, false, false, promotion);
        
        board.set(to, movingPiece);
        board.set(from, null);
        if (movingPiece != null) movingPiece.setMoved(true);

        if (isPawnPromotion) {
            Piece newPiece = switch (Character.toUpperCase(promotion)) {
                case 'R' -> new Rook(board, movingPiece.isWhite());
                case 'B' -> new Bishop(board, movingPiece.isWhite());
                case 'N' -> new Knight(board, movingPiece.isWhite());
                default -> new Queen(board, movingPiece.isWhite());
            };
            board.set(to, newPiece);
        }

        whiteToMove = !whiteToMove;
        history.add(from.toString() + to.toString());

        if (!hasAnyLegalMove(whiteToMove)) {
            gameOver = true;
        }
    }
    
    public boolean inCheck(boolean isWhiteSide) {
        Position kingPos = findKing(isWhiteSide);
        if (kingPos == null) return false;
        return isUnderAttack(kingPos, !isWhiteSide);
    }

    private boolean isUnderAttack(Position target, boolean attackingSideIsWhite) {
        for (Piece p : board.pieces(attackingSideIsWhite)) {
            if (p.getAttacks().contains(target)) {
                return true;
            }
        }
        return false;
    }

    private Position findKing(boolean isWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.get(pos);
                if (p instanceof King && p.isWhite() == isWhite) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean moveResultsInCheck(Position from, Position to) {
        Board tempBoard = board.copy();
        Piece p = tempBoard.get(from);
        if (p == null) return false;

        tempBoard.set(to, p);
        tempBoard.set(from, null);
        
        Game tempGame = new Game();
        tempGame.board = tempBoard;

        return tempGame.inCheck(p.isWhite());
    }
    
    private boolean hasAnyLegalMove(boolean isWhiteSide) {
        for (Piece p : board.pieces(isWhiteSide)) {
            List<Position> possibleMoves = p.getPossibleMoves();
            for (Position to : possibleMoves) {
                if (!moveResultsInCheck(p.getPosition(), to)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setupPieces() {
        board.placePiece(new Rook(board, true),   new Position(7, 0));
        board.placePiece(new Knight(board, true), new Position(7, 1));
        board.placePiece(new Bishop(board, true), new Position(7, 2));
        board.placePiece(new Queen(board, true),  new Position(7, 3));
        board.placePiece(new King(board, true),   new Position(7, 4));
        board.placePiece(new Bishop(board, true), new Position(7, 5));
        board.placePiece(new Knight(board, true), new Position(7, 6));
        board.placePiece(new Rook(board, true),   new Position(7, 7));
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, true), new Position(6, c));
        }

        board.placePiece(new Rook(board, false),   new Position(0, 0));
        board.placePiece(new Knight(board, false), new Position(0, 1));
        board.placePiece(new Bishop(board, false), new Position(0, 2));
        board.placePiece(new Queen(board, false),  new Position(0, 3));
        board.placePiece(new King(board, false),   new Position(0, 4));
        board.placePiece(new Bishop(board, false), new Position(0, 5));
        board.placePiece(new Knight(board, false), new Position(0, 6));
        board.placePiece(new Rook(board, false),   new Position(0, 7));
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, false), new Position(1, c));
        }
    }
}
