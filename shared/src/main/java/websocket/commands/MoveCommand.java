package websocket.commands;

public class MoveCommand extends UserGameCommand {

    private final String move;
    public MoveCommand(CommandType commandType, String authToken, Integer gameID, String move) {
        super(commandType, authToken, gameID);
        this.move = move;
    }

    public String getMove() {
        return move;
    }
}
