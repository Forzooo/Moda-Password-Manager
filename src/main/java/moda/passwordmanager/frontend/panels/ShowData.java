package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
//import moda.passwordmanager.frontend.components.CloseTab;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ShowData extends JPanel {

    // Attribute to communicate with the backend
    private InterThreadCommunication itc;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    private JTabbedPane dataTabbedPane;  // The tabbed pane shows the dataList and the data the user has selected

    /**
     * The service data shown in the JList of "Show Data" panel which it's updated automatically by the timer
     */
    private ArrayList<Data> userData;  // A Data object is required as each service shown needs to be associated with its ID
    private DefaultListModel<String> userDataModel;

    // Swing components
    private JList<String> dataList;

    public ShowData(InterThreadCommunication itc, int MIN_CONTENT_WIDTH, Dimension windowSize,
                    int sidebarPanelWidth) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.itc = itc;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        // Initialize the user data ArrayList and Model
        this.userData = new ArrayList<>();
        this.userDataModel = new DefaultListModel<>();

        initPanel(sidebarPanelWidth);
        initComponents();
        initListeners();
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
        setLayout(new BorderLayout());  // Set its layout

        // Calculate the preferred width and height
        int width = (int) (this.windowSize.getWidth() - sidebarPanelWidth);
        int height = (int) this.windowSize.getHeight();

        // Set the preferred size
        setPreferredSize(new Dimension(width, height));

        setBackground(Color.WHITE);
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        this.dataTabbedPane = new JTabbedPane();

        // Create the JList used to show all the data saved inside the database
        this.dataList = new JList<>();
        this.dataList.setFixedCellHeight(30);
        this.dataList.setModel(this.userDataModel);  // Set the model of the JList (Strings containing service data)
        this.dataList.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));

        dataList.setSelectionBackground(Color.black);
        dataList.setSelectionForeground(Color.white);

        JScrollPane scrollPane = new JScrollPane(this.dataList);
        scrollPane.setPreferredSize(new Dimension(1000, 750));
        scrollPane.setBorder(new EmptyBorder(10,30,10,30));

        this.dataTabbedPane.addTab("User Data", scrollPane);

        add(this.dataTabbedPane, BorderLayout.CENTER);
//        add(scrollPane, BorderLayout.CENTER);  // Add the ScrollPane with the JList to the panel
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
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
//                    Data userSingleData = getData(id);

                    addDataTab(getData(id));
                    // TODO: Add proper panel

                    // Create a Show Data Dialog to display the data retrieved
//                    moda.passwordmanager.frontend.dialogs.ShowData showData = new moda.passwordmanager.frontend.dialogs.ShowData(itc, userSingleData);
//                    showData.setVisible(true);
                }
            }
        });

        this.dataList.setCellRenderer(new DefaultListCellRenderer(){ //imposto un metodo per far renderizzare le celle della lista come mi pare
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
    }

    private void addDataTab(Data userData){
        UserData userDataTab = new UserData(this.itc, userData, getWidth(), getHeight());
//        CloseTab closeTab = new CloseTab(this.dataTabbedPane, userDataTab);

        this.dataTabbedPane.addTab(userData.getSERVICE(), userDataTab);
//        this.dataTabbedPane.setTabComponentAt(this.dataTabbedPane.indexOfComponent(userDataTab), closeTab);
    }

    /**
     * Retrieve the data associated with an ID from the database to show it in "Show Data" section
     * @param id
     * @return
     */
    private Data getData(int id){
        // Create the event to send to the backend
        Event getData = new Event("get-data", id);

        // Wait for the response
        Event getSingleDataCompleted = this.itc.requestAndReceive(getData);

        Data userData = (Data) getSingleDataCompleted.getData().getFirst();  // Get the user data

        return userData;
    }

    public ArrayList<Data> getUserData() {
        return this.userData;
    }

    public DefaultListModel<String> getUserDataModel() {
        return this.userDataModel;
    }

    /**
     * Return the index of the Data object of UserData that has the same ID. -1 is returned if it does not exist
     */
    public int indexOfUserData(int ID){
        for (int i = 0; i < this.userData.size(); i++){
            if (this.userData.get(i).getID() == ID) {
                return i;
            }
        }
        return -1;
    }

}
