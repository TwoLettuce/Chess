package chess;

import java.util.Arrays;
import java.util.Objects;
import static java.lang.Math.abs;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public class ChessBoard {

    private final ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {

    }

    //copy constructor
    public ChessBoard(ChessBoard board){
        for (int row = 1; row <= 8; row ++){
            for (int col = 1; col <= 8; col++){
                addPiece(new ChessPosition(row, col), board.getPiece(new ChessPosition(row, col)));
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    public void removePiece(ChessPosition position) {
        board[position.getRow()-1][position.getColumn()-1] = null;
    }

    public void movePiece(ChessMove move){
        if (getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.KING){
            moveKing(move);
            return;
        }
        if (getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN){
            movePawn(move);
        }

        if (getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN && move.getPromotionPiece() != null) {
            addPiece(move.getEndPosition(), new ChessPiece(getPiece(move.getStartPosition()).getTeamColor(), move.getPromotionPiece()));
        } else {
            addPiece(move.getEndPosition(), getPiece(move.getStartPosition()));
        }

        removePiece(move.getStartPosition());
    }

    private void movePawn(ChessMove move) {
        ChessPosition attackedPawn;
        if (getPiece(move.getStartPosition()).getTeamColor() == ChessGame.TeamColor.WHITE) {
            attackedPawn = new ChessPosition(move.getEndPosition().getRow() - 1, move.getEndPosition().getColumn());
        } else {
            attackedPawn = new ChessPosition(move.getEndPosition().getRow() + 1, move.getEndPosition().getColumn());
        }

        if (getPiece(move.getEndPosition()) == null && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()) {
                removePiece(attackedPawn);
        }
    }

    private void moveKing(ChessMove move) {
        addPiece(move.getEndPosition(), getPiece(move.getStartPosition()));
        removePiece(move.getStartPosition());
        if (abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) > 1) {
            if (Objects.equals(move.getEndPosition(), new ChessPosition(1, 3)) && !getPiece(new ChessPosition(1, 1)).hasMoved) {
                movePiece(new ChessMove(new ChessPosition(1, 1), new ChessPosition(1, 4), null));
            } else if (Objects.equals(move.getEndPosition(), new ChessPosition(1, 7)) && !getPiece(new ChessPosition(1, 8)).hasMoved) {
                movePiece(new ChessMove(new ChessPosition(1, 8), new ChessPosition(1, 6), null));
            } else if (Objects.equals(move.getEndPosition(), new ChessPosition(8, 3)) && !getPiece(new ChessPosition(8, 1)).hasMoved) {
                movePiece(new ChessMove(new ChessPosition(8, 1), new ChessPosition(8, 4), null));
            } else if (Objects.equals(move.getEndPosition(), new ChessPosition(8, 7)) && !getPiece(new ChessPosition(8, 8)).hasMoved) {
                movePiece(new ChessMove(new ChessPosition(8, 8), new ChessPosition(8, 6), null));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        for(int i = 7; i >=0; i--){
            for (int n = 0; n < 8; n++){
                if (board[i][n] == null) {
                    boardString.append("-");
                }
                else {
                    boardString.append(board[i][n].toString());
                }
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //remove all existing pieces from board
        for (int i = 0; i < 8; i++){
            for (int n = 0; n < 8; n++){
                if (board[i][n] != null){
                    board[i][n] = null;
                }
            }
        }

        //pawns
        for (int i = 1; i <= 8; i++){
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

        //white pieces
        addPiece(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        //black pieces
        addPiece(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
