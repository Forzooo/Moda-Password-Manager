package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class InterThreadCommunication {

    private LinkedBlockingQueue<Event> sender;  // Send Event from this queue to the other thread
    private LinkedBlockingQueue<Event> receiver;  // Receive Event from this queue from the other thread

    /**
     * The list where all the events not handled by the receiver are temporary stored, this because they have an
     * EventType different than the one expected to receive.
     */
    private ArrayList<Event> eventList;

    /**
     * @param sender The Queue that sends the Event objects
     * @param receiver The Queue that receives the Event objects
     */
    public InterThreadCommunication(LinkedBlockingQueue<Event> sender, LinkedBlockingQueue<Event> receiver){
        super();

        this.sender = sender;
        this.receiver = receiver;
        this.eventList = new ArrayList<>();
    }

    /**
     * Send an Event to the other thread.
     */
    public void send(Event event) {
        try {
            this.sender.put(event);  // Put the event in the queue to send it to the other thread
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receive an Event from the other thread.
     * @param type The type of event that we want to receive
     * @return The event read of the EventType specified
     */
    public Event receive(EventType type) {
        try {
            Event eventReceived = this.receiver.take();

            // Check whether the type of the event is the one requested and return the Event if it's the right one
            if (eventReceived.getTYPE().equals(type)){
                return eventReceived;
            }

            storeAndNotify(eventReceived);  // Store and notify the Event received

            return receive(type);  // Recursively call the receive method again until an Event of the requested EventType
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Store the Event read from the Queue inside the Event List and notify the other thread waiting that there's an
     * Event of its EventType
     * @param eventReceived The Event read from the Queue
     */
    private void storeAndNotify(Event eventReceived){
        this.eventList.add(eventReceived);  // Add the event received from the Queue

        try {
            // Create a notify Event to let the other thread waiting know that there is an Event of its EventType
            Event notify = new Event("notify-listeners", EventType.REQUEST);
            this.receiver.put(notify);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
