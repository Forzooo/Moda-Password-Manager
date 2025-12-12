package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;

public class Event {

    private final String OPERATION;  // The operation to perform
    private ArrayList<Object> data;  // The data that is communicated

    /**
     * The ID, used to identify the Event communication, is unique and randomly generated and can only be accessed
     * by package classes
     */
    private int id;

    public Event(String operation){
        this.OPERATION = operation;
        this.data = new ArrayList<>();
        this.id = -1;
    }

    public Event(String operation, Object data){
        this.OPERATION = operation;
        this.data = new ArrayList<>();
        this.data.add(data);
        this.id = -1;
    }

    public Event(String operation, ArrayList<Object> data){
        this.OPERATION = operation;
        this.data = new ArrayList<>(data);
        this.id = -1;
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
    protected void setId(int id){
        if (this.id == -1){
            this.id = id;
        }
    }

    protected int getId() {
        return this.id;
    }

    /**
     * Returns whether the ID has been set to a value different than -1
     */
    protected boolean isIdSet(){
        return this.id != -1;
    }
}
