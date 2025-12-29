package moda.passwordmanager.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CryptographyTests {

    private final static String MASTER_PASSWORD = "1234";

    private final static String PLAINTEXT = "test";
    private final static byte[] CIPHERTEXT = {-1, -28, -120, -59, -31, 111, -18, 59, 88, -99, -12, 57, -41, -106, -61,
            82, -35, -40, -24, 82, -61, -127, -60, -125, 8, 94, 105, -2, 4, -117, -41, 78, 81, 58, -99, 125, -31, 7, 65,
            -112, -98, 24, 28, -94, 127, -81, 75, 52};

    private Cryptography cryptography;

    /**
     * Initialize the cryptography object before each test
     */
    @BeforeEach
    void init(){
        this.cryptography = new Cryptography();
        this.cryptography.setMasterPassword(MASTER_PASSWORD.getBytes());
    }

    /* The encryption cannot be tested under the current configuration of the cryptography class
    @Test
    void encrypt(){

    }
    */

    /**
     * Test the decryption of a ciphertext
     */
    @Test
    void decrypt(){
        assertEquals(PLAINTEXT, new String(this.cryptography.decrypt(CIPHERTEXT)));
    }

}
