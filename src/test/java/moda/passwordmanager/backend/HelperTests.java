package moda.passwordmanager.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;

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
    private final static String STRING_PLAINTEXT = "Test";
    private final static String STRING_CIPHERTEXT = "aCRSwS7aFZXmixeSRHPxgLwHLXejH22ZkJAxfxDtqxtJWZBxhdQyyFGuM3+UNI3Z";
    private final static Data DATA_PLAINTEXT = new Data(STRING_PLAINTEXT, STRING_PLAINTEXT, STRING_PLAINTEXT,
            STRING_PLAINTEXT, STRING_PLAINTEXT);
    private final static Data DATA_CIPHERTEXT = new Data(
            "SvZNvHkFLzlNyYnAfiubsivzoD8kDCxs+WN1L6XObeQfRbSL469P+sWWZvLiB3+v",
            "hqm3IG7983V5gbfxsyixvo4ZO6Ziajr9Hlds0zjgYstxcZqv6rhT2vfWHMqUWcPY",
            "4X7xLUCgUs+5bDORr6mTUCAeDqR0NaCM53f+wuppHDMY6kNnE1uGIvBGQepRbtFZ",
            "tmWEn5QoLbzoLOW4tGNCTSYHhgbQ9JGFfZnlIhRCVnPNKt+N0IpNTcair2KYVliI",
            "m5pMeoBQ++fmEO/5FedHYXG5HkYdAYXczoSGOrkBZ25h2uzk+/NCWNeU/Wfu5YHm"
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
        assertEquals(this.settings.readStringProperty(DATABASE_ROOT_NODE+"path"), databasePath);
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
