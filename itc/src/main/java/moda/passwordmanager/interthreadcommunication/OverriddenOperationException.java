package moda.passwordmanager.interthreadcommunication;

/**
 * The OverriddenOperation exception is arisen when an operation that already exists in the handler map is trying to be
 * readded.
 */
public class OverriddenOperationException extends RuntimeException {
    public OverriddenOperationException(String message) {
        super(message);
    }
}
