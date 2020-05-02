package org.ideabrowser.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.ideabrowser.EmbeddedBrowser;
import org.jetbrains.annotations.NotNull;

public class EmbeddedBrowserFactory implements ToolWindowFactory {

    public EmbeddedBrowserFactory() {
    }

    public void createToolWindowContent(@NotNull Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(new EmbeddedBrowser(),"", false);
        toolWindow.getContentManager().addContent(content);
    }
}
