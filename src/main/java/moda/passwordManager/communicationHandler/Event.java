package moda.passwordManager.communicationHandler;

import java.util.ArrayList;

public class Event {

    private final String NAME;  // The name of the event which specifies which operation to perform
    private ArrayList data;  // The data that is communicated with the other thread

    public Event(String name, ArrayList data){
        this.NAME = name;
        this.data = new ArrayList(data);
    }

    public String getNAME() {
        return NAME;
    }

    public ArrayList getData() {
        return data;
    }
}
