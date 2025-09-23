package chess;

import java.lang.reflect.Array;
import java.util.*;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

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
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        //white pawn logic
        if(board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            //forward
            if (board.getPiece(new ChessPosition(row + 1, col)) == null) {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), null));
                //forward 2 spaces
                if (row == 2 && board.getPiece(new ChessPosition(row+2, col)) == null){
                    legalMoves.add(new ChessMove(myPosition, new ChessPosition(row + 2, col), null));
                }
            }


            //forward and left (capture only)
            if (col-1 >= 1 && board.getPiece(new ChessPosition(row+1, col-1)) != null && board.getPiece(new ChessPosition(row+1, col-1)).getTeamColor() == ChessGame.TeamColor.BLACK){
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row+1, col-1), null));
            }

            //forward and right (capture only)
            if (col+1 <=8 && board.getPiece(new ChessPosition(row+1, col+1)) != null && board.getPiece(new ChessPosition(row+1, col+1)).getTeamColor() == ChessGame.TeamColor.BLACK){
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row+1, col+1), null));
            }

            //Add promotion options, if applicable
            ArrayList<ChessMove> currentMovesList = (ArrayList<ChessMove>) legalMoves.clone();
            for (ChessMove move : currentMovesList){
                if (move.getEndPosition().getRow() == 8){
                    for (PieceType i : PieceType.values()){
                        if (i != PieceType.KING && i != PieceType.PAWN)
                            legalMoves.add(new ChessMove(myPosition, move.getEndPosition(), i));
                    }
                    legalMoves.remove(move);
                }
            }
        }
        //black pawn logic
        else if(board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
            //forward
            if (board.getPiece(new ChessPosition(row - 1, col)) == null) {
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), null));

                //forward 2 spaces
                if (row == 7 && board.getPiece(new ChessPosition(row-2, col)) == null){
                    legalMoves.add(new ChessMove(myPosition, new ChessPosition(row - 2, col), null));
                }
            }
            //forward and left (capture only)
            if (col-1 >= 1 && board.getPiece(new ChessPosition(row-1, col-1))!= null && board.getPiece(new ChessPosition(row-1, col-1)).getTeamColor() == ChessGame.TeamColor.WHITE){
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row-1, col-1), null));
            }

            //forward and right (capture only)
            if (col+1 <= 8 && board.getPiece(new ChessPosition(row-1, col+1)) != null && board.getPiece(new ChessPosition(row-1, col+1)).getTeamColor() == ChessGame.TeamColor.WHITE){
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row-1, col+1), null));
            }

            //add promotion options, if applicable
            ArrayList<ChessMove> currentMovesList = (ArrayList<ChessMove>) legalMoves.clone();
            for (ChessMove move : currentMovesList){
                if (move.getEndPosition().getRow() == 1){
                    for (PieceType i : PieceType.values()){
                        if (i != PieceType.KING && i != PieceType.PAWN)
                            legalMoves.add(new ChessMove(myPosition, move.getEndPosition(), i));
                    }
                    legalMoves.remove(move);
                }
            }

        }

        return legalMoves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPos){
        ArrayList<ChessMove> legalMoves = new ArrayList<>();

        int col = myPos.getColumn();
        int row = myPos.getRow()+1;

        //up
        while (row <= 8){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row++;
        }

        //right
        row = myPos.getRow(); col = myPos.getColumn()+1;
        while (col <= 8){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            col++;
        }

        //down
        row = myPos.getRow()-1; col = myPos.getColumn();
        while (row >=1){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row--;
        }

        //left
        row = myPos.getRow(); col = myPos.getColumn()-1;
        while (col >= 1){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            col--;
        }

        removeNull(legalMoves);
        return legalMoves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        //moving up and right
        while (row + 1 < 9 && col + 1 < 9){
            row++;
            col++;
            legalMoves.add(valMove(board, myPosition, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();

        //moving down and right
        while (row - 1 > 0 && col + 1 < 9){
            row--;
            col++;
            legalMoves.add(valMove(board, myPosition, new ChessPosition(row, col)));
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
            legalMoves.add(valMove(board, myPosition, new ChessPosition(row, col)));
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
            legalMoves.add(valMove(board, myPosition, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        legalMoves.removeIf(Objects::isNull);

        return legalMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        //up-up-right
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row+2, col+1)));

        //up-right-right
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row+1, col+2)));

        //down-right-right
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row-1, col+2)));

        //down-down-right
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row-2, col+1)));

        //down-down-left
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row-2, col-1)));

        //down-left-left
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row-1, col-2)));

        //up-left-left
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row+1, col-2)));

        //up-up-left
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row+2, col -1)));

        removeNull(legalMoves);

        return legalMoves;


    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        List<ChessMove> legalMoves = new ArrayList<ChessMove>();

        //up
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row + 1, col)));

        //up-right
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row + 1, col + 1)));

        //right
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row, col +1)));

        //down-right
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row -1, col +1)));

        //down
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row -1, col)));

        //down-left
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row-1, col-1)));

        //left
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row, col-1)));

        //up-left
        legalMoves.add(valMove(board, myPosition, new ChessPosition(row+1, col-1)));


        legalMoves.removeIf(Objects::isNull);

        return legalMoves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPos){
        ArrayList<ChessMove> legalMoves = new ArrayList<>();

        int col = myPos.getColumn();
        int row = myPos.getRow()+1;

        //up
        while (row <= 8){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row++;
        }

        //up-right
        row = myPos.getRow()+1; col = myPos.getColumn()+1;
        while (row <= 8 && col <= 8){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row++; col++;
        }

        //right
        row = myPos.getRow(); col = myPos.getColumn()+1;
        while (col <= 8){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            col++;
        }

        //down-right
        row = myPos.getRow()-1; col = myPos.getColumn()+1;
        while (row >=1 && col <= 8){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row--; col++;
        }

        //down
        row = myPos.getRow()-1; col = myPos.getColumn();
        while (row >=1){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row--;
        }

        //down-left
        row = myPos.getRow()-1; col = myPos.getColumn()-1;
        while (row >=1 && col >= 1){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row--; col--;
        }

        //left
        row = myPos.getRow(); col = myPos.getColumn()-1;
        while (col >= 1){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            col--;
        }

        //up-left
        row = myPos.getRow()+1; col = myPos.getColumn()-1;
        while (row <=8 && col >= 1){
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null) { break; }
            row++; col--;
        }

        removeNull(legalMoves);
        return legalMoves;
    }


    private ChessMove valMove(ChessBoard board, ChessPosition myPosition, ChessPosition newPosition){
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
