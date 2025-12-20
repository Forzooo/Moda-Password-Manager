package moda.passwordmanager.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelperTests {

    private Settings settings;
    private Helper helper;

    private final static String TEST_STRING = "Test";
    private final static String TEST_STRING_BASE64 = "VGVzdA==";

    // String generation test attributes
    private final static String STRING_GENERATION_SETTINGS_ROOT_NODE = "string_generation/";

    // Google Drive test attributes
    private final static String GOOGLE_DRIVE_ROOT_NODE = "google_drive/";

    // Database test attributes
    private final static String DATABASE_ROOT_NODE = "database/";

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
     * Ensure that the Helper returns the current state of Google Drive
     */
    @Test
    void isGoogleDriveEnabled(){
        boolean googleDriveState = this.helper.isGoogleDriveEnabled();
        assertEquals(this.settings.readBooleanProperty(GOOGLE_DRIVE_ROOT_NODE+"enabled"), googleDriveState);
    }

    /**
     * Ensure that the Helper returns the path of the database
     */
    @Test
    void getDatabasePath(){
        String databasePath = this.helper.getDatabasePath();
        assertEquals(this.settings.readStringProperty(DATABASE_ROOT_NODE+"path"), databasePath);
    }

}
