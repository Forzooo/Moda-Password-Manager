package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;

public class Event {

    /**
     * The operation that is request to be performed
     */
    private final String OPERATION;
    private ArrayList<Object> data;  // The data that is communicated

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
        this.OPERATION = operation;
        this.data = new ArrayList<>();
        this.communicationID = -1;
        this.sequenceNumber = 0;
    }

    public Event(String operation, Object data){
        this.OPERATION = operation;
        this.data = new ArrayList<>();
        this.data.add(data);
        this.communicationID = -1;
        this.sequenceNumber = 0;
    }

    public Event(String operation, ArrayList<Object> data){
        this.OPERATION = operation;
        this.data = new ArrayList<>(data);
        this.communicationID = -1;
        this.sequenceNumber = 0;
    }

    public void addData(Object data){
        this.data.add(data);
    }

    public String getOperation() {
        return OPERATION;
    }

    public ArrayList<Object> getData() {
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
