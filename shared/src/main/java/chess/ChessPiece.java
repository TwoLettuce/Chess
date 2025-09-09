package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }


    @Override
    public String toString() {
        if(pieceColor == ChessGame.TeamColor.WHITE){
            return switch (type) {
                case KING -> "K";
                case QUEEN -> "Q";
                case BISHOP -> "B";
                case KNIGHT -> "N";
                case ROOK -> "R";
                case PAWN -> "P";
            };
        } else {
            return switch (type) {
                case KING -> "k";
                case QUEEN -> "q";
                case BISHOP -> "b";
                case KNIGHT -> "n";
                case ROOK -> "r";
                case PAWN -> "p";
            };
        }
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch (type) {
            case KING ->  {
                return kingMoves(board, myPosition);
            }
            case PAWN -> {
                return pawnMoves(board, myPosition);
            }
            case ROOK -> {
                return rookMoves(board, myPosition);
            }
            case BISHOP -> {
                return bishopMoves(board, myPosition);
            }
            case KNIGHT -> {
                return knightMoves(board, myPosition);
            }
            case QUEEN -> {
                return queenMoves(board, myPosition);
            }
            case null, default -> {
                return new ArrayList<ChessMove>();
            }
        }


    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        throw new RuntimeException("Not Implemented");
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        throw new RuntimeException("Not Implemented");
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> legalMoves = new ArrayList<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        //moving up and right
        while (row + 1 < 9 && col + 1 < 9){
            row++;
            col++;
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                break;
            } else {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            }
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();

        //moving down and right
        while (row - 1 > 0 && col + 1 > 0){
            row--;
            col++;
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                break;
            } else {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            }
        }


        row = myPosition.getRow();
        col = myPosition.getColumn();

        //moving down and left

        while (row - 1 > 0 && col - 1 > 0) {
            row--;
            col--;
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                break;
            } else {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            }
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();

        //moving up and left
        while (row + 1 < 9 && col - 1 > 0){
            row ++;
            col --;
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                break;
            } else {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
            }
        }

        return legalMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        throw new RuntimeException("Not Implemented");
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        throw new RuntimeException("Not Implemented");
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        throw new RuntimeException("Not Implemented");
    }

}
