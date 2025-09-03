package moda.passwordManager.communicationHandler;

import java.util.ArrayList;

public class Event {

    private final String NAME;  // The name of the event which specifies which operation to perform
    private ArrayList<Object> data;  // The data that is communicated with the other thread

    public Event(String name){
        this.NAME = name;
        this.data = new ArrayList<>();
    }

    public Event(String name, Object data){
        this.NAME = name;
        this.data = new ArrayList<>();
        this.data.add(data);
    }

    public Event(String name, ArrayList<Object> data){
        this.NAME = name;
        this.data = new ArrayList<>(data);
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
}
