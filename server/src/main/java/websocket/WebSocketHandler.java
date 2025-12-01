package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import datamodel.GameData;
import io.javalin.websocket.*;
import jakarta.websocket.OnMessage;
import org.jetbrains.annotations.NotNull;
import websocket.commands.ConnectCommand;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;
import dataaccess.DataAccess;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ErrorMessage;
import websocket.messages.GameMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

import java.util.List;
import java.util.Objects;

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

    @OnMessage
    public void onMessage(String msg){

    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        UserGameCommand.CommandType commandType = serializer.fromJson(ctx.message(), UserGameCommand.class).getCommandType();
        String player;
        int gameID;
        try {
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
                    ChessMove move = moveCommand.getMove();
                    gameID = moveCommand.getGameID();
                    makeMove(player, move, gameID, ctx.session);
                    break;
                case RESIGN:
                    UserGameCommand resignCommand = serializer.fromJson(ctx.message(), UserGameCommand.class);
                    player = dataAccess.getAuthData(resignCommand.getAuthToken()).username();
                    resign(player, gameID, ctx.session);
                    break;
                case LEAVE:
                    UserGameCommand leaveCommand = serializer.fromJson(ctx.message(), UserGameCommand.class);
                    player = dataAccess.getAuthData(leaveCommand.getAuthToken()).username();
                    leave(player, ctx.session);
                    break;
            }
        } catch (NullPointerException ex){
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Invalid Authentication Data.");
            connections.sendMessage(message, ctx.session);
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        System.out.println("Websocket connection closed successfully");
    }

    private void connect(int gameID, String color, String player, Session session) throws IOException, DataAccessException {
        try {
            if (connections.contains(session)){
                connections.remove(session);
            }
            GameMessage gameMessage = new GameMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    dataAccess.getGame(gameID).getGame());
            connections.add(session);
            connections.sendMessage(gameMessage, session);
        } catch (NullPointerException ex){
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Invalid game ID.");
            connections.sendMessage(message, session);
        }

        try {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " has joined the game.");
            connections.broadcastMessage(message, List.of(session));
        } catch (IOException ex){
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Server Error.");
            connections.sendMessage(message, session);
        }
    }


    private void makeMove(String player, ChessMove move, int gameID, Session session) throws IOException {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            try {
                validateThisPlayerCanMove(gameData, player);
                gameData.getGame().makeMove(move);
                dataAccess.updateGame(gameID, gameData.getGame());
            }
            catch (InvalidMoveException e) {
                ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                        "Invalid move.");
                connections.sendMessage(message, session);
                return;
            }
            GameMessage gameMessage = new GameMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    gameData.getGame());
            connections.broadcastMessage(gameMessage, List.of());
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " made the move " + move + ".");
            connections.broadcastMessage(message, List.of(new Session[]{session}));
            broadcastBoardState(gameData);
        } catch (IOException | DataAccessException ex){
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Server Error.");
            connections.sendMessage(message, session);
        }
    }

    private void broadcastBoardState(GameData gameData) throws IOException {
        ServerMessage message = null;
        if (gameData.getGame().isInCheck(ChessGame.TeamColor.WHITE)){
             message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                     gameData.getWhiteUsername() + " (white) is in check!");
             connections.broadcastMessage(message, List.of());

        } else if (gameData.getGame().isInCheck(ChessGame.TeamColor.BLACK)){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getBlackUsername() + " (black) is in check!");
            connections.broadcastMessage(message, List.of());

        } else if (gameData.getGame().isInCheckmate(ChessGame.TeamColor.WHITE)){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getWhiteUsername() + " (white) is in checkmate! Black wins!");
            connections.broadcastMessage(message, List.of());

        } else if (gameData.getGame().isInCheckmate(ChessGame.TeamColor.BLACK)){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getBlackUsername() + " (black) is in checkmate! Black wins");
            connections.broadcastMessage(message, List.of());

        } else if (gameData.getGame().isInStalemate(gameData.getGame().getTeamTurn())){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getGame().getTeamTurn() + " can't make any moves. Stalemate!");
            connections.broadcastMessage(message, List.of());
        }

    }

    private void validateThisPlayerCanMove(GameData gameData, String player) throws InvalidMoveException {
        if (Objects.equals(player, gameData.getWhiteUsername())){
            if (gameData.getGame().getTeamTurn() != ChessGame.TeamColor.WHITE){
                throw new InvalidMoveException("Not your turn!");
            }
        } else if (Objects.equals(player, gameData.getBlackUsername())){
            if (gameData.getGame().getTeamTurn() != ChessGame.TeamColor.BLACK){
                throw new InvalidMoveException("Not your turn!");
            }
        } else {
            throw new InvalidMoveException("Can't move as Observer!");
        }
    }

    private void resign(String player, int gameID, Session session){
        try {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " has resigned.");
            connections.broadcastMessage(message, List.of(new Session[]{}));
            GameData gameData = dataAccess.getGame(gameID);
            gameData.getGame().setGameOver(true);
            dataAccess.updateGame(gameID, gameData.getGame());
        } catch (IOException ex){
            ex.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException("bad gameID");
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
