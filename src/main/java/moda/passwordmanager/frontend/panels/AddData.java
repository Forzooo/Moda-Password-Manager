package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.components.Placeholder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddData extends JPanel {

    private final InterThreadCommunication ITC;

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

    public AddData(InterThreadCommunication itc) {
        super();  // Initialize the Panel

        this.ITC = itc;

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the panel
     */
    private void initPanel() {

        // The constraint "fill" is used to let the components use all the panel
        setLayout(new MigLayout("fill"));

        setBackground(Color.WHITE);  // TODO: Temporary
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents() {
        JLabel addDataLabel = new JLabel();
        addDataLabel.setText("Add your data");
        addDataLabel.setFont(new Font("Arial", Font.BOLD, 32));

        // Create all the JTextField for the data input
        this.usernameTextField = new JTextField();
        this.usernamePlaceholder = new Placeholder(this.usernameTextField, "Username");

        this.emailAddressTextField = new JTextField();
        this.emailAddressPlaceholder = new Placeholder(this.emailAddressTextField, "Email Address (email@example.com)");

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        this.passwordTextField = new JTextField();
        this.passwordPlaceholder = new Placeholder(this.passwordTextField, "Password");

        // Create the button for the generation of a password
        this.generatePasswordButton = new JButton();
        this.generatePasswordButton.setText("Generate Password");
        this.generatePasswordButton.setToolTipText("Generate a password");  // The tooltip will became useful when the
                                                                            // the button will have an icon instead
        this.serviceTextField = new JTextField();
        this.servicePlaceholder = new Placeholder(this.serviceTextField, "Service (Google, Microsoft, ...)");

        this.additionalDataTextField = new JTextField();
        this.additionalDataPlaceholder = new Placeholder(this.additionalDataTextField,
                "Additional Data (Data not covered by the other fields)");

        this.resetButton = new JButton();
        this.resetButton.setText("Reset");

        this.saveButton = new JButton();
        this.saveButton.setText("Save");

        // Add all the components to the Panel
        add(addDataLabel, "span, align center, wrap");
        add(this.usernameTextField, "span, grow, wrap");
        add(this.emailAddressTextField, "span, grow, wrap");
        add(this.passwordTextField, "split 2, grow, align center");
        add(this.generatePasswordButton, "wrap");
        add(this.serviceTextField, "span, grow, wrap");
        add(this.additionalDataTextField, "span, grow, wrap");
        add(this.resetButton, "split 2, align center");
        add(this.saveButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners() {
        // Reset the placeholders as the form is cleared
        this.resetButton.addActionListener(e -> resetPlaceholders());

        // Save the data entered in the JTextFields in the database by calling the backend
        this.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // The service must be set before adding the data as it needs to be shown in the "Show Data" section
                if (servicePlaceholder.isShown() || serviceTextField.getText().isBlank()) {
                    return;
                }

                // Since some JTextFields are optional, it may happen that some of them are still shown when the
                // save button is clicked
                hidePlaceholders();

                // Call the save data function to tell the backend to save the data into the database
                saveData(usernameTextField.getText(), emailAddressTextField.getText(),
                        passwordTextField.getText(), serviceTextField.getText(),
                        additionalDataTextField.getText());

                // Reset the placeholder after the data has been saved
                resetPlaceholders();
            }
        });

        this.generatePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordPlaceholder.hide();  // Hide the placeholder
                passwordTextField.setText(generateString());  // Generate the password
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
     * Hide all the placeholders that are shown
     */
    private void hidePlaceholders(){
        if (this.usernamePlaceholder.isShown()){
            this.usernamePlaceholder.hide();
        }

        if (this.emailAddressPlaceholder.isShown()){
            emailAddressPlaceholder.hide();
        }

        if (this.passwordPlaceholder.isShown()){
            this.passwordPlaceholder.hide();
        }

        if (this.servicePlaceholder.isShown()){
            this.servicePlaceholder.hide();
        }

        if (this.additionalDataPlaceholder.isShown()){
            this.additionalDataPlaceholder.hide();
        }
    }

    /**
     * Save the data the user has entered into the database

     */
    private void saveData(String username, String emailAddress, String password, String service, String additionalData) {
        // Create the Data object with the user data to send to the backend
        Data userData = new Data(username, emailAddress, password, service, additionalData);

        // Create the Event to send to the backend
        Event saveData = new Event("save-data", userData);
        this.ITC.send(saveData);
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

    /**
     * Generate a random string
     */
    private String generateString() {
        Event generatePassword = new Event("generate-string");
        Event response = this.ITC.request(generatePassword);  // Wait for the result

        return (String) response.getData().getFirst();  // Return the string generated
    }
}
