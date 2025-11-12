package server;

import dataaccess.MySQLDataAccess;
import handler.ExceptionHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
//import dataaccess.MemoryDataAccess;
import datamodel.AuthData;
import datamodel.JoinRequest;
import datamodel.LoginData;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;
import service.DataService;
import java.util.Map;

public class Server {



    private final Javalin server;
    private final UserService userService;
    private final DataService dataService;
    private final GameService gameService;
    private DataAccess dataAccess;

    public Server() {
        try {
            dataAccess = new MySQLDataAccess();
        } catch (Exception ex) {
            System.out.println("ouch");
        }
        userService = new UserService(dataAccess);
        dataService = new DataService(dataAccess);
        gameService = new GameService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", this::clear);

        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.get("game", this::listGames);
        server.post("game", this::createGame);
        server.put("game", this::joinGame);
    }


    private void clear(Context ctx) {
        var serializer = new Gson();
        try {
            dataService.clear();
            ctx.result(serializer.toJson(new JsonObject()));
        } catch (DataAccessException e){
            ctx.status(ExceptionHandler.getErrorCode(e)).json(serializer.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), UserData.class);
        AuthData response;
        try {
            response = userService.register(request);
            ctx.json(serializer.toJson(response));
        } catch (DataAccessException e){
            ctx.status(ExceptionHandler.getErrorCode(e)).json(serializer.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void login(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), LoginData.class);
        AuthData response;
        try {
            response = userService.login(request);
            ctx.json(serializer.toJson(response));
        } catch (DataAccessException e) {
            ctx.status(ExceptionHandler.getErrorCode(e)).json(serializer.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void logout(Context ctx) {
        var serializer = new Gson();
        var authToken = ctx.header("authorization");
        try {
            userService.logout(authToken);
            ctx.json(serializer.toJson(new JsonObject()));
        } catch (DataAccessException e) {
            ctx.status(ExceptionHandler.getErrorCode(e)).json(serializer.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void listGames(Context ctx) {
        var serializer = new Gson();
        var authToken = ctx.header("authorization");
        try {
            var response = gameService.listGames(authToken);
            ctx.json(serializer.toJson(Map.of("games", response)));
        } catch (DataAccessException e) {
            ctx.status(ExceptionHandler.getErrorCode(e)).json(serializer.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void createGame(Context ctx) {
        var serializer = new Gson();
        var authToken = ctx.header("authorization");
        Object gameNameAsObject = serializer.fromJson(ctx.body(), Map.class).get("gameName");

        try {
            var gameID = gameService.createGame(authToken, gameNameAsObject);
            ctx.json(serializer.toJson(Map.of("gameID", gameID)));
        } catch (DataAccessException e){
            ctx.status(ExceptionHandler.getErrorCode(e)).json(serializer.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void joinGame(Context ctx) {
        var serializer = new Gson();
        var authToken = ctx.header("authorization");
        JoinRequest joinRequest = serializer.fromJson(ctx.body(), JoinRequest.class);
        try {
            gameService.joinGame(authToken, joinRequest);
            ctx.json(serializer.toJson(new JsonObject()));
        } catch (DataAccessException e) {
            ctx.status(ExceptionHandler.getErrorCode(e)).json(serializer.toJson(Map.of("message", e.getMessage())));
        }
    }


    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
