package org.ideabrowser;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.components.fields.ExtendableTextComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Implementation of a component where users may enter a String that will be used to perform a search. It is visually identified by a magnifying glass.
 *
 * SearchWithHistoryTextField itself does not handle the history per se, which is done by {@link EmbeddedBrowserController} .
 * Instead, as it implements {@link SearchHistoryListener}, it is designed to be registered as a listener on the controller
 * and will then be notified upon history changes.
 * Please note that the history is not persisted and is only for the current session.
 *
 * It delegates all the UI stuff to {@link com.intellij.ide.ui.laf.darcula.ui.TextFieldWithPopupHandlerUI} hence ensuring
 * that the guidelines defined here https://jetbrains.design/intellij/controls/search_field/, are followed
 */
// Implementation note: since TextFieldWithPopupHandlerUI / DarculaTextFieldUI only requires a JTextField, this class does not extend ExtendableTextField.
// Support for the loading icon is provided through TextFieldWithPopupHandlerUI's "search.extension" client property
public class SearchWithHistoryTextField extends JTextField implements SearchHistoryListener {

    private static final String VARIANT_KEY = "JTextField.variant";
    private static final String SEARCH_VARIANT_VALUE = "search";

    private static final String FIND_POPUP_KEY = "JTextField.Search.FindPopup";
    private static final String GAP_KEY = "JTextField.Search.Gap";
    private static final String EXTENSION_KEY = "search.extension";

    // Popup with Search history. It is updated as new items are entered by users,
    // and is retrieved by DarculaTextFieldUI in FIND_POPUP_KEY.
    // Entries in the menu are
    private final JPopupMenu historyMenu;

    public SearchWithHistoryTextField() {
        historyMenu = new JBPopupMenu();

        putClientProperty(VARIANT_KEY, SEARCH_VARIANT_VALUE);
        putClientProperty(FIND_POPUP_KEY, historyMenu);
        putClientProperty(GAP_KEY, 5);
    }

    /**
     * Notification that the history items have changed and that the popup must be rebuilt.
     *
     * This method is not called internally, instead it is designed to be registered as a callback in a HistoryModel.
     */
    @Override
    public void onHistoryChanged(SearchHistoryIterator historyIterator) {
         historyMenu.removeAll();
         for (int i=0; i<historyIterator.size();i++) {
             String displayName = historyIterator.displayName(i);
             String text = historyIterator.url(i);
             final JMenuItem menuItem = new JBMenuItem(new AbstractAction(displayName) {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     SearchWithHistoryTextField.this.setText(text);
                     // selecting an item from the history will automatically trigger a search
                     SearchWithHistoryTextField.this.fireActionPerformed();
                 }
             });
             historyMenu.add(menuItem);
         }
    }

    public enum SearchingStatus {
        RUNNING,
        FAILED,
        SUCCEEDED
    }
    public void setSearchingStatus(SearchingStatus status) {
        ExtendableTextComponent.Extension extension = null;
        switch (status) {
            case RUNNING: extension = ExtendableTextComponent.Extension.create(AnimatedIcon.Default.INSTANCE, null, null); break;
            case FAILED: extension = ExtendableTextComponent.Extension.create(AllIcons.General.Error, null, null); break;
            // case SUCCEEDED: leave extension to null, this will clear out any previous running  / failed extension.
        }
        putClientProperty(EXTENSION_KEY, extension);

        // Resetting VARIANT is the only wy i could find to force TextFieldWithPopupHandlerUI to refresh the icons layout
        putClientProperty(VARIANT_KEY, null);
        putClientProperty(VARIANT_KEY, "search");
    }
}
