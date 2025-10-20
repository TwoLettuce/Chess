package server;

import Handler.ExceptionHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.AuthData;
import datamodel.JoinRequest;
import datamodel.LoginData;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;
import service.DataService;

import java.util.ArrayList;
import java.util.Map;

public class Server {

    private final Javalin server;
    private UserService userService;
    private DataService dataService;
    private GameService gameService;
    private DataAccess dataAccess;

    public Server() {
        dataAccess = new MemoryDataAccess();
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
        dataService.clear();
        ctx.result(serializer.toJson(new JsonObject()));
//        ctx.status(500).result(serializer.toJson(e.getMessage()));
    }

    private void register(Context ctx) throws DataAccessException{
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

    private void login(Context ctx) throws DataAccessException {
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
        /*TODO: change line 109 to only return the value retrieved by get("gameName"), then
          TODO: change gameService.createGame to take gameName as an Object, throw an exception if null, otherwise convert it to a string and continue
        * */
        Object gameName = serializer.fromJson(ctx.body(), Map.class).get("gameName");
        try {
            var gameID = gameService.createGame(authToken, gameName);
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
