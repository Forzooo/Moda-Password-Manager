package moda.passwordmanager.interthreadcommunication;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventListenerTests {

    private final static String EVENT_NAME = "Test";
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


}
