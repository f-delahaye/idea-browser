package org.ideabrowser;

import com.intellij.icons.AllIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FinderPanel extends JPanel implements FinderControllerListener {

    private final JButton nextOccurrence;
    private final JTextField textField;

    public FinderPanel(FinderController finderController) {
        super(new FlowLayout(FlowLayout.LEFT));

        finderController.setFinderListener(this);

        textField = new JTextField();
        add(textField);

        Action nextOccurrenceAction = new AbstractAction(null, AllIcons.Actions.NextOccurence) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                finderController.findNext();
            }
        };

        nextOccurrence = new JButton(nextOccurrenceAction);
        textField.addActionListener(e -> finderController.setText(textField.getText()));
        add(nextOccurrence);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension max = super.getMaximumSize();
        // Overlayout uses component's maximum size.
        // If its not overriden, finderPanel will take up all available space and thus hiding viewerPanel
        return new Dimension(max.width, getPreferredSize().height);
    }

    @Override
    public void enableNextOccurrence() {
        nextOccurrence.setEnabled(true);
    }

    @Override
    public void disableNextOccurrence() {
        nextOccurrence.setEnabled(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            textField.requestFocusInWindow();
        }
    }
}
