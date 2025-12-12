package moda.passwordmanager.frontend;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.panels.ShowData;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.EventListener;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.util.ArrayList;

public class FrontendEventListener extends EventListener {

    private InterThreadCommunication itc;
    private boolean runFlag;  // Flag used to indicate when the thread has to stop
    private Event eventToSend;  // The event that is sent to the Backend
    private ShowData showData;  // The Listener needs the Show Data Panel to call the service fields

    public FrontendEventListener(InterThreadCommunication itc, ShowData showData){
        super(itc, "Frontend Event Listener");  // Set the name of the thread for debug purposes
        this.itc = itc;
        this.runFlag = true;
        this.showData = showData;

        initHandler();  // Initialize all the operations to handle
    }

    /**
     * Add all the operations to handle
     */
    private void initHandler(){
        addOperation("exception-raised", this::exceptionRaised);
        addOperation("update-service-fields", this::updateServiceFields);
        addOperation("reset-service-fields", this::resetServiceFields);
    }

    /**
     * Retrieve an exception raised in the backend and show it with a MessageBox in the EDT Thread before closing
     * the connection and exiting
     */
    private void exceptionRaised(){
        // Retrieve the data from the request
        ArrayList<Object> requestData = getRequestData();
        String threadName = (String) requestData.getFirst();  // The name of the thread where the exception occurred
        Throwable throwable = (Throwable) requestData.get(1);  // The stack trace of the exception

        String stackTrace = Frontend.getStackTrace(throwable);  // Get the full stack trace of the throwable
        String message = Frontend.getLastStackTrace(stackTrace, 5) +
                "\r\nThe traceback has been saved to the data folder.";

        // Show the exception as a Message Dialog with the type of error message
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message,
                    "The following exception occurred in the " +  threadName + "thread", JOptionPane.ERROR_MESSAGE);

            closeConnection();  // The "close-connection" event must be sent after the JOptionPane has been closed
        });
    }

    /**
     * Update the service data with the new one
     */
    private void updateServiceFields(){
        // The updated service data from the backend
        ArrayList<Data> backendData = (ArrayList<Data>) getRequestData().getFirst();

        // Get the User Data and its model to update them with the changes
        ArrayList<Data> userData = this.showData.getUserData();
        DefaultListModel<String> userDataModel = this.showData.getUserDataModel();

        for (Data data : backendData){
            // Retrieve the indexes of the Data objects that have the same ID
            int userDataIndex = this.showData.indexOfUserData(data.getID());

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

    /**
     * Reset the service data
     */
    private void resetServiceFields(){
        this.showData.getUserData().clear();
        this.showData.getUserDataModel().clear();
    }

}
