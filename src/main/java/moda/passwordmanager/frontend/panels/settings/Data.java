package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Data extends Section {

    private JButton changeMasterPasswordButton;  // Change the master password of the current database4

    private JTextField stringLengthTextField;
    private JCheckBox stringLettersCheckbox;
    private JCheckBox stringNumbersCheckbox;
    private JCheckBox stringSpecialCharactersCheckbox;
    private JButton configureStringGenerationButton;

    public Data(InterThreadCommunication itc){
        super("Data", itc);

        initComponents();
        initListeners();
        setDefaultStringGenerationConfiguration();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private void initComponents(){
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

        this.changeMasterPasswordButton = new JButton();
        this.changeMasterPasswordButton.setText("Change the master password");
        this.changeMasterPasswordButton.setMaximumSize(buttonDimension);

        this.stringLengthTextField = new JTextField();
        this.stringLengthTextField.putClientProperty("JTextField.placeholderText", "String length");

        this.stringLettersCheckbox = new JCheckBox();
        this.stringLettersCheckbox.setText("Include letters (a-zA-Z)");

        this.stringNumbersCheckbox = new JCheckBox();
        this.stringNumbersCheckbox.setText("Include numbers (0-9)");

        this.stringSpecialCharactersCheckbox = new JCheckBox();
        this.stringSpecialCharactersCheckbox.setText("Include special characters (!?,...)");

        this.configureStringGenerationButton = new JButton();
        this.configureStringGenerationButton.setText("Change");
        this.configureStringGenerationButton.setMaximumSize(buttonDimension);

        addOption(this.changeMasterPasswordButton);

        addSection("String generation");
        addOption(this.stringLengthTextField);
        addOption(this.stringLettersCheckbox);
        addOption(this.stringNumbersCheckbox);
        addOption(this.stringSpecialCharactersCheckbox);
        addOption(this.configureStringGenerationButton);
    }

    private void initListeners(){
        this.changeMasterPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the master password of the database before using it
                String masterPassword = JOptionPane.showInputDialog(getRootPane(), "Enter the new master password",
                        "");

                // Initial checks on the master password entered to ensure that it is a valid string, otherwise abort the
                // operation
                if (masterPassword == null || masterPassword.isBlank()){
                    return;
                }

                changeMasterPassword(masterPassword);
            }
        });

        this.configureStringGenerationButton.addActionListener(e -> configureStringGeneration());
    }

    /**
     * Change the current master password of the database to a new one
     * @param masterPassword The new master password
     */
    private void changeMasterPassword(String masterPassword){
        // Perform some initial conditions check on the master password
        if (!Utilities.checkMasterPassword(masterPassword.toCharArray())){
            return;
        }

        Event event = new Event("change-master-password", masterPassword.toCharArray());
        getITC().request(event);  // Wait for the end of the operations in the backend
    }

    /**
     * Configure the string generation
     */
    private void configureStringGeneration(){
        // Retrieve the data from the user
        int stringLength = Integer.parseInt(this.stringLengthTextField.getText());  // Convert the text to an int
        boolean lettersSelected = this.stringLettersCheckbox.isSelected();
        boolean numbersSelected = this.stringNumbersCheckbox.isSelected();
        boolean specialCharactersSelected = this.stringSpecialCharactersCheckbox.isSelected();

        // Create the Event with the data
        Event event = new Event("configure-string-generation");
        event.addData(stringLength);
        event.addData(lettersSelected);
        event.addData(numbersSelected);
        event.addData(specialCharactersSelected);

        getITC().send(event);  // Send the event
    }

    /**
     * Set the default values of the string configuration
     */
    private void setDefaultStringGenerationConfiguration(){
        // Create the event and wait for the data
        Event event = new Event("get-string-generation-configuration");
        Event backendResponse = getITC().request(event);

        ArrayList<Object> data = backendResponse.getData();  // Retrieve the data

        // Set the data to the components
        this.stringLengthTextField.setText(String.valueOf(data.getFirst()));
        this.stringLettersCheckbox.setSelected((boolean) data.get(1));
        this.stringNumbersCheckbox.setSelected((boolean) data.get(2));
        this.stringSpecialCharactersCheckbox.setSelected((boolean) data.get(3));
    }

}
