package moda.passwordmanager.backend;

/**
 * The MasterPasswordNotSet exception is arisen when a cryptographical operation is requested before the key (the
 * master password) is set.
 */
public class MasterPasswordNotSet extends RuntimeException {
    public MasterPasswordNotSet(String message) {
        super(message);
    }
}
