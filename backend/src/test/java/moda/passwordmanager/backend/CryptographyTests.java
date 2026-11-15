package moda.passwordmanager.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CryptographyTests {

    private static final String MASTER_PASSWORD = "1234";

    private static final String PLAINTEXT = "test";
    private static final byte[] CIPHERTEXT = {-1, -28, -120, -59, -31, 111, -18, 59, 88, -99, -12, 57, -41, -106, -61,
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

    /**
     * Test the encryption of a plaintext
     */
    @Test
    void encrypt(){
        byte[] ciphertext = this.cryptography.encrypt(PLAINTEXT);
        String decryptedCiphertext = new String(this.cryptography.decrypt(ciphertext));

        assertNotEquals(PLAINTEXT.getBytes(), ciphertext);  // We assert that the plaintext does not match the ciphertext
        assertEquals(PLAINTEXT, decryptedCiphertext);  // Then that the encryption was done successfully
    }

    /**
     * Test the decryption of a ciphertext
     */
    @Test
    void decrypt(){
        assertEquals(PLAINTEXT, new String(this.cryptography.decrypt(CIPHERTEXT)));
    }

}
