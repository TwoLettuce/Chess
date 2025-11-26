package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    private final String color;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, String color) {
        super(commandType, authToken, gameID);
        this.color = color;
    }

    public String getColor(){
        return color;
    }
}
