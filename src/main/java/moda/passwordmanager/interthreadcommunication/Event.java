package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;

public class Event {

    private final String NAME;  // The name of the event which specifies which operation to perform
    private ArrayList<Object> data;  // The data that is communicated with the other thread
    private final EventType TYPE;  // Whether the Event is a request or a response

    public Event(String name, EventType type){
        this.NAME = name;
        this.data = new ArrayList<>();
        this.TYPE = type;
    }

    public Event(String name, Object data, EventType type){
        this.NAME = name;
        this.data = new ArrayList<>();
        this.data.add(data);
        this.TYPE = type;
    }

    public Event(String name, ArrayList<Object> data, EventType type){
        this.NAME = name;
        this.data = new ArrayList<>(data);
        this.TYPE = type;
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

    public EventType getTYPE() {
        return TYPE;
    }
}
