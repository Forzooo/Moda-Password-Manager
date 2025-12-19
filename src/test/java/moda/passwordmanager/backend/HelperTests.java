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
    private final static int STRING_GENERATION_LENGTH = 64;  // Avoid 32 as it's the default value
    private final static boolean STRING_GENERATION_CHARS = false;

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
        // Set the values of the configuration before retrieving them
        this.settings.writeProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"length", STRING_GENERATION_LENGTH);
        this.settings.writeProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"letters", STRING_GENERATION_CHARS);
        this.settings.writeProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"numbers", STRING_GENERATION_CHARS);
        this.settings.writeProperty(STRING_GENERATION_SETTINGS_ROOT_NODE+"special", STRING_GENERATION_CHARS);

        ArrayList<Object> configuration = this.helper.getStringGenerationConfiguration();
        assertEquals(STRING_GENERATION_LENGTH, configuration.getFirst());
        assertEquals(STRING_GENERATION_CHARS, configuration.get(1));
        assertEquals(STRING_GENERATION_CHARS, configuration.get(2));
        assertEquals(STRING_GENERATION_CHARS, configuration.get(3));
    }


}
