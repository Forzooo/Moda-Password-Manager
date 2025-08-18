package moda.passwordManager.frontend.panels;

import moda.passwordManager.communicationHandler.CommunicationHandler;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    // Attribute to communicate with the backend
    private CommunicationHandler communicationHandler;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    public SettingsPanel(CommunicationHandler communicationHandler, int MIN_CONTENT_WIDTH, Dimension windowSize, int sidebarPanelWidth) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.communicationHandler = communicationHandler;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        initPanel(sidebarPanelWidth);
        initComponents();
        initListeners();
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

        // Set the margin of the Add Data Panel [top: 60, bottom: 60, left: 30, right: 30]
        setBorder(BorderFactory.createEmptyBorder(60,30,60,30));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){

    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){

    }
}
