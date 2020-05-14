package org.ideabrowser;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class FinderPanel extends JPanel {
    private final JTextField textField;

    public FinderPanel(FinderController finderController) {

        super(new FlowLayout(FlowLayout.LEFT));

// TODO
//        finderController.setFinderListener(this);

        this.textField = new JTextField();
        textField.setColumns(30);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                finderController.setText(textField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                finderController.setText(textField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
            }
        });

        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new NextOccurrenceAction(finderController));
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("idea-browser.find", group, true);
        JComponent toolbarComponent = actionToolbar.getComponent();
        toolbarComponent.setBorder(JBUI.Borders.empty());
        add(textField);
        add(toolbarComponent);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension max = super.getMaximumSize();
        // Overlay layout uses component's maximum size.
        // If its not overridden, finderPanel will take up all available space and thus hiding viewerPanel
        return new Dimension(max.width, getPreferredSize().height);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            textField.requestFocusInWindow();
        }
    }

    private class NextOccurrenceAction extends AnAction {
        private final FinderController finderController;

        public NextOccurrenceAction(FinderController finderController) {
            super( "Next Occurrence", null, AllIcons.Actions.FindAndShowNextMatches);
            this.finderController = finderController;
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke("ctrl K")), FinderPanel.this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            finderController.findNext();
        }
    }
}
