package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public record ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {

    /**
     * @return ChessPosition of starting location
     */
    @Override
    public ChessPosition startPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    @Override
    public ChessPosition endPosition() {

        return endPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove move = (ChessMove) o;
        boolean startPositionsAreEqual = Objects.equals(startPosition, move.startPosition);
        return startPositionsAreEqual && Objects.equals(endPosition, move.endPosition) && promotionPiece == move.promotionPiece;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    @Override
    public ChessPiece.PieceType promotionPiece() {
        return promotionPiece;
    }

    @Override
    public String toString() {
        return "{" +
                startPosition +
                endPosition +
                ", " + promotionPiece +
                '}';
    }
}
