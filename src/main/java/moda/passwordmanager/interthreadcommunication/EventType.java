package moda.passwordmanager.interthreadcommunication;

/**
 * The EventType enum is used to handle properly the Inter Thread Communication between the Backend and the Frontend,
 * because it allows to handle differently the requests and the responses between the two.
 */
public enum EventType {
    REQUEST,
    RESPONSE
}
