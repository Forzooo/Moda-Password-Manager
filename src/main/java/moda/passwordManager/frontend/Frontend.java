package moda.passwordManager.frontend;

import moda.passwordManager.backend.Data;
import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.components.Placeholder;
import moda.passwordManager.frontend.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
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
public class Frontend extends JPanel {

    private final static String CURRENT_VERSION = "0.1.0";  // The current version of the software

    // Define the Scheduled Executor Service and its delay used to perform background tasks
    private ScheduledExecutorService executorService;
    private final int INITIAL_DELAY = 1500;  // The delay before starting to execute any task
    private final int DELAY = 10000;  // The delay between each cycle of tasks to perform

    // The dynamicState indicates which Panel needs to be switched to from the current one selected
    private GUIState dynamicState;
    private JPanel currentPanel;

    /*
    A Dimension attribute, retrieved from getToolkit().getScreenSize(), used to dynamically resize
    the components of the window
     */
    private Dimension windowSize;

    private final int MIN_CONTENT_WIDTH = 500;

    // All the JPanel of the GUI, defined as class attributes
    private JPanel sidebarPanel;
    private AddDataPanel addDataPanel;
    private ShowDataPanel showDataPanel;
    private JPanel settingsPanel;

    // The CommunicationHandler object used to communicate with the Backend thread
    private CommunicationHandler communicationHandler;

