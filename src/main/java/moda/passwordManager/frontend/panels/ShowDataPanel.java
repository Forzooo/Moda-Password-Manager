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
                    Data userSingleData = getData(id);

                    // Create a JDialog where the data will be shown
                    JDialog showData = new JDialog();
                    showData.setSize(new Dimension(600, 400));

                    showData.setResizable(false);
                    showData.setLocationRelativeTo(null);
                    showData.setAlwaysOnTop(true);

                    showData.add(new SingleDataPanel(communicationHandler, userSingleData));
                    showData.setVisible(true);
                }
            }
        });
    }

    /**
     * Retrieve the data associated with an ID from the database to show it in "Show Data" section
     * @param id
     * @return
     */
    private Data getData(int id){
        // Create the event to send to the backend
        ArrayList dataToSend = new ArrayList();
        dataToSend.add(id);

        Event getData = new Event("get-data", dataToSend);
        this.communicationHandler.send(getData);

        Event getSingleDataCompletd = this.communicationHandler.receive();  // Wait for the response

        Data userData = (Data) getSingleDataCompletd.getData().getFirst();  // Get the user data

        return userData;
    }

    /**
     * Update the userData and its model to show the updated data of the database
     */
    public void updateUserData(){
        // Create and send the event to the backend asking for the user data
        Event updateUserData = new Event("get-service-fields", new ArrayList());
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
