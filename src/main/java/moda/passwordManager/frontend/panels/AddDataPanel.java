package moda.passwordManager.frontend.panels;

import moda.passwordManager.backend.Data;
import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.components.Placeholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AddDataPanel extends JPanel {

    // Attribute to communicate with the backend
    private CommunicationHandler communicationHandler;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    // Attributes of general use components
    private JTextField usernameTextField;
    private JTextField emailAddressTextField;
    private JTextField passwordTextField;
    private JTextField serviceTextField;
    private JTextField additionalDataTextField;

    private Placeholder usernamePlaceholder;
    private Placeholder emailAddressPlaceholder;
    private Placeholder passwordPlaceholder;
    private Placeholder servicePlaceholder;
    private Placeholder additionalDataPlaceholder;

    private JButton resetButton;
    private JButton saveButton;

    public AddDataPanel(CommunicationHandler communicationHandler, int MIN_CONTENT_WIDTH, Dimension windowSize, int sidebarPanelWidth) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.communicationHandler = communicationHandler;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        initPanel(sidebarPanelWidth);
        initComponents();
        initActionListener();
    }

    /**
     * Get the layout used for the panel
     * @return BorderLayout
     */
    private BoxLayout getPanelLayout() {
        return new BoxLayout(this, BoxLayout.Y_AXIS);
    }

    /**
     * Set the configuration of the JPanel
     * @param sidebarPanelWidth
     */
    private void initPanel(int sidebarPanelWidth){
        setLayout(getPanelLayout());  // Set its layout

        // Set the preferred size
        setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - sidebarPanelWidth), (int) this.windowSize.getHeight()));

        setBackground(Color.WHITE);

        // Set the margin of the Add Data Panel [top: 60, bottom: 60, left: 30, right: 30]
        setBorder(BorderFactory.createEmptyBorder(60,30,60,30));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){

        JLabel addDataLabel = new JLabel();
        addDataLabel.setText("Add a new data:");
        addDataLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Create all the JTextField for the data input
        Dimension textFieldDimension = new Dimension(1600, 30);  // Define the dimension of any JTextField

        this.usernameTextField = new JTextField();
        this.usernameTextField.setMaximumSize(textFieldDimension);
        this.usernamePlaceholder = new Placeholder(this.usernameTextField, "Username");

        this.emailAddressTextField = new JTextField();
        this.emailAddressTextField.setMaximumSize(textFieldDimension);
        this.emailAddressPlaceholder = new Placeholder(this.emailAddressTextField, "Email Address");

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        this.passwordTextField = new JTextField();
        this.passwordTextField.setMaximumSize(textFieldDimension);
        this.passwordPlaceholder = new Placeholder(this.passwordTextField, "Password");

        this.serviceTextField = new JTextField();
        this.serviceTextField.setMaximumSize(textFieldDimension);
        this.servicePlaceholder = new Placeholder(this.serviceTextField, "Service");

        this.additionalDataTextField = new JTextField();
        this.additionalDataTextField.setMaximumSize(textFieldDimension);
        this.additionalDataPlaceholder = new Placeholder(this.additionalDataTextField, "Additional Data");

        // Create the JButton for Reset and Confirm operations
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

        this.resetButton = new JButton();
        this.resetButton.setText("Reset");
        this.resetButton.setMaximumSize(buttonDimension);

        this.saveButton = new JButton();
        this.saveButton.setText("Save");
        this.saveButton.setMaximumSize(buttonDimension);

        // Add all the components to the Panel
        add(addDataLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));  // Add RigidArea to add spacing between components
        add(usernameTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(emailAddressTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(passwordTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(serviceTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(additionalDataTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(resetButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(saveButton);
    }

    /**
     * Initialize all the action listeners
     */
    private void initActionListener(){
        // When the Reset JButton is clicked then all the JTextField placeholders are reset
        this.resetButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Reset the JTextFields by showing their placeholder
                        resetPlaceholders();
                    }
                }
        );

        // Save the data entered in the JTextFields in the database by calling the backend
        this.saveButton.addActionListener(
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

                        // Call the save data function to tell the backend to save the data into the database
                        saveData(usernameTextField.getText(), emailAddressTextField.getText(),
                                passwordTextField.getText(), serviceTextField.getText(),
                                additionalDataTextField.getText()
                        );

                        // Reset the placeholder after the data has been saved
                        resetPlaceholders();
                    }
                }
        );


    }

    /**
     * Reset all the placeholders
     */
    private void resetPlaceholders(){
        this.usernamePlaceholder.showPlaceholder();
        this.emailAddressPlaceholder.showPlaceholder();
        this.passwordPlaceholder.showPlaceholder();
        this.servicePlaceholder.showPlaceholder();
        this.additionalDataPlaceholder.showPlaceholder();
    }

    /**
     * Save the data the user has entered in "Add Data" section into the database
     * @param username
     * @param emailAddress
     * @param password
     * @param service
     * @param additionalData
     */
    private void saveData(String username, String emailAddress, String password, String service, String additionalData){
        // Create the Data object with the user data to send to the backend
        Data userData = new Data(username, emailAddress, password, service, additionalData);

        ArrayList dataToSend = new ArrayList();
        dataToSend.add(userData);

        // Create the Event to send to the backend
        moda.passwordManager.communicationHandler.Event saveData = new Event("save-data", dataToSend);
        this.communicationHandler.send(saveData);
        this.communicationHandler.receive();
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

}
