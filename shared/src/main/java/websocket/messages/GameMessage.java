package websocket.messages;

import chess.ChessGame;

public class GameMessage extends ServerMessage{

    private final ChessGame game;

    public GameMessage(ServerMessageType type, ChessGame game) {
        super(type, null);
        this.game = game;
    }

    public ChessGame getGame(){
        return game;
    }
}
