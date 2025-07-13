package moda.passwordManager.frontend;

import moda.passwordManager.backend.Data;
import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
while the right side is defined based on the button selected on the sidebar, thus it's dynamic and needs
a proper handling using the switchPanel() method
 */
public class Frontend extends JPanel implements ActionListener {

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
    private JPanel addDataPanel;
    private JPanel showDataPanel;
    private JPanel settingsPanel;

    /**
     * The service_data shown in the JList of "Show Data" panel <br/>
     * It's updated automatically by the timer
     */
    private ArrayList<Data> userData;  // A Data object is required as each service shown needs to be associated with its ID
    private DefaultListModel<String> userDataModel;

    // The CommunicationHandler object used to communicate with the Backend thread
    private CommunicationHandler communicationHandler;

    public Frontend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue, int width, int height){
        setSize(width, height);  // Set the initial dimension of the Frame

        initCommunication(backendQueue, frontendQueue);
        initExecutorService();

        // Initialize all the Panels
        initBoard();  // Set the properties of the Board
        initSidebarPanel();
        initAddDataPanel();
        initShowDataPanel();
        initSettingsPanel();
    }

    private void initBoard(){

        setFocusable(true);  // Set the focus on the frame to get the keyboard inputs
        setLayout(new BorderLayout());  // The layout for the Board is the Border one
//        addKeyListener(new TAdapter());

        // Set the initial state of the dynamic part to Show All Panel
        this.dynamicState = GUIState.SHOW_DATA;

        this.windowSize = getToolkit().getScreenSize();  // Get the initial size of the window

        // Initialize the user data ArrayList and Model
        this.userData = new ArrayList<>();
        this.userDataModel = new DefaultListModel<>();

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
        sendMasterPassword("Moda-Test");  // TODO: Remove the function after the Issue #10 has been completed
    }

    /**
     * Initialize the executor service used to perform background tasks in the frontend
     */
    private void initExecutorService(){
        this.executorService = Executors.newSingleThreadScheduledExecutor();  // Create a single thread for the periodic execution of methods

        this.executorService.scheduleAtFixedRate(this::updateUserData, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
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

        Button addDataButton = new Button();
        addDataButton.setText("Add Data");
        addDataButton.setMaximumSize(buttonDimension);
        addDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.ADD_DATA;
                switchPanel();
            }
        });

        Button showDataButton = new Button();
        showDataButton.setText("Show Data");
        showDataButton.setMaximumSize(buttonDimension);
        showDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.SHOW_DATA;
                switchPanel();
            }
        });

        Button settingsButton = new Button();  // TODO: Use the settings icon instead of the text
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

        buttonPanel.add(addDataButton);
        buttonPanel.add(showDataButton);
        buttonPanel.add(settingsButton);

        this.sidebarPanel.add(buttonPanel, BorderLayout.SOUTH);

//        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, (int) (this.windowSize.getHeight()/5))));
//        this.sidebarPanel.add(addDataButton);
//        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));
//        this.sidebarPanel.add(showDataButton);
//        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, (int) this.windowSize.getHeight()/6)));
//        this.sidebarPanel.add(settingsButton);
//        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));

    }

    // Initialize all the components of the Add Data Panel
    private void initAddDataPanel(){

        this.addDataPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMinimumSize() {
                // altezza 0 → “qualsiasi”, conta solo la larghezza minima
                return new Dimension(MIN_CONTENT_WIDTH, 0);
            }
        };

        this.addDataPanel.setLayout(new BoxLayout(this.addDataPanel, BoxLayout.Y_AXIS));
        this.addDataPanel.setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - this.sidebarPanel.getWidth()), (int) this.windowSize.getHeight()));
