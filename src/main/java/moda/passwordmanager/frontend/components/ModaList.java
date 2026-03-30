package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;

public class ModaList extends JList<String> {

    public ModaList(ListModel<String> dataModel, int fontSize, int cellHigh) {
        super(dataModel);

        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, fontSize));
        setFixedCellHeight(cellHigh);

        initCellRenderer();
    }

    public ModaList(ListModel<String> dataModel, int fontSize) {
        super(dataModel);

        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, fontSize));
        setFixedCellHeight(30);
        initCellRenderer();

    }

    public ModaList(ListModel<String> dataModel) {
        super(dataModel);

        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        setFixedCellHeight(30);

        initCellRenderer();
    }

    private void initCellRenderer() {
        setCellRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (isSelected){  // The selected row has black background
                    c.setBackground(Color.BLACK);
                } else {  // The other rows have two different colors
                    if (index % 2 == 0){
                        c.setBackground(new Color(255, 255, 255));
                    } else {
                        c.setBackground(new Color(241, 241, 241));
                    }
                }
                return c;
            }
        });
    }
}
