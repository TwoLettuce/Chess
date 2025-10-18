package service;

import Handler.ExceptionHandler;
import dataaccess.DataAccess;
import dataaccess.InvalidCredentialsException;
import datamodel.AuthData;
import datamodel.UserData;

public class UserService {
    private DataAccess dataAccess;
    //TODO: create a constructor that uses a DataAccess
    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData userData) throws InvalidCredentialsException {
        ExceptionHandler.userDataFieldsEmpty(userData);

        AuthData authData = dataAccess.registerUser(userData);
        return new AuthData(authData.username(), authData.authToken());
    }


    public void login(){}

    public void logout(){}
}
