package org.ideabrowser;

import org.ideabrowser.EngineController.URLChecker;
import org.ideabrowser.idea.EmbeddedBrowserSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;

import static org.mockito.Mockito.*;

public class EngineControllerTest {

    private EngineController controller;
    private EmbeddedBrowserSettings settings;
    private URLChecker urlChecker;
    private EngineControllerListener viewListener;
    private SearchHistoryListener historyListener;

    @Before
    public void before() {
        this.settings = mock(EmbeddedBrowserSettings.class);
        this.urlChecker = mock(URLChecker.class);
        this.viewListener = mock(EngineControllerListener.class);
        this.historyListener = mock(SearchHistoryListener.class);
        this.controller = new EngineController(settings, urlChecker);
        controller.setViewListener(viewListener);
        controller.setSearchHistoryListener(historyListener);
    }

    @After
    public void verifyListeners() {
        // we cant really verify unexpected calls to listeners other than by using verifyNoMoreInteractions
        verifyNoMoreInteractions(viewListener);
        verifyNoMoreInteractions(historyListener);
    }

    @Test
    public void requestUrlWithoutHttp() {
        controller.request("test.com");
        // expectations: controller automatically prepends http
        verify(viewListener).onRequestedURLChanged("http://test.com");
    }

    @Test
    public void requestMalformedUrl() throws IOException {
        doThrow(new IOException()).when(urlChecker).checkURL(any());

        when(settings.getSearchEngineTemplate()).thenReturn("http://searchengine.com?q=TOKEN");
        controller.request("http.://test.com");
        verify(viewListener).onRequestedURLChanged("http://searchengine.com?q=http.://test.com");

        controller.request("hhtp://test.com");
        verify(viewListener).onRequestedURLChanged("http://searchengine.com?q=hhtp://test.com");
    }

    @Test
    public void requestInvalidUrl() throws IOException {
        when(settings.getSearchEngineTemplate()).thenReturn("http://searchengine.com?q=TOKEN");
        doThrow(new IOException()).when(urlChecker).checkURL(new URL("http://test_com"));
        controller.request("test_com");
        verify(viewListener).onRequestedURLChanged("http://searchengine.com?q=test_com");
    }

    @Test
    public void requestNoSearchEngineTemplateConfigured() throws IOException {
        doThrow(new IOException()).when(urlChecker).checkURL(new URL("http://test_com"));
        controller.request("test_com");
        // As validated in previous test, we should pass the query to the search engine ... unless none is configured!
        verify(viewListener).onRequestedURLChanged("test_com");
    }

    @Test
    public void onLoadedWithHistoryDisabled() {
        when(settings.getMaxHistorySize()).thenReturn(0);
        controller.onLoaded("title", "http://test.com");

        verify(viewListener).onURLChanged("http://test.com");
        // no callback on the history listener
    }

    @Test
    public void onLoadedWithDuplicates() {
        when(settings.getMaxHistorySize()).thenReturn(1);
        controller.onLoaded("title", "http://test.com");
        controller.onLoaded("title", "http://test.com");

        // Arguably, we should call on URLChanged only once
        // In reality though, the web engine should detect that the url is the same and it should fire onLoaded only once
        verify(viewListener, times(2)).onURLChanged("http://test.com");

        verify(historyListener, Mockito.times(1)).onHistoryChanged(Mockito.argThat(
                iterator -> iterator.size() == 1 &&
                "title".equals(iterator.displayName(0)) &&
                "http://test.com".equals(iterator.url(0))
        ));
    }

    @Test
    public void onLoadedWithMaxSize() {
        when(settings.getMaxHistorySize()).thenReturn(1);
        controller.onLoaded("title", "http://test.com");
        verify(viewListener).onURLChanged("http://test.com");
        verify(historyListener, Mockito.times(1)).onHistoryChanged(any());

        controller.onLoaded("title2", "http://test2.com");
        verify(viewListener).onURLChanged("http://test2.com");
        verify(historyListener, Mockito.times(1)).onHistoryChanged(Mockito.argThat(
                iterator -> iterator.size() == 1 &&
                        "title2".equals(iterator.displayName(0)) &&
                        "http://test2.com".equals(iterator.url(0))
        ));
    }

    @Test
    public void onLoadedWithUnboundedHistory() {
        when(settings.getMaxHistorySize()).thenReturn(-1);

        controller.onLoaded("title", "http://test.com");
        verify(historyListener, Mockito.times(1)).onHistoryChanged(any());
        verify(viewListener).onURLChanged("http://test.com");

        controller.onLoaded("title2", "http://test2.com");
        verify(viewListener).onURLChanged("http://test2.com");
        verify(historyListener, Mockito.times(1)).onHistoryChanged(Mockito.argThat(
                iterator -> iterator.size() == 2 &&
                        "title".equals(iterator.displayName(0)) &&
                        "http://test.com".equals(iterator.url(0)) &&
                        "title2".equals(iterator.displayName(1)) &&
                        "http://test2.com".equals(iterator.url(1))

        ));
    }


}