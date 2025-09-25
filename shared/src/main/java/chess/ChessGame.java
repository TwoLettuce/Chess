package chess;

import jdk.dynalink.beans.MissingMemberHandlerFactory;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

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
        ChessBoard currentBoard = new ChessBoard(board);
        ArrayList<ChessMove> goodMoves = new ArrayList<>();
        for (ChessMove move : board.getPiece(startPosition).pieceMoves(board, startPosition)){
            try {
                makeMove(move);
                if (!isInCheck(currentTurn))
                    goodMoves.add(move);
                board = new ChessBoard(currentBoard);
            } catch(InvalidMoveException _) {
                board = new ChessBoard(currentBoard);
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
        if(board.getPiece(move.getStartPosition()) == null) throw new InvalidMoveException("There's no piece there!");
        if(board.getPiece(move.getStartPosition()).getTeamColor() != getTeamTurn()) throw new InvalidMoveException("That's not your piece!");
        if(!board.getPiece(move.getStartPosition()).pieceMoves(board, move.getStartPosition()).contains(move)) throw new InvalidMoveException("Invalid move!");
        board.movePiece(move);
        if(isInCheck(board.getPiece(move.getEndPosition()).getTeamColor()))
            throw new InvalidMoveException();

        if (getTeamTurn() == TeamColor.BLACK) {
            setTeamTurn(TeamColor.WHITE);
        } else {
            setTeamTurn(TeamColor.BLACK);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for (int col = 1; col <= 8; col++){
            for (int row = 1; row <= 8; row++){
                var thisPiece = board.getPiece(new ChessPosition(row, col));
                if (thisPiece == null) continue;
                if(thisPiece.getPieceType() == ChessPiece.PieceType.KING && thisPiece.getTeamColor() == teamColor){
                    return checkForAttackers(teamColor, new ChessPosition(row, col));
                }
            }
        }
        return false;
    }

    private boolean checkForAttackers(TeamColor teamColor, ChessPosition myPos){
        return checkKing(teamColor, myPos) || checkDiagonally(teamColor, myPos) || checkCardinals(teamColor, myPos) || checkKnight(teamColor, myPos);
    }

    private boolean checkKing(TeamColor teamColor, ChessPosition myPos) {
        int row = myPos.getRow();
        int col = myPos.getColumn();

        var piecesFound = new ArrayList<ChessPiece>();

         if (row+1 <= 8) {
             piecesFound.add(board.getPiece(new ChessPosition(row+1, col)));
             if (col+1 <= 8)
                 piecesFound.add(board.getPiece(new ChessPosition(row+1, col+1)));
             if (col-1 >= 1)
                 piecesFound.add(board.getPiece(new ChessPosition(row+1, col-1)));
         }
         if (col+1 <= 8)
             piecesFound.add(board.getPiece(new ChessPosition(row, col+1)));
         if (col-1 >= 1)
             piecesFound.add(board.getPiece(new ChessPosition(row, col-1)));

        if (row-1 >= 1) {
            piecesFound.add(board.getPiece(new ChessPosition(row-1, col)));
            if (col+1 <= 8)
                piecesFound.add(board.getPiece(new ChessPosition(row-1, col+1)));
            if (col-1 >= 1)
                piecesFound.add(board.getPiece(new ChessPosition(row-1, col-1)));
        }


        for (ChessPiece piece : piecesFound){
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING)
                return true;
        }

        return false;
    }

    //checks for unobstructed enemy pawns, bishops, and queens that are able to attack the King
    private boolean checkDiagonally(TeamColor teamColor, ChessPosition myPos){
        int row = myPos.getRow();
        int col = myPos.getColumn();

        ArrayList<ChessPiece> piecesFound = new ArrayList<>();

        //check up and right
        row ++; col++;
        while (row <= 8 && col <= 8){
            if(board.getPiece(new ChessPosition(row, col)) != null){
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                row++; col++;
            }
        }

        //check up and left
        row = myPos.getRow()+1;
        col = myPos.getColumn()-1;
        while (row <= 8 && col >= 1) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                row++; col--;
            }
        }

        //check down and right
        row = myPos.getRow()-1;
        col = myPos.getColumn()+1;
        while (row >= 1 && col <= 8) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                row--; col++;
            }
        }

        //check down and left
        row = myPos.getRow()-1;
        col = myPos.getColumn()-1;
        while (row >= 1 && col >= 1) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                row--; col--;
            }
        }

        //check to see if any collected pieces put the king in danger
        row = myPos.getRow();
        col = myPos.getColumn();
        for (var piece : piecesFound){
            if (piece.getTeamColor() != teamColor){
                switch (piece.getPieceType()){
                    case QUEEN, BISHOP:
                        return true;
                    case PAWN:
                        if (teamColor == TeamColor.WHITE) {
                            if (board.getPiece(new ChessPosition(row + 1, col + 1)) == piece ||
                                    board.getPiece(new ChessPosition(row + 1, col - 1)) == piece) {
                                return true;
                            }
                        } else {
                            if (board.getPiece(new ChessPosition(row - 1, col + 1)) == piece ||
                                    board.getPiece(new ChessPosition(row - 1, col - 1)) == piece) {
                                return true;
                            }
                        }
                }
            }
        }

        //found no enemy pieces in striking range
        return false;
    }

    //checks for unobstructed enemy rooks or queens that are able to attack the king
    private boolean checkCardinals(TeamColor teamColor, ChessPosition myPos){
        int row = myPos.getRow();
        int col = myPos.getColumn();

        ArrayList<ChessPiece> piecesFound = new ArrayList<>();

        //check up
        row ++;
        while (row <= 8){
            if(board.getPiece(new ChessPosition(row, col)) != null){
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                row++;
            }
        }

        //check right
        row = myPos.getRow();
        col = myPos.getColumn()+1;
        while (col <= 8) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                col++;
            }
        }

        //check down
        row = myPos.getRow()-1;
        col = myPos.getColumn();
        while (row >= 1) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                row--;
            }
        }

        //check left
        row = myPos.getRow();
        col = myPos.getColumn()-1;
        while (col >= 1) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                break;
            } else {
                col--;
            }
        }

        //check to see if any collected pieces put the king in danger
        for (var piece : piecesFound){
            if ((piece.getPieceType() == ChessPiece.PieceType.ROOK || piece.getPieceType() == ChessPiece.PieceType.QUEEN)
                    && piece.getTeamColor() != teamColor){
                return true;
            }
        }

        //found no enemy pieces in striking range
        return false;
    }

    //checks for enemy knights that are able to attack the King
    private boolean checkKnight(TeamColor teamColor, ChessPosition myPos){
        int row = myPos.getRow();
        int col = myPos.getColumn();

        //list all possible locations for an enemy knight to attack the King from
        var possibleKnights = new ArrayList<ChessPiece>();
        for (int offset : Set.of(1, 2)) {
            for (int inset : Set.of(1, 2)){
                if (inset == offset) continue;
                if (row + offset <= 8 && col + inset <= 8)
                    possibleKnights.add(board.getPiece(new ChessPosition(row + offset, col + inset)));
                if (row + offset <= 8 && col - inset >= 1)
                    possibleKnights.add(board.getPiece(new ChessPosition(row + offset, col - inset)));
                if (row - offset >= 1 && col + inset <= 8)
                    possibleKnights.add(board.getPiece(new ChessPosition(row - offset, col + inset)));
                if (row - offset >= 1 && col - inset >= 1)
                    possibleKnights.add(board.getPiece(new ChessPosition(row - offset, col - inset)));
            }
        }

        //make sure none of those locations has an enemy knight
        for (var piece : possibleKnights){
            if (piece == null) continue;
            if (piece.getTeamColor() != teamColor && piece.getPieceType() == ChessPiece.PieceType.KNIGHT){
                return true;
            }
        }

        return false;
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
        if (isInCheck(teamColor)) return false;

        for(int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                var thisPos = new ChessPosition(row, col);
                if(board.getPiece(thisPos) != null && board.getPiece(thisPos).getTeamColor() == teamColor){
                    if (validMoves(thisPos).isEmpty()){
                        continue;
                    } else {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }
}
