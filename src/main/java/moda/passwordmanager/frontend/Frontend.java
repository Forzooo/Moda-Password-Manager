package moda.passwordmanager.frontend;

import moda.passwordmanager.frontend.dialogs.Settings;
import moda.passwordmanager.frontend.dialogs.Startup;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.panels.*;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Frontend extends JPanel {

    // The InterThreadCommunication objects used to communicate with the Backend thread
    private final InterThreadCommunication ITC;

    // The Frontend Event Listener used to receive and handle requests from the backend
    private FrontendEventListener eventListener;

    private JPanel selectedPanel;  // The current panel shown next to the sidebar

    private final static int MIN_CONTENT_WIDTH = 500;

    // The panels that are handled by the frontend panel
    private AddData addDataPanel;
    private ShowData showDataPanel;

    public Frontend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                    String databasePath){

        // Start the communication between the backend and the frontend
        this.ITC = new InterThreadCommunication(backendQueue, frontendQueue);

        setInitialDatabase(databasePath);  // Set, if not empty, the database to use

        // Set the default exception handler for the Frontend threads
        Thread.setDefaultUncaughtExceptionHandler(this::exceptionHandler);

        initStartup();

        initPanel();  // Set the properties of the panel
        initPanels();  // Initialize all the JPanels
        initEventListener();  // Initialize the Event Listener only after all the frontend components have been init

    }

    /**
     * Handle the unhandled exception in the frontend by showing a messagebox about it
     * @param e The exception that has occurred
     */
    private void exceptionHandler(Thread t, Throwable e){
        String stackTrace = Utilities.getStackTrace(e);  // Get the full stack trace of the throwable
        // Show the exception as a Message Dialog with the type of error message
        JOptionPane.showMessageDialog(this, Utilities.getStackTraceRows(stackTrace, 5),
                "The following exception occurred in the " + t.getName() + " thread", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Set the initial database to use
     * @param databasePath The path of the database to use
     */
    private void setInitialDatabase(String databasePath){
        // If the path is empty, the user has not chosen a database to open at the execution of the application
        if (databasePath.isEmpty()){
            return;
        }
        Event event = new Event("set-database", databasePath);
        this.ITC.request(event);
    }

    /**
     * Ask the user for the master password before starting to use the password manager
     */
    private void initStartup(){
        Startup startup = new Startup(this.ITC);
        startup.setVisible(true);
    }

    /**
     * Initialize the panel
     */
    private void initPanel(){
        setFocusable(true);  // Set the focus on the frame to get the keyboard inputs
        setLayout(new BorderLayout());  // The layout for the frontend is the Border one
    }

    /**
     * Initialize the Frontend event listener
     */
    private void initEventListener(){
        this.eventListener = new FrontendEventListener(this.ITC, this.showDataPanel);
        this.eventListener.start();
    }

    /**
     * Initialize all the panels
     */
    private void initPanels(){
        Dimension windowSize = getToolkit().getScreenSize();  // Get the initial size of the window

        Sidebar sidebar = new Sidebar(windowSize);
        add(sidebar, BorderLayout.WEST);  // Add the Sidebar to the Frame

        this.addDataPanel = new AddData(this.ITC);

        this.showDataPanel = new ShowData(this.ITC, MIN_CONTENT_WIDTH, windowSize, sidebar.getWidth());

        // Add the Show All Panel to the GUI as it's the default panel at the start
        this.selectedPanel = this.showDataPanel;
        add(this.showDataPanel, BorderLayout.CENTER);
    }

    /**
     * Switch to a new JPanel hiding the previous one
     */
    public void switchPanel(GUIState selectedPanel){
        remove(this.selectedPanel);  // Remove the previous panel from the Board

        // Based on the section chosen change the current panel to the new one
        switch (selectedPanel){
            case ADD_DATA -> this.selectedPanel = this.addDataPanel;
            case SHOW_DATA -> this.selectedPanel = this.showDataPanel;
        }

        add(this.selectedPanel, BorderLayout.CENTER);  // Add the selected panel to the Board

        // Revalidate and repaint the GUI with the graphical changes
        revalidate();
        repaint();
    }

    /**
     * Open the settings dialog
     */
    public void openSettings(){
        Settings settings = new Settings(this.ITC);
        settings.setVisible(true);
    }

}
