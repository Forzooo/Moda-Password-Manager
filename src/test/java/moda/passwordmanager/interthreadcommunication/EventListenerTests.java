package moda.passwordmanager.interthreadcommunication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventListenerTests {

    private static final String OPERATION = "Test";
    private static final String OPERATION_2 = "Test_2";
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
        this.eventListener.addOperation(OPERATION, ()->{});  // Add the operation to the handler map

        // Create and make a request to the event listener
        Event event = new Event(OPERATION);
        Event response = this.sender.request(event);

        assertEquals(OPERATION, response.getOperation());
    }

    /**
     * Test that the EventListener handles the locked operation only when it becomes unlocked
     */
    @Test
    void lockedOperation(){
        // Add the locked operation to the handler map
        this.eventListener.addLockedOperation(OPERATION, () -> {});

        // Send the event with the locked operation
        Event event = new Event(OPERATION);
        this.sender.send(event);

        // Unlock the operation and wait for a response
        this.eventListener.unlockOperation(OPERATION);
        Event response = this.sender.receive();

        assertEquals(OPERATION, response.getOperation());
    }

    /**
     * Ensure that the EventListener discards any event that has the sequence number equal or greater than 2
     */
    @Test
    void discardEvent(){
        // Add the operations to the handler map
        this.eventListener.addOperation(OPERATION, () -> {});
        this.eventListener.addOperation(OPERATION_2, () -> {});

        // The event which has to be discarded from the EventListener, thus we need to increment the sequence number
        // two times
        Event eventToDiscard = new Event(OPERATION);
        eventToDiscard.incrementSequenceNumber();
        eventToDiscard.incrementSequenceNumber();

        Event eventToReceive = new Event(OPERATION_2);

        this.sender.send(eventToDiscard);
        this.sender.send(eventToReceive);

        Event eventListenerResponse = this.sender.receive();
        assertEquals(OPERATION_2, eventListenerResponse.getOperation());
    }

    /**
     * Ensure that readding the same operation to the event listener, raises an OverriddenOperationException
     */
    @Test
    void raiseOverriddenOperationException(){
        boolean exceptionRaised = false;

        this.eventListener.addOperation(OPERATION, () -> {});

        try{
            this.eventListener.addOperation(OPERATION, () -> {});
        }catch (OverriddenOperationException e){
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }

    /**
     * Ensure that when the handleExceptionRaised method is called, it sends an empty Event
     */
    @Test
    void handleExceptionRaised(){
        this.eventListener.addOperation(OPERATION, () -> {});

        // We need to handle a synchronous event first, otherwise the handleExceptionRaised won't have a sequence number
        this.sender.request(new Event(OPERATION));
        this.eventListener.handleExceptionRaised();

        Event response = this.sender.receive();

        assertEquals("", response.getOperation());
    }
}
