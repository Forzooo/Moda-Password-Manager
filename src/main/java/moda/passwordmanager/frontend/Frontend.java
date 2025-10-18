package moda.passwordmanager.frontend;

import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.dialogs.MasterPasswordDialog;
import moda.passwordmanager.frontend.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Frontend extends JPanel implements ActionListener {

    private final static String VERSION = "0.4.0";  // The current version of the software

    // The InterThreadCommunication objects used to communicate with the Backend thread
    private InterThreadCommunication itc;

    // Define the Scheduled Executor Service and its delay used to perform background tasks
    private ScheduledExecutorService executorService;
    private final static int INITIAL_DELAY = 5;  // The delay, in seconds, before starting to execute any task

    // The Frontend Event Listener used to receive and handle requests from the backend
    private FrontendEventListener eventListener;

    // The dynamicState indicates which Panel needs to be switched to from the current one selected
    private GUIState dynamicState;
    private JPanel currentPanel;  // The current selected JPanel

    private Timer swingTimer;  // The timer used to show the JPanel chosen by the user
    private final static int SWING_TIMER_DELAY = 500;  // Repeat each timer action every second

    /**
    * A Dimension attribute, retrieved from getToolkit().getScreenSize(), used to dynamically resize
    * the components of the window
    */
    private Dimension windowSize;

    private final int MIN_CONTENT_WIDTH = 500;

    // All the JPanel of the GUI, defined as class attributes
    private SidebarPanel sidebarPanel;
    private AddDataPanel addDataPanel;
    private ShowDataPanel showDataPanel;
    private SettingsPanel settingsPanel;

    public Frontend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                    String databasePath, int width, int height){

        initCommunication(backendQueue, frontendQueue);  // Start the communication between the backend and the frontend
        setInitialDatabase(databasePath);  // Set, if not empty, the database to use

        // Set the default exception handler for the Frontend thread
        Thread.setDefaultUncaughtExceptionHandler(this::exceptionHandler);

        initMasterPassword();

        // The Executor Service must be init after the masterPasswordDialog as it requires the master password to operate
        initExecutorService();

        initPanel(width, height);  // Set the properties of the panel
        initPanels();  // Initialize all the JPanels
        initSwingTimer();  // Initialize the Swing timer only after all the frontend components have been created
        initEventListener();  // Initialize the Event Listener only after all the frontend components have been init

    }

    /**
     * Retrieve the icon of the password manager from the resources folder
     * @return Icon of the password manager
     */
    public static Image getIcon(){
        // Get the image from the resources
        ImageIcon imageIcon = new ImageIcon(Frontend.class.getResource("/icon.png"));
        return imageIcon.getImage();
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
        this.itc.requestAndReceive(event);
    }

    /**
     * Ask the user for the master password before starting to use the password manager
     */
    private void initMasterPassword(){
        MasterPasswordDialog masterPasswordDialog = new MasterPasswordDialog(this.itc);
        masterPasswordDialog.setVisible(true);
    }

    /**
     * Initialize the panel
     * @param width The width of the panel
     * @param height The height of the panel
     */
    private void initPanel(int width, int height){
        setSize(width, height);  // Set the initial dimension of the Frame

        setFocusable(true);  // Set the focus on the frame to get the keyboard inputs
        setLayout(new BorderLayout());  // The layout for the Board is the Border one

        this.windowSize = getToolkit().getScreenSize();  // Get the initial size of the window
    }

    /**
     * Initialize the communication between the frontend and the backend
     * @param backendQueue The queue that events are received from
     * @param frontendQueue The queue that events are sent from
     */
    private void initCommunication(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        this.itc = new InterThreadCommunication(frontendQueue, backendQueue);
    }

    /**
     * Initialize the Frontend event listener
     */
    private void initEventListener(){
        this.eventListener = new FrontendEventListener(this.itc, this.showDataPanel);
        this.eventListener.start();
    }

    /**
     * Initialize the executor service used to perform background tasks in the frontend
     */
    private void initExecutorService(){
        // Create a single thread for the periodic execution of methods
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::synchronizeGoogleDrive, INITIAL_DELAY, 60, TimeUnit.SECONDS);
    }

    /**
     * Initialize the Swing timer used to perform graphical background tasks in the frontend
     */
    private void initSwingTimer(){
        this.swingTimer = new Timer(SWING_TIMER_DELAY, this::actionPerformed);
        this.swingTimer.start();
    }

    /**
     * Initialize all the panels
     */
    private void initPanels(){
        this.sidebarPanel = new SidebarPanel(this.windowSize, VERSION);
        add(this.sidebarPanel, BorderLayout.WEST);  // Add the Sidebar to the Frame

        this.addDataPanel = new AddDataPanel(this.itc, this.MIN_CONTENT_WIDTH, this.windowSize,
                                             this.sidebarPanel.getWidth());

        this.showDataPanel = new ShowDataPanel(this.itc, this.MIN_CONTENT_WIDTH, this.windowSize,
                                               this.sidebarPanel.getWidth());
        this.dynamicState = GUIState.SHOW_DATA;  // Set the default dynamic state to be the Show Data panel

        this.settingsPanel = new SettingsPanel(this.itc, this.MIN_CONTENT_WIDTH, this.windowSize,
                                               this.sidebarPanel.getWidth());

        // Add the Show All Panel to the Board as it's the default panel at the start
        this.currentPanel = this.showDataPanel;
        add(this.showDataPanel, BorderLayout.CENTER);
    }

    /**
     * Switch to a new JPanel hiding the previous one
     */
    private void switchPanel(){
        // Check if the panel has changed, otherwise stop the method to avoid useless operations
        if (this.dynamicState.equals(this.sidebarPanel.getDynamicState())){
            return;
        }
        this.dynamicState = this.sidebarPanel.getDynamicState();  // Update the dynamic state

        remove(this.currentPanel);  // Remove the current (old) panel from the Board

        // Based on the section chosen change the current panel to the new one
        switch (this.dynamicState){
            case ADD_DATA -> this.currentPanel = this.addDataPanel;
            case SHOW_DATA -> this.currentPanel = this.showDataPanel;
            case SETTINGS -> this.currentPanel = this.settingsPanel;
        }

        add(this.currentPanel, BorderLayout.CENTER);  // Add the new panel to the Board
        revalidate();
        repaint();
    }

    /**
     * The tasks that the Swing Timer performs periodically
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        switchPanel();
    }

    private void synchronizeGoogleDrive(){
        // Check each time whether the automatic synchronization is enabled before synchronizing
        Event getSynchronization = new Event("get-google-drive-synchronization");

        // Wait for the response from the backend
        getSynchronization = this.itc.requestAndReceive(getSynchronization);
        boolean enabled = (boolean) getSynchronization.getData().getFirst();

        // Don't synchronize if the automatic synchronization it's not enabled
        if (!enabled){
            return;
        }

        // Synchronize with Google Drive
        Event synchronize = new Event("google-drive-synchronize");
        this.itc.request(synchronize);
    }

    /**
     * Handle the unhandled exception in the frontend by showing a messagebox about it
     * @param e The exception that has occurred
     */
    private void exceptionHandler(Thread t, Throwable e){
        // Show the exception as a Message Dialog with the type of error message
        JOptionPane.showMessageDialog(this, e.toString(), "An exception occurred in the Frontend",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Wait for unhandled exceptions in the backend and then close the connection with the backend and stop
     * the execution
     */

}
