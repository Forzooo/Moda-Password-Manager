package moda.passwordManager.communicationHandler;

import java.util.ArrayList;

public class Event {

    private String name;  // The name of the event which specifies which operation to perform
    private ArrayList data;  // The data that is communicated with the other thread

    public Event(String name, ArrayList data){
        this.name = name;
        this.data = new ArrayList(data);
    }

    public String getName() {
        return name;
    }

    public ArrayList getData() {
        return data;
    }
}
