package moda.passwordmanager.interthreadcommunication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTests {

    private final static String EVENT_NAME = "Test";
    private final static int EVENT_ID = 1;
    private Event event;

    /**
     * Initialize the Event object before each test
     */
    @BeforeEach
    void init(){
        this.event = new Event(EVENT_NAME);
    }

    /**
     * Ensure that the ID of an event if it's not set, is equal to -1
     */
    @Test
    void defaultEventID(){
        assertEquals(-1, this.event.getId());
    }

    /**
     * Ensure that setting the ID of an event works
     */
    @Test
    void setEventID(){
        this.event.setId(EVENT_ID);
        assertEquals(EVENT_ID, this.event.getId());
    }

    /**
     * Ensure that the value of the ID of an Event cannot be changed
     */
    @Test
    void modifyEventID(){
        this.event.setId(EVENT_ID);
        this.event.setId(EVENT_ID+1);
        assertEquals(EVENT_ID, this.event.getId());
    }

    /**
     * Ensure that when setting an ID the isIdSet method returns true
     */
    @Test
    void isEventIdSet(){
        this.event.setId(EVENT_ID);
        assertTrue(this.event.isIdSet());
    }

    /**
     * Ensure that when the ID is not set yet, the isIdSet method returns false
     */
    @Test
    void isEventIdSetFalse(){
        assertFalse(this.event.isIdSet());
    }

}
