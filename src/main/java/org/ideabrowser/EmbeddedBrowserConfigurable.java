package org.ideabrowser;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.JBIntSpinner;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Class to configure {@link EmbeddedBrowser}
 *
 * It was originally designed to be returned by the createConfigurable method of the WebBrowser representing the Embedded Browser.
 * This would have made it possible to edit the settings by using the existing BrowserPane, selecting that instance and then clicking on edit.
 *
 * It *almost* works, unfortunately it fails with
 *
 * java.lang.AssertionError
 * 	at com.intellij.ide.browsers.BrowserSettingsPanel$6.cloneSettings(BrowserSettingsPanel.java:213)
 * 	at com.intellij.ide.browsers.BrowserSettingsPanel$6.edit(BrowserSettingsPanel.java:199)
 * 	at com.intellij.ide.browsers.BrowserSettingsPanel$6.edit(BrowserSettingsPanel.java:183)
 * 	at com.intellij.util.ui.table.TableModelEditor.lambda$addDialogActions$2(TableModelEditor.java:101)
 * 	at com.intellij.ui.ToolbarDecorator$2.doEdit(ToolbarDecorator.java:434)
 * 	at com.intellij.ui.CommonActionsPanel$Buttons.performAction(CommonActionsPanel.java:58)
 * 	at com.intellij.ui.CommonActionsPanel$MyActionButton.actionPerformed(CommonActionsPanel.java:252)
 *
 * WebBrowser currently uses the Safari family whose createBrowserSpecificSettings returns null hence the error.
 * Using another predefined family would have failed too, trying to clone a ChromeSetting into a EmbeddedBrowserSettings.
 *
 * What was really needed here was a specific family.
 *
 * So instead, EmbeddedBrowserConfigurable is available as a separate entry in the Settings menu.
 */
public class EmbeddedBrowserConfigurable implements Configurable {

    private EmbeddedBrowserPreferencesPanel panel;
    private final EmbeddedBrowserSettings settings;

    public EmbeddedBrowserConfigurable() {
        this.settings = EmbeddedBrowserSettings.getInstance();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Embedded Browser";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        this.panel = new EmbeddedBrowserPreferencesPanel();
        return panel;
    }

    @Override
    public boolean isModified() {
        return panel.isModified(settings);
    }

    @Override
    public void apply() {
        panel.applyPanelTo(settings);
    }

    @Override
    public void reset() {
        panel.resetPanelFrom(settings);
    }

    private static class EmbeddedBrowserPreferencesPanel extends JPanel {

        private final JBIntSpinner maxHistorySizeField;
        private final JTextField searchEngineTemplateField;

        EmbeddedBrowserPreferencesPanel() {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));

            // panel that will contain the controls aligned using GBL
            JPanel panel = new JPanel(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = JBUI.insets(5, 0, 0, 5);

            panel.add(new JLabel("Max history size:"), c);

            c.gridx = 1;
            maxHistorySizeField = new JBIntSpinner(5, 2, 20);
            panel.add(maxHistorySizeField, c);

            c.gridx = 0;
            c.gridy = 1;
            panel.add(new JLabel("Search engine template:"), c);

            c.gridx = 1;
            c.gridwidth = 3;
            searchEngineTemplateField = new JTextField();
            panel.add(searchEngineTemplateField, c);

            c.gridx = 3;
            c.gridy = 2;
            c.gridwidth = 0;
            JButton addToWebBrowserBtn= new JButton("Add To Web Browsers");
            panel.add(addToWebBrowserBtn, c);
            if (EmbeddedBrowserUrlOpener.existsEmbeddedBrowser()) {
                addToWebBrowserBtn.setEnabled(false);
            } else {
                addToWebBrowserBtn.setEnabled(true);
                addToWebBrowserBtn.addActionListener( e-> {
                    EmbeddedBrowserUrlOpener.createEmbeddedBrowser();
                    addToWebBrowserBtn.setEnabled(false);
                });
            }

            // Adds panel into the main component using flow layout to move it on the top left corner
            add(panel);
        }

        void resetPanelFrom(EmbeddedBrowserSettings settings) {
            maxHistorySizeField.setNumber(settings.getMaxHistorySize());
            searchEngineTemplateField.setText(settings.getSearchEngineTemplate());
        }

        void applyPanelTo(EmbeddedBrowserSettings settings) {
            settings.setMaxHistorySize(maxHistorySizeField.getNumber());
            settings.setSearchEngineTemplate(searchEngineTemplateField.getText());
        }

        boolean isModified(EmbeddedBrowserSettings settings) {
            return maxHistorySizeField.getNumber() != settings.getMaxHistorySize() ||
                    !(searchEngineTemplateField.getText().equals(settings.getSearchEngineTemplate()));
        }

    }
}
