package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.interthreadcommunication.EventType;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.components.Placeholder;
import moda.passwordmanager.frontend.dialogs.ConfigureGenerationPasswordDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AddDataPanel extends JPanel {

    // Attribute to communicate with the backend
    private InterThreadCommunication interThreadCommunication;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    // Swing components
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
    private JButton generatePasswordButton;
    private JButton configurePasswordGeneration;

    public AddDataPanel(InterThreadCommunication interThreadCommunication, int MIN_CONTENT_WIDTH, Dimension windowSize, int sidebarPanelWidth) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.interThreadCommunication = interThreadCommunication;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        initPanel(sidebarPanelWidth);
        initComponents();
        initListeners();
    }

    /**
     * Get the layout used for the panel
     *
     * @return BoxLayout
     */
    private BoxLayout getPanelLayout() {
        return new BoxLayout(this, BoxLayout.Y_AXIS);
    }

    /**
     * Set the configuration of the JPanel
     *
     * @param sidebarPanelWidth
     */
    private void initPanel(int sidebarPanelWidth) {
        setLayout(getPanelLayout());  // Set its layout

        // Set the preferred size
        setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - sidebarPanelWidth), (int) this.windowSize.getHeight()));

        setBackground(Color.WHITE);

        // Set the margin of the Add Data Panel [top: 60, bottom: 60, left: 30, right: 30]
        setBorder(BorderFactory.createEmptyBorder(60, 30, 60, 30));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents() {

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
        this.emailAddressPlaceholder = new Placeholder(this.emailAddressTextField, "Email Address (email@example.com)");

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        this.passwordTextField = new JTextField();
        this.passwordTextField.setMaximumSize(textFieldDimension);
        this.passwordPlaceholder = new Placeholder(this.passwordTextField, "Password");

        this.serviceTextField = new JTextField();
        this.serviceTextField.setMaximumSize(textFieldDimension);
        this.servicePlaceholder = new Placeholder(this.serviceTextField, "Service (Google, Microsoft, ...)");

        this.additionalDataTextField = new JTextField();
        this.additionalDataTextField.setMaximumSize(textFieldDimension);
        this.additionalDataPlaceholder = new Placeholder(this.additionalDataTextField, "Additional Data (Data " +
                "not covered by the other fields)");

        // Create the JButton for Reset and Confirm operations
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

        this.resetButton = new JButton();
        this.resetButton.setText("Reset");
        this.resetButton.setMaximumSize(buttonDimension);

        this.saveButton = new JButton();
        this.saveButton.setText("Save");
        this.saveButton.setMaximumSize(buttonDimension);

        // Create the Buttons for the Generation and the configuration of the password
        this.generatePasswordButton = new JButton();
        this.generatePasswordButton.setText("Generate Password");
        this.generatePasswordButton.setMaximumSize(buttonDimension);

        this.configurePasswordGeneration = new JButton();
        this.configurePasswordGeneration.setText("Configure the Password Generation");
        this.configurePasswordGeneration.setMaximumSize(buttonDimension);

        // Add all the components to the Panel
        add(addDataLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));  // Add RigidArea to add spacing between components
        add(this.usernameTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(this.emailAddressTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(this.passwordTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(this.serviceTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(this.additionalDataTextField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(this.generatePasswordButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(this.configurePasswordGeneration);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(this.resetButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(this.saveButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners() {
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
        this.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Only the service placeholder is required to be set before saving some data
                if (servicePlaceholder.isShown()) {
                    return;
                }

                // Since some Text Fields are optional, it may happen that some of them are still shown when the
                // "Save Data" button is clicked
                hidePlaceholders();

                // Call the save data function to tell the backend to save the data into the database
                saveData(usernameTextField.getText(), emailAddressTextField.getText(),
                        passwordTextField.getText(), serviceTextField.getText(),
                        additionalDataTextField.getText()
                );

                // Reset the placeholder after the data has been saved
                resetPlaceholders();
            }
        });

        this.generatePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordPlaceholder.hide();  // Hide the placeholder
                generatePassword();  // Generate the password
            }
        });

        // Initialize the Dialog for the configuration and show it to the user
        this.configurePasswordGeneration.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigureGenerationPasswordDialog configureGenerationPasswordDialog = new ConfigureGenerationPasswordDialog(interThreadCommunication);
                configureGenerationPasswordDialog.setVisible(true);
            }
        });
    }

    /**
     * Reset all the placeholders
     */
    private void resetPlaceholders() {
        this.usernamePlaceholder.show();
        this.emailAddressPlaceholder.show();
        this.passwordPlaceholder.show();
        this.servicePlaceholder.show();
        this.additionalDataPlaceholder.show();
    }

    /**
     * Check whether placeholders are shown, then hide them
     */
    private void hidePlaceholders(){
        if (usernamePlaceholder.isShown()){
            usernamePlaceholder.hide();
        }

        if (emailAddressPlaceholder.isShown()){
            emailAddressPlaceholder.hide();
        }

        if (passwordPlaceholder.isShown()){
            passwordPlaceholder.hide();
        }

        if (servicePlaceholder.isShown()){
            servicePlaceholder.hide();
        }

        if (additionalDataPlaceholder.isShown()){
            additionalDataPlaceholder.hide();
        }
    }

    /**
     * Save the data the user has entered in "Add Data" section into the database
     *
     * @param username
     * @param emailAddress
     * @param password
     * @param service
     * @param additionalData
     */
    private void saveData(String username, String emailAddress, String password, String service, String additionalData) {
        // Check whether additionalData has been set, otherwise set it to blank instead of the placeholder text
        if (additionalDataPlaceholder.isShown()){
            additionalData = "";
        }

        // Create the Data object with the user data to send to the backend
        Data userData = new Data(username, emailAddress, password, service, additionalData);

        // Create the Event to send to the backend
        Event saveData = new Event("save-data", userData, EventType.REQUEST);

        this.interThreadCommunication.send(saveData);
        this.interThreadCommunication.receive(Frontend.getBackendEventResponse());
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

    private void generatePassword() {

        // Create the Event to send to the backend
        Event generatePassword = new Event("generate-string", EventType.REQUEST);
        this.interThreadCommunication.send(generatePassword);

        Event response = this.interThreadCommunication.receive(Frontend.getBackendEventResponse());  // Wait for the result

        String password = (String) response.getData().getFirst();  // Get the password from the backend
        this.passwordTextField.setText(password);  // Set the password to the TextField
    }
}
