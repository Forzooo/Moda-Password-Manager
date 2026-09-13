package moda.passwordmanager.backend;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataTests {

    private static final int ID = 1;
    private static final int ID_NOT_SET = -1;
    private static final String USERNAME = "username";
    private static final String EMAIL_ADDRESS = "email@example.com";
    private static final String PASSWORD = "password";
    private static final String SERVICE = "service";
    private static final String ADDITIONAL_DATA = "Data";

    /**
     * Ensure that when creating a Data object without providing an ID, it is set to -1
     */
    @Test
    void stringConstructor(){
        Data data = new Data(USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE, ADDITIONAL_DATA);
        assertEquals(ID_NOT_SET, data.getId());
    }

    /**
     * Ensure that, when creating a Data object with a Map, it instantiates it correctly
     */
    @Test
    void mapConstructor(){
        EnumMap<Data.Fields, String> map = new EnumMap<>(Data.Fields.class);
        map.put(Data.Fields.ID, String.valueOf(ID));
        map.put(Data.Fields.USERNAME, USERNAME);
        map.put(Data.Fields.EMAIL_ADDRESS, EMAIL_ADDRESS);
        map.put(Data.Fields.PASSWORD, PASSWORD);
        map.put(Data.Fields.SERVICE, SERVICE);
        map.put(Data.Fields.ADDITIONAL_DATA, ADDITIONAL_DATA);

        Data data = new Data(map);

        assertEquals(Integer.class, ((Object) data.getId()).getClass());  // Also assert that the ID is converted from
                                                                          // String to Integer
        assertEquals(ID, data.getId());
        assertEquals(EMAIL_ADDRESS, data.getEmailAddress());
        assertEquals(PASSWORD, data.getPassword());
        assertEquals(SERVICE, data.getService());
        assertEquals(ADDITIONAL_DATA, data.getAdditionalData());
    }

    /**
     * Ensure that the method 'asLinkedHashMap' returns all the fields of the Data object
     */
    @Test
    void asLinkedHashMap(){
        Data data = new Data(ID, USERNAME, EMAIL_ADDRESS, PASSWORD, SERVICE, ADDITIONAL_DATA);
        LinkedHashMap<Data.Fields, String> userData = data.asLinkedHashMap();

        assertEquals(ID, Integer.parseInt(userData.get(Data.Fields.ID)));
        assertEquals(USERNAME, userData.get(Data.Fields.USERNAME));
        assertEquals(EMAIL_ADDRESS, userData.get(Data.Fields.EMAIL_ADDRESS));
        assertEquals(PASSWORD, userData.get(Data.Fields.PASSWORD));
        assertEquals(SERVICE, userData.get(Data.Fields.SERVICE));
        assertEquals(ADDITIONAL_DATA, userData.get(Data.Fields.ADDITIONAL_DATA));
    }

}
