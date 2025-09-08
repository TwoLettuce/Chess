package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public class ChessBoard {

    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {

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

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        for(int i = 0; i < 8; i++){
            for (int n = 0; n < 8; n++){
                if (board[i][n] == null)
                    boardString.append("-");
                else
                    boardString.append(board[i][n].toString());
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

    }
}
