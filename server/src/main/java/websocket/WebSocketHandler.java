package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.ConnectCommand;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;
import dataaccess.DataAccess;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;

import java.util.List;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final Gson serializer = new Gson();
    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        System.out.println("Websocket connection established successfully");

        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        UserGameCommand.CommandType commandType = serializer.fromJson(ctx.message(), UserGameCommand.class).getCommandType();
        String player;
        int gameID;
        switch (commandType){
            case CONNECT:
                ConnectCommand connectCommand = serializer.fromJson(ctx.message(), ConnectCommand.class);
                player = dataAccess.getAuthData(connectCommand.getAuthToken()).username();
                gameID = connectCommand.getGameID();
                String color = connectCommand.getColor();
                connect(gameID, color, player, ctx.session);
                break;
            case MAKE_MOVE:
                MoveCommand moveCommand = serializer.fromJson(ctx.message(), MoveCommand.class);
                player = dataAccess.getAuthData(moveCommand.getAuthToken()).username();
                String move = moveCommand.getMove();
                gameID = moveCommand.getGameID();
                makeMove(player, move, gameID, ctx.session);
                break;
            case RESIGN:
                UserGameCommand resignCommand = serializer.fromJson(ctx.message(), UserGameCommand.class);
                player = dataAccess.getAuthData(resignCommand.getAuthToken()).username();
                resign(player, ctx.session);
                break;
            case LEAVE:
                UserGameCommand leaveCommand = serializer.fromJson(ctx.message(), UserGameCommand.class);
                player = dataAccess.getAuthData(leaveCommand.getAuthToken()).username();
                leave(player, ctx.session);
                break;
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        System.out.println("Websocket connection closed successfully");
    }

    private void connect(int gameID, String color, String player, Session session) throws IOException, DataAccessException {
        ServerMessage gameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                dataAccess.getGame(gameID).getGame().getBoard().toString());
        connections.sendMessage(gameMessage, session);
        connections.add(session);
        try {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " has joined the game as " + color + ".");
            connections.broadcastMessage(message, List.of(session));
        } catch (IOException ex){
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR,
                    "Server Error.");
            connections.sendMessage(message, session);
        }
    }


    private void makeMove(String player, String move, int gameID, Session session) throws IOException {
        try {
            ServerMessage gameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    dataAccess.getGame(gameID).getGame().toString());
            connections.sendMessage(gameMessage, session);

            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " made the move " + move + ".");
            connections.broadcastMessage(message, List.of(new Session[]{session}));
        } catch (IOException | DataAccessException ex){
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR,
                    "Server Error.");
            connections.sendMessage(message, session);
        }
    }

    private void resign(String player, Session session){
        connections.remove(session);
        try {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " has resigned.");
            connections.broadcastMessage(message, List.of(new Session[]{session}));
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void leave(String player, Session session){
        connections.remove(session);
        try {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " has left the game.");
            connections.broadcastMessage(message, List.of(new Session[]{session}));
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }


}
