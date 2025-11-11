package ui;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

/**
 * this class has a draw(ChessBoard board, boolean) method that will draw the given chess board.
 */
public class ChessBoardUI {
    private static final int NUM_ROWS = 8;
    private static final int NUM_COLS = 8;
    private static final char[] COL_HEADER = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    private static final char[] REVERSE_COL_HEADER = {'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'};
    private static final int[] ROW_HEADER = {8, 7, 6, 5, 4, 3, 2, 1};
    private static final int[] REVERSE_ROW_HEADER = {1, 2, 3, 4, 5, 6, 7, 8};

    private ChessBoard board;


    /**
     * the draw method to allow drawing from black's perspective
     * @param board - the chess board to be drawn in the command line
     * @param isBlack - will determine from which perspective the board is to be drawn.
     */
    public void draw(ChessBoard board, boolean isBlack){
        this.board = board;
        printHeader(isBlack);
        printRows(isBlack);
        printHeader(isBlack);
    }

    private void printHeader(boolean isBlack){
        StringBuilder header = new StringBuilder();
        header.append(EscapeSequences.SET_BG_COLOR_RED + EscapeSequences.EMPTY);
        header.append(EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
        char[] headerArray;
        if (isBlack) {
            headerArray = REVERSE_COL_HEADER;
        } else {
            headerArray = COL_HEADER;
        }
        for (char head : headerArray){
            header.append("\u2003").append(head).append(" ");
        }
        header.append(EscapeSequences.EMPTY);
        header.append(EscapeSequences.RESET_BG_COLOR);
        System.out.println(header);
    }


    private void printRows(boolean isBlack){
        int[] rowHeader;
        if (isBlack){
            rowHeader = REVERSE_ROW_HEADER;
        } else {
            rowHeader = ROW_HEADER;
        }
        boolean whiteSquare = false;

        for (int row : rowHeader){
            StringBuilder thisRow = new StringBuilder();
            thisRow.append(EscapeSequences.SET_BG_COLOR_RED);
            thisRow.append("\u2003").append(row).append(" ");
            for (int col = 1; col <= NUM_COLS; col++) {
                if (whiteSquare){
                    thisRow.append(EscapeSequences.SET_BG_COLOR_WHITE);
                } else {
                    thisRow.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                }
                String piece = translateToANSI(board.getPiece(new ChessPosition(row, col)));
                thisRow.append(piece);
                whiteSquare = !whiteSquare;
            }
            whiteSquare = !whiteSquare;
            thisRow.append(EscapeSequences.SET_BG_COLOR_RED);
            thisRow.append(" ").append(row).append("\u2003");
            thisRow.append(EscapeSequences.RESET_BG_COLOR);
            System.out.println(thisRow);
        }
    }

    private String translateToANSI(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return switch (piece.getPieceType()) {
                case PAWN -> EscapeSequences.WHITE_PAWN;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case KING -> EscapeSequences.WHITE_KING;
            };
        } else {
            return switch (piece.getPieceType()) {
                case PAWN -> EscapeSequences.BLACK_PAWN;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case KING -> EscapeSequences.BLACK_KING;
            };
        }
    }
}
