package moda.passwordManager.frontend.dialogs;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.components.Placeholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A JDialog used to retrieve the parameters of the generation of the password
 */
public class GeneratePasswordDialog extends JDialog {

    private CommunicationHandler communicationHandler;

    // Dialog components
    private JTextField passwordLengthTextField;
    private Placeholder passwordLengthPlaceholder;

    private JCheckBox lettersCheckBox;
    private JCheckBox numbersCheckBox;
    private JCheckBox specialCharactersCheckBox;

    private JButton saveConfigurationButton;

    public GeneratePasswordDialog(CommunicationHandler communicationHandler){
        super();

        this.communicationHandler = communicationHandler;

        initDialog();
        initComponents();
        initActionListener();
    }

    /**
     * Get the layout used for the Dialog
     *
     * @return BoxLayout
     */
    private FlowLayout getDialogLayout() {
        return new FlowLayout();  // TODO: Set a proper layout
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setLayout(getDialogLayout());  // Set its layout

        setTitle("Configuration of the password");

        // Set the preferred size
        setSize(new Dimension(700, 700));  // TODO: Set a proper dimension

        setBackground(Color.WHITE);
    }

    /**
     * Initialize and add all the Swing components of the Dialog
     */
    private void initComponents(){
        this.passwordLengthTextField = new JTextField();
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

        add(this.passwordLengthTextField);
        add(this.lettersCheckBox);
        add(this.numbersCheckBox);
        add(this.specialCharactersCheckBox);
        add(this.saveConfigurationButton);
    }

    /**
     * Initialize all the Action Listeners of the components
     */
    private void initActionListener(){
        // TODO: Add check on the user input to allow only numbers
        this.passwordLengthTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        this.saveConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int stringLength = Integer.parseInt(passwordLengthTextField.getText());  // Convert the text to an int

                char[] stringCharacters = createStringCharacters();

                // Create the data to send with the Event
                ArrayList dataToSend = new ArrayList();
                dataToSend.add(stringLength);
                dataToSend.add(stringCharacters);

                // Create the Event
                Event event = new Event("configure-string-generation", dataToSend);
                communicationHandler.send(event);  // Send the event
                communicationHandler.receive();  // Wait for the event to be completed
//                notifyUser();  // Example method to show the user a messagebox with the operation status
                dispose();  // Destroy the JDialog after the configuration has been saved
            }
        });
    }

    private char[] createStringCharacters(){
        // Initialize the arrays with the different options of the characters
        char[] letters = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };

        char[] numbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        char[] specialCharacters = {'!', '?', '.', ','};

        // Define the set as an ArrayList as it's easier to handle
        ArrayList<Character> stringCharactersArrayList = new ArrayList<>();

        if (lettersCheckBox.isSelected()){
            for (char letter : letters){
                stringCharactersArrayList.add(letter);
            }
        }

        if (numbersCheckBox.isSelected()){
            for (char number : numbers){
                stringCharactersArrayList.add(number);
            }
        }

        if (specialCharactersCheckBox.isSelected()){
            for (char specialCharacter : specialCharacters){
                stringCharactersArrayList.add(specialCharacter);
            }
        }

        // Convert the ArrayList to a char array
        char[] stringCharacters = new char[stringCharactersArrayList.size()];

        for (int i = 0; i < stringCharacters.length; i++){
            stringCharacters[i] = stringCharactersArrayList.get(i);
        }

        return stringCharacters;
    }
}
