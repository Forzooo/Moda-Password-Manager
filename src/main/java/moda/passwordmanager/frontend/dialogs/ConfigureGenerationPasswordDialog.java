package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.interthreadcommunication.EventType;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.frontend.components.Placeholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * A JDialog used to retrieve the parameters of the generation of the password
 */
public class ConfigureGenerationPasswordDialog extends JDialog {

    private InterThreadCommunication interThreadCommunication;

    // Dialog components
    private JTextField passwordLengthTextField;
    private Placeholder passwordLengthPlaceholder;

    private JCheckBox lettersCheckBox;
    private JCheckBox numbersCheckBox;
    private JCheckBox specialCharactersCheckBox;

    private JButton saveConfigurationButton;

    public ConfigureGenerationPasswordDialog(InterThreadCommunication interThreadCommunication){
        super();

        this.interThreadCommunication = interThreadCommunication;

        initDialog();
        initComponents();
        setCurrentParameters();
        initListeners();
    }

    /**
     * Get the layout used for the Dialog
     *
     * @return BoxLayout
     */
    private BoxLayout getDialogLayout() {
        // The target is the Dialog so to use it we retrieve it from the content pane otherwise we would use the
        // entire container including other JPanels
        return new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setLayout(getDialogLayout());  // Set its layout
        setTitle("Configuration of the password");
        setSize(new Dimension(400, 350));  // Set the preferred size
        setBackground(Color.WHITE);
        setIconImage(Frontend.getIcon());
    }

    /**
     * Initialize and add all the Swing components of the Dialog
     */
    private void initComponents(){
        // All the Swing components are inside a panel to allow more stylization over the ones that dialog provides
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(700, 500));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel configurePasswordLabel = new JLabel();
        configurePasswordLabel.setText("Configure the password generation");
        configurePasswordLabel.setFont(new Font("Arial MT Bold", Font.BOLD, 18));

        this.passwordLengthTextField = new JTextField();
        this.passwordLengthTextField.setMaximumSize(new Dimension(400, 30));
        this.passwordLengthPlaceholder = new Placeholder(this.passwordLengthTextField, "Enter the password length");
        this.passwordLengthPlaceholder.show();

        this.lettersCheckBox = new JCheckBox();
        this.lettersCheckBox.setText("Include letters (a-z, A-Z)");

        this.numbersCheckBox = new JCheckBox();
        this.numbersCheckBox.setText("Include numbers (0-9)");

        this.specialCharactersCheckBox = new JCheckBox();
        this.specialCharactersCheckBox.setText("Include special characters (!?.,)");

        this.saveConfigurationButton = new JButton();
        this.saveConfigurationButton.setText("Save configuration");

        // Adding all the components to the panel
        panel.add(configurePasswordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(this.passwordLengthTextField);
        panel.add(this.lettersCheckBox);
        panel.add(this.numbersCheckBox);
        panel.add(this.specialCharactersCheckBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(this.saveConfigurationButton);

        // Adding the panel to the dialog
        add(panel);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        // Save the user configuration by sending the options to the backend
        this.saveConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Retrieve the data from the user
                int stringLength = Integer.parseInt(passwordLengthTextField.getText());  // Convert the text to an int
                boolean lettersSelected = lettersCheckBox.isSelected();
                boolean numbersSelected = numbersCheckBox.isSelected();
                boolean specialCharactersSelected = specialCharactersCheckBox.isSelected();

                // Create the Event with the data
                Event event = new Event("configure-string-generation", EventType.REQUEST);
                event.addData(stringLength);
                event.addData(lettersSelected);
                event.addData(numbersSelected);
                event.addData(specialCharactersSelected);

                interThreadCommunication.send(event);  // Send the event
                interThreadCommunication.receive(Frontend.getBackendEventResponse());  // Wait for the event to be completed
//                notifyUser();  // Example method to show the user a messagebox with the operation status
                dispose();  // Destroy the JDialog after the configuration has been saved
            }
        });
    }

    /**
     * Ask the backend for the parameters currently being used by the user to set them in the components
     */
    private void setCurrentParameters(){
        // Create the event and wait for the data
        Event event = new Event("get-string-generation-configuration", EventType.REQUEST);
        this.interThreadCommunication.send(event);
        Event backendResponse = this.interThreadCommunication.receive(Frontend.getBackendEventResponse());

        ArrayList data = backendResponse.getData();  // Retrieve the data

        // Set the data to the components
        this.passwordLengthPlaceholder.hide();  // Hide the placeholder first
        this.passwordLengthTextField.setText(String.valueOf(data.getFirst()));
        this.lettersCheckBox.setSelected((boolean) data.get(1));
        this.numbersCheckBox.setSelected((boolean) data.get(2));
        this.specialCharactersCheckBox.setSelected((boolean) data.get(3));
    }

}
