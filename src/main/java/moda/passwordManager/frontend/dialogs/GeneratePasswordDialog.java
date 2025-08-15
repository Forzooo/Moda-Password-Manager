package moda.passwordManager.frontend.dialogs;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.components.Placeholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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

        // Set the preferred size
        setSize(new Dimension(400, 350));

        setBackground(Color.WHITE);
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
     * Initialize all the Action Listeners of the components
     */
    private void initActionListener(){
        // Save the user configuration by sending the options to the backend
        this.saveConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Retrieve the data from the user
                int stringLength = Integer.parseInt(passwordLengthTextField.getText());  // Convert the text to an int
                char[] stringCharacters = createStringCharacters();

                // Add the data to send with the Event
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

        char[] specialCharacters = {
            '!', '?', '.', ',', '#', '$', '%', '&', '\'', '"', '(', ')', '+',
            '-', '*', ':', ';', '@', '^', '_', '[', ']', '{', '}', '<', '>'
        };

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

        // Convert the ArrayList to a char array for compatibility with string generation of the backend
        char[] stringCharacters = new char[stringCharactersArrayList.size()];

        for (int i = 0; i < stringCharacters.length; i++){
            stringCharacters[i] = stringCharactersArrayList.get(i);
        }

        return stringCharacters;
    }
}
