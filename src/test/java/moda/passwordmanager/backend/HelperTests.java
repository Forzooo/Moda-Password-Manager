package moda.passwordmanager.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Currently for the current configuration of the cryptography class, the encryption tests cannot be performed, so they
// are ignored and no test case involves them
class HelperTests {

    private Settings settings;
    private Helper helper;

    private final static String TEST_STRING = "Test";
    private final static String TEST_STRING_BASE64 = "VGVzdA==";

    // String generation test attributes
    private final static String STRING_GENERATION_SETTINGS_ROOT_NODE = "string_generation/";

    // Database test attributes
    private final static String DATABASE_ROOT_NODE = "database/";

    // Decryption test attributes
    private final static String MASTER_PASSWORD = "dev";
    private final static String STRING_PLAINTEXT = "Test";
    private final static String STRING_CIPHERTEXT = "yrflaOOdfwumdECCw4OaxY+R+myhKKr1XU843ieinqMx1Y7BGItZBL3+ybitpPbH";
    private final static Data DATA_PLAINTEXT = new Data(STRING_PLAINTEXT, STRING_PLAINTEXT, STRING_PLAINTEXT,
            STRING_PLAINTEXT, STRING_PLAINTEXT);
    private final static Data DATA_CIPHERTEXT = new Data(
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
    private Path TEST_DIRECTORY;

    @BeforeEach
    void init(){
        Cryptography cryptography = new Cryptography();
        cryptography.setMasterPassword(MASTER_PASSWORD.getBytes());
        this.settings = new Settings(TEST_DIRECTORY+"\\"+Helper.getSettingsFile());
        this.helper = new Helper(cryptography, settings);
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
     * Test the getter of string configuration
     */
    @Test
    void getStringGenerationConfiguration(){
        ArrayList<Object> configuration = this.helper.getStringGenerationConfiguration();

        assertEquals(this.settings.readIntProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"length"),
                configuration.getFirst());
        assertEquals(this.settings.readBooleanProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"letters"),
                configuration.get(1));
        assertEquals(this.settings.readBooleanProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"numbers"),
                configuration.get(2));
        assertEquals(this.settings.readBooleanProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"special"),
                configuration.get(3));
    }

    /**
     * Ensure that the Helper returns the path of the database
     */
    @Test
    void getDatabasePath(){
        String databasePath = this.helper.getDatabasePath();
        assertEquals(this.settings.readStringProperty(DATABASE_ROOT_NODE+"selected"), databasePath);
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

}
