package dataaccess;

public class InvalidCredentialsException extends DataAccessException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
