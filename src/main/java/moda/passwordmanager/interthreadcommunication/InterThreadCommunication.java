package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class InterThreadCommunication {

    private LinkedBlockingQueue<Event> sender;  // Send Event from this queue to the other thread
    private LinkedBlockingQueue<Event> receiver;  // Receive Event from this queue from the other thread

    private ArrayList<Integer> activeIDs;  // This ArrayList is used to ensure that the ID generated must be unique

    // Collection of semaphores used to set the threads to wait for non-high priority events
    private Semaphore nonHighPriority;

    // Semaphores used to set the threads to wait
    private Semaphore highPrioritySemaphore;
    private final static int SEMAPHORE_PERMITS = 0;

    /**
     * The Events that have an high priority, that means the same thread needs the response from the event it has send,
     * have their ID number stored here to filter them.
     */
    private ArrayList<Integer> highPriorityEvents;

    /**
     * @param sender The Queue that sends the Event objects
     * @param receiver The Queue that receives the Event objects
     */
    public InterThreadCommunication(LinkedBlockingQueue<Event> sender, LinkedBlockingQueue<Event> receiver){
        this.sender = sender;
        this.receiver = receiver;

        this.activeIDs = new ArrayList<>();
        this.highPriorityEvents = new ArrayList<>();

        this.nonHighPriority = new Semaphore(SEMAPHORE_PERMITS);
        this.highPrioritySemaphore = new Semaphore(SEMAPHORE_PERMITS);
    }

    /**
     * Send an event request to the other Queue.
     */
    public void request(Event event) {
        try {
            addEventID(event);
            this.sender.put(event);  // Put the event in the queue to send it to the other thread
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reply to a request of the other queue
     * @param request The request made by the other queue: its ID is required to use the same communication
     * @param response The response to the request
     */
    public void reply(Event request, Event response){
        try{
            addEventID(response, request.getId());
            this.sender.put(response);  // Put the event in the queue to send it to the other thread
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the ID of an event that has to be sent
     */
    private void addEventID(Event event){
        int id = generateID();  // Generate a random unique ID
        this.activeIDs.add(id);  // Add the ID to the active ones
        event.setId(id);  // Set the generated ID
    }

    /**
     * Set the ID of an event that has to be sent as a reply
     * @param id The ID of the event that request a response
     */
    private void addEventID(Event event, int id){
        this.activeIDs.add(id);  // Add the ID to the active ones
        event.setId(id);  // Set the ID to be the same as the request one
    }

    /**
     * Generate a random ID for an Event ensuring that it's unique
     * @return Generated ID
     */
    private int generateID(){
        Random random = new Random();  // We can use a non-secure Random as we're not dealing with high sensitive data
        int id = random.nextInt(0, Integer.MAX_VALUE);

        // Re-generate the ID if it's the same value as one inside the ActiveIDs ArrayList
        while (this.activeIDs.contains(id)){
            id = random.nextInt(0, Integer.MAX_VALUE);
        }
        return id;
    }

    /**
     * Receive an Event from the other Queue.
     * @return The event sent
     */
    public Event receive() {
        try {
            Event event = this.receiver.take();  // Wait for an event sent from the receiver queue

            int eventID = event.getId();  // Retrieve the ID of the event

            // If the event sent is a response, remove its ID from the active ones
            if (this.activeIDs.contains(eventID)){
                this.activeIDs.remove((Integer) eventID);
            }

            // Check whether the event is a high priority one
            if (this.highPriorityEvents.contains(eventID)){
                this.receiver.put(event);  // Put the event in the response queue to be found by other threads
                waitNonHighPriority();  // Let the thread wait until the high priority event is removed from the queue
                return receive();  // Recall the receive method until it founds a non-high priority event to return
            }else{  // If the event is not a high priority one, then return it
                this.nonHighPriority.release();
                return event;
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receive a high priority event from the other queue, with a specific ID
     * @param priorityID The ID of the high priority event
     * @return The high priority event
     */
    private Event receive(int priorityID){
        try {
            Event event = this.receiver.take();  // Wait for an event sent from the receiver queue

            // Check whether the ID of the event is the high priority one we want
            if (event.getId() == priorityID){
                this.highPrioritySemaphore.release();
                this.highPriorityEvents.remove((Integer) priorityID);  // Remove the ID from the high priority events
                this.activeIDs.remove((Integer) priorityID);  // Remove the ID from the active IDs
                return event;
            }else{
                this.receiver.put(event);  // Put the event in the response queue to be found by other threads
                waitHighPriority();  // Wait until an high priority event is the first in the queue
                return receive(priorityID);  // Recall the method until the request event is found
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Send an Event to the other Queue and wait for its response with priority over other threads that are waiting
     * for responses
     * @param event Event to send
     * @return The response's event
     */
    public Event requestAndReceive(Event event){
        request(event);
        addHighPriority(event.getId());
        return receive(event.getId());
    }

    /**
     * Set the thread in a waiting condition until the high priority event is removed from the queue
     */
    private void waitNonHighPriority(){
        try {
            this.highPrioritySemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.highPrioritySemaphore = new Semaphore(SEMAPHORE_PERMITS);
    }

    /**
     * Set the thread in a waiting condition until the high priority event is removed from the queue
     */
    private void waitHighPriority(){
        try {
            this.nonHighPriority.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.nonHighPriority = new Semaphore(SEMAPHORE_PERMITS);
    }

    /**
     * Add a high priority event to the list
     * @param ID The ID of the event
     */
    private void addHighPriority(int ID){
        this.highPriorityEvents.add(ID);
    }

}
