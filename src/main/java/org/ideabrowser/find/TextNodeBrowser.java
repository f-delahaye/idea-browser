package org.ideabrowser.find;

import org.w3c.dom.Text;

public interface TextNodeBrowser {
    Text first();
    Text next(Text node);
    Text previous(Text node);
}
