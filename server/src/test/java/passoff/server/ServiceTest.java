package passoff.server;

import dataaccess.DataAccess;
import dataaccess.InvalidCredentialsException;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.UserService;

public class ServiceTest {

    @Test
    public void registerNormalUser() throws InvalidCredentialsException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var response = userService.register(new UserData("myUsername", "myPassword", "myEmail@cs240.gov"));
        Assertions.assertNotNull(response);
    }
}
