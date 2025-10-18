package server;

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
        try {
            dataService.clear();
            ctx.result(serializer.toJson(new JsonObject()));
        } catch (DataAccessException e) {
            ctx.status(500).result(serializer.toJson(e.getMessage()));
        }

    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), UserData.class);
        AuthData response;
        try {
            response = userService.register(request);
        } catch (DataAccessException e) {
            ctx.result(serializer.toJson(e.getMessage()));
            return;
        }

        ctx.result(serializer.toJson(response));
    }

    private void login(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), LoginData.class);
        AuthData response;
        try {
            response = userService.login(request);
        } catch (DataAccessException e) {
            ctx.result(serializer.toJson(e.getMessage()));
            return;
        }
        ctx.result(serializer.toJson(response));

    }

    private void logout(Context ctx) {
        var serializer = new Gson();
        String authToken = serializer.fromJson(ctx.body(), String.class);
        try {
            userService.logout(authToken);
            ctx.result(serializer.toJson("{}"));
        } catch (DataAccessException e) {
            ctx.result(serializer.toJson(e.getMessage()));
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
