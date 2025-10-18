package Handler;

import dataaccess.InvalidCredentialsException;
import datamodel.LoginData;
import datamodel.UserData;

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

    public static int getErrorCode(Exception ex) {
        String errorMessage = ex.getMessage();
        switch (errorMessage) {
            case "Error: bad request" -> {
                return 400;
            }
            case "Error: unauthorized" -> {
                return 401;
            }
            case "Error: already taken" -> {
                return 403;
            }
            default -> {
                return 500;
            }
        }
    }
}
