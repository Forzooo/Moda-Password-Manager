package moda.passwordmanager.backend;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

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
     * Ensure that when creating a Data object without providing an ID, it is set to -1
     */
    @Test
    void stringConstructor(){
        Data data = new Data(USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE, ADDITIONAL_DATA);
        assertEquals(ID_NOT_SET, data.getId());
    }

    /**
     * Ensure that the method 'asLinkedHashMap' returns all the fields of the Data object
     */
    @Test
    void asLinkedHashMap(){
        Data data = new Data(ID, USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE, ADDITIONAL_DATA);
        LinkedHashMap<String, String> userData = data.asLinkedHashMap();

        assertEquals(ID, Integer.parseInt(userData.get("id")));
        assertEquals(USERNAME, userData.get("username"));
        assertEquals(EMAIL_ADDRESS, userData.get("email_address"));
        assertEquals(PASSWORD, userData.get("password"));
        assertEquals(SERVICE, userData.get("service"));
        assertEquals(ADDITIONAL_DATA, userData.get("additional_data"));
    }

}
