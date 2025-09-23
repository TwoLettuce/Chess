package chess;

import jdk.dynalink.beans.MissingMemberHandlerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor currentTurn;
    ChessBoard board = new ChessBoard();

    public ChessGame() {
        currentTurn = TeamColor.WHITE;
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;

    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard currentBoard = board;
        ArrayList<ChessMove> goodMoves = new ArrayList<>();
        for (ChessMove move : board.getPiece(startPosition).pieceMoves(board, startPosition)){
            try {
                makeMove(move);
                if (!isInCheck(currentTurn))
                    goodMoves.add(move);
                board = currentBoard;
            } catch(InvalidMoveException _) {
                board = currentBoard;
            }
        }
        return goodMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        board.removePiece(move.getStartPosition());
        if(isInCheck(board.getPiece(move.getEndPosition()).getTeamColor()))
            throw new InvalidMoveException();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    //checks for unobstructed enemy pawns, bishops, and queens that are able to attack the King
    private boolean checkDiagonally(TeamColor teamColor){
        return false;
    }

    //checks for unobstructed enemy rooks that are able to attack the king
    private boolean checkVertHorz(TeamColor teamColor){
        return false;
    }

    //checks for enemy knights that are able to attack the King
    private boolean checkKnight(TeamColor teamColor){

    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        for (int row = 1; row <=8; row++){
            for (int col = 1; col <=8; col++){

                ChessPosition thisPosition = new ChessPosition(row, col);
                if (board.getPiece(thisPosition) == null)
                    continue;
                if (board.getPiece(thisPosition).getTeamColor() == teamColor) {
                    Collection<ChessMove> potentialMoves = validMoves(thisPosition);

                    if (!potentialMoves.isEmpty())
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented");
    }


}
