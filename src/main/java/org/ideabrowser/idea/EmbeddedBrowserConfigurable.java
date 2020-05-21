package org.ideabrowser.idea;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import org.ideabrowser.EmbeddedBrowser;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

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
 * Using another predefined family would have failed too, trying to clone a ChromeSetting into a org.ideabrowser.idea.EmbeddedBrowserSettings.
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
        private final JBCheckBox logsEnabledCb;
        private final JBTextField tagsToIgnoreField;

        EmbeddedBrowserPreferencesPanel() {
            super(new BorderLayout());

            JPanel centerPanel = new JPanel(new VerticalLayout(5, SwingConstants.LEFT));

            // browserOptionsPanel that will contain the controls aligned using GBL
            JPanel browserOptionsPanel = new JBPanel<>(new GridBagLayout()).withBorder(IdeBorderFactory.createTitledBorder("Browser"));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = JBUI.insets(5, 0, 0, 5);

            browserOptionsPanel.add(new JLabel("Max history size:"), c);

            c.gridx = 1;
            maxHistorySizeField = new JBIntSpinner(5, 2, 20);
            browserOptionsPanel.add(maxHistorySizeField, c);

            c.gridx = 0;
            c.gridy = 1;
            browserOptionsPanel.add(new JLabel("Search template:"), c);

            c.gridx = 1;
            c.gridwidth = 3;
            searchEngineTemplateField = new JTextField();
            browserOptionsPanel.add(searchEngineTemplateField, c);

            c.gridx = 0;
            c.gridx = 3;
            c.gridy++;
            c.gridwidth = 0;
            JButton addToWebBrowserBtn= new JButton("Add To Web Browsers");
            browserOptionsPanel.add(addToWebBrowserBtn, c);
            if (EmbeddedBrowserUrlOpener.existsEmbeddedBrowser()) {
                addToWebBrowserBtn.setEnabled(false);
            } else {
                addToWebBrowserBtn.setEnabled(true);
                addToWebBrowserBtn.addActionListener( e-> {
                    EmbeddedBrowserUrlOpener.createEmbeddedBrowser();
                    addToWebBrowserBtn.setEnabled(false);
                });
            }

            // Adds browserOptionsPanel into the main component using flow layout to move it on the top left corner
            centerPanel.add(browserOptionsPanel, VerticalLayout.TOP);

            JPanel finderAndHighlighterOptionsPanel = new JBPanel<>(new GridBagLayout()).withBorder(IdeBorderFactory.createTitledBorder("Finder and Highlighter"));

            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            finderAndHighlighterOptionsPanel.add(new JLabel("Tags to ignore:"), c);

            c.gridx = 1;
            this.tagsToIgnoreField = new JBTextField();
            tagsToIgnoreField.setToolTipText("A comma separated list of HTML tags that will be ignored when searching for text occurrences");
            finderAndHighlighterOptionsPanel.add(tagsToIgnoreField, c);

            c.gridx = 0;
            c.gridy++;
            logsEnabledCb = new JBCheckBox("Enable logs");
            logsEnabledCb.setToolTipText("Enable Finder's matches & response times");
            finderAndHighlighterOptionsPanel.add(logsEnabledCb, c);

            centerPanel.add(finderAndHighlighterOptionsPanel, VerticalLayout.TOP);

            add(centerPanel, BorderLayout.CENTER);
        }

        void resetPanelFrom(EmbeddedBrowserSettings settings) {
            maxHistorySizeField.setNumber(settings.getMaxHistorySize());
            searchEngineTemplateField.setText(settings.getSearchEngineTemplate());
            logsEnabledCb.setSelected(settings.isLogsEnabled());
            tagsToIgnoreField.setText(String.join(", ", settings.getTagsToIgnore()));
        }

        void applyPanelTo(EmbeddedBrowserSettings settings) {
            settings.setMaxHistorySize(maxHistorySizeField.getNumber());
            settings.setSearchEngineTemplate(searchEngineTemplateField.getText());
            settings.setLogsEnabled(logsEnabledCb.isSelected());
            settings.setTagsToIgnore(parseTagsToIgnore());
        }

        boolean isModified(EmbeddedBrowserSettings settings) {
            return maxHistorySizeField.getNumber() != settings.getMaxHistorySize() ||
                    !(Objects.equals(searchEngineTemplateField.getText(), settings.getSearchEngineTemplate())) ||
                    !(Arrays.equals(parseTagsToIgnore(), settings.getTagsToIgnore())) ||
                    logsEnabledCb.isSelected() != settings.isLogsEnabled();
        }

        @NotNull
        private String[] parseTagsToIgnore() {
            return tagsToIgnoreField.getText().split(", *");
        }

    }
}
