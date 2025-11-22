package moda.passwordmanager.interthreadcommunication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;


class InterThreadCommunicationTests {

    private final static String EVENT_NAME = "Test";
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
        assertEquals(EVENT_NAME, event.getNAME());
    }

    /**
     * Send a Test Event from another thread and receive it in this one, ensuring that it has the same name.
     */
    @Test
    void sendAndReceiveTwoThreads(){
        Thread sender = new Thread(() -> this.sender.send(new Event(EVENT_NAME)));
        sender.start();
        Event event = this.receiver.receive();

        assertEquals(EVENT_NAME, event.getNAME());
    }

    /**
     * Perform an ITC request using two threads ensuring that it returns the same event name
     */
    @Test
    void requestTwoThreads(){
        Thread receiverThread = new Thread(() -> {
            Event request = this.receiver.receive();
            this.receiver.makeResponse(request, new Event(request.getNAME()));
        });
        receiverThread.start();
        Event response = this.sender.request(new Event(EVENT_NAME));

        assertEquals(EVENT_NAME, response.getNAME());
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
            this.receiver.makeResponse(request, new Event(request.getNAME()));
            this.receiver.send(new Event(request.getNAME()+"-Asynchronous"));
        });
        receiverThread.start();

        // The sender thread is used to prove that a synchronous event is returned to the requester instead of the
        // first thread listening for responses
        Thread senderThread = new Thread(() -> {
            Event response = this.sender.receive();

            // Assert that the event received is not the synchronous one
            assertNotEquals(EVENT_NAME, response.getNAME());
        });
        senderThread.start();

        Event request = this.sender.request(new Event(EVENT_NAME));
        try {
            senderThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(EVENT_NAME, request.getNAME());
    }

}
