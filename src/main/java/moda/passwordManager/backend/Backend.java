package moda.passwordManager.backend;

public class Backend extends Thread {

    // Objects of the backend classes
    private Cryptography cryptography;
    private Database database;
    private GoogleDrive googleDrive;

    // The CommunicationHandler object used to communicate with the Frontend thread
    private CommunicationHandler communicationHandler;

    // The Master Password used for encryption purposes
    private byte[] masterPassword;  // TODO: Check for any security issues involving RAM

    public Backend(){

    }

    @Override
    public void run() {
        super.run();

    }
}
