package com.vivekganesan;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ZipMissingDependenciesMojoTest {

    private ZipMissingDependenciesMojo mojo;

    @Mock
    private DependencyResolver dependencyResolver;
    @Mock
    private ArtifactoryChecker artifactoryChecker;
    @Mock
    private PomParser pomParser;
    @Mock
    private ZipPackager zipPackager;
    @Mock
    private Log log;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mojo = new ZipMissingDependenciesMojo();
        mojo.setDependencyResolver(dependencyResolver);
        mojo.setArtifactoryChecker(artifactoryChecker);
        mojo.setPomParser(pomParser);
        mojo.setZipPackager(zipPackager);
        mojo.setLog(log);

        mojo.publicRepoUrl = "http://public-repo-url";
        mojo.jfrogUrl = "http://jfrog-url";
        mojo.username = "username";
        mojo.password = "password";
        mojo.pomFile = new File("pom.xml");
        mojo.outputZip = new File("output.zip");
        mojo.localMavenRepo = new File("local-repo");
        mojo.workingDirectory = new File("working-dir");
    }

    @Test
    public void testExecute_AllDependenciesAvailable() throws Exception {
        List<String> dependencies = Arrays.asList("group:artifact:1.0.0");

        when(pomParser.getDependenciesFromPom(anyString())).thenReturn(dependencies);
        when(artifactoryChecker.checkDependencyInArtifactory(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        mojo.execute();

        verify(dependencyResolver).resolveDependenciesFromPublicRepo(eq("http://public-repo-url"), any(File.class), any(File.class));
        verify(pomParser).getDependenciesFromPom(anyString());
        verify(artifactoryChecker).checkDependencyInArtifactory(eq("http://jfrog-url"), eq("username"), eq("password"), eq("group"), eq("artifact"), eq("1.0.0"));
        verify(log).info("All dependencies are available in Artifactory.");
        verify(zipPackager, never()).packageDependenciesIntoZip(anyList(), anyString(), any(File.class));
    }

    @Test
    public void testExecute_MissingDependencies() throws Exception {
        List<String> dependencies = Arrays.asList("group:artifact:1.0.0");

        when(pomParser.getDependenciesFromPom(anyString())).thenReturn(dependencies);
        when(artifactoryChecker.checkDependencyInArtifactory(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        mojo.execute();

        verify(dependencyResolver).resolveDependenciesFromPublicRepo(eq("http://public-repo-url"), any(File.class), any(File.class));
        verify(pomParser).getDependenciesFromPom(anyString());
        verify(artifactoryChecker).checkDependencyInArtifactory(eq("http://jfrog-url"), eq("username"), eq("password"), eq("group"), eq("artifact"), eq("1.0.0"));
        verify(dependencyResolver).downloadDependency(eq("group:artifact:1.0.0"), any(File.class), any(File.class));
        verify(zipPackager).packageDependenciesIntoZip(eq(dependencies), anyString(), any(File.class));
        verify(log).info(contains("Missing dependencies have been downloaded and packaged into"));
    }

    @Test(expected = MojoExecutionException.class)
    public void testExecute_Exception() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(dependencyResolver).resolveDependenciesFromPublicRepo(anyString(), any(File.class), any(File.class));

        mojo.execute();
    }
}