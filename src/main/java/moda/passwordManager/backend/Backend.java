package moda.passwordManager.backend;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Backend extends Thread {

    // Objects of the backend classes
    private Cryptography cryptography;
    private Database database;
    private GoogleDrive googleDrive;

    // The CommunicationHandler object used to communicate with the Frontend thread
    private CommunicationHandler communicationHandler;

    private byte[] masterPassword;  // The Master Password used for encryption purposes

    private Event eventToSend;  // The event that is sent to the frontend. Must be set using its setter
    private ArrayList dataToSend;  // The data that is added to the Event to send to the frontend

    public Backend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        // Create the communication handler with the two queues
        this.communicationHandler = new CommunicationHandler(backendQueue, frontendQueue);

        // Initialize all the backend components
        this.cryptography = new Cryptography();
        this.database = new Database();
        this.googleDrive = new GoogleDrive();

        this.eventToSend = null;
    }

    @Override
    public void run() {
        while (true){
            /**
             * If there's an even to send, send it
             * It does not make the thread to stop forever because if an event is read from the Frontend, it's
             * processed and the data will be sent in another event before checking for new events.
             * Lastly, Backend does not send Event on its own so waiting for events it's not a problem.
             */
            if (this.eventToSend != null){
                this.communicationHandler.send(this.eventToSend);
                resetSendData();  // Reset the data to send to the frontend
            }
            Event event = this.communicationHandler.receive();  // Wait for an event from the Frontend
            processEvent(event);  // Process the operation from th
            createResponse(event);
        }
    }

    public void setEventToSend(Event eventToSend){
        this.eventToSend = eventToSend;
    }

    /**
     * Reset the data to be sent after it has been sent to the frontend.
     */
    public void resetSendData(){
        this.eventToSend = null;
        this.dataToSend = new ArrayList();
    }

    public void processEvent(Event event){
        switch (event.getName()){
            case "set-master-password":
                // Retrieve the master password from the data sent
                setMasterPassword(event.getData().getFirst());
                break;

        }
    }

    public void createResponse(Event event){
        switch (event.getName()){
            case "set-master-password":
                // Send back a message to tell the master password has been set
                this.eventToSend = new Event("set-master-password-completed", this.dataToSend);
                break;
        }
    }

    /**
     * Internally set the master password. <br/>
     * Can be called only from event: "set-master-password"
     * @param masterPassword
     */
    private void setMasterPassword(Object masterPassword){
        this.masterPassword = (byte[]) masterPassword;
    }

}
