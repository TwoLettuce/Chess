package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor currentTurn = TeamColor.WHITE;
    ChessBoard board = new ChessBoard();
    ChessBoard previousBoard;
    private boolean gameOver;
    
    public ChessGame() {
        board.resetBoard();
        gameOver = false;
    }
    public ChessGame(TeamColor currentTurn, ChessBoard board){
        this.currentTurn = currentTurn;
        this.board = board;
        gameOver = false;
    }
    public ChessGame(TeamColor currentTurn, ChessBoard board, boolean gameOver){
        this.currentTurn = currentTurn;
        this.board = board;
        this.gameOver = gameOver;
    }

    public ChessGame(TeamColor currentTurn, ChessBoard board, ChessBoard previousBoard, boolean gameOver){
        this.currentTurn = currentTurn;
        this.board = board;
        this.gameOver = gameOver;
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

    private void flipTeamTurn(){
        if (currentTurn == TeamColor.BLACK) { currentTurn = TeamColor.WHITE; }
        else { currentTurn = TeamColor.BLACK; }
    }

    public boolean isGameOver() {
        return isInCheckmate(TeamColor.WHITE) || isInCheckmate(TeamColor.BLACK) || isInStalemate(currentTurn) || gameOver;
    }

    public void setGameOver(boolean gameOver){
        this.gameOver = gameOver;
    }

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
                tryMove(move);
                if (!isInCheck(currentTurn)) {
                    goodMoves.add(move);
                }
                board = new ChessBoard(currentBoard);
            } catch (InvalidMoveException ex) {
                board = new ChessBoard(currentBoard);
            }
        }
        goodMoves.addAll(checkCastling(board.getPiece(startPosition).getTeamColor()));
        goodMoves.addAll(enPassant(board.getPiece(startPosition).getTeamColor()));
        return goodMoves;
    }

    private Collection<ChessMove> enPassant(TeamColor teamColor) {
        ArrayList<ChessMove> possibleEnPassants = new ArrayList<>();
        int row;
        int moveRow;
        if(teamColor == TeamColor.WHITE) {
            row = 5;
            moveRow = 6;
        } else {
            row = 4;
            moveRow = 3;
        }

        for (int col = 1; col <= 8; col++){
            ChessPiece thisPiece = board.getPiece(new ChessPosition(row, col));
            if (thisPiece == null || thisPiece.getPieceType() != ChessPiece.PieceType.PAWN || thisPiece.getTeamColor() != teamColor) { continue; }

            if(checkPassantRight(new ChessPosition(row, col), teamColor)){
                possibleEnPassants.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(moveRow, col+1), null));
            }
            if (checkPassantLeft(new ChessPosition(row, col), teamColor)) {
                possibleEnPassants.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(moveRow, col-1), null));

            }
        }

        return possibleEnPassants;
    }

    boolean checkPassantRight(ChessPosition pawnPosition, TeamColor teamColor){
        return pawnMovedTwoSpaces(pawnPosition.getColumn()+1, teamColor);
    }


    boolean checkPassantLeft(ChessPosition pawnPosition, TeamColor teamColor){
        return pawnMovedTwoSpaces(pawnPosition.getColumn()-1, teamColor);
    }

    boolean pawnMovedTwoSpaces(int pawnColumn, TeamColor teamColor){
        if (pawnColumn < 1 || pawnColumn > 8) { return false; }
        ChessPosition thisPawnPosition;
        ChessPosition pawnStartPosition;
        ChessPiece.PieceType pawn = ChessPiece.PieceType.PAWN;

        if (teamColor == TeamColor.WHITE) {
            thisPawnPosition = new ChessPosition(5, pawnColumn);
            pawnStartPosition = new ChessPosition(7, pawnColumn);
        } else {
            thisPawnPosition = new ChessPosition(4, pawnColumn);
            pawnStartPosition = new ChessPosition(2, pawnColumn);
        }

        if (board.getPiece(thisPawnPosition) == null || previousBoard.getPiece(pawnStartPosition) == null) {
            return false;
        }
        ChessPiece pawnAtStart = previousBoard.getPiece(pawnStartPosition);
        if (board.getPiece(thisPawnPosition).getPieceType() == pawn && board.getPiece(thisPawnPosition).getTeamColor() != teamColor){
            if (pawnAtStart.getPieceType() == pawn && previousBoard.getPiece(pawnStartPosition).getTeamColor() != teamColor) {
                return board.getPiece(pawnStartPosition) == null && previousBoard.getPiece(thisPawnPosition) == null;
            }
        }

        return false;
    }


    private void tryMove(ChessMove move) throws InvalidMoveException {
        if(board.getPiece(move.startPosition()) == null) { throw new InvalidMoveException("There's no piece there!"); }
        if (board.getPiece(move.startPosition()).getPieceType() != ChessPiece.PieceType.KING) {
            Collection<ChessMove> moveList = board.getPiece(move.startPosition()).pieceMoves(board, move.startPosition());
            moveList.addAll(enPassant(getTeamTurn()));
            if (!moveList.contains(move)) {
                throw new InvalidMoveException("Invalid move!");
            }
        }

        board.movePiece(move);
        if(isInCheck(board.getPiece(move.endPosition()).getTeamColor())) {

            throw new InvalidMoveException("That move puts you in check!");
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (gameOver){
            throw new InvalidMoveException("Game is over!");
        }
        ChessBoard willBePreviousBoard = new ChessBoard(board);
        tryMove(move);
        if(board.getPiece(move.endPosition()).getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("That's not your piece!");
        }
        flipTeamTurn();
        board.getPiece(move.endPosition()).hasMoved = true;
        previousBoard = willBePreviousBoard;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        try {
            ChessPosition kingPos = findKing(teamColor);
            return checkForAttackers(teamColor, kingPos);
        } catch (Exception ex){
            return false;
        }
    }

    private boolean checkForAttackers(TeamColor teamColor, ChessPosition myPos){
        return checkKing(myPos) || checkDiagonally(teamColor, myPos) || checkCardinals(teamColor, myPos) || checkKnight(teamColor, myPos);
    }

    private boolean checkKing(ChessPosition myPos) {
        int row = myPos.getRow();
        int col = myPos.getColumn();

        var piecesFound = new ArrayList<ChessPiece>();

        for (int rowOffset : new int[] {-1,0,1}){
            for (int colOffset : new int[] {-1,0,1}){
                if (rowOffset == 0 && colOffset == 0){continue;}
                if (ChessPiece.checkRowAndCol(row + rowOffset, col + colOffset)){
                    piecesFound.add(board.getPiece(new ChessPosition(row+rowOffset, col+colOffset)));
                }
            }
        }
        for (ChessPiece piece : piecesFound){
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING) {return true;}
        }
        return false;
    }

    private boolean checkDiagonally(TeamColor teamColor, ChessPosition myPos){
        int row;
        int col;
        ArrayList<ChessPiece> piecesFound = new ArrayList<>();

        for (int rowOffset : new int[] {-1,1}){
            for (int colOffset : new int[] {-1,1}){
                row = myPos.getRow() + rowOffset;
                col = myPos.getColumn() + colOffset;
                while (ChessPiece.checkRowAndCol(row, col)){
                    if(board.getPiece(new ChessPosition(row, col)) != null){
                        piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                        break;
                    } else {
                        row += rowOffset;
                        col += colOffset;
                    }
                }
            }
        }

        //check to see if any collected pieces put the king in danger
        row = myPos.getRow();
        col = myPos.getColumn();
        int pawnRowOffset;

        if(TeamColor.WHITE == teamColor){
            pawnRowOffset = 1;
        } else {
            pawnRowOffset = -1;
        }

        for (var piece : piecesFound){
            if (piece.getTeamColor() != teamColor){
                switch (piece.getPieceType()){
                    case QUEEN, BISHOP:
                        return true;
                    case PAWN:
                        if (board.getPiece(new ChessPosition(row + pawnRowOffset, col + 1)) == piece ||
                                board.getPiece(new ChessPosition(row + pawnRowOffset, col - 1)) == piece) {
                            return true;
                        }
                }
            }
        }

        //found no enemy pieces in striking range
        return false;
    }

    private boolean checkCardinals(TeamColor teamColor, ChessPosition myPos){
        int row = myPos.getRow();
        int col = myPos.getColumn();
        ArrayList<ChessPiece> piecesFound = new ArrayList<>();
        for (int offset : new int[] {-1,1}){
            row += offset;
            while (ChessPiece.checkRowAndCol(row, col)){
                if (board.getPiece(new ChessPosition(row, col)) != null){
                    piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                    break;
                } else {
                    row += offset;
                }
            }
            row = myPos.getRow();
            col = myPos.getColumn() + offset;
            while (ChessPiece.checkRowAndCol(row, col)){
                if(board.getPiece(new ChessPosition(row, col)) != null){
                    piecesFound.add(board.getPiece(new ChessPosition(row, col)));
                    break;
                } else {
                    col += offset;
                }
            }
            col = myPos.getColumn();
        }
        //check to see if any collected pieces put the king in danger
        for (var piece : piecesFound){
            if ((piece.getPieceType() == ChessPiece.PieceType.ROOK || piece.getPieceType() == ChessPiece.PieceType.QUEEN)
                    && piece.getTeamColor() != teamColor){
                return true;
            }
        }
        return false;
    }

    private boolean checkKnight(TeamColor teamColor, ChessPosition myPos){
        int row = myPos.getRow();
        int col = myPos.getColumn();
        //list all possible locations for an enemy knight to attack the King from
        var possibleKnights = new ArrayList<ChessPiece>();
        for (int rowOffset : new int[] {-2, -1, 1, 2}) {
            for (int colOffset : new int[] {-2, -1, 1, 2}){
                if (Math.abs(colOffset) == Math.abs(rowOffset)) {continue;}
                if (ChessPiece.checkRowAndCol(row+rowOffset, col+colOffset)) {
                    possibleKnights.add(board.getPiece(new ChessPosition(row + rowOffset, col + colOffset)));
                }
            }
        }
        //make sure none of those locations has an enemy knight
        for (var piece : possibleKnights){
            if (piece == null) {continue;}
            if (piece.getTeamColor() != teamColor && piece.getPieceType() == ChessPiece.PieceType.KNIGHT){
                return true;
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        for (int row = 1; row <=8; row++){
            for (int col = 1; col <=8; col++){
                ChessPosition thisPosition = new ChessPosition(row, col);
                if (!(board.getPiece(thisPosition) == null) && board.getPiece(thisPosition).getTeamColor() == teamColor) {
                    Collection<ChessMove> potentialMoves = validMoves(thisPosition);
                    if (!potentialMoves.isEmpty()) {return false;}
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
        if (isInCheck(teamColor)) {return false;}
        for(int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                var thisPos = new ChessPosition(row, col);
                if(board.getPiece(thisPos) != null && board.getPiece(thisPos).getTeamColor() == teamColor && !validMoves(thisPos).isEmpty()) {
                    return false;
                }
            }
        }
        gameOver = true;
        return true;
    }

    private Collection<ChessMove> checkCastling (TeamColor teamColor){
        ChessBoard currentBoard = new ChessBoard(board);
        ArrayList<ChessMove> validCastlingMoves = new ArrayList<>();
        ChessPosition kingPos;
        try {
            kingPos = findKing(teamColor);
        } catch (Exception ex) {
            return validCastlingMoves;
        }
        if (board.getPiece(kingPos).hasMoved || kingPos.getColumn() != 5) {
            return validCastlingMoves;
        }
        if ((teamColor == TeamColor.BLACK && kingPos.getRow() != 8) || (teamColor == TeamColor.WHITE && kingPos.getRow() != 1)) {
            return validCastlingMoves;
        }
        ChessPosition queensRookPos = new ChessPosition(kingPos.getRow(), 1);
        ChessPosition kingsRookPos = new ChessPosition(kingPos.getRow(), 8);

        if (board.getPiece(queensRookPos) != null && !board.getPiece(queensRookPos).hasMoved) {
            boolean queenSideClear = true;
            for (int i = 1; i < 4; i++){
                if (board.getPiece(new ChessPosition(kingPos.getRow(), kingPos.getColumn()-i)) != null){
                    queenSideClear = false;
                    break;
                }
            }
            if (queenSideClear && !isInCheck(getTeamTurn())) {
                boolean queenCastleIsLegal = false;
                for (int i = 1; i < 3; i++) {
                    var move = new ChessMove(kingPos, new ChessPosition(kingPos.getRow(), kingPos.getColumn()-i), null);
                    board.movePiece(move);
                    if(isInCheck(board.getPiece(move.endPosition()).getTeamColor())) {
                        board = new ChessBoard(currentBoard);
                        queenCastleIsLegal = false;
                        break;
                    }
                    board = new ChessBoard(currentBoard);
                    queenCastleIsLegal = true;
                }
                if (queenCastleIsLegal) {
                    validCastlingMoves.add(new ChessMove(kingPos, new ChessPosition(kingPos.getRow(), kingPos.getColumn()-2), null));
                }
            }
        }

        if (board.getPiece(kingsRookPos) != null && !board.getPiece(kingsRookPos).hasMoved) {
            boolean kingSideClear = true;
            for (int i = 1; i < 3; i++){
                if (board.getPiece(new ChessPosition(kingPos.getRow(), kingPos.getColumn()+i)) != null){
                    kingSideClear = false;
                }
            }

            if (kingSideClear && !isInCheck(getTeamTurn())) {
                boolean kingCastleIsLegal = false;
                for (int i = 1; i < 3; i++) {
                    var move = new ChessMove(kingPos, new ChessPosition(kingPos.getRow(), kingPos.getColumn()+i), null);
                    board.movePiece(move);
                    if(isInCheck(board.getPiece(move.endPosition()).getTeamColor())) {
                        board = new ChessBoard(currentBoard);
                        kingCastleIsLegal = false;
                        break;
                    }
                    board = new ChessBoard(currentBoard);
                    kingCastleIsLegal = true;
                }
                if (kingCastleIsLegal) {
                    validCastlingMoves.add(new ChessMove(kingPos, new ChessPosition(kingPos.getRow(), kingPos.getColumn() + 2), null));
                }
            }
        }

        return validCastlingMoves;
    }

    private ChessPosition findKing(TeamColor teamColor) throws Exception {
        ChessPosition kingPos = null;
        for (int col = 1; col <= 8; col++) {
            for (int row = 1; row <= 8; row++) {
                var thisPiece = board.getPiece(new ChessPosition(row, col));
                if (thisPiece == null) {continue;}
                if (thisPiece.getPieceType() == ChessPiece.PieceType.KING && thisPiece.getTeamColor() == teamColor) {
                    kingPos = new ChessPosition(row, col);
                }
            }
        }
        if (kingPos == null) {throw new Exception("there's no king on the board?");}
        return kingPos;
    }
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        previousBoard = this.board;
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
