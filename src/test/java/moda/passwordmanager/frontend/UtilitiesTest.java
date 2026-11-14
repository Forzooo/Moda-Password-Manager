package moda.passwordmanager.frontend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilitiesTest {

    /**
     * Ensure that the check master password checks for the right conditions
     */
    @Test
    void checkMasterPassword(){
        assertFalse(Utilities.checkMasterPassword(new char[0]));
        assertTrue(Utilities.checkMasterPassword(new char[1]));
    }
}
