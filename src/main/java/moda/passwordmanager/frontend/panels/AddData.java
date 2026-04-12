package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.components.ModaButton;
import moda.passwordmanager.frontend.components.ModaTextField;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddData extends JPanel {

    private final InterThreadCommunication ITC;

    private ModaTextField usernameTextField;
    private ModaTextField emailAddressTextField;
    private ModaTextField passwordTextField;
    private ModaTextField serviceTextField;
    private ModaTextField additionalDataTextField;

    private ModaButton resetButton;
    private ModaButton saveButton;
    private ModaButton generatePasswordButton;

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
        setLayout(new MigLayout("fillx", "[al center]"));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        JLabel addDataLabel = new JLabel();
        addDataLabel.setText("Add your data");
        addDataLabel.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 32));

        Dimension textFieldDimension = new Dimension(600, 60);  // The dimension of each text field

        // Create all the JTextField for the data input
        this.usernameTextField = new ModaTextField();
        this.usernameTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.usernameTextField"));
        this.usernameTextField.setPreferredSize(textFieldDimension);

        this.emailAddressTextField = new ModaTextField();
        this.emailAddressTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.emailAddressTextField"));
        this.emailAddressTextField.setPreferredSize(textFieldDimension);

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        this.passwordTextField = new ModaTextField();
        this.passwordTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.passwordTextField"));
        this.passwordTextField.setPreferredSize(textFieldDimension);

        // Create the button for the generation of a password
        this.generatePasswordButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE);
        this.generatePasswordButton.setIcon(Utilities.getIcon("generate_string.png"));
        this.generatePasswordButton.setToolTipText(Utilities.getLocaleString("Moda.AddData.generatePasswordButtonToolTip"));

        Utilities.applyTrailingButtonProperties(this.generatePasswordButton);
        this.passwordTextField.putClientProperty("JTextField.trailingComponent", this.generatePasswordButton);

        this.serviceTextField = new ModaTextField(textFieldDimension.height);
        this.serviceTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.serviceTextField"));
        this.serviceTextField.setPreferredSize(textFieldDimension);

        this.additionalDataTextField = new ModaTextField();
        this.additionalDataTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.additionalDataTextField"));
        this.additionalDataTextField.setPreferredSize(textFieldDimension);

        this.resetButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, 60, 20);
        this.resetButton.setText(Utilities.getLocaleString("Moda.AddData.resetButton"));

        this.saveButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, 60, 20);
        this.saveButton.setText(Utilities.getLocaleString("Moda.AddData.saveButton"));

        // Add all the components to the Panel
        add(addDataLabel, "span, wrap 50");
        add(this.usernameTextField, "span, wrap 20");
        add(this.emailAddressTextField, "span, wrap 20");
        add(this.passwordTextField, "span, wrap 20");
        add(this.serviceTextField, "span, wrap 20");
        add(this.additionalDataTextField, "span, wrap 30");
        add(this.resetButton, "split 2");
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

                resetTextFields();  // Reset all the text fields after the data has been saved
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
        return Utilities.getLocaleString("Moda.AddData.panelTitle");
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
