package chess;

import java.util.*;


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
            legalMoves.add(validateMove(board, myPosition, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();

        //moving down and right
        while (row - 1 > 0 && col + 1 > 0){
            row--;
            col++;
            legalMoves.add(validateMove(board, myPosition, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }
        row = myPosition.getRow();
        col = myPosition.getColumn();

        //moving down and left

        while (row - 1 > 0 && col - 1 > 0) {
            row--;
            col--;
            legalMoves.add(validateMove(board, myPosition, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();

        //moving up and left
        while (row + 1 < 9 && col - 1 > 0){
            row ++;
            col --;
            legalMoves.add(validateMove(board, myPosition, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        legalMoves.removeIf(Objects::isNull);

        return legalMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        throw new RuntimeException("Not Implemented");
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        List<ChessMove> legalMoves = new ArrayList<ChessMove>();

        //up
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row + 1, col)));

        //up-right
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row + 1, col + 1)));

        //right
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row, col +1)));

        //down-right
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row -1, col +1)));

        //down
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row -1, col)));

        //down-left
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row-1, col-1)));

        //left
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row, col-1)));

        //up-left
        legalMoves.add(validateMove(board, myPosition, new ChessPosition(row+1, col-1)));


        legalMoves.removeIf(Objects::isNull);

        return legalMoves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        throw new RuntimeException("Not Implemented");
    }


    private ChessMove validateMove(ChessBoard board, ChessPosition myPosition, ChessPosition newPosition){
        if (newPosition.getRow() > 8 || newPosition.getColumn() > 8 || newPosition.getRow() < 1 || newPosition.getColumn() < 1){
            return null;
        }

        if(board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()){
            return new ChessMove(myPosition, newPosition, null);
        } else {
            return null;
        }
    }

    private void removeNull(ArrayList<ChessMove> legalMoves){
        legalMoves.removeIf(Objects::isNull);
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
