package moda.passwordManager.frontend;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.dialogs.MasterPasswordDialog;
import moda.passwordManager.frontend.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
The Board class is defined as two parts: the left one and the right one.
The left one is a sidebar which is static, that means it won't change its appearance during the execution,
while the right side is defined based on the JButton selected on the sidebar, thus it's dynamic and needs
a proper handling using the switchPanel() method
 */
public class Frontend extends JPanel implements ActionListener {

    private final static String VERSION = "0.1.0";  // The current version of the software

    // Define the Scheduled Executor Service and its delay used to perform background tasks
    private ScheduledExecutorService executorService;
    private final static int INITIAL_DELAY = 1500;  // The delay before starting to execute any task
    private final static int DELAY = 10000;  // The delay between each cycle of tasks to perform

    // The dynamicState indicates which Panel needs to be switched to from the current one selected
    private GUIState dynamicState;
    private JPanel currentPanel;  // The current selected JPanel

    private Timer swingTimer;  // The timer used to show the JPanel chosen by the user
    private final static int TIMER_DELAY = 500;  // Repeat each timer action every second

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

    // The CommunicationHandler object used to communicate with the Backend thread
    private CommunicationHandler communicationHandler;

    public Frontend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue, int width, int height){
        setSize(width, height);  // Set the initial dimension of the Frame

        initCommunication(backendQueue, frontendQueue);  // Start the communication between the backend and the frontend

        // Ask the user for the master password before starting to use the password manager
        MasterPasswordDialog masterPasswordDialog = new MasterPasswordDialog(this.communicationHandler);
        masterPasswordDialog.setVisible(true);

        // The Executor Service must be init after the masterPasswordDialog as it requires the master password to operate
        initExecutorService();

        initFrame();  // Set the properties of the JFrame
        initPanels();  // Initialize all the JPanels
        initSwingTimer();  // Initialize the Swing timer only after all the frontend components have been created
    }

    private void initFrame(){

        setFocusable(true);  // Set the focus on the frame to get the keyboard inputs
        setLayout(new BorderLayout());  // The layout for the Board is the Border one

        this.windowSize = getToolkit().getScreenSize();  // Get the initial size of the window

    }

    /**
     * Initialize the communication between the frontend and the backend
     * @param backendQueue
     * @param frontendQueue
     */
    private void initCommunication(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        this.communicationHandler = new CommunicationHandler(frontendQueue, backendQueue);
    }

    private void masterPasswordDialog(){

    }

    /**
     * Initialize the executor service used to perform background tasks in the frontend
     */
    private void initExecutorService(){
        this.executorService = Executors.newSingleThreadScheduledExecutor();  // Create a single thread for the periodic execution of methods

        this.executorService.scheduleAtFixedRate(this::updateUserData, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Initialize the Swing timer used to perform graphical tasks in the frontend
     */
    private void initSwingTimer(){
        this.swingTimer = new Timer(TIMER_DELAY, this::actionPerformed);
        this.swingTimer.start();
    }

    /**
     * Initialize all the panels
     */
    private void initPanels(){

        this.sidebarPanel = new SidebarPanel(this.windowSize, VERSION);
        add(this.sidebarPanel, BorderLayout.WEST);  // Add the Sidebar to the Frame

        this.addDataPanel = new AddDataPanel(this.communicationHandler, this.MIN_CONTENT_WIDTH, this.windowSize,
                                             this.sidebarPanel.getWidth());

        this.showDataPanel = new ShowDataPanel(this.communicationHandler, this.MIN_CONTENT_WIDTH, this.windowSize,
                                               this.sidebarPanel.getWidth());
        this.dynamicState = GUIState.SHOW_DATA;  // Set the default dynamic state to be the Show Data panel

        this.settingsPanel = new SettingsPanel(this.communicationHandler, this.MIN_CONTENT_WIDTH, this.windowSize,
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

    /**
     * Method used only to call the update user data inside the ShowDataPanel class
     */
    private void updateUserData(){
        this.showDataPanel.updateUserData();
    }

}