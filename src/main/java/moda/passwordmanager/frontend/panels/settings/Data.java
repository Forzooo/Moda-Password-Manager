package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import raven.modal.Toast;

import javax.swing.*;
import java.util.List;

public class Data extends Section {

    // Change the master password of the current database
    private JPasswordField updateMasterPasswordTextField;
    private JButton updateMasterPasswordButton;

    private JTextField stringLengthTextField;
    private JCheckBox stringLettersCheckbox;
    private JCheckBox stringNumbersCheckbox;
    private JCheckBox stringSpecialCharactersCheckbox;
    private JButton configureStringGenerationButton;

    public Data(InterThreadCommunication itc){
        super(Utilities.getLocaleString("Moda.Data.panelTitle"), itc);

        initComponents();
        setSelectedSettings();
        initListeners();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private void initComponents(){
        JPanel updateMasterPasswordPanel = new JPanel();

        JLabel updateMasterPasswordLabel = new JLabel();
        updateMasterPasswordLabel.setText(Utilities.getLocaleString("Moda.Data.updateMasterPasswordLabel"));

        this.updateMasterPasswordTextField = new JPasswordField();
        this.updateMasterPasswordTextField.setPreferredSize(getTextFieldDimension());
        this.updateMasterPasswordTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.Data.updateMasterPasswordTextFieldPlaceholder"));

        this.updateMasterPasswordButton = new JButton();
        this.updateMasterPasswordButton.setText(Utilities.getLocaleString("Moda.Data.updateMasterPasswordButton"));
        this.updateMasterPasswordButton.setMaximumSize(getButtonDimension());

        this.stringLengthTextField = new JTextField();
        this.stringLengthTextField.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.Data.stringLengthTextField"));

        this.stringLettersCheckbox = new JCheckBox();
        this.stringLettersCheckbox.setText(Utilities.getLocaleString("Moda.Data.stringLettersCheckbox"));

        this.stringNumbersCheckbox = new JCheckBox();
        this.stringNumbersCheckbox.setText(Utilities.getLocaleString("Moda.Data.stringNumbersCheckbox"));

        this.stringSpecialCharactersCheckbox = new JCheckBox();
        this.stringSpecialCharactersCheckbox.setText(Utilities.getLocaleString("Moda.Data.stringSpecialCharactersCheckbox"));

        this.configureStringGenerationButton = new JButton();
        this.configureStringGenerationButton.setText(Utilities.getLocaleString("Moda.Data.configureStringGenerationButton"));
        this.configureStringGenerationButton.setMaximumSize(getButtonDimension());

        updateMasterPasswordPanel.add(this.updateMasterPasswordTextField);
        updateMasterPasswordPanel.add(this.updateMasterPasswordButton);

        addOption(updateMasterPasswordLabel);
        addOption(updateMasterPasswordPanel);

        addSection(Utilities.getLocaleString("Moda.Data.stringGenerationSection"));
        addOption(this.stringLengthTextField, "grow");
        addOption(this.stringLettersCheckbox);
        addOption(this.stringNumbersCheckbox);
        addOption(this.stringSpecialCharactersCheckbox);
        addOption(this.configureStringGenerationButton);
    }

    @Override
    protected void setSelectedSettings() {
        Event stringConfigurationEvent = getItc().request(new Event("get-string-generation"));

        List<Object> data = stringConfigurationEvent.getData();  // Retrieve the data

        // Set the data to the components
        this.stringLengthTextField.setText(String.valueOf(data.getFirst()));
        this.stringLettersCheckbox.setSelected((boolean) data.get(1));
        this.stringNumbersCheckbox.setSelected((boolean) data.get(2));
        this.stringSpecialCharactersCheckbox.setSelected((boolean) data.get(3));
    }

    private void initListeners(){
        this.updateMasterPasswordButton.addActionListener(e -> updateMasterPassword());

        this.configureStringGenerationButton.addActionListener(e -> setStringGeneration());
    }

    /**
     * Change the current master password of the database to a new one
     */
    private void updateMasterPassword(){
        // Retrieve the master password from the password field
        char[] masterPassword = this.updateMasterPasswordTextField.getPassword();

        // Perform some initial conditions check on the master password
        if (!Utilities.checkMasterPassword(masterPassword)){
            Utilities.showToast(getParent(), Toast.Type.ERROR, Utilities.getLocaleString("Moda.Toast.updateMasterPasswordError"));
            return;
        }

        Event event = new Event("update-master-password", masterPassword);
        getItc().request(event);  // Wait for the end of the operations in the backend
        Utilities.showToast(getParent(), Toast.Type.SUCCESS, Utilities.getLocaleString("Moda.Toast.updateMasterPassword"));
    }

    /**
     * Set the string generation parameters in the backend
     */
    private void setStringGeneration(){
        // Ensure that the user has entered an integer, otherwise an exception would be arisen
        int stringLength;
        try{
            stringLength = Integer.parseInt(this.stringLengthTextField.getText());  // Convert the text to an int
        } catch (NumberFormatException e) {
            Utilities.showToast(getRootPane(), Toast.Type.ERROR, Utilities.getLocaleString("Moda.Toast.stringGenerationIntegerError"));
            return;
        }

        // The length of the string to generate must be at least 1
        if (stringLength < 1){
            Utilities.showToast(getRootPane(), Toast.Type.ERROR, Utilities.getLocaleString("Moda.Toast.stringGenerationLengthError"));
            return;
        }

        boolean lettersSelected = this.stringLettersCheckbox.isSelected();
        boolean numbersSelected = this.stringNumbersCheckbox.isSelected();
        boolean specialCharactersSelected = this.stringSpecialCharactersCheckbox.isSelected();

        // Ensure that at least one of the three checkbox is selected, otherwise an exception will arise in the backend
        // when the user tries to generate a string
        if (!lettersSelected && !numbersSelected && !specialCharactersSelected){
            Utilities.showToast(getRootPane(), Toast.Type.ERROR, Utilities.getLocaleString("Moda.Toast.stringGenerationOptionsError"));

            return;
        }

        // Create the Event with the data
        Event event = new Event("set-string-generation");
        event.addData(stringLength);
        event.addData(lettersSelected);
        event.addData(numbersSelected);
        event.addData(specialCharactersSelected);

        getItc().send(event);  // Send the event
        Utilities.showToast(getRootPane(), Toast.Type.SUCCESS, "Moda.Toast.stringGeneration");
    }
}
