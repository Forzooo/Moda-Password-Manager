package moda.passwordmanager.frontend;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.panels.ShowDataPanel;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Map;

public class FrontendEventListener extends Thread {

    private InterThreadCommunication itc;
    private boolean runFlag;  // Flag used to indicate when the thread has to stop
    private Event eventToSend;  // The event that is sent to the Backend
    private ShowDataPanel showDataPanel;  // The Listener needs the Show Data Panel to call the service fields

    public FrontendEventListener(InterThreadCommunication itc, ShowDataPanel showDataPanel){
        this.itc = itc;
        this.runFlag = true;
        this.showDataPanel = showDataPanel;
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
        ArrayList<Object> eventData = event.getData();

        switch (event.getNAME()){
            case "exception-raised":
                exceptionRaised((String) eventData.getFirst());

            case "update-service-fields":
                updateServiceFields((ArrayList<Data>) eventData.getFirst());

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

    /**
     * Update the service data with the new one
     * @param backendData The updated service data from the backend
     */
    private void updateServiceFields(ArrayList<Data> backendData){
        // Get the User Data and its model to update them with the changes
        ArrayList<Data> userData = this.showDataPanel.getUserData();
        DefaultListModel<String> userDataModel = this.showDataPanel.getUserDataModel();

        for (Data data : backendData){
            // Retrieve the indexes of the Data objects that have the same ID
            int userDataIndex = this.showDataPanel.indexOfUserData(data.getID());

            // If the ID has not been found, then add the Data object
            if (userDataIndex == -1){
                userData.add(data);
                userDataModel.addElement(data.getSERVICE());
            }else if (data.getSERVICE().isEmpty()){
            // If the service field is empty that means the record has been deleted, and it has to be removed from the list
                userData.remove(userDataIndex);
                userDataModel.remove(userDataIndex);
            }else{  // Otherwise update the current data where the index is the same for the data and the data model
                userData.set(userDataIndex, data);
                userDataModel.set(userDataIndex, data.getSERVICE());
            }
        }
    }

}
