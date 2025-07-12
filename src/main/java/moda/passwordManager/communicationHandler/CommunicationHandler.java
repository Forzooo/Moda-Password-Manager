package moda.passwordManager.communicationHandler;

import java.util.concurrent.LinkedBlockingQueue;

public class CommunicationHandler {

    private LinkedBlockingQueue<Event> sender;  // Send Event from this queue to the other thread
    private LinkedBlockingQueue<Event> receiver;  // Receive Event from this queue from the other thread

    public CommunicationHandler(LinkedBlockingQueue<Event> sender, LinkedBlockingQueue<Event> receiver){
        super();

        this.sender = sender;
        this.receiver = receiver;
    }

    /**
     * Send an Event to the other thread. <br/>
     */
    public void send(Event event) {
        try {
            this.sender.put(event);  // Put the event in the queue
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receive an Event from the other thread.
     */
    public Event receive() {
        try {
            return this.receiver.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
