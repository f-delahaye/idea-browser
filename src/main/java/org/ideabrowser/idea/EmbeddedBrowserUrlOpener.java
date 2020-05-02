package org.ideabrowser.idea;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.UrlOpener;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.ideabrowser.EmbeddedBrowser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EmbeddedBrowserUrlOpener extends UrlOpener {

    private static final String PREDEFINED_EMBEDDED_BROWSER_ID = "7574ed43-5379-4e5c-8c0b-16fc172e5b71";
    private static final UUID PREDEFINED_EMBEDDED_BROWSER_UUID = UUID.fromString(PREDEFINED_EMBEDDED_BROWSER_ID);

    public static void createEmbeddedBrowser() {
        if (!existsEmbeddedBrowser()) {
            WebBrowserManager.getInstance().addBrowser(PREDEFINED_EMBEDDED_BROWSER_UUID,
                    BrowserFamily.SAFARI, // familly is not nullable but can only be one of the supported values... JavaFX's WebEngine is based on DevKit which is also used by Sofari
                    "Embedded",
                    "N/A", // We need a non null value for the browser to show up in the list of available browsers when editing an html file ... Would still appear in the settings screen though even if null
                    true,
                    null);
        }
    }

    public static boolean existsEmbeddedBrowser() {
        return WebBrowserManager.getInstance().findBrowserById(PREDEFINED_EMBEDDED_BROWSER_ID) != null;
    }

    @Override
    public boolean openUrl(@NotNull WebBrowser browser, @NotNull String url, @Nullable Project project) {
        if (browser.getId().equals(PREDEFINED_EMBEDDED_BROWSER_UUID) && project != null) {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Browser");
            toolWindow.show(() -> ((EmbeddedBrowser)toolWindow.getContentManager().getContents()[0].getComponent()).onRequestedURLChanged(url));
            return true;
        }
        return false;
    }
}
