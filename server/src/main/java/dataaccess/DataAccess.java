package dataaccess;

import datamodel.AuthData;
import datamodel.UserData;

public interface DataAccess {
    AuthData registerUser(UserData userData);

    UserData getUser(String username);
}
