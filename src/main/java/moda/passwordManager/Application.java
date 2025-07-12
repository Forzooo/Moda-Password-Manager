package moda.passwordManager;

import moda.passwordManager.backend.Backend;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.Frontend;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;


public class Application extends JFrame {
    public Application(){
        initUI();
    }

    private void initUI(){
        Dimension screen = getToolkit().getScreenSize();

        int width = (int) (screen.getWidth() * 3/4);
        int height = (int) (screen.getHeight() * 3/4);

        add(new Frontend(width, height));
        pack();

        setTitle("MODA - Password Manager");
        setSize(width, height);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Application ex = new Application();
            ex.setVisible(true);
        });

        // Create the two LinkedBlockingQueue objects here to pass them to the Backend and the Frontend
        LinkedBlockingQueue<Event> backendQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Event> frontendQueue = new LinkedBlockingQueue<>();

        Backend backend = new Backend(backendQueue, frontendQueue);
        backend.start();
    }
}