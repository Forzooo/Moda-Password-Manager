package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.components.ModernButton;
import moda.passwordmanager.frontend.components.ModernTextField;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import net.miginfocom.swing.MigLayout;
import raven.modal.Toast;

import javax.swing.*;
import java.awt.*;

public class AddData extends JPanel {

    private final InterThreadCommunication itc;

    private ModernTextField usernameTextField;
    private ModernTextField emailAddressTextField;
    private ModernTextField passwordTextField;
    private ModernTextField serviceTextField;
    private ModernTextField additionalDataTextField;

    private ModernButton resetButton;
    private ModernButton saveButton;
    private ModernButton generatePasswordButton;

    public AddData(InterThreadCommunication itc){
        super();  // Initialize the Panel

        this.itc = itc;

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
        this.usernameTextField = new ModernTextField();
        this.usernameTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.usernameTextField"));
        this.usernameTextField.setPreferredSize(textFieldDimension);

        this.emailAddressTextField = new ModernTextField();
        this.emailAddressTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.emailAddressTextField"));
        this.emailAddressTextField.setPreferredSize(textFieldDimension);

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        this.passwordTextField = new ModernTextField();
        this.passwordTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.passwordTextField"));
        this.passwordTextField.setPreferredSize(textFieldDimension);

        // Create the button for the generation of a password
        this.generatePasswordButton = new ModernButton(ModernButton.Style.WHITE);
        this.generatePasswordButton.setIcon(Utilities.getIcon("generate_string.png"));
        this.generatePasswordButton.setToolTipText(Utilities.getLocaleString("Moda.AddData.generatePasswordButtonToolTip"));

        Utilities.applyTrailingButtonProperties(this.generatePasswordButton);
        this.passwordTextField.putClientProperty("JTextField.trailingComponent", this.generatePasswordButton);

        this.serviceTextField = new ModernTextField();
        this.serviceTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.serviceTextField"));
        this.serviceTextField.setPreferredSize(textFieldDimension);

        this.additionalDataTextField = new ModernTextField();
        this.additionalDataTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.AddData.additionalDataTextField"));
        this.additionalDataTextField.setPreferredSize(textFieldDimension);

        this.resetButton = new ModernButton(ModernButton.Style.WHITE);
        this.resetButton.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 20));
        this.resetButton.setPreferredSize(new Dimension(this.resetButton.getWidth(), 60));
        this.resetButton.setText(Utilities.getLocaleString("Moda.AddData.resetButton"));

        this.saveButton = new ModernButton(ModernButton.Style.WHITE);
        this.saveButton.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 20));
        this.saveButton.setPreferredSize(new Dimension(this.resetButton.getWidth(), 60));
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
        this.saveButton.addActionListener(e -> {
            // The service must be set before adding the data as it needs to be shown in the "Show Data" section
            if (serviceTextField.getText().isBlank()) {
                return;
            }

            // Call the save data function to tell the backend to save the data into the database
            saveData(usernameTextField.getText(), emailAddressTextField.getText(),
                    passwordTextField.getText(), serviceTextField.getText(),
                    additionalDataTextField.getText());

            resetTextFields();  // Reset all the text fields after the data has been saved
        });

        this.generatePasswordButton.addActionListener(e ->passwordTextField.setText(generateString()));
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

        this.itc.send(new Event("add-data", userData));
        Utilities.showToast(getParent(), Toast.Type.SUCCESS, Utilities.getLocaleString("Moda.Toast.addDataSuccessful"));
    }

    /**
     * Generate a random string
     */
    private String generateString(){
        Event generatePassword = new Event("generate-string");
        Event response = this.itc.request(generatePassword);  // Wait for the result

        return (String) response.getData().getFirst();  // Return the string generated
    }
}