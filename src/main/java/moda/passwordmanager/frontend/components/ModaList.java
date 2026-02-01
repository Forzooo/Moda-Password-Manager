package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;

public class ModaList extends JList<String> {

    public ModaList(ListModel<String> dataModel, int fontSize, int cellHigh) {
        super(dataModel);

        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, fontSize));
        setFixedCellHeight(cellHigh);
    }

    public ModaList(ListModel<String> dataModel, int fontSize) {
        super(dataModel);

        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, fontSize));
        setFixedCellHeight(30);
    }

    public ModaList(ListModel<String> dataModel) {
        super(dataModel);

        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        setFixedCellHeight(30);
    }
}
