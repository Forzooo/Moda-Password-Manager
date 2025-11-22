package moda.passwordmanager;

import moda.passwordmanager.backend.Backend;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;


public class Application extends JFrame {

    public Application(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                       String databaseToUse){
        initUI(backendQueue, frontendQueue, databaseToUse);
    }

    private void initUI(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                        String databaseToUse){
        Dimension screen = getToolkit().getScreenSize();

        int width = (int) (screen.getWidth() * 4/5);
        int height = (int) (screen.getHeight() * 4/5);

        add(new Frontend(backendQueue, frontendQueue, databaseToUse, width, height));
        pack();

        setTitle("MODA - Password Manager");
        setSize(width, height);

        setIconImage(Frontend.getIcon());  // Get the icon and set it

        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Parse the arguments and look for the path of a database to use
     * @param args The arguments
     * @return An empty string or the path of a database
     */
    private static String databaseParsing(String[] args){
        String databasePath = "";

        for (String arg : args){
            // The databases that the Password Manager use have the extension ".modb"
            if (arg.endsWith(".modb")){
                databasePath = arg;
                break;
            }
        }

        return databasePath;
    }

    public static void main(String[] args) {
        String databaseToUse = databaseParsing(args);  // Parse the args to look for a database to use

        // Create the two LinkedBlockingQueue objects here to pass them to the Backend and the Frontend
        LinkedBlockingQueue<Event> backendQueue = InterThreadCommunication.createQueue();
        LinkedBlockingQueue<Event> frontendQueue = InterThreadCommunication.createQueue();

        EventQueue.invokeLater(() -> {
            Application application = new Application(backendQueue, frontendQueue, databaseToUse);
            application.setVisible(true);
        });

        Backend backend = new Backend(backendQueue, frontendQueue);
        // Set the handler from the object itself otherwise it would use the one from the Frontend
        backend.setUncaughtExceptionHandler(backend::handleException);
        backend.start();
    }
}