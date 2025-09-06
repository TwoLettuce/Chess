package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public class ChessBoard {
    private ChessPosition[][] board = new ChessPosition[8][8];
    public ChessBoard() {
        for (int i = 0; i < 8; i++){
            for (int n = 0; n < 8; n++){
                board[i][n] = new ChessPosition(i, n);
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
        if (position.piece == null) {
            position.piece = piece;
        } else {
            throw new RuntimeException("Tried to put a piece where a piece already exists!");
        }
    }

    @Override
    public String toString() {
        String boardString = "";
        for(int i = 0; i < 8; i++){
            for (int n = 0; n < 8; n++){
                if (board[i][n].piece == null)
                    boardString += "-";
                else
                    boardString += board[i][n].piece.toString();
            }
            boardString += "\n";
        }
        return boardString;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return position.piece;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //remove all existing pieces from board
        for (int i = 0; i < 8; i++){
            for (int n = 0; n < 8; n++){
                if (board[i][n].piece != null){
                    board[i][n].piece = null;
                }
            }
        }

        for (int i = 0; i < 8; i++){
            //White pawns
            addPiece(board[6][i], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            //Black pawns
            addPiece(board[1][i], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

        //White pieces
        addPiece(board[7][0], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(board[7][1], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(board[7][2], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(board[7][3], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(board[7][4], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(board[7][5], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(board[7][6], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(board[7][7], new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        //Black pieces
        addPiece(board[0][0], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(board[0][1], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(board[0][2], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(board[0][3], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(board[0][4], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        addPiece(board[0][5], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(board[0][6], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(board[0][7], new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
    }
}
