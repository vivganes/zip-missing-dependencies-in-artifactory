package com.vivekganesan;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArtifactoryCheckerTest {

    private ArtifactoryChecker artifactoryChecker;

    @Mock
    private HttpURLConnection connection;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        artifactoryChecker = new ArtifactoryChecker() {
            @Override
            protected HttpURLConnection createConnection(URL url) {
                return connection;
            }
        };
    }

    @Test
    public void testCheckDependencyInArtifactory_Found() throws Exception {
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"results\":[{\"uri\":\"http://example.com\"}]}".getBytes()));

        boolean result = artifactoryChecker.checkDependencyInArtifactory("http://jfrog.url", "user", "pass", "group", "artifact", "1.0.0");

        assertTrue(result);
        verify(connection).setRequestMethod("GET");
        verify(connection).setRequestProperty(eq("Authorization"), anyString());
    }

    @Test
    public void testCheckDependencyInArtifactory_NotFound() throws Exception {
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"results\":[]}".getBytes()));

        boolean result = artifactoryChecker.checkDependencyInArtifactory("http://jfrog.url", "user", "pass", "group", "artifact", "1.0.0");

        assertFalse(result);
    }

    @Test(expected = RuntimeException.class)
    public void testCheckDependencyInArtifactory_Error() throws Exception {
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        artifactoryChecker.checkDependencyInArtifactory("http://jfrog.url", "user", "pass", "group", "artifact", "1.0.0");
    }
}