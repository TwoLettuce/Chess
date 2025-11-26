package websocket.messages;

public class ErrorMessage extends ServerMessage{
    private final String errorMessage;

    public ErrorMessage(ServerMessageType type, String message) {
        super(type, null);
        errorMessage = message;
    }
}
