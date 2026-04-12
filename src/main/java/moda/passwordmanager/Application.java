package moda.passwordmanager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import moda.passwordmanager.backend.Backend;
import moda.passwordmanager.frontend.properties.Language;
import moda.passwordmanager.frontend.properties.Themes;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;


public class Application extends JFrame {

    private final static String TITLE = "MODA - Password Manager";
    private final static String VERSION = "0.5.2";  // The current version of the software
    private static Locale locale;  // The language used by the application

    public Application(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                       String databaseToUse){
        initFlatLaf();  // It has to be called before any Swing component
        initUI(backendQueue, frontendQueue, databaseToUse);
    }

    /**
     * Register the FlatLaf custom default sources to load the properties files
     */
    private void initFlatLaf(){
        FlatLaf.registerCustomDefaultsSource("moda.passwordmanager");  // Register the properties files
    }

    /**
     * Apply a theme to the application
     */
    public static void applyTheme(Themes theme){
        switch (theme){
            case Light -> FlatLightLaf.setup();
            case Dark -> FlatDarkLaf.setup();
        }
    }

    private void initUI(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                        String databaseToUse){
        Dimension screen = getToolkit().getScreenSize();

        int width = (int) (screen.getWidth() * 4/5);
        int height = (int) (screen.getHeight() * 4/5);

        setTitle(getApplicationTitle());
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));

        setIconImage(getIcon());  // Get the icon and set it

        setLocationRelativeTo(null);  // Set the application to be at the center of the screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new Frontend(backendQueue, frontendQueue, databaseToUse));
        pack();
    }

    /**
     * Parse the arguments and look for the path of a database to use
     * @param args The arguments
     * @return The path of the database if it exists, otherwise an empty string
     */
    private static String parseDatabasePath(String[] args){
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
        return Utilities.getIcon("logo.png").getImage();
    }

    /**
     * Retrieve the title of the application
     */
    public static String getApplicationTitle(){
        return TITLE;
    }

    /**
     * Retrieve the current version of the application
     */
    public static String getVersion(){
        return VERSION;
    }

    /**
     * Set the language of the application
     */
    public static void setApplicationLanguage(Language language) {
        switch (language){
            case English -> Application.locale = Locale.ENGLISH;
            case Italian -> Application.locale = Locale.ITALY;
        }
    }

    /**
     * Get the current language of the translation
     */
    public static Locale getApplicationLocale(){
        return Application.locale;
    }

    public static void main(String[] args){
        String databaseToUse = parseDatabasePath(args);  // Parse the args to look for a database to use

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