package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
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

    private JButton resetButton;
    private JButton saveButton;
    private JButton generatePasswordButton;

    public AddData(InterThreadCommunication itc){
        super();  // Initialize the Panel

        this.ITC = itc;

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the panel
     */
    private void initPanel(){
        // The constraint "fill" is used to let the components use all the panel
        setLayout(new MigLayout("fill"));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        JLabel addDataLabel = new JLabel();
        addDataLabel.setText("Add your data");
        addDataLabel.setFont(new Font("Arial", Font.BOLD, 32));

        Dimension textFieldDimension = new Dimension(600, 70);  // The dimension of each text field

        // Create all the JTextField for the data input
        this.usernameTextField = new JTextField();
        this.usernameTextField.putClientProperty("JTextField.placeholderText", "Username");
        this.usernameTextField.setPreferredSize(textFieldDimension);

        this.emailAddressTextField = new JTextField();
        this.emailAddressTextField.putClientProperty("JTextField.placeholderText", "Email Address (email@example.com)");
        this.emailAddressTextField.setPreferredSize(textFieldDimension);

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        this.passwordTextField = new JTextField();
        this.passwordTextField.putClientProperty("JTextField.placeholderText", "Password");
        this.passwordTextField.setPreferredSize(textFieldDimension);

        // Create the button for the generation of a password
        this.generatePasswordButton = new JButton();
        this.generatePasswordButton.setIcon(Utilities.getIcon("generate_string.png"));
        this.generatePasswordButton.setToolTipText("Generate a password");

        Utilities.applyTrailingButtonProperties(this.generatePasswordButton);
        this.passwordTextField.putClientProperty("JTextField.trailingComponent", this.generatePasswordButton);

        this.serviceTextField = new JTextField();
        this.serviceTextField.putClientProperty("JTextField.placeholderText", "Service (Google, Microsoft, ...)");
        this.serviceTextField.setPreferredSize(textFieldDimension);

        this.additionalDataTextField = new JTextField();
        this.additionalDataTextField.putClientProperty("JTextField.placeholderText",
                "Additional Data (Data not covered by the other fields)");
        this.additionalDataTextField.setPreferredSize(textFieldDimension);

        this.resetButton = new JButton();
        this.resetButton.setText("Reset");

        this.saveButton = new JButton();
        this.saveButton.setText("Save");

        // Add all the components to the Panel
        add(addDataLabel, "span, align center, wrap");
        add(this.usernameTextField, "span, align center, wrap");
        add(this.emailAddressTextField, "span, align center, wrap");
        add(this.passwordTextField, "span, align center, wrap");
        add(this.serviceTextField, "span, align center, wrap");
        add(this.additionalDataTextField, "span, align center, wrap");
        add(this.resetButton, "split 2, align center");
        add(this.saveButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        // Reset the placeholders as the form is cleared
        this.resetButton.addActionListener(e -> resetTextFields());

        // Save the data entered in the JTextFields in the database by calling the backend
        this.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // The service must be set before adding the data as it needs to be shown in the "Show Data" section
                if (serviceTextField.getText().isBlank()) {
                    return;
                }

                // Call the save data function to tell the backend to save the data into the database
                saveData(usernameTextField.getText(), emailAddressTextField.getText(),
                        passwordTextField.getText(), serviceTextField.getText(),
                        additionalDataTextField.getText());
            }
        });

        this.generatePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordTextField.setText(generateString());  // Generate the password
            }
        });
    }

    /**
     * Returns the title of the panel
     */
    public static String getPanelTitle(){
        return "Add Data";
    }

    /**
     * Reset the text fields by setting their text to be blank
     */
    private void resetTextFields(){
        this.usernameTextField.setText("");
        this.emailAddressTextField.setText("");
        this.passwordTextField.setText("");
        this.serviceTextField.setText("");
        this.additionalDataTextField.setText("");
    }

    /**
     * Save the data the user has entered into the database
     */
    private void saveData(String username, String emailAddress, String password, String service, String additionalData){
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
    private String generateString(){
        Event generatePassword = new Event("generate-string");
        Event response = this.ITC.request(generatePassword);  // Wait for the result

        return (String) response.getData().getFirst();  // Return the string generated
    }
}
