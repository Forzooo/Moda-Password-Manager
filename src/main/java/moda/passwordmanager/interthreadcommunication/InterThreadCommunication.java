package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class InterThreadCommunication {

    private final LinkedBlockingQueue<Event> INPUT_QUEUE;  // Receive Event from this queue from the other thread
    private final LinkedBlockingQueue<Event> OUTPUT_QUEUE;  // Send Event from this queue to the other thread

    private ArrayList<Integer> activeIDs;  // This ArrayList is used to ensure that the ID generated must be unique

    // Collection of semaphores used to set the threads to wait for a specific type of event
    private final static int SEMAPHORE_PERMITS = 0;
    private Semaphore asynchronousSemaphore;
    private Semaphore synchronousSemaphore;

    /**
     * The Events that are synchronous, that means the same thread needs the response from the event it has send,
     * have their ID number stored here to filter them.
     */
    private ArrayList<Integer> synchronousEvents;

    /**
     * @param inputQueue The Queue that receives the Event objects
     * @param outputQueue The Queue that sends the Event objects
     */
    public InterThreadCommunication(LinkedBlockingQueue<Event> inputQueue, LinkedBlockingQueue<Event> outputQueue){
        this.INPUT_QUEUE = inputQueue;
        this.OUTPUT_QUEUE = outputQueue;

        this.activeIDs = new ArrayList<>();
        this.synchronousEvents = new ArrayList<>();

        this.asynchronousSemaphore = new Semaphore(SEMAPHORE_PERMITS);
        this.synchronousSemaphore = new Semaphore(SEMAPHORE_PERMITS);
    }

    /**
     * Create and return a queue used for the ITC
     */
    public static LinkedBlockingQueue<Event> createQueue(){
        return new LinkedBlockingQueue<>();
    }

    /**
     * Send an event to the other Queue
     */
    public void send(Event event) {
        try {
            // Add an ID to the Event only if it hasn't one yet, as the event could be a response to a request
            if (!event.isIdSet()){
                addEventID(event);
            }
            event.incrementSequenceNumber();  // Increment the sequence number of the event
            this.OUTPUT_QUEUE.put(event);  // Put the event in the queue to send it to the other thread
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a synchronous request and wait for the response
     * @param request The event to send
     * @return The response to the request
     */
    public Event request(Event request){
        send(request);
        addSynchronousEvent(request.getCommunicationID());
        return receive(request.getCommunicationID());
    }

    /**
     * Receive an Event from the other Queue
     * @return The event sent
     */
    protected Event receive() {
        // The method has protected as visibility because if a thread, that does not inherit the EventListener class,
        // receives a synchronous event, it is unable to use the makeResponse method to reply to the request creating a
        // deadlock for the sender thread
        try {
            Event event = this.INPUT_QUEUE.take();  // Wait for an event sent from the receiver queue

            int eventID = event.getCommunicationID();  // Retrieve the ID of the event

            // If the event sent is a response, remove its ID from the active ones
            if (this.activeIDs.contains(eventID)){
                this.activeIDs.remove((Integer) eventID);
            }

            // Check whether the event is a synchronous one
            if (this.synchronousEvents.contains(eventID)){
                this.INPUT_QUEUE.put(event);  // Put the event in the input queue to let it be found by other threads
                waitAsynchronousEvent();  // Let the thread wait until the synchronous event is removed from the queue
                return receive();  // Recall the receive method until it founds an asynchronous event to return
            }else{  // If the event is not a synchronous one, then return it
                this.asynchronousSemaphore.release();
                return event;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receive a synchronous event from the other queue, with a specific ID
     * @param id The ID of the synchronous event
     * @return The synchronous event
     */
    private Event receive(int id){
        try {
            Event event = this.INPUT_QUEUE.take();  // Wait for an event sent from the receiver queue

            // Check whether the ID of the event is the one of the synchronous event we want
            if (event.getCommunicationID() == id){
                this.synchronousSemaphore.release();
                this.synchronousEvents.remove((Integer) id);  // Remove the ID from the synchronous events
                this.activeIDs.remove((Integer) id);  // Remove the ID from the active IDs
                return event;
            }else{
                this.INPUT_QUEUE.put(event);  // Put the event in the response queue to be found by other threads
                waitSynchronousEvent();  // Wait until a synchronous event is the first in the queue
                return receive(id);  // Recall the method until the request event is found
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Configure an Event as a response
     * @param request The request made by the other queue: its ID is required to use the same communication
     * @param eventOperation The name of the event that will be created
     */
    protected Event makeResponse(Event request, String eventOperation){
        Event response = new Event(eventOperation);  // Create the event response with the operation specified

        // The response needs to have the same sequence number as the request
        while (response.getSequenceNumber() != request.getSequenceNumber()){
            response.incrementSequenceNumber();
        }

        response.setCommunicationID(request.getCommunicationID());  // Set the ID to be the same as the request one

        return response;
    }

    /**
     * Set the ID of an event that has to be sent
     */
    private void addEventID(Event event){
        int id = generateID();  // Generate a random unique ID
        this.activeIDs.add(id);  // Add the ID to the active ones
        event.setCommunicationID(id);  // Set the generated ID
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
     * Set the thread in a waiting condition until the synchronous event is removed from the queue
     */
    private void waitAsynchronousEvent(){
        try {
            this.synchronousSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.synchronousSemaphore = new Semaphore(SEMAPHORE_PERMITS);
    }

    /**
     * Set the thread in a waiting condition until the asynchronous event is removed from the queue
     */
    private void waitSynchronousEvent(){
        try {
            this.asynchronousSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.asynchronousSemaphore = new Semaphore(SEMAPHORE_PERMITS);
    }

    /**
     * Add a synchronous event to the list
     * @param ID The ID of the event
     */
    private void addSynchronousEvent(int ID){
        this.synchronousEvents.add(ID);
    }

}
