package moda.passwordmanager.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.util.ArrayList;

class DatabaseTests {

    /**
     * The temporary directory where the database file will be placed. It must not be a static attribute as otherwise the
     * path would be the same for each test case. Lastly, the directory is destroyed after each test.
     */
    @TempDir
    private Path TEST_DIRECTORY;
    private final static String DATABASE_NAME = "test.modb";
    private final static String SECONDARY_DATABASE_NAME = "test2.modb";

    // The numbers of total records used
    private final static int RECORDS_NUMBER = 2;

    private final static int ID = 1;
    private final static String USERNAME = "username";
    private final static String EMAIL_ADDRESS = "email@example.com";
    private final static String PASSWORD = "password";
    private final static String SERVICE = "service";
    private final static String ADDITIONAL_DATA = "Data";

    private final static int ID_2 = 2;
    private final static String USERNAME_2 = "username2";
    private final static String EMAIL_ADDRESS_2 = "email2@example.com";
    private final static String PASSWORD_2 = "password2";
    private final static String SERVICE_2 = "service2";
    private final static String ADDITIONAL_DATA_2 = "Data2";

    private final static Data TEST_DATA = new Data(ID, USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE, ADDITIONAL_DATA);

    // The updated test data
    private final static Data UPDATED_TEST_DATA = new Data(ID, USERNAME_2, EMAIL_ADDRESS_2, PASSWORD_2, SERVICE_2,
            ADDITIONAL_DATA_2);

    private final static Data TEST_DATA_2 = new Data(ID_2, USERNAME_2, EMAIL_ADDRESS_2,
            PASSWORD_2, SERVICE_2, ADDITIONAL_DATA_2);

    private final static Data UPDATED_TEST_DATA_2 = new Data(ID_2, USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE,
            ADDITIONAL_DATA);

    private Database database;

    /**
     * Before each test the database object has to be created again as the test directory changes every time
     */
    @BeforeEach
    void init(){
        this.database = new Database(TEST_DIRECTORY + "\\" + DATABASE_NAME);
    }

    /**
     * After each test close the connection with the database to allow JUnit to delete the temporary directory
     */
    @AfterEach
    void cleanup(){
        this.database.closeConnection();
    }

    /**
     * Ensure that the getDatabaseName returns the name of the database
     */
    @Test
    void getDatabaseName(){
        assertEquals(DATABASE_NAME, this.database.getDatabaseName());
    }

    /**
     * Ensure that a record is added to the database
     */
    @Test
    void addReadRecord(){
        this.database.addRecord(TEST_DATA);
        Data record = this.database.getRecord(ID);

        assertEquals(TEST_DATA, record);
    }

    /**
     * Ensure that a record is updated correctly
     */
    @Test
    void updateRecord(){
        this.database.addRecord(TEST_DATA);
        this.database.updateRecord(UPDATED_TEST_DATA);

        Data updatedRecord = this.database.getRecord(ID);

        assertEquals(UPDATED_TEST_DATA, updatedRecord);
    }

    /**
     * Ensure that a record is deleted correctly
     */
    @Test
    void deleteRecord(){
        this.database.addRecord(TEST_DATA);
        this.database.deleteRecord(ID);

        // A deleted record has only the ID set, where the other fields are null thus we use assertNull
        Data deletedRecord = this.database.getRecord(ID);

        assertNull(deletedRecord.getUSERNAME());
        assertNull(deletedRecord.getEMAIL_ADDRESS());
        assertNull(deletedRecord.getPASSWORD());
        assertNull(deletedRecord.getSERVICE());
        assertNull(deletedRecord.getADDITIONAL_DATA());
    }

    /**
     * Ensure that changing a database works properly
     */
    @Test
    void changeDatabase(){
        this.database.addRecord(TEST_DATA);  // Add the record to the first database

        // Change the database to another one and add a new record inside it
        this.database.changeDatabase(TEST_DIRECTORY + "\\" + SECONDARY_DATABASE_NAME);

        // We're using the updated data instead of the test data 2 as it will be the first record of the second database,
        // so it requires to have the ID = 1, where the test data 2 has ID = 2
        this.database.addRecord(UPDATED_TEST_DATA);

        // Change the database to the first one and assert that the record inside it is the right one
        this.database.changeDatabase(TEST_DIRECTORY + "\\" + DATABASE_NAME);

        Data recordFirstDatabase = this.database.getRecord(ID);
        assertEquals(TEST_DATA, recordFirstDatabase);

        // Change again the database and assert that the record inside is the second one
        this.database.changeDatabase(TEST_DIRECTORY + "\\" + SECONDARY_DATABASE_NAME);

        Data recordSecondDatabase = this.database.getRecord(ID);
        assertEquals(UPDATED_TEST_DATA, recordSecondDatabase);
    }

    /**
     * Ensure that the database returns the first service field
     */
    @Test
    void getFirstServiceField(){
        // We have to add records to the database before we're getting the first service field
        this.database.addRecord(TEST_DATA);
        this.database.addRecord(TEST_DATA_2);

        Data firstServiceField = this.database.getFirstServiceField();
        assertEquals(SERVICE, firstServiceField.getSERVICE());
    }

    /**
     * Ensure that the database returns all the service fields
     */
    @Test
    void getServiceFields(){
        // We have to add records to the database before we're getting the first service field
        this.database.addRecord(TEST_DATA);
        this.database.addRecord(TEST_DATA_2);

        ArrayList<Data> serviceFields = this.database.getServiceFields();
        assertEquals(SERVICE, serviceFields.getFirst().getSERVICE());
        assertEquals(SERVICE_2, serviceFields.get(1).getSERVICE());
        assertEquals(RECORDS_NUMBER, serviceFields.size());  // Also ensure that the number of records returned is 2
    }

    /**
     * Ensure that the database returns all the records inside it
     */
    @Test
    void getRecords(){
        this.database.addRecord(TEST_DATA);
        this.database.addRecord(TEST_DATA_2);

        ArrayList<Data> records = this.database.getRecords();
        Data firstRecord = records.getFirst();
        Data secondRecord = records.get(1);

        assertEquals(TEST_DATA, firstRecord);
        assertEquals(TEST_DATA_2, secondRecord);
        assertEquals(RECORDS_NUMBER, records.size());  // Also ensure that the number of records returned is 2
    }

    /**
     * Ensure that the databases changes all the records inside it
     */
    @Test
    void changeRecords(){
        this.database.addRecord(TEST_DATA);
        this.database.addRecord(TEST_DATA_2);

        ArrayList<Data> records = new ArrayList<>();
        records.add(UPDATED_TEST_DATA);
        records.add(UPDATED_TEST_DATA_2);

        this.database.changeRecords(records);

        ArrayList<Data> updatedRecords = this.database.getRecords();

        Data firstRecord = updatedRecords.getFirst();
        assertEquals(UPDATED_TEST_DATA, firstRecord);

        Data secondRecord = updatedRecords.get(1);
        assertEquals(UPDATED_TEST_DATA_2, secondRecord);
    }

}
