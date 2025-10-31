package handler;

import dataaccess.DataAccessException;
import dataaccess.InvalidCredentialsException;
import datamodel.LoginData;
import datamodel.UserData;

import java.util.Objects;

public class ExceptionHandler {
    public static void verifyFieldsNotEmpty(UserData userData) throws InvalidCredentialsException {
        if (userData.username() == null || userData.password() == null || userData.email() == null
        || userData.username().isBlank() || userData.password().isBlank() || userData.email().isBlank()){
            throw new InvalidCredentialsException("Error: bad request");
        }
    }

    public static void verifyFieldsNotEmpty(LoginData loginData) throws InvalidCredentialsException {
        if (loginData.username() == null || loginData.password() == null ||
                loginData.username().isBlank() || loginData.password().isBlank()){
            throw new InvalidCredentialsException("Error: bad request");
        }
    }

    public static void validateColor(String playerColor) throws DataAccessException {
        if (!(Objects.equals(playerColor, "WHITE") || Objects.equals(playerColor, "BLACK"))){
            throw new DataAccessException("Error: bad request");
        }
    }

    public static int getErrorCode(Exception ex) {
        String errorMessage = ex.getMessage();
        switch (errorMessage) {
            case "Error: bad request" -> {
                return 400;
            }
            case "Error: unauthorized" -> {
                return 401;
            }
            case "Error: already taken", "Error: Forbidden" -> {
                return 403;
            }
            default -> {
                return 500;
            }
        }
    }
}
