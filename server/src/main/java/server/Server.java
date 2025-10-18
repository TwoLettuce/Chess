package server;

import Handler.ExceptionHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;
import service.DataService;

import java.util.Map;

public class Server {

    private final Javalin server;
    private UserService userService;
    private DataService dataService;
    private DataAccess dataAccess;


    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        dataService = new DataService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", this::clear);

        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);

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
        String authToken = serializer.fromJson(ctx.body(), String.class);
        try {
            userService.logout(authToken);
            ctx.json(serializer.toJson("{}"));
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
