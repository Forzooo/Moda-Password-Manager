package moda.passwordmanager.frontend;

import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import java.util.ArrayList;

public class FrontendEventListener extends Thread {

    private InterThreadCommunication itc;
    private boolean runFlag;  // Flag used to indicate when the thread has to stop
    private Event eventToSend;  // The event that is sent to the Backend

    public FrontendEventListener(InterThreadCommunication itc){
        this.itc = itc;
        this.runFlag = true;

    }

    @Override
    public void run() {
        super.run();

        while (this.runFlag){
            Event event = this.itc.receive();  // Wait for an event from the Backend

            createEvent(event);  // Create an event to send to the Backend
            handleEvent(event);  // Handle the operation requested from the Backend

            // Check whether there is an Event to send to the Backend
            if (this.eventToSend != null){
                this.itc.reply(event, this.eventToSend);
                resetSendData();  // Reset the data to send to the Backend
            }
        }
    }

    /**
     * Create the event that will be sent to the Frontend
     * @param event
     */
    private void createEvent(Event event){
        this.eventToSend = new Event(event.getNAME());
    }

    private void handleEvent(Event event){
        ArrayList<Object> eventData = new ArrayList<>();

        switch (event.getNAME()){
            case "exception-raised":
                exceptionRaised((String) eventData.getFirst());

            default:
                this.eventToSend = null;
        }
    }

    private void resetSendData(){
        this.eventToSend = null;
    }

    /**
     * Retrieve an exception raised in the backend and show it with a MessageBox in the EDT Thread before closing
     * the connection and exiting
     * @param exception
     */
    private void exceptionRaised(String exception){
        // Show the exception as a Message Dialog with the type of error message
//        JOptionPane.showMessageDialog(this, exception, "An exception occurred in the Backend",
//                JOptionPane.ERROR_MESSAGE);

        System.out.println(exception);

        // Send a "close-connection" event to the backend to tell it to stop its execution
        Event closeConnection = new Event("close-connection");

        // Check that the backend has confirmed the connection to be closed and stop the execution
        if (this.itc.requestAndReceive(closeConnection).getNAME().equals("close-connection")){
            System.exit(0);
        }
    }
}
