package moda.passwordmanager.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsTests {

    // The property node used by the test can be any
    private final static String PROPERTY_NODE = "database/selected";
    private final static String STRING_PROPERTY = "Test";
    private final static int INT_PROPERTY = 1;
    private final static boolean BOOLEAN_PROPERTY = true;
    private final static ArrayList LIST_PROPERTY = new ArrayList();

    /**
     * The temporary directory where the settings file will be placed. It must not be a static attribute as otherwise the
     * path would be the same for each test case. Lastly, the directory is destroyed after each test.
     */
    @TempDir
    private Path TEST_DIRECTORY;

    private Settings settings;

    /**
     * Before each test the settings object has to be created again as the test directory changes every time
     */
    @BeforeEach
    void init(){
        this.settings = new Settings(TEST_DIRECTORY +"\\"+Helper.getSettingsFile());
    }

    /**
     * Ensure that a string property is written in the file
     */
    @Test
    void writeReadStringProperty(){
        this.settings.writeProperty(PROPERTY_NODE, STRING_PROPERTY);
        assertEquals(STRING_PROPERTY, this.settings.readStringProperty(PROPERTY_NODE));
    }

    /**
     * Ensure that a int property is written in the file
     */
    @Test
    void writeReadIntProperty(){
        this.settings.writeProperty(PROPERTY_NODE, INT_PROPERTY);
        assertEquals(INT_PROPERTY, this.settings.readIntProperty(PROPERTY_NODE));
    }

    /**
     * Ensure that a boolean property is written in the file
     */
    @Test
    void writeReadBooleanProperty(){
        this.settings.writeProperty(PROPERTY_NODE, BOOLEAN_PROPERTY);
        assertEquals(BOOLEAN_PROPERTY, this.settings.readBooleanProperty(PROPERTY_NODE));
    }

    /**
     * Ensure that a boolean property is written in the file
     */
    @Test
    void writeReadListProperty(){
        this.settings.writeListProperty(PROPERTY_NODE, LIST_PROPERTY);
        assertEquals(LIST_PROPERTY, this.settings.readListProperty(PROPERTY_NODE));
    }

}
