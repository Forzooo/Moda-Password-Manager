package moda.passwordManager.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*
The Board class is defined as two parts: the left one and the right one.
The left one is a sidebar which is static, that means it won't change its appearance during the execution,
while the right side is defined based on the button selected on the sidebar, thus it's dynamic and needs
a proper handling using the switchPanel() method
 */
public class Frontend extends JPanel implements ActionListener {

    // Define the Timer and its delay used to synchronize between multiple devices using GDrive
    private Timer timer;
    private final int DELAY = 1500;

    // The dynamicState indicates which Panel needs to be switched to from the current one selected
    private GUIState dynamicState;
    private JPanel currentPanel;

    /*
    A Dimension attribute, retrieved from getToolkit().getScreenSize(), used to dynamically resize
    the components of the window
     */
    private Dimension windowSize;

    // All the JPanel of the GUI, defined as class attributes
    private JPanel sidebarPanel;
    private JPanel addDataPanel;
    private JPanel showAllPanel;
    private JPanel settingsPanel;

    public Frontend(int width, int height){
        setPreferredSize(new Dimension(width, height));  // Set the initial dimension of the Frame

        // Initialize all the Panels
        initBoard();  // Set the properties of the Board
        initSidebarPanel();
        initAddDataPanel();
        initShowDataPanel();
        initSettingsPanel();
    }

    private void initBoard(){
        setFocusable(true);  // Set the focus on the frame to get the keyboard inputs
        setLayout(new BorderLayout());  // The layout for the Board is the Border one
//        addKeyListener(new TAdapter());

        // Set the initial state of the dynamic part to Show All Panel
        this.dynamicState = GUIState.SHOW_DATA;

        this.windowSize = getToolkit().getScreenSize();  // Get the initial size of the window

        this.timer = new Timer(DELAY, this::actionPerformed);
        this.timer.start();
    }

    // Initialize all the components of the Sidebar Panel
    private void initSidebarPanel(){

        this.sidebarPanel = new JPanel();  // Create the Sidebar panel

        this.sidebarPanel.setPreferredSize(new Dimension((int) this.windowSize.getWidth()/5, (int) this.windowSize.getHeight()));
//        this.sidebarPanel.setBackground(dark);
        this.sidebarPanel.setBackground(Color.BLACK);

        // Set the margin of the Sidebar [top: 60, bottom: 60, left: 30, right: 30]
        this.sidebarPanel.setBorder(BorderFactory.createEmptyBorder(60,30,60,30));

        // Set the layout of the Sidebar to BoxLayout giving importance to the Y axis
        this.sidebarPanel.setLayout(new BoxLayout(this.sidebarPanel, BoxLayout.Y_AXIS));

        add(this.sidebarPanel, BorderLayout.WEST);  // Add the Sidebar to the Panel

        // Create the JLabel that displays the name of the Password Manager
        JLabel passwordManagerLabel = new JLabel();
        passwordManagerLabel.setText("MODA");
//        passwordManagerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // Set the text-alignment to center
        passwordManagerLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 80));  // Set the font of the label
