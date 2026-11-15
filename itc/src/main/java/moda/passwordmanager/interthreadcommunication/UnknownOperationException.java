package moda.passwordmanager.interthreadcommunication;

/**
 * The UnknownOperation exception is arisen when the EventListener thread receives an event containing an operation
 * that is not inside the handler map.
 */
public class UnknownOperationException extends RuntimeException {
    public UnknownOperationException(String message) {
        super(message);
    }
}
