package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;

public class UserPasswordField extends UserDataField {

    private final InterThreadCommunication itc;
    private JButton generatePasswordButton;

    /**
     * Set the configuration of the Panel
     */
    public UserPasswordField(String dataField, InterThreadCommunication itc) {
        super(dataField);
        this.itc = itc;

        initComponents();
        initListener();
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        this.generatePasswordButton = new JButton();
        this.generatePasswordButton.setIcon(Utilities.getIcon("generate_string.png"));
        this.generatePasswordButton.setToolTipText(Utilities.getLocaleString("Moda.UserPasswordField.generatePasswordButton"));
        Utilities.applyTrailingButtonProperties(this.generatePasswordButton);
    }

    /**
     * Initialize the listener on the component
     */
    private void initListener(){
        this.generatePasswordButton.addActionListener(e -> generatePassword());
    }

    /**
     * Generate a password and set the password text field to it
     */
    private void generatePassword(){
        Event event = this.itc.request(new Event("generate-string"));
        String password = (String) event.getData().getFirst();
        setText(password);  // Set the text to be the generated password
    }

    /**
     * Set the data to be editable
     */
    @Override
    public void enableEditing(){
        super.enableEditing();

        // The generate password button is shown only in editing mode
        getDataField().putClientProperty("JTextField.trailingComponent", this.generatePasswordButton);
    }

    /**
     * Set the data to be read-only
     */
    @Override
    public void disableEditing(){
        super.disableEditing();

        // The generate password button is shown only in editing mode, thus we put again the copy button instead
        getDataField().putClientProperty("JTextField.trailingComponent", getCopyButton());
    }
}