//        passwordManagerLabel.setForeground(light);  // Set the color of the label
        passwordManagerLabel.setForeground(Color.WHITE);  // Set the color of the label

        // Create the JButtons used to switch between JPanels of the dynamic part
        Dimension buttonDimension = new Dimension(350, 50);

        Button addDataButton = new Button();
        addDataButton.setText("Add Data");
        addDataButton.setMaximumSize(buttonDimension);
        addDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.ADD_DATA;
                switchPanel();
            }
        });

        Button showDataButton = new Button();
        showDataButton.setText("Show Data");
        showDataButton.setMaximumSize(buttonDimension);
        showDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.SHOW_DATA;
                switchPanel();
            }
        });

        Button settingsButton = new Button();  // TODO: Use the settings icon instead of the text
        settingsButton.setText("Settings");
        settingsButton.setMaximumSize(buttonDimension);
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.SETTINGS;
                switchPanel();
            }
        });

        // Add the components to the Sidebar
        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add RigidArea to add spacing between components
        this.sidebarPanel.add(passwordManagerLabel);
        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, (int) (this.windowSize.getHeight()/5))));
        this.sidebarPanel.add(addDataButton);
        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        this.sidebarPanel.add(showDataButton);
        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, (int) this.windowSize.getHeight()/6)));
        this.sidebarPanel.add(settingsButton);
        this.sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));

    }

    // Initialize all the components of the Add Data Panel
    private void initAddDataPanel(){

        this.addDataPanel = new JPanel();

        this.addDataPanel.setLayout(new BoxLayout(this.addDataPanel, BoxLayout.Y_AXIS));
        this.addDataPanel.setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - this.sidebarPanel.getWidth()), (int) this.windowSize.getHeight()));
//        this.addDataPanel.setBackground(light);
        this.addDataPanel.setBackground(Color.WHITE);

        // Set the margin of the Add Data Panel [top: 60, bottom: 60, left: 30, right: 30]
        this.addDataPanel.setBorder(BorderFactory.createEmptyBorder(60,30,60,30));

        JLabel addDataLabel = new JLabel();
        addDataLabel.setText("Add a new data:");
        addDataLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Create all the JTextField for the data input
        Dimension textFieldDimension = new Dimension(1600, 30);  // Define the dimension of any JTextField

        JTextField usernameTextField = new JTextField();
        usernameTextField.setMaximumSize(textFieldDimension);
        Placeholder usernamePlaceholder = new Placeholder(usernameTextField, "Username");

        JTextField emailAddressTextField = new JTextField();
        emailAddressTextField.setMaximumSize(textFieldDimension);
        Placeholder emailAddressPlaceholder = new Placeholder(emailAddressTextField, "Email Address");

        // The password field is not a JPasswordField because the user needs to know the password being entered in the database
        JTextField passwordTextField = new JTextField();
        passwordTextField.setMaximumSize(textFieldDimension);
        Placeholder passwordPlaceholder = new Placeholder(passwordTextField, "Password");

        JTextField serviceTextField = new JTextField();
        serviceTextField.setMaximumSize(textFieldDimension);
        Placeholder servicePlaceholder = new Placeholder(serviceTextField, "Service");

        JTextField additionalDataTextField = new JTextField();
        additionalDataTextField.setMaximumSize(textFieldDimension);
        Placeholder additionalDataPlaceholder = new Placeholder(additionalDataTextField, "Additional Data");

        // Create the MButton for Reset and Confirm operations
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any MButton

        Button resetButton = new Button();
        resetButton.setText("Reset");
        resetButton.setMaximumSize(buttonDimension);
        resetButton.addActionListener(  // When the Reset button is clicked then all the JTextFields are reset
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Reset the JTextFields by showing their placeholder
                        usernamePlaceholder.showPlaceholder();
                        emailAddressPlaceholder.showPlaceholder();
                        passwordPlaceholder.showPlaceholder();
                        servicePlaceholder.showPlaceholder();
                        additionalDataPlaceholder.showPlaceholder();
                    }
                }
        );

        Button saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setMaximumSize(buttonDimension);

        // Save the data entered in the JTextField in the database after some cryptographic operations
        saveButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // If at least one placeholder is enabled then don't allow the data to be saved
                        if(usernamePlaceholder.isShowPlaceholderFlag() || emailAddressPlaceholder.isShowPlaceholderFlag() ||
                                passwordPlaceholder.isShowPlaceholderFlag() || servicePlaceholder.isShowPlaceholderFlag() ||
                                additionalDataPlaceholder.isShowPlaceholderFlag()
                        ){
                            return;
                        }

                        // Call the save data function which will encrypt the data and save it into the database
