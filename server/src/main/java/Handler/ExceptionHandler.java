package Handler;

import dataaccess.InvalidCredentialsException;
import datamodel.UserData;

public class ExceptionHandler {
    public static void userDataFieldsEmpty(UserData userData) throws InvalidCredentialsException {
        if (userData.username().isBlank() || userData.password().isBlank() || userData.email().isBlank()){
            throw new InvalidCredentialsException("Field in registration is empty or only whitespace");
        }
    }
}
