package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.ConnectCommand;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

@ClientEndpoint
public class WebSocketFacade {

    Session session;
    static ServerMessageHandler serverMessageHandler;
    private Timer ping;


    public WebSocketFacade(String url, ServerMessageHandler serverMessageHandler){
        try {

            url = url.replace("http", "ws");

            URI webSocketURI = new URI(url + "/ws");


            WebSocketFacade.serverMessageHandler = serverMessageHandler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, webSocketURI);


        } catch (URISyntaxException | IOException | DeploymentException ex){
            ex.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        ping = new Timer(true);
        ping.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (session.isOpen()) {
                    try {
                        session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{1}));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 25000, 25000);
    }

    @OnMessage
    public void onMessage(String json){
        serverMessageHandler.notify(json);
    }

    public void connectToGame(String authToken, int gameID, String color) {
        try {
            ConnectCommand command = new ConnectCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) {
        try {
            MoveCommand command = new MoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void resign(String authToken, int gameID){
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void leaveGame(String authToken, int gameID){
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException | IllegalStateException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (ping != null) {
            ping.cancel();
        }
    }
}
