package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import datamodel.GameData;
import io.javalin.websocket.*;
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

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final Gson serializer = new Gson();
    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }


    public void setupPong(Session session) {
        session.setIdleTimeout(Duration.ofDays(0));
        Timer pong = new Timer(true);
        pong.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (session.isOpen()) {
                    try {
                        session.getRemote().sendPing(ByteBuffer.wrap(new byte[]{1}));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 25000, 25000);
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        System.out.println("Websocket connection established successfully");

        ctx.enableAutomaticPings();
        setupPong(ctx.session);
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
                    connect(gameID, color, player, connectCommand.getAuthToken(), ctx.session);
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
                    gameID = resignCommand.getGameID();
                    resign(player, gameID, ctx.session);
                    break;
                case LEAVE:
                    UserGameCommand leaveCommand = serializer.fromJson(ctx.message(), UserGameCommand.class);
                    player = dataAccess.getAuthData(leaveCommand.getAuthToken()).username();
                    gameID = leaveCommand.getGameID();
                    leave(player, gameID, leaveCommand.getAuthToken(), ctx.session);
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

    private void connect(int gameID, String color, String player, String authToken, Session session) throws IOException, DataAccessException {
        try {
            if (connections.contains(session)){
                connections.remove(session);
            }
            GameMessage gameMessage = new GameMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    dataAccess.getGame(gameID).getGame());
            connections.add(session, gameID);
            connections.sendMessage(gameMessage, session);
        } catch (NullPointerException ex){
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Invalid game ID.");
            connections.sendMessage(message, session);
        }

        try {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " has joined the game as " + color + ".");
            connections.broadcastMessage(message, List.of(session), gameID);
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
                validateIsPlayer(gameData, player);
                if (gameData.getGame().isGameOver()){
                    throw new DataAccessException("Game Over!");
                }
                gameData.getGame().makeMove(move);
                dataAccess.updateGame(gameID, gameData.getGame());
            }
            catch (InvalidMoveException e) {
                ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                        "Got Error Message: " + e.getMessage());
                connections.sendMessage(message, session);
                return;
            }
            GameMessage gameMessage = new GameMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    gameData.getGame());
            connections.broadcastMessage(gameMessage, List.of(), gameID);
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " made the move " + move + ".");
            connections.broadcastMessage(message, List.of(new Session[]{session}), gameID);
            broadcastBoardState(gameData, gameID);
        } catch (IOException | DataAccessException ex){
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Server Error.");
            connections.sendMessage(message, session);
        }
    }

    private void broadcastBoardState(GameData gameData, int gameID) throws IOException {
        ServerMessage message;
        if (gameData.getGame().isInCheckmate(ChessGame.TeamColor.WHITE)){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getWhiteUsername() + " (white) is in checkmate! Black wins!");
            connections.broadcastMessage(message, List.of(), gameID);

        } else if (gameData.getGame().isInCheckmate(ChessGame.TeamColor.BLACK)){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getBlackUsername() + " (black) is in checkmate! White wins");
            connections.broadcastMessage(message, List.of(), gameID);
        } else if (gameData.getGame().isInCheck(ChessGame.TeamColor.WHITE)){
             message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                     gameData.getWhiteUsername() + " (white) is in check!");
             connections.broadcastMessage(message, List.of(), gameID);

        } else if (gameData.getGame().isInCheck(ChessGame.TeamColor.BLACK)){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getBlackUsername() + " (black) is in check!");
            connections.broadcastMessage(message, List.of(), gameID);

        } else if (gameData.getGame().isInStalemate(gameData.getGame().getTeamTurn())){
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    gameData.getGame().getTeamTurn() + " can't make any moves. Stalemate!");
            connections.broadcastMessage(message, List.of(), gameID);
        }

    }

    private void validateIsPlayer(GameData gameData, String player) throws InvalidMoveException {
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

    private void resign(String player, int gameID, Session session) throws IOException {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            validateIsNotObserver(gameData, player);
            if (gameData.getGame().isGameOver()){
                throw new IOException("Game is already over!");
            }
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " has resigned. Game over!");
            connections.broadcastMessage(message, List.of(new Session[]{}), gameID);
            gameData.getGame().setGameOver(true);
            dataAccess.updateGame(gameID, gameData.getGame());
        } catch (DataAccessException e) {
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Invalid game ID.");
            connections.sendMessage(message, session);
        } catch (IOException e) {
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    e.getMessage());
            connections.sendMessage(message, session);
        }
    }

    private void validateIsNotObserver(GameData gameData, String player) throws IOException {
        if (!Objects.equals(player, gameData.getBlackUsername()) && !Objects.equals(player, gameData.getWhiteUsername())){
            throw new IOException("Observers can't resign!");
        }
    }

    private void leave(String player, int gameID, String authToken, Session session) throws IOException {
        connections.remove(session);
        try {
            GameData gameData = dataAccess.getGame(gameID);
            String color = determinePlayerColor(gameData, dataAccess.getAuthData(authToken).username());
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    player + " (" + color + ") has left the game.");
            connections.broadcastMessage(message, List.of(new Session[]{session}), gameID);
            if (color.equals("OBSERVER")){
                return;
            }
            dataAccess.removeUserFromGame(color, gameID);
        } catch (IOException ex){
            ex.printStackTrace();
        } catch (DataAccessException e) {
            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Invalid game!");
            connections.sendMessage(message, session);
        }
    }

    private String determinePlayerColor(GameData gameData, String username) {
        if (Objects.equals(gameData.getWhiteUsername(), username)){
            return "WHITE";
        } else if (Objects.equals(gameData.getBlackUsername(), username)){
            return "BLACK";
        } else {
            return "OBSERVER";
        }
    }


}
