package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;
import java.util.List;

public class Event {

    /**
     * The operation that is request to be performed
     */
    private final String operation;
    private final ArrayList<Object> data;  // The data that is communicated

    /**
     * The ID identifies the Event communication, is unique and randomly generated and can only be accessed
     * by package classes
     */
    private int communicationID;

    /**
     * The sequence number identifies the current state of the communication, where 0 is the setup state of the Event
     * and is increased each time it is sent through the ITC class APIs
     */
    private int sequenceNumber;

    public Event(String operation){
        this.operation = operation;
        this.data = new ArrayList<>();
        this.communicationID = -1;
        this.sequenceNumber = 0;
    }

    public Event(String operation, Object data){
        this.operation = operation;
        this.data = new ArrayList<>();
        this.data.add(data);
        this.communicationID = -1;
        this.sequenceNumber = 0;
    }

    public Event(String operation, List<Object> data){
        this.operation = operation;
        this.data = new ArrayList<>(data);
        this.communicationID = -1;
        this.sequenceNumber = 0;
    }

    /**
     * Add an object to the data to sent with the Event
     */
    public void addData(Object data){
        this.data.add(data);
    }

    /**
     * Get the operation of the event
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Get the data of the event
     */
    public List<Object> getData() {
        return data;
    }

    /**
     * The ID can be set only the first time, and is considered to be a constant after
     */
    protected void setCommunicationID(int communicationID){
        if (this.communicationID == -1){
            this.communicationID = communicationID;
        }
    }

    protected int getCommunicationID() {
        return this.communicationID;
    }

    protected void incrementSequenceNumber(){
        this.sequenceNumber++;
    }

    protected int getSequenceNumber(){
        return this.sequenceNumber;
    }

    /**
     * Returns whether the ID has been set to a value different than -1
     */
    protected boolean isIdSet(){
        return this.communicationID != -1;
    }
}
