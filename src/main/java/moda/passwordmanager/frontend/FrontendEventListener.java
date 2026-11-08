package moda.passwordmanager.frontend;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.backend.googledrive.ConflictData;
import moda.passwordmanager.frontend.dialogs.GoogleDriveSynchronization;
import moda.passwordmanager.frontend.panels.ShowData;
import moda.passwordmanager.interthreadcommunication.EventListener;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import raven.modal.Toast;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FrontendEventListener extends EventListener {

    private final ShowData showData;  // The EventListener needs the Show Data Panel to call the service fields

    public FrontendEventListener(InterThreadCommunication itc, ShowData showData){
        super(itc, "Frontend Event Listener");  // Set the name of the thread for debug purposes
        this.showData = showData;

        initHandler();  // Initialize all the operations to handle
    }

    /**
     * Add all the operations to handle
     */
    private void initHandler(){
        addOperation("check-new-version", this::checkNewVersion);
        addOperation("exception-raised", this::exceptionRaised);
        addOperation("google-drive-synchronization-conflicts", this::openGoogleDriveSynchronizationDialog);
        addOperation("show-toast", this::showToast);
        addOperation("update-service-fields", this::updateServiceFields);
    }

    /**
     * Let the user know the result of a version check
     */
    private void checkNewVersion(){
        // If a new version exists, we show the version number too
        if ((boolean) getRequestData().getFirst()) {
            SwingUtilities.invokeLater(() -> Utilities.showToast(this.showData.getParent(), Toast.Type.INFO,
                    Utilities.getLocaleString("Moda.Toast.checkNewVersionAvailable") + " "
                            + getRequestData().get(1)));
        }else{
            SwingUtilities.invokeLater(() -> Utilities.showToast(this.showData.getParent(), Toast.Type.INFO,
                    Utilities.getLocaleString("Moda.Toast.checkNewVersionUnavailable")));
        }
    }

    /**
     * Retrieve an exception raised in the backend and show it with a MessageBox in the EDT Thread before closing
     * the connection and exiting
     */
    private void exceptionRaised(){
        // Retrieve the data from the request
        List<Object> requestData = getRequestData();
        String threadName = (String) requestData.getFirst();  // The name of the thread where the exception occurred
        Throwable throwable = (Throwable) requestData.get(1);  // The stack trace of the exception

        String stackTrace = Utilities.getStackTrace(throwable);  // Get the full stack trace of the throwable
        String message = Utilities.getStackTraceRows(stackTrace, 5) +
                Utilities.getLocaleString("Moda.FrontendEventListener.exceptionRaisedMessage");

        // Show the exception as a Message Dialog with the type of error message
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message,
                    Utilities.getLocaleString("Moda.FrontendEventListener.exceptionRaisedTitle") + " " +
                    threadName, JOptionPane.ERROR_MESSAGE);

            closeConnection();  // The "close-connection" event must be sent after the JOptionPane has been closed
            System.exit(0);  // Terminate the execution of the software
        });
    }

    /**
     * Open the Google Drive Synchronization dialog with the conflicted data
     */
    private void openGoogleDriveSynchronizationDialog(){
        ArrayList<ConflictData> conflictData = (ArrayList<ConflictData>) getRequestData().getFirst();

        SwingUtilities.invokeLater(() -> {
            // To get the Frontend frame we have to use the getFrames method of the Frame class as we don't have a
            // reference for it
            // Also, it is always the first index as there are no other JFrame
            GoogleDriveSynchronization googleDriveSynchronization = new GoogleDriveSynchronization(java.awt.Frame.getFrames()[0],
                    getItc(), conflictData);
            googleDriveSynchronization.setVisible(true);
        });
    }

    /**
     * Show the Toast that the Backend request to display
     */
    private void showToast(){
        String localeString = (String) getRequestData().getFirst();
        Toast.Type type = (Toast.Type) getRequestData().get(1);

        // We need to localize the string as the Backend returns its locale value to avoid having to call the Frontend
        // Utilities class
        Utilities.showToast(this.showData.getRootPane(), type, Utilities.getLocaleString(localeString));
    }

    /**
     * Update the service data with the new one
     */
    private void updateServiceFields(){
        // The updated service data from the backend
        ArrayList<Data> backendData = (ArrayList<Data>) getRequestData().getFirst();

        for (Data data : backendData){
            // If the service field is empty that means the record has been deleted, and it has to be removed from the list
            if (data.getService().isBlank()){
                this.showData.removeData(data);
            }else{  // Otherwise add/update the Map object with the ID and its service
                this.showData.addData(data);
            }
        }
    }
}