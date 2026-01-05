package moda.passwordmanager.interthreadcommunication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;


class InterThreadCommunicationTests {

    private final static String EVENT_NAME = "Test";
    private final static int EVENT_ID = 1;
    private InterThreadCommunication sender;
    private InterThreadCommunication receiver;

    /**
     * Before each test initialize the sender and receive ITC objects
     */
    @BeforeEach
    void init(){
        LinkedBlockingQueue<Event> inputQueue = InterThreadCommunication.createQueue();
        LinkedBlockingQueue<Event> outputQueue = InterThreadCommunication.createQueue();
        this.sender = new InterThreadCommunication(inputQueue, outputQueue);
        this.receiver = new InterThreadCommunication(outputQueue, inputQueue);
    }

    /**
     * Send a Test Event to the receiver and ensure that it has received it, all of it in the same thread.
     */
    @Test
    void sendAndReceiveSameThread(){
        this.sender.send(new Event(EVENT_NAME));
        Event event = receiver.receive();
        assertEquals(EVENT_NAME, event.getOperation());
    }

    /**
     * Send a Test Event from another thread and receive it in this one, ensuring that it has the same name.
     */
    @Test
    void sendAndReceiveTwoThreads(){
        Thread sender = new Thread(() -> this.sender.send(new Event(EVENT_NAME)));
        sender.start();
        Event event = this.receiver.receive();

        assertEquals(EVENT_NAME, event.getOperation());
    }

    /**
     * Perform an ITC request using two threads ensuring that it returns the same event name
     */
    @Test
    void requestTwoThreads(){
        Thread receiverThread = new Thread(() -> {
            Event request = this.receiver.receive();
            Event response = this.receiver.makeResponse(request, request.getOperation());
            this.receiver.send(response);
        });
        receiverThread.start();
        Event response = this.sender.request(new Event(EVENT_NAME));

        assertEquals(EVENT_NAME, response.getOperation());
    }

    /**
     * Ensure that the synchronized event is returned to the same thread even if there's already other threads
     * listening for events
     */
    @Test
    void requestProblemThreeThreads(){
        // Send the response to the request and send another event for the senderThread
        Thread receiverThread = new Thread(() -> {
            Event request = this.receiver.receive();
            Event synchronous = this.receiver.makeResponse(request, request.getOperation());
            this.receiver.send(synchronous);
            this.receiver.send(new Event(request.getOperation()+"-Asynchronous"));
        });
        receiverThread.start();

        // The sender thread is used to prove that a synchronous event is returned to the requester instead of the
        // first thread listening for responses
        Thread senderThread = new Thread(() -> {
            Event response = this.sender.receive();

            // Assert that the event received is not the synchronous one
            assertNotEquals(EVENT_NAME, response.getOperation());
        });
        senderThread.start();

        Event request = this.sender.request(new Event(EVENT_NAME));
        try {
            senderThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(EVENT_NAME, request.getOperation());
    }

    /**
     * Ensure that makeResponse method returns an event with the same ID as the request one
     */
    @Test
    void makeResponse(){
        Event request = new Event(EVENT_NAME);
        request.setCommunicationID(EVENT_ID);  // Set the ID of the event

        // Make the response and assert the ID
        Event response = this.sender.makeResponse(request, request.getOperation());
        assertEquals(EVENT_ID,response.getCommunicationID());
    }

}
