package view.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import view.gui.icons.TrashIcon;

public final class Toolbar extends JPanel {

    private final Button clearButton;
    private final Button seedFillButton;

    private static final Color DIVIDER = new Color(0x3A3E43);
    private static final Color BACKGROUND = new Color(0x25282C);

    public Toolbar() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
            BorderFactory.createEmptyBorder(10, 6, 4, 6)
        ));

        JPanel fillControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        seedFillButton = new Button("Seed fill", false);
        fillControls.setOpaque(false);
        fillControls.add(seedFillButton);
        add(fillControls, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        clearButton = new Button("Vymazat", new TrashIcon());
        actions.setOpaque(false);
        actions.add(clearButton);
        add(actions, BorderLayout.EAST);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        preferredSize.height = 56;
        return preferredSize;
    }

    public void setSeedFillMode(boolean active) {
        seedFillButton.setText(active ? "Vyberte bod" : "Seed fill");
    }

    public void addClearListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    public void addSeedFillListener(ActionListener listener) {
        seedFillButton.addActionListener(listener);
    }

}
