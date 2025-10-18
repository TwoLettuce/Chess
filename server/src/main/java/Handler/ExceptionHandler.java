package Handler;

import dataaccess.InvalidCredentialsException;
import datamodel.LoginData;
import datamodel.UserData;

public class ExceptionHandler {
    public static void verifyFieldsNotEmpty(UserData userData) throws InvalidCredentialsException {
        if (userData.username().isBlank() || userData.password().isBlank() || userData.email().isBlank()){
            throw new InvalidCredentialsException("Field in registration is empty or only whitespace");
        }
    }

    public static void verifyFieldsNotEmpty(LoginData loginData) throws InvalidCredentialsException {
        if (loginData.username().isBlank() || loginData.password().isBlank()){
            throw new InvalidCredentialsException("Field in registration is empty or only whitespace");
        }
    }
}
