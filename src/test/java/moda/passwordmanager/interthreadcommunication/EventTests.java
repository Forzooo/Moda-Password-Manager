package moda.passwordmanager.interthreadcommunication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTests {

    private final static String OPERATION = "Test";
    private final static int COMMUNICATION_ID = 1;
    private final static int EVENT_COMMUNICATION_ID_NOT_SET = -1;
    private final static int EXPECTED_SEQUENCE_NUMBER = 1;
    private Event event;

    /**
     * Initialize the Event object before each test
     */
    @BeforeEach
    void init(){
        this.event = new Event(OPERATION);
    }

    /**
     * Ensure that the communication ID of an event if it's not set, is equal to -1
     */
    @Test
    void defaultCommunicationID(){
        assertEquals(-1, this.event.getCommunicationID());
    }

    /**
     * Ensure that setting the communication ID of an event works
     */
    @Test
    void setCommunicationID(){
        this.event.setCommunicationID(COMMUNICATION_ID);
        assertEquals(COMMUNICATION_ID, this.event.getCommunicationID());
    }

    /**
     * Ensure that the value of the communication ID of an Event cannot be changed
     */
    @Test
    void modifyCommunicationID(){
        this.event.setCommunicationID(COMMUNICATION_ID);
        this.event.setCommunicationID(COMMUNICATION_ID+1);
        assertEquals(COMMUNICATION_ID, this.event.getCommunicationID());
    }

    /**
     * Ensure that when setting the communication ID, the isIdSet method returns true
     */
    @Test
    void isCommunicationIDSet(){
        this.event.setCommunicationID(COMMUNICATION_ID);
        assertTrue(this.event.isIdSet());
    }

    /**
     * Ensure that when the communication ID is not set yet, the isIdSet method returns false
     */
    @Test
    void isCommunicationIDSetFalse(){
        assertFalse(this.event.isIdSet());
    }


    /**
     * Ensure that the method 'incrementSequenceNumber' increments the sequence number of the event
     */
    @Test
    void incrementSequenceNumber(){
        this.event.incrementSequenceNumber();
        assertEquals(EXPECTED_SEQUENCE_NUMBER, this.event.getSequenceNumber());
    }

}