    public Frontend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue, int width, int height){
        setSize(width, height);  // Set the initial dimension of the Frame

        initCommunication(backendQueue, frontendQueue);  // Start the communication between the backend and the frontend

        masterPasswordDialog();  // Ask the user for the master password before starting to use the password manager

        // The Executor Service must be after the masterPasswordDialog as it requires the master password to operate
        initExecutorService();

        // Initialize all the Panels
        initBoard();  // Set the properties of the Board

        initSidebarPanel();
        initPanels();  // Initialize all the JPanels

        //        initAddDataPanel();
//        initShowDataPanel();
        initSettingsPanel();
    }

    private void initBoard(){

        setFocusable(true);  // Set the focus on the frame to get the keyboard inputs
        setLayout(new BorderLayout());  // The layout for the Board is the Border one
//        addKeyListener(new TAdapter());

        // Set the initial state of the dynamic part to Show All Panel
        this.dynamicState = GUIState.SHOW_DATA;

        this.windowSize = getToolkit().getScreenSize();  // Get the initial size of the window

//        this.timer = new Timer(DELAY, this::actionPerformed);
//        this.timer.start();

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
        JDialog askMasterPassword = new JDialog((Frame) null, "Master Password", true);

//        askMasterPassword.setUndecorated(true);
        askMasterPassword.setTitle("Inserisci la Master Password");
        askMasterPassword.setSize(300, 100);
        askMasterPassword.setLocationRelativeTo(null);
        askMasterPassword.setAlwaysOnTop(true);

        askMasterPassword.setLayout(new FlowLayout());

        JPasswordField input = new JPasswordField();
        input.setPreferredSize(new Dimension(200, 25));

        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMasterPassword(input.getPassword());
                askMasterPassword.dispose();  // Close the window
            }
        });

        JButton sendButton = new JButton("LogIn");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve the master password and send it to the backend
                sendMasterPassword(input.getPassword());
                askMasterPassword.dispose();  // Close the window
            }
        });

        askMasterPassword.add(input);
        askMasterPassword.add(sendButton);

        askMasterPassword.setVisible(true);
    }

    /**
     * Initialize the executor service used to perform background tasks in the frontend
     */
    private void initExecutorService(){
        this.executorService = Executors.newSingleThreadScheduledExecutor();  // Create a single thread for the periodic execution of methods

        this.executorService.scheduleAtFixedRate(this::updateUserData, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Initialize all the panels
     */
    private void initPanels(){

        this.addDataPanel = new AddDataPanel(this.communicationHandler, this.MIN_CONTENT_WIDTH, this.windowSize,
                                             this.sidebarPanel.getWidth());
        this.showDataPanel = new ShowDataPanel(this.communicationHandler, this.MIN_CONTENT_WIDTH, this.windowSize,
                                               this.sidebarPanel.getWidth());

        // Add the Show All Panel to the Board as it's the default panel at the start
        this.currentPanel = this.showDataPanel;
        add(this.showDataPanel, BorderLayout.CENTER);
    }

    // Initialize all the components of the Sidebar Panel
    private void initSidebarPanel(){

        final int MAX_SIDEBAR = 300;

        this.sidebarPanel = new JPanel() {
            @Override public Dimension getPreferredSize() {
                Container parent = getParent(); // il Frame
                if (parent != null) {
                    int larghezza = Math.min(parent.getWidth() / 3, MAX_SIDEBAR);
                    return new Dimension(larghezza, parent.getHeight());
                }
                return new Dimension(MAX_SIDEBAR, 0);
            }
        };

        this.sidebarPanel.setBackground(Color.BLACK);

        //this.sidebarPanel.setPreferredSize(new Dimension((int) this.windowSize.getWidth()/5, (int) this.windowSize.getHeight()));
        //this.sidebarPanel.setMaximumSize(new Dimension((int) this.windowSize.getWidth()/5, Integer.MAX_VALUE));

        add(this.sidebarPanel, BorderLayout.WEST);  // Add the Sidebar to the Panel

        // Create the JLabel that displays the name of the Password Manager
        JLabel passwordManagerLabel = new JLabel();
        passwordManagerLabel.setText("MODA");
//        passwordManagerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // Set the text-alignment to center
        passwordManagerLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 80));  // Set the font of the label
//        passwordManagerLabel.setForeground(light);  // Set the color of the label
        passwordManagerLabel.setForeground(Color.WHITE);  // Set the color of the label

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));

        // Create the JButtons used to switch between JPanels of the dynamic part
        Dimension buttonDimension = new Dimension(350, 50);

        JButton addDataButton = new JButton();
        addDataButton.setText("Add Data");
        addDataButton.setMaximumSize(buttonDimension);
        addDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.ADD_DATA;
                switchPanel();
            }
        });

        JButton showDataButton = new JButton();
        showDataButton.setText("Show Data");
        showDataButton.setMaximumSize(buttonDimension);
        showDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.SHOW_DATA;
                switchPanel();
            }
        });

        JButton settingsButton = new JButton();  // TODO: Use the settings icon instead of the text
        settingsButton.setText("Settings");
        settingsButton.setMaximumSize(buttonDimension);
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.SETTINGS;
                switchPanel();
            }
        });

        // Add the components to the Sidebar
        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add RigidArea to add spacing between components
        this.sidebarPanel.add(passwordManagerLabel);

        // Add the current version of the software at the bottom of the sidebar
        JLabel currentVersionLabel = new JLabel();
        currentVersionLabel.setText("Version: " + CURRENT_VERSION);
        currentVersionLabel.setForeground(Color.WHITE);  // Set the color of the label

        this.sidebarPanel.add(currentVersionLabel);
        this.sidebarPanel.add(Box.createRigidArea(new Dimension(220, 20))); // Add RigidArea to add spacing between components

        buttonPanel.add(addDataButton);
        buttonPanel.add(showDataButton);
        buttonPanel.add(settingsButton);

        this.sidebarPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    // Initialize all the components of the Settings Panel
    private void initSettingsPanel(){

        this.settingsPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMinimumSize() {
                // altezza 0 -> “qualsiasi”, conta solo la larghezza minima
                return new Dimension(MIN_CONTENT_WIDTH, 0);
            }
        };
//        this.settingsPanel.setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - this.sidebarPanel.getWidth()), (int) this.windowSize.getHeight()));
        this.settingsPanel.setBackground(Color.RED);

        JLabel label = new JLabel();

        label.setText("Settings");

        settingsPanel.add(label, BorderLayout.CENTER);
    }

    // This method is used to switch to a new panel hiding the previous one
    private void switchPanel(){
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
     * Send the master password the user has entered to the backend thread
     * @param masterPassword
     */
    private void sendMasterPassword(char[] masterPassword){
        // Create and send the event to the backend telling to set the master password
        ArrayList dataToSend = new ArrayList();  // The communication requires using an ArrayList for the data
        dataToSend.add(new String(masterPassword));  // Convert the char array to a string
        Event setMasterPassword = new Event("set-master-password", dataToSend);

        this.communicationHandler.send(setMasterPassword);

        // Wait for the confirm event and notify the user about it
        Event confirmEvent = this.communicationHandler.receive();
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

    /**
     * Method used only to call the update user data inside the ShowDataPanel class
     */
    private void updateUserData(){
        this.showDataPanel.updateUserData();
    }

}