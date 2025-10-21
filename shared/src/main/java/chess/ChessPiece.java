package chess;

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
    public boolean hasMoved;

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
                return new ArrayList<>();
            }
        }
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessGame.TeamColor teamColor = board.getPiece(myPosition).getTeamColor();

        int startingRow;
        int movementDirection;
        int promotionRow;

        if(teamColor == ChessGame.TeamColor.WHITE){
            startingRow = 2;
            movementDirection = 1;
            promotionRow = 8;
        } else {
            startingRow = 7;
            movementDirection = -1;
            promotionRow = 1;
        }

        //forward
        if (board.getPiece(new ChessPosition(row + movementDirection, col)) == null) {
            legalMoves.add(new ChessMove(myPosition, new ChessPosition(row + movementDirection, col), null));
            //forward 2 spaces
            if (row == startingRow && board.getPiece(new ChessPosition(row + 2*movementDirection, col)) == null){
                legalMoves.add(new ChessMove(myPosition, new ChessPosition(row + 2*movementDirection, col), null));
            }
        }


        //pawn captures
        for (int colOffset : new int[] {1,-1}) {
            if (checkRowAndCol(row + movementDirection, col + colOffset)) {
                ChessPiece pieceAtDestination = board.getPiece(new ChessPosition(row + movementDirection, col + colOffset));
                if (pieceAtDestination != null && pieceAtDestination.getTeamColor() != teamColor) {
                    legalMoves.add(new ChessMove(myPosition, new ChessPosition(row + movementDirection, col + colOffset), null));
                }
            }
        }

        //Add promotion options, if applicable
        ArrayList<ChessMove> currentMovesList = new ArrayList<>(legalMoves);
        for (ChessMove move : currentMovesList){
            if (move.getEndPosition().getRow() == promotionRow){
                for (PieceType promotionPiece : PieceType.values()) {
                    if (promotionPiece != PieceType.KING && promotionPiece != PieceType.PAWN) {
                        legalMoves.add(new ChessMove(myPosition, move.getEndPosition(), promotionPiece));
                    }
                }
                legalMoves.remove(move);
            }
        }

        return legalMoves;
    }

    private Collection<ChessMove> moveCardinally(ChessBoard board, ChessPosition myPos){
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

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPos){
            return moveCardinally(board, myPos);
    }


    private Collection<ChessMove> moveDiagonally(ChessBoard board, ChessPosition myPos){
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
        int row = myPos.getRow();
        int col = myPos.getColumn();

        //moving up and right
        while (row + 1 < 9 && col + 1 < 9){
            row++;
            col++;
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        row = myPos.getRow();
        col = myPos.getColumn();

        //moving down and right
        while (row - 1 > 0 && col + 1 < 9){
            row--;
            col++;
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }
        row = myPos.getRow();
        col = myPos.getColumn();

        //moving down and left

        while (row - 1 > 0 && col - 1 > 0) {
            row--;
            col--;
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        row = myPos.getRow();
        col = myPos.getColumn();

        //moving up and left
        while (row + 1 < 9 && col - 1 > 0){
            row ++;
            col --;
            legalMoves.add(valMove(board, myPos, new ChessPosition(row, col)));
            if (board.getPiece(new ChessPosition(row, col)) != null){
                break;
            }
        }

        legalMoves.removeIf(Objects::isNull);

        return legalMoves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPos){
        return moveDiagonally(board, myPos);
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int rowOffset : new int[]{-2, -1, 1, 2}) {
            for (int colOffset : new int[]{-2, -1, 1, 2}) {
                if (Math.abs(rowOffset) != Math.abs(colOffset)){
                    legalMoves.add(valMove(board, myPosition, new ChessPosition(row+rowOffset, col+colOffset)));
                }
            }
        }
        removeNull(legalMoves);
        return legalMoves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        List<ChessMove> legalMoves = new ArrayList<ChessMove>();

        for (int rowOffset : new int[]{-1, 0, 1}){
            for (int colOffset : new int[]{-1,0,1}){
                legalMoves.add(valMove(board, myPosition, new ChessPosition(row + rowOffset, col + colOffset)));
            }
        }
        legalMoves.removeIf(Objects::isNull);

        return legalMoves;
    }



    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPos){
        ArrayList<ChessMove> legalMoves = new ArrayList<>();

        legalMoves.addAll(moveCardinally(board, myPos));
        legalMoves.addAll(moveDiagonally(board, myPos));

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

    private boolean checkRowAndCol(int row, int col){
        return row <= 8 && row >= 1 && col <= 8 && col >= 1;
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
