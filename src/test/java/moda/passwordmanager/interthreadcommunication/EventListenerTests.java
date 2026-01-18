package moda.passwordmanager.interthreadcommunication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventListenerTests {

    private final static String EVENT_NAME = "Test";
    private final static String EVENT_NAME_2 = "Test_2";
    private InterThreadCommunication sender;
    private EventListener eventListener;

    /**
     * Before each test initialize the EventListener and the ITC objects
     */
    @BeforeEach
    void init(){
        LinkedBlockingQueue<Event> queue = InterThreadCommunication.createQueue();
        LinkedBlockingQueue<Event> queue2 = InterThreadCommunication.createQueue();

        this.sender = new InterThreadCommunication(queue, queue2);

        // As we're testing an abstract class, we need to create an anonymous class that inherits the abstract one
        this.eventListener = new EventListener(new InterThreadCommunication(queue2, queue)){};
        this.eventListener.start();
    }

    /**
     * Test that the EventListener handles and returns a response to a generic request
     */
    @Test
    void handleRequest(){
        this.eventListener.addOperation(EVENT_NAME, ()->{});  // Add the operation to the handler map

        // Create and make a request to the event listener
        Event event = new Event(EVENT_NAME);
        Event response = this.sender.request(event);

        assertEquals(EVENT_NAME, response.getOperation());
    }

    /**
     * Ensure that the EventListener discards any event that has the sequence number equal or greater than 2
     */
    @Test
    void discardEvent(){
        // Add the operations to the handler map
        this.eventListener.addOperation(EVENT_NAME, () -> {});
        this.eventListener.addOperation(EVENT_NAME_2, () -> {});

        // The event which has to be discarded from the EventListener, thus we need to increment the sequence number
        // two times
        Event eventToDiscard = new Event(EVENT_NAME);
        eventToDiscard.incrementSequenceNumber();
        eventToDiscard.incrementSequenceNumber();

        Event eventToReceive = new Event(EVENT_NAME_2);

        this.sender.send(eventToDiscard);
        this.sender.send(eventToReceive);

        Event eventListenerResponse = this.sender.receive();
        assertEquals(EVENT_NAME_2, eventListenerResponse.getOperation());
    }

    /**
     * Ensure that readding the same operation to the event listener, raises an OverriddenOperationException
     */
    @Test
    void raiseOverriddenOperationException(){
        boolean exceptionRaised = false;

        this.eventListener.addOperation(EVENT_NAME, () -> {});

        try{
            this.eventListener.addOperation(EVENT_NAME, () -> {});
        }catch (OverriddenOperationException e){
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }

}
