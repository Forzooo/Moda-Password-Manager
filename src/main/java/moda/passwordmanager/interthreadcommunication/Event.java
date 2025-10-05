package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;

public class Event {

    private final String NAME;  // The name of the event which specifies which operation to perform
    private ArrayList<Object> data;  // The data that is communicated with the other thread
    private int id;  // The ID, used to identify the Event communication, is unique and randomly generated

    /**
     * Define whether the Event is a High priority one: the thread that started the communication is the one that
     * needs to receive the reply
     */

    public Event(String name){
        this.NAME = name;
        this.data = new ArrayList<>();
        this.id = -1;
    }

    public Event(String name, Object data){
        this.NAME = name;
        this.data = new ArrayList<>();
        this.data.add(data);
        this.id = -1;
    }

    public Event(String name, ArrayList<Object> data){
        this.NAME = name;
        this.data = new ArrayList<>(data);
        this.id = -1;
    }

    public void addData(Object data){
        this.data.add(data);
    }

    public String getNAME() {
        return NAME;
    }

    public ArrayList<Object> getData() {
        return data;
    }

    /**
     * The ID can be set only the first time, and is considered to be a constant after
     */
    public void setId(int id){
        if (this.id == -1){
            this.id = id;
        }
    }

    public int getId() {
        return id;
    }
}
