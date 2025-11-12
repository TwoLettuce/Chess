import chess.*;
import client.ChessClient;
import serverfacade.ServerFacade;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client ♕");
        ChessClient client = new ChessClient("http://localhost:8080");
        client.run();
    }
}