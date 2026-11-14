package moda.passwordmanager.backend;

import moda.passwordmanager.backend.settings.SensitiveSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveSettingsTests {

    /**
     * The temporary directory where the database file will be placed. It must not be a static attribute as otherwise the
     * path would be the same for each test case. Lastly, the directory is destroyed after each test.
     */
    @TempDir
    private Path testDirectory;

    private static final String MASTER_PASSWORD = "1234";

    private Database database;
    private Helper helper;
    private SensitiveSettings sensitiveSettings;

    // As we cannot modify the init method, we have to use one of its properties to test the various cases
    private static final String PROPERTY_PATH = "google_drive/enabled";
    private static final String STRING_PROPERTY = "Test";
    private static final int INT_PROPERTY = 1;
    private static final boolean BOOLEAN_PROPERTY = true;
    private static final List<Object> LIST_PROPERTY = new ArrayList<>();

    @BeforeEach
    void init(){
        Cryptography cryptography = new Cryptography();
        cryptography.setMasterPassword(MASTER_PASSWORD.getBytes());

        this.database = new Database(testDirectory + "\\" + "test.modb");

        this.helper = new Helper(cryptography);
        this.sensitiveSettings = new SensitiveSettings(database, this.helper);
        this.sensitiveSettings.init();  // We have to initialize the records because it is the only way to add them
    }

    @AfterEach
    void cleanup(){
        this.database.delete();
    }

    /**
     * Ensure that a string property is written in the Sensitive Settings table
     */
    @Test
    void writeReadStringProperty(){
        this.sensitiveSettings.writeProperty(PROPERTY_PATH, STRING_PROPERTY);
        assertEquals(STRING_PROPERTY, this.sensitiveSettings.readStringProperty(PROPERTY_PATH));
    }

    /**
     * Ensure that the method raises an UnsupportedOperationException
     */
    @Test
    void writeReadStringSubproperty(){
        boolean exceptionRaised = false;
        try{
            this.sensitiveSettings.writeSubproperty(PROPERTY_PATH, STRING_PROPERTY);
        }catch (UnsupportedOperationException e){
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }

    /**
     * Ensure that a int property is written in the Sensitive Settings table
     */
    @Test
    void writeReadIntProperty(){
        this.sensitiveSettings.writeProperty(PROPERTY_PATH, INT_PROPERTY);
        assertEquals(INT_PROPERTY, this.sensitiveSettings.readIntProperty(PROPERTY_PATH));
    }

    /**
     * Ensure that the method raises an UnsupportedOperationException
     */
    @Test
    void writeReadIntSubproperty(){
        boolean exceptionRaised = false;
        try{
            this.sensitiveSettings.writeSubproperty(PROPERTY_PATH, INT_PROPERTY);
        }catch (UnsupportedOperationException e){
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }

    /**
     * Ensure that a boolean property is written in the Sensitive Settings table
     */
    @Test
    void writeReadBooleanProperty(){
        this.sensitiveSettings.writeProperty(PROPERTY_PATH, BOOLEAN_PROPERTY);
        assertEquals(BOOLEAN_PROPERTY, this.sensitiveSettings.readBooleanProperty(PROPERTY_PATH));
    }

    /**
     * Ensure that the method raises an UnsupportedOperationException
     */
    @Test
    void writeReadBooleanSubproperty(){
        boolean exceptionRaised = false;
        try{
            this.sensitiveSettings.writeSubproperty(PROPERTY_PATH, BOOLEAN_PROPERTY);
        }catch (UnsupportedOperationException e){
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }

    /**
     * Ensure that the method raises an UnsupportedOperationException
     */
    @Test
    void writeReadListProperty(){
        boolean exceptionRaised = false;
        try{
            this.sensitiveSettings.writeSubproperty(PROPERTY_PATH, LIST_PROPERTY);
        }catch (UnsupportedOperationException e){
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }

    /**
     * Ensure that the method raises an UnsupportedOperationException
     */
    @Test
    void writeReadListSubproperty(){
        boolean exceptionRaised = false;
        try{
            this.sensitiveSettings.writeSubproperty(PROPERTY_PATH, LIST_PROPERTY);
        }catch (UnsupportedOperationException e){
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }

}