//                        saveData(usernameTextField.getText(), emailAddressTextField.getText(),
//                                passwordTextField.getText(), serviceTextField.getText(),
//                                additionalDataTextField.getText()
//                        );

                        // Reset the placeholder after the data has been saved
                        usernamePlaceholder.showPlaceholder();
                        emailAddressPlaceholder.showPlaceholder();
                        passwordPlaceholder.showPlaceholder();
                        servicePlaceholder.showPlaceholder();
                        additionalDataPlaceholder.showPlaceholder();
                    }
                }
        );

        // Add all the components to the Panel
        this.addDataPanel.add(addDataLabel);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));  // Add RigidArea to add spacing between components
        this.addDataPanel.add(usernameTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(emailAddressTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(passwordTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(serviceTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(additionalDataTextField);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.addDataPanel.add(resetButton);
        this.addDataPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.addDataPanel.add(saveButton);

    }

    // Initialize all the components of the Show All Panel
    private void initShowDataPanel(){
        this.showAllPanel = new JPanel();
        this.showAllPanel.setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - this.sidebarPanel.getWidth()), (int) this.windowSize.getHeight()));

        // Create the List Model for the JList and read all the data from the database
//        DefaultListModel<Data> dataModel = new DefaultListModel<>();

//        for (int row = 1; row <= this.database.getRowsNumber(); row++){
//            dataModel.addElement(this.database.getData(row));
//        }

        // Create the JList used to show all the data saved inside the database
        JList dataList = new JList();
//        dataList.setModel(dataModel);  // Set the Model of the JList to the one created
        dataList.setPreferredSize(new Dimension(300, 250));

        dataList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                Data data = dataModel.getElementAt(dataList.getSelectedIndex());
//                cryptography.setMasterPassword(masterPassword);
//
//                JOptionPane.showMessageDialog(showAllPanel,
//                        "The data retrieved is: \n" +
//                                "- Username: " + Data.encodeToString(cryptography.decrypt(Data.decode(data.getUSERNAME()))) + "\n" +
//                                "- Email Address: " + Data.encodeToString(cryptography.decrypt(Data.decode(data.getEMAIL_ADDRESS()))) + "\n" +
//                                "- Password: " + Data.encodeToString(cryptography.decrypt(Data.decode(data.getPASSWORD()))) + "\n" +
//                                "- Service: " + Data.encodeToString(cryptography.decrypt(Data.decode(data.getSERVICE()))) + "\n" +
//                                "- Additional Data: " + Data.encodeToString(cryptography.decrypt(Data.decode(data.getADDITIONAL_DATA()))) + "\n"
//                );
            }
        });


        // Add the Show All Panel to the Board as it's the default panel at the start
        this.currentPanel = this.showAllPanel;
        add(this.showAllPanel, BorderLayout.CENTER);

        // Add all the components to the JPanel
        this.showAllPanel.add(dataList);
    }

    // Initialize all the components of the Settings Panel
    private void initSettingsPanel(){
        this.settingsPanel = new JPanel();
        this.settingsPanel.setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - this.sidebarPanel.getWidth()), (int) this.windowSize.getHeight()));
        this.settingsPanel.setBackground(Color.RED);
    }

    // This method is used to switch to a new panel hiding the previous one
    private void switchPanel(){
        remove(this.currentPanel);  // Remove the current (old) panel from the Board

        // Based on the section chosen change the current panel to the new one
        switch (this.dynamicState){
            case ADD_DATA -> this.currentPanel = this.addDataPanel;
            case SHOW_DATA -> this.currentPanel = this.showAllPanel;
            case SETTINGS -> this.currentPanel = this.settingsPanel;
        }

        add(this.currentPanel, BorderLayout.CENTER);  // Add the new panel to the Board
        revalidate();
        repaint();
    }

    // TODO: Add Google Drive synchronization if enabled by the user
    // Method executed by the timer
    @Override
    public void actionPerformed(ActionEvent e) {

    }
}