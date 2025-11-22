package moda.passwordmanager.interthreadcommunication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventTests {

    private final static int EVENT_ID = 1;

    /**
     * Ensure that the ID of an event if it's not set, is equal to -1
     */
    @Test
    void defaultEventID(){
        Event event = new Event("Test");
        assertEquals(-1, event.getId());
    }

    /**
     * Ensure that setting the ID of an event works
     */
    @Test
    void setEventID(){
        Event event = new Event("Test");
        event.setId(EVENT_ID);
        assertEquals(EVENT_ID, event.getId());
    }

    /**
     * Ensure that the value of the ID of an Event cannot be changed
     */
    @Test
    void modifyEventID(){
        Event event = new Event("Test");
        event.setId(EVENT_ID);
        event.setId(EVENT_ID+1);
        assertEquals(EVENT_ID, event.getId());
    }

}
