package moda.passwordManager.frontend.panels;

import moda.passwordManager.backend.Data;
import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ShowDataPanel extends JPanel {

    // Attribute to communicate with the backend
    private CommunicationHandler communicationHandler;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    /**
     * The service_data shown in the JList of "Show Data" panel <br/>
     * It's updated automatically by the timer
     */
    private ArrayList<Data> userData;  // A Data object is required as each service shown needs to be associated with its ID
    private DefaultListModel<String> userDataModel;

    // Swing components
    private JList dataList;

    public ShowDataPanel(CommunicationHandler communicationHandler, int MIN_CONTENT_WIDTH, Dimension windowSize, int sidebarPanelWidth) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.communicationHandler = communicationHandler;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        // Initialize the user data ArrayList and Model
        this.userData = new ArrayList<>();
        this.userDataModel = new DefaultListModel<>();

        initPanel(sidebarPanelWidth);
        initComponents();
        initActionListener();
    }

    /**
     * Get the layout used for the panel
     * @return BorderLayout
     */
    private BorderLayout getPanelLayout() {
        return new BorderLayout();
    }

    @Override
    public Dimension getMinimumSize() {
        // altezza 0 -> “qualsiasi”, conta solo la larghezza minima
        return new Dimension(MIN_CONTENT_WIDTH, 0);
    }

    /**
     * Set the configuration of the JPanel
     * @param sidebarPanelWidth
     */
    private void initPanel(int sidebarPanelWidth){
        setLayout(getPanelLayout());  // Set its layout

        // Set the preferred size
        setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - sidebarPanelWidth), (int) this.windowSize.getHeight()));

        setBackground(Color.WHITE);
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        // Create the JList used to show all the data saved inside the database
        this.dataList = new JList();
        this.dataList.setFixedCellHeight(30);
        this.dataList.setModel(this.userDataModel);  // Set the model of the JList (Strings containing service data)
        this.dataList.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));

        dataList.setSelectionBackground(Color.black);
        dataList.setSelectionForeground(Color.white);

        dataList.setCellRenderer(new DefaultListCellRenderer(){ //imposto un metodo per far renderizzare le celle della lista come mi pare
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) { //questo non so bene cosa sia, ma nell'esempio che ho spudoratamente copiato era così
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (!isSelected) {  //questo perché invece quando è selezionato sarà nero
                    if (! (index % 2 == 0)) {   //banalmente se la riga è pari avrà un colore di sfondo, mentre se è dispari un'altro
                        c.setBackground(new Color(241, 241, 241)); // pari righe
                    } else {
                        c.setBackground(new Color(255, 255, 255)); // righe dispari
                    }
                } else {
                    c.setBackground(list.getSelectionBackground());
                    c.setForeground(list.getSelectionForeground());
                }

                return c;

            }
        });

        JScrollPane scrollPane = new JScrollPane(this.dataList);
        scrollPane.setPreferredSize(new Dimension(1000, 750));

        scrollPane.setBorder(new EmptyBorder(10,30,10,30));
        add(scrollPane, BorderLayout.CENTER);  // Add the ScrollPane with the JList to the panel
    }

    /**
     * Initialize all the action listeners
     */
    private void initActionListener(){
        this.dataList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Only allow double clicks
                if (e.getClickCount() == 2) {
                    // Retrieve the ID from the selected data
                    Data dataSelected = userData.get(dataList.getSelectedIndex());
                    int id = dataSelected.getID();

                    // Retrieve the data with the ID from the database
                    Data userSingleData = getSingleData(id);

                    // Create a JDialog where the data will be shown
                    JDialog showData = new JDialog();
                    showData.setSize(new Dimension(600, 400));

                    showData.setUndecorated(true);
                    showData.setResizable(false);
                    showData.setLocationRelativeTo(null);
                    showData.setAlwaysOnTop(true);

                    showData.add(new SingleDataPanel(userSingleData));
                    showData.setVisible(true);

                    /*
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(new EmptyBorder(20,20,20,20));

                    JButton okButton = new JButton("OK");
                    panel.add(okButton);

                    JTextField dataField;
                    List<JTextField> dataFields = new ArrayList<>();

                    // Add dynamically all the user data retrieved
                    for (String data : userSingleData.getFullUserData()){
                        // Create the JLabel
                        dataField = new JTextField();
                        dataField.setText(data);
                        dataField.setEditable(false);
                        dataFields.add(dataField);

                        // Create the JButton to copy the data
                        JButton copyDataButton = new JButton();
                        copyDataButton.setText("❏");
                        copyDataButton.setPreferredSize(new Dimension(50, 50));
                        copyDataButton.setMaximumSize(new Dimension(50, 50));
                        copyDataButton.setMinimumSize(new Dimension(50, 50));

                        copyDataButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Get the clipboard from the system
                                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                StringSelection dataToCopy = new StringSelection(data);  // Create a Transferable
                                clipboard.setContents(dataToCopy, dataToCopy);  // Copy the transferable
                                JOptionPane.showMessageDialog(panel, "Copied the data to the clipboard.");
                            }
                        });

                        // Add the JLabel and the JButton to the panel
                        JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
                        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));  // Padding between rows
                        rowPanel.add(dataField, BorderLayout.CENTER);
                        rowPanel.add(copyDataButton, BorderLayout.EAST);

                        panel.add(rowPanel);
                    }

                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showData.dispose();
                        }
                    });

                    JButton modifyButton = new JButton("Modify");
                    panel.add(modifyButton);

                    JButton deleteButton = new JButton("Delete");
                    panel.add(deleteButton);

                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JOptionPane.showConfirmDialog(showData, "Stai per eliminare definitivamente la password\nprocedere?");
                        }
                    });

                    JButton saveButton = new JButton("Save Changes");
                    saveButton.setVisible(false);  // It's shown only when modifyButton is clicked

                    panel.add(saveButton);

                    // Add the action lister after the creation of the OK JButton as it needs to be removed
                    // when the modify JButton is clicked
                    modifyButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Set the JTextFields to be editable to allow changes
                            for (JTextField fields : dataFields){
                                fields.setEditable(true);
                            }
                            modifyButton.setEnabled(false);  // Disable the JButton as it's already being used
                            okButton.setVisible(false);  // Hide the ok JButton
                            saveButton.setVisible(true);  // Show the JButton used to apply changes
                        }
                    });

                    // Save the changes and send the event to the backend
                    saveButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Store the updated strings into an array
                            String[] updatedData = new String[5];

                            for (int i = 0; i < dataFields.size(); i++){
                                updatedData[i] = dataFields.get(i).getText();
                            }
                            // Send the data to the backend
                            changeData(id, updatedData[0], updatedData[1], updatedData[2], updatedData[3], updatedData[4]);

                            // Hide the save JButton, show the ok JButton and enable the modify JButton again
                            saveButton.setVisible(false);
                            okButton.setVisible(true);
                            modifyButton.setEnabled(true);
                        }
                    });

                    showData.add(panel);
                    showData.setVisible(true);
                     */
                }


            }
        });
    }

    /**
     * Retrieve the data associated with an ID from the database to show it in "Show Data" section
     * @param id
     * @return
     */
    private Data getSingleData(int id){
        // Create the event to send to the backend
        ArrayList dataToSend = new ArrayList();
        dataToSend.add(id);

        Event getSingleData = new Event("get-single-data", dataToSend);
        this.communicationHandler.send(getSingleData);

        Event getSingleDataCompletd = this.communicationHandler.receive();  // Wait for the response

        Data userSingleData = (Data) getSingleDataCompletd.getData().getFirst();  // Get the user single data

        return userSingleData;
    }

    /**
     * Update a record of the database
     * @param id
     * @param username
     * @param emailAddress
     * @param password
     * @param service
     * @param additional
     */
    private void changeData(int id, String username, String emailAddress, String password, String service, String additional){
        // Create the data to send with the event
        Data data = new Data(id, username, emailAddress, password, service, additional);

        ArrayList dataToSend = new ArrayList();
        dataToSend.add(data);

        // Create and send the event
        Event event = new Event("change-data", dataToSend);
        this.communicationHandler.send(event);
        this.communicationHandler.receive();
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

    /**
     * Update the userData and its model to show the updated data of the database
     */
    public void updateUserData(){
        // Create and send the event to the backend asking for the user data
        Event updateUserData = new Event("get-full-service-data", new ArrayList());
        this.communicationHandler.send(updateUserData);

        // Wait for the response of the backend and update the data with the new one
        Event updatedDataEvent = this.communicationHandler.receive();
        ArrayList<Data> updatedData = (ArrayList<Data>) updatedDataEvent.getData().getFirst();

        this.userData.clear();  // Clear the ArrayList from the previous data
        this.userData.addAll(updatedData);  // Update the ArrayList with the new data

        this.userDataModel.clear();  // Clear the model from the previous data
        for (int i = 0; i < updatedData.size(); i++){
            this.userDataModel.add(i, updatedData.get(i).getSERVICE());
        }
    }

}
