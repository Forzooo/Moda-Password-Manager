package moda.passwordmanager.backend;

import moda.passwordmanager.frontend.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Currently for the current configuration of the cryptography class, the encryption tests cannot be performed, so they
// are ignored and no test case involves them
class HelperTests {

    private Helper helper;

    private static final String TEST_STRING = "Test";
    private static final String TEST_MD5_CHECKSUM = "0cbc6611f5540bd0809a388dc95a615b";
    private static final String TEST_STRING_BASE64 = "VGVzdA==";

    // Background executor tests attributes
    private int[] backgroundTestData;
    private Semaphore semaphore;
    private static final int BACKGROUND_TEST_DATA_PERIODICALLY = 3;

    // Decryption test attributes
    private static final String MASTER_PASSWORD = "dev";
    private static final String STRING_PLAINTEXT = "Test";
    private static final String STRING_CIPHERTEXT = "yrflaOOdfwumdECCw4OaxY+R+myhKKr1XU843ieinqMx1Y7BGItZBL3+ybitpPbH";
    private static final Data DATA_PLAINTEXT = new Data(STRING_PLAINTEXT, STRING_PLAINTEXT, STRING_PLAINTEXT,
            STRING_PLAINTEXT, STRING_PLAINTEXT);
    private static final Data DATA_CIPHERTEXT = new Data(
            "cJ2pjIGv3/xrxKbcC+kf3Xdwj4eC6MFozR3ffiT5asBNCQkEBZn/JuixAYJYJmQP",
            "YoIOEHEf5Bb+PSuaKa0Ctk6c/2SJqs90IWllpemEJp0KuGyDrQbKXE3T1tLW2m/d",
            "sDTNHKDmEsrCCz/lFTUzptcwb3ZyWMDSqif/OvaNDjOTomCVrNHM+w92tNVQ97xS",
            "jeMOllUlPG0P8YWzZ78Np5dH8KkXyvn4H+IkDiPq+aRgCMRuBAuPx3myq9/SkOTq",
            "B12TW0JIRTJH5o12kVkWpo7v1rttKILjetBplc1RmnBr6I4cwoy9FLbU7js0t/Ac"
    );

    /**
     * The temporary directory where the settings file will be placed. It must not be a static attribute as otherwise the
     * path would be the same for each test case. Lastly, the directory is destroyed after each test.
     */
    @TempDir
    private Path testDirectory;

    @BeforeEach
    void init(){
        Cryptography cryptography = new Cryptography();
        cryptography.setMasterPassword(MASTER_PASSWORD.getBytes());
        this.helper = new Helper(cryptography);

        this.backgroundTestData = new int[1];
        this.semaphore = new Semaphore(0);
    }

    /**
     * Ensure that the Helper can execute a method in the background
     */
    @Test
    void executeInBackground(){
        // To check that it works, we set the test data to 1 and release the semaphore
        this.helper.executeInBackground(() -> {
            this.backgroundTestData[0] = 1;
            this.semaphore.release();
        });

        try {
            this.semaphore.acquire();  // The main thread has to wait until the Executor Service has set the value to 1
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(1, this.backgroundTestData[0]);
    }

    /**
     * Ensure that the Helper can execute a method in the background periodically
     */
    @Test
    void executePeriodicallyInBackground(){
        // The same logic as before, but this time we increment it multiple times until it reaches 3
        this.helper.executePeriodicallyInBackground(() -> {
            if (this.backgroundTestData[0] < BACKGROUND_TEST_DATA_PERIODICALLY){
                this.backgroundTestData[0] += 1;
                this.semaphore.release();
            }
        }, 1);

        try {
            this.semaphore.acquire(BACKGROUND_TEST_DATA_PERIODICALLY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(BACKGROUND_TEST_DATA_PERIODICALLY, this.backgroundTestData[0]);
    }

    /**
     * Ensure that it returns the filename
     */
    @Test
    void getFilenameFromPath(){
        // The first test also ensures that the normalization is working correctly
        assertEquals("test.txt", Helper.getFilenameFromPath("C:\\path\\path2\\test.txt"));
        assertEquals("test.txt", Helper.getFilenameFromPath("C:/path/path2/test.txt"));
    }

    /**
     * Test the encoding of a string into base 64
     */
    @Test
    void encodeBase64(){
        assertEquals(TEST_STRING_BASE64, Helper.encodeBase64(TEST_STRING.getBytes()));
    }

    /**
     * Test the decoding of a string from base 64
     */
    @Test
    void decodeBase64(){
        assertEquals(TEST_STRING, new String(Helper.decodeBase64(TEST_STRING_BASE64)));
    }

    /**
     * Test the decryption of a string encoded in base 64
     */
    @Test
    void decryptString(){
        assertEquals(STRING_PLAINTEXT, this.helper.decrypt(STRING_CIPHERTEXT));
    }

    @Test
    void decryptData(){
        assertEquals(DATA_PLAINTEXT, this.helper.decryptData(DATA_CIPHERTEXT));
    }

    /**
     * Ensure that it calculates the MD5 checksum of the content of a file properly
     */
    @Test
    void calculateFileMD5(){
        File file = new File(testDirectory +"\\test.txt");
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(TEST_STRING);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(TEST_MD5_CHECKSUM, Helper.calculateFileMD5(file.toString()));
    }
}
