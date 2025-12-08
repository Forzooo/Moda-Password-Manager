package moda.passwordmanager.interthreadcommunication;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The EventListener class provides the base operations for Events handling, that any EventListener class needs to
 * have.
 */
public abstract class EventListener extends Thread {

    private final InterThreadCommunication ITC;
    private boolean runFlag;  // Flag used to indicate when the thread has to stop

    /**
     * The request is used by higher level methods to retrieve the data associated with it, or its operation if it's
     * an ExceptionHandler
     */
    private Event request;
    private Event response;  // The event that is sent as a reply

    /**
     * The handlerMap maps the operations with a method reference and it's used by the handleRequest method
     */
    private HashMap<String, Runnable> handlerMap;

    public EventListener(InterThreadCommunication itc){
        super();
        this.ITC = itc;
        this.runFlag = true;
        this.handlerMap = new HashMap<>();
    }

    public EventListener(InterThreadCommunication itc, String threadName){
        super(threadName);  // Set the name of the thread for log purposes
        this.ITC = itc;
        this.runFlag = true;
        this.handlerMap = new HashMap<>();
    }

    @Override
    public void run() {
        super.run();

        while (this.runFlag){
            this.request = this.ITC.receive();  // Wait for a request

            makeResponse(this.request);  // Create the response to send to the other queue
            handleRequest(this.request);  //  Handle the operation requested

            // It can happen that the operation requested is not in the handling map, thus it is recognized as
            // an unknown event, and the data has already been reset
            if (this.response != null){
                this.ITC.send(this.response);  // Send the response to the other queue
                resetResponse();  // Reset the data to send for the next Event
            }
        }

    }

    /**
     * Close the connection and terminate the execution
     */
    protected void closeConnection(){
        this.runFlag = false;  // Terminate the execution of the thread
        Event closeConnection = new Event("close-connection");
        this.ITC.send(closeConnection);
        System.exit(0);
    }

    /**
     * Make the response to the event received
     */
    private void makeResponse(Event request){
         this.response = this.ITC.makeResponse(request, request.getOperation());
    }

    /**
     * Handle the operation requested
     */
    private void handleRequest(Event request){
        String operation = request.getOperation();  // Get the operation to perform
        if (this.handlerMap.containsKey(operation)){
            this.handlerMap.get(operation).run();  // Execute the method reference
        }else{
            resetResponse();  // The operation requested is unknown, thus the response is reset
        }
    }

    /**
     * Add an operation to the handler
     */
    protected void addOperation(String operation, Runnable method){
        this.handlerMap.put(operation, method);
    }

    /**
     * Reset the response
     */
    private void resetResponse(){
        this.response = null;
    }

    protected ArrayList<Object> getRequestData(){
        return this.request.getData();
    }

    /**
     * Add data to the response
     */
    protected void addResponseData(Object data){
        this.response.addData(data);
    }

    protected InterThreadCommunication getITC(){
        return this.ITC;
    }

    /**
     * If an exception is raised, and it needs to have the communication channel free, then send the last event received
     */
    protected void handleExceptionRaised(){
        // Create a dummy response used only to stop any synchronous event
        Event response = this.ITC.makeResponse(this.request, "");
        this.ITC.send(response);
    }

}
