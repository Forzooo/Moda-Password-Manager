package moda.passwordmanager;

import com.formdev.flatlaf.FlatLightLaf;
import moda.passwordmanager.backend.Backend;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;


public class Application extends JFrame {

    private final static String VERSION = "1.0.0";  // The current version of the software

    public Application(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                       String databaseToUse){
        initFlatLaf();  // It has to be called before any Swing component
        initUI(backendQueue, frontendQueue, databaseToUse);
    }

    /**
     * Apply the FlatLaf look and feel to the UI
     */
    private void initFlatLaf(){
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    private void initUI(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                        String databaseToUse){
        Dimension screen = getToolkit().getScreenSize();

        int width = (int) (screen.getWidth() * 4/5);
        int height = (int) (screen.getHeight() * 4/5);

        setTitle(getApplicationTitle());
        setSize(width, height);

        setIconImage(getIcon());  // Get the icon and set it

        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new Frontend(backendQueue, frontendQueue, databaseToUse, width, height));
        pack();
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

    /**
     * Retrieve the icon of the password manager from the resources folder
     * @return Icon of the password manager
     */
    public static Image getIcon(){
        // Get the image from the resources
        ImageIcon imageIcon = new ImageIcon(Application.class.getResource("/icon.png"));
        return imageIcon.getImage();
    }

    /**
     * Retrieve the title of the application
     */
    public static String getApplicationTitle(){
        return "MODA - Password Manager";
    }

    /**
     * Retrieve the current version of the application
     */
    public static String getVersion(){
        return VERSION;
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