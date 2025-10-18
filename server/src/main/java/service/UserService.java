package service;

import Handler.ExceptionHandler;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;

import java.util.Objects;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData userData) throws DataAccessException {
        ExceptionHandler.verifyFieldsNotEmpty(userData);
        return dataAccess.registerUser(userData);
    }


    public AuthData login(LoginData loginData) throws DataAccessException{
        ExceptionHandler.verifyFieldsNotEmpty(loginData);
        return dataAccess.login(loginData);
    }

    public void logout(String authToken) throws DataAccessException{
        dataAccess.logout(authToken);
    }
}
