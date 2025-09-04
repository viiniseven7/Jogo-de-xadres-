package controller;

import model.board.Board;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {

    private Board board;
    private boolean whiteToMove = true;
    private boolean gameOver = false;

    private Position enPassantTarget = null;

    // Histórico simples (ex.: "e2e4", "O-O")
    private final List<String> history = new ArrayList<>();

    public Game() {
        this.board = new Board();
        setupPieces();
    }

    // ==== API usada pela GUI ====

    public Board board() { return board; }

    public boolean whiteToMove() { return whiteToMove; }

    public List<String> history() { return Collections.unmodifiableList(history); }

    public boolean isGameOver() { return gameOver; }

    /**
     * Retorna o nome da cor vencedora ("Brancas" ou "Pretas").
     * Só deve ser chamado depois que isGameOver() retornar true.
     * O vencedor é o jogador que fez o último lance.
     */
    public String getWinner() {
        if (!gameOver) return null;
       
        return whiteToMove ? "Pretas" : "Brancas";
    }

    /**
     * Reinicia o jogo para o estado inicial.
     */
    public void reset() {
        this.board = new Board();
        setupPieces();
        whiteToMove = true;
        gameOver = false;
        enPassantTarget = null;
        history.clear();
    }

/
    public List<Position> legalMovesFrom(Position from) {
        if (gameOver) return List.of();

        Piece p = board.get(from);
        if (p == null) return List.of();
        if (p.isWhite() != whiteToMove) return List.of();
        
        return p.getPossibleMoves();
    }

    /** Verdadeiro se um peão que sai de 'from' e chega em 'to' promove. */
    public boolean isPromotion(Position from, Position to) {
        Piece p = board.get(from);
        if (!(p instanceof Pawn)) return false;
        if (p.isWhite()) return to.getRow() == 0;   // peão branco chegando na 8ª (topo)
        else              return to.getRow() == 7;  // peão preto chegando na 1ª (base)
    }

    /** Executa o lance (detecta roque, en passant e promoção). */
    public void move(Position from, Position to, Character promotion) {
        if (gameOver) return;

        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return;

        boolean isPawn = (p instanceof Pawn);

        // Verifica a promoção ANTES de mover a peça para não perder a referência.
        boolean isPawnPromotion = isPawn && isPromotion(from, to);

        boolean isKing = (p instanceof King);
        int dCol = Math.abs(to.getColumn() - from.getColumn());
        if (isKing && dCol == 2) {
            int row = from.getRow();
            board.set(to, p);
            board.set(from, null);
            if (to.getColumn() == 6) { // O-O
                Piece rook = board.get(new Position(row, 7));
                board.set(new Position(row, 5), rook);
                board.set(new Position(row, 7), null);
                if (rook != null) rook.setMoved(true);
                addHistory("O-O");
            } else { 
                Piece rook = board.get(new Position(row, 0));
                board.set(new Position(row, 3), rook);
                board.set(new Position(row, 0), null);
                if (rook != null) rook.setMoved(true);
                addHistory("O-O-O");
            }
            p.setMoved(true);
            enPassantTarget = null;
            whiteToMove = !whiteToMove;
            return;
        }

        // -------- EN PASSANT --------
        boolean isEnPassant = isPawn && to.equals(enPassantTarget) && from.getColumn() != to.getColumn() && board.get(to) == null;
        if (isEnPassant) {
            board.set(to, p);
            board.set(from, null);
            int dir = p.isWhite() ? 1 : -1;
            board.set(new Position(to.getRow() + dir, to.getColumn()), null);
            p.setMoved(true);
            addHistory(coord(from) + "x" + coord(to) + " e.p.");
            enPassantTarget = null;
            whiteToMove = !whiteToMove;
            return;
        }

        // -------- LANCE NORMAL --------
        Piece capturedBefore = board.get(to);
        board.set(to, p);
        board.set(from, null);
        p.setMoved(true);

        // -------- MARCA/RESSETA EN PASSANT --------
        if (isPawn && Math.abs(to.getRow() - from.getRow()) == 2) {
            enPassantTarget = new Position((from.getRow() + to.getRow()) / 2, from.getColumn());
        } else {
            enPassantTarget = null;
        }

        // -------- PROMOÇÃO --------
        if (promotion != null && isPawnPromotion) {
            Piece newPiece = switch (Character.toUpperCase(promotion)) {
                case 'R' -> new Rook(board, p.isWhite());
                case 'B' -> new Bishop(board, p.isWhite());
                case 'N' -> new Knight(board, p.isWhite());
                default -> new Queen(board, p.isWhite());
            };
            newPiece.setMoved(true);
            board.set(to, newPiece);
        }

        // -------- VERIFICA FIM DE JOGO --------
        if (capturedBefore instanceof King) {
            gameOver = true;
        }

        addHistory(coord(from) + (capturedBefore != null ? "x" : "-") + coord(to));

        // Só passa o turno se o jogo não acabou neste lance
        if (!gameOver) {
            whiteToMove = !whiteToMove;
        }
    }

    /** Indica se o lado passado está em xeque (stub por enquanto). */
    public boolean inCheck(boolean whiteSide) {
        // Implementação completa exige varrer movimentos do oponente até o rei.
        return false;
    }

    private void addHistory(String moveStr) {
        history.add(moveStr);
    }

    private String coord(Position p) {
        char file = (char) ('a' + p.getColumn());
        int rank = 8 - p.getRow();
        return "" + file + rank;
    }

    private void setupPieces() {
        // Brancas
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
        // Pretas
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