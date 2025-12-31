package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;

public class UserPasswordField extends UserDataField {

    private final InterThreadCommunication ITC;
    private JButton generatePasswordButton;

    /**
     * Set the configuration of the Panel
     */
    public UserPasswordField(String dataField, InterThreadCommunication itc) {
        super(dataField);
        this.ITC = itc;

        initComponents();
        initListeners();
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        this.generatePasswordButton = new JButton("Generate a Password");
        this.generatePasswordButton.setToolTipText("Generate a password");
        this.generatePasswordButton.setVisible(false);  // The button is shown only when the editing mode is enabled
        this.generatePasswordButton.setPreferredSize(new Dimension(70, 70));
        this.generatePasswordButton.setMaximumSize(new Dimension(70, 70));

        add(this.generatePasswordButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.generatePasswordButton.addActionListener(e -> generatePassword());
    }

    /**
     * Generate a password and set the password text field to it
     */
    private void generatePassword(){
        Event event = this.ITC.request(new Event("generate-string"));
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
        this.generatePasswordButton.setVisible(true);
        this.generatePasswordButton.setEnabled(true);
    }

    /**
     * Set the data to be read-only
     */
    @Override
    public void disableEditing(){
        super.disableEditing();

        // The generate password button is shown only in editing mode
        this.generatePasswordButton.setVisible(false);
        this.generatePasswordButton.setEnabled(false);
    }
}
