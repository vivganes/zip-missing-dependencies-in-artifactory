package com.vivekganesan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DependencyResolverTest {

    private DependencyResolver dependencyResolver;

    @Mock
    private ProcessBuilder processBuilder;

    @Mock
    private Process process;

    @Before 
    public void setUp() throws Exception {
        dependencyResolver = spy(new DependencyResolver());
        doReturn(processBuilder).when(dependencyResolver).createProcessBuilder(any());
        when(processBuilder.start()).thenReturn(process);
        when(process.waitFor()).thenReturn(0);
    }

    @Test
    public void testResolveDependenciesFromPublicRepo() throws Exception {
        File localRepo = new File("local");
        File workingDir = new File("working");
        dependencyResolver.resolveDependenciesFromPublicRepo("http://repo.url", localRepo, workingDir);

        verify(dependencyResolver).executeCommand(argThat(command -> 
            command.contains("mvn") && command.contains("dependency:resolve")), eq(workingDir));
        verify(processBuilder).directory(workingDir);
        verify(processBuilder).inheritIO();
        verify(processBuilder).start();
        verify(process).waitFor();
    }

    @Test
    public void testDownloadDependency() throws Exception {
        File localRepo = new File("local");
        File workingDir = new File("working");
        dependencyResolver.downloadDependency("group:artifact:1.0.0", localRepo, workingDir);

        verify(dependencyResolver, times(2)).executeCommand(argThat(command -> 
            command.contains("mvn") && (command.contains("dependency:get") || command.contains("dependency:copy"))), eq(workingDir));
        verify(processBuilder, times(2)).directory(workingDir);
        verify(processBuilder, times(2)).inheritIO();
        verify(processBuilder, times(2)).start();
        verify(process, times(2)).waitFor();
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteCommand_Failure() throws Exception {
        when(process.waitFor()).thenReturn(1);

        File localRepo = new File("local");
        File workingDir = new File("working");
        dependencyResolver.resolveDependenciesFromPublicRepo("http://repo.url", localRepo, workingDir);
    }
}