package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

public class Server {

    private final Javalin server;
    private UserService userService;
    private DataAccess dataAccess;

    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", ctx -> ctx.result("{}"));

        server.post("user", this::register);
        server.post("session", this::login);

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


    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