//        this.addDataPanel.setBackground(light);
        this.addDataPanel.setBackground(Color.WHITE);

        // Set the margin of the Add Data Panel [top: 60, bottom: 60, left: 30, right: 30]
        this.addDataPanel.setBorder(BorderFactory.createEmptyBorder(60,30,60,30));

        JLabel addDataLabel = new JLabel();
        addDataLabel.setText("Add a new data:");
        addDataLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Create all the JTextField for the data input
        Dimension textFieldDimension = new Dimension(1600, 30);  // Define the dimension of any JTextField

        JTextField usernameTextField = new JTextField();
        usernameTextField.setMaximumSize(textFieldDimension);
        Placeholder usernamePlaceholder = new Placeholder(usernameTextField, "Username");

        JTextField emailAddressTextField = new JTextField();
        emailAddressTextField.setMaximumSize(textFieldDimension);
        Placeholder emailAddressPlaceholder = new Placeholder(emailAddressTextField, "Email Address");

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        JTextField passwordTextField = new JTextField();
        passwordTextField.setMaximumSize(textFieldDimension);
        Placeholder passwordPlaceholder = new Placeholder(passwordTextField, "Password");

        JTextField serviceTextField = new JTextField();
        serviceTextField.setMaximumSize(textFieldDimension);
        Placeholder servicePlaceholder = new Placeholder(serviceTextField, "Service");

        JTextField additionalDataTextField = new JTextField();
        additionalDataTextField.setMaximumSize(textFieldDimension);
        Placeholder additionalDataPlaceholder = new Placeholder(additionalDataTextField, "Additional Data");

        // Create the MButton for Reset and Confirm operations
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any MButton

        Button resetButton = new Button();
        resetButton.setText("Reset");
        resetButton.setMaximumSize(buttonDimension);
        resetButton.addActionListener(  // When the Reset button is clicked then all the JTextFields are reset
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Reset the JTextFields by showing their placeholder
                        usernamePlaceholder.showPlaceholder();
                        emailAddressPlaceholder.showPlaceholder();
                        passwordPlaceholder.showPlaceholder();
                        servicePlaceholder.showPlaceholder();
                        additionalDataPlaceholder.showPlaceholder();
                    }
                }
        );

        Button saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setMaximumSize(buttonDimension);

        // Save the data entered in the JTextField in the database after some cryptographic operations
        saveButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // If at least one placeholder is enabled then don't allow the data to be saved
                        if(usernamePlaceholder.isShowPlaceholderFlag() || emailAddressPlaceholder.isShowPlaceholderFlag() ||
                                passwordPlaceholder.isShowPlaceholderFlag() || servicePlaceholder.isShowPlaceholderFlag() ||
                                additionalDataPlaceholder.isShowPlaceholderFlag()
                        ){
                            return;
                        }

                        // Call the save data function which will encrypt the data and save it into the database
//                        saveData(usernameTextField.getText(), emailAddressTextField.getText(),
//                                passwordTextField.getText(), serviceTextField.getText(),
//                                additionalDataTextField.getText()
//                        );

                        // Reset the placeholder after the data has been saved
                        usernamePlaceholder.showPlaceholder();
                        emailAddressPlaceholder.showPlaceholder();
                        passwordPlaceholder.showPlaceholder();
                        servicePlaceholder.showPlaceholder();
                        additionalDataPlaceholder.showPlaceholder();
                    }
                }
        );

        // Add all the components to the Panel
        this.addDataPanel.add(addDataLabel);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));  // Add RigidArea to add spacing between components
        this.addDataPanel.add(usernameTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(emailAddressTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(passwordTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(serviceTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(additionalDataTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(resetButton);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.addDataPanel.add(saveButton);

    }

    // Initialize all the components of the Show All Panel
    private void initShowDataPanel(){

        this.showDataPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMinimumSize() {
                // altezza 0 → “qualsiasi”, conta solo la larghezza minima
                return new Dimension(MIN_CONTENT_WIDTH, 0);
            }
        };

        // Create the JList used to show all the data saved inside the database
        JList dataList = new JList();
        dataList.setModel(this.userDataModel);  // Set the Model of the JList to the userData one

        dataList.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20));

        dataList.setFixedCellHeight(40);

        JScrollPane scrollPane = new JScrollPane(dataList);
        scrollPane.setPreferredSize(new Dimension(1000, 750));

        scrollPane.setBorder(new EmptyBorder(10,30,10,30));

        // Add the Show All Panel to the Board as it's the default panel at the start
        this.currentPanel = this.showDataPanel;
        add(this.showDataPanel, BorderLayout.CENTER);

        // Add all the components to the JPanel
        this.showDataPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // Initialize all the components of the Settings Panel
    private void initSettingsPanel(){

        this.settingsPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMinimumSize() {
                // altezza 0 → “qualsiasi”, conta solo la larghezza minima
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

    // Method executed periodically by the timer
    @Override
    public void actionPerformed(ActionEvent e) {
        updateUserData();
    }

    /**
     * Send the master password the user has entered to the backend
     * @param masterPassword
     */
    private void sendMasterPassword(String masterPassword){
        // Create and send the event to the backend telling to set the master password
        ArrayList dataToSend = new ArrayList();  // The communication requires using an ArrayList for the data
        dataToSend.add(masterPassword);
        Event setMasterPassword = new Event("set-master-password", dataToSend);

        this.communicationHandler.send(setMasterPassword);

        // Wait for the confirm event and notify the user about it
        Event confirmEvent = this.communicationHandler.receive();
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

    /**
     * Update the userData and its model to show the updated data of the database
     */
    private void updateUserData(){
        // Create and send the event to the backend asking for the user data
        Event updateUserData = new Event("get-full-service-data", new ArrayList());
        this.communicationHandler.send(updateUserData);

        // Wait for the response of the backend and update the data with the new one
        Event updatedDataEvent = this.communicationHandler.receive();
        ArrayList<Data> updatedData = (ArrayList<Data>) updatedDataEvent.getData().getFirst();

        this.userData.clear();  // Clear the ArrayList from the previous data
        this.userData.addAll(updatedData);  // Update the ArrayList with the new data

        this.userDataModel.clear();  // Clear the model from the previous data
        for (int i = 0; i < updatedData.size(); i++){
            this.userDataModel.add(i, updatedData.get(i).getSERVICE());
        }

    }
}