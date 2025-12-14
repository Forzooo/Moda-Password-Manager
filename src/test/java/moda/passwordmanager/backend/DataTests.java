package moda.passwordmanager.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataTests {

    private final static int ID = 1;
    private final static int ID_NOT_SET = -1;
    private final static String USERNAME = "username";
    private final static String EMAIL_ADDRESS = "email@example.com";
    private final static String PASSWORD = "password";
    private final static String SERVICE = "service";
    private final static String ADDITIONAL_DATA = "Data";

    /**
     * Ensure that when creating a Data object, where the data are passed as a byte array, the ID is set to -1 and the
     * byte arrays are encoded to UTF-8
     */
    @Test
    void byteConstructor(){
        Data data = new Data(USERNAME.getBytes(), EMAIL_ADDRESS.getBytes(), PASSWORD.getBytes(), SERVICE.getBytes(),
                ADDITIONAL_DATA.getBytes());
        assertEquals(USERNAME, data.getUSERNAME());
        assertEquals(EMAIL_ADDRESS, data.getEMAIL_ADDRESS());
        assertEquals(PASSWORD, data.getPASSWORD());
        assertEquals(SERVICE, data.getSERVICE());
        assertEquals(ADDITIONAL_DATA, data.getADDITIONAL_DATA());
        assertEquals(ID_NOT_SET, data.getID());
    }

    /**
     * Ensure that when creating a Data object without providing an ID, it is set to -1
     */
    @Test
    void stringConstructor(){
        Data data = new Data(USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE, ADDITIONAL_DATA);
        assertEquals(ID_NOT_SET, data.getID());
    }

    /**
     * Ensure that the method 'getFullUserData' returns the data of the object in the following order: username, email
     * address, password, service, additional data.
     */
    @Test
    void getFullUserData(){
        Data data = new Data(ID, USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE, ADDITIONAL_DATA);
        String[] fullUserData = data.getFullUserData();

        assertEquals(USERNAME, fullUserData[0]);
        assertEquals(EMAIL_ADDRESS, fullUserData[1]);
        assertEquals(PASSWORD, fullUserData[2]);
        assertEquals(SERVICE, fullUserData[3]);
        assertEquals(ADDITIONAL_DATA, fullUserData[4]);
    }

}
