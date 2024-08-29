package com.vivekganesan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ZipPackagerTest {

    private ZipPackager zipPackager;

    @Mock
    private ZipOutputStream mockZipOutputStream;

    @Before
    public void setUp() {
        zipPackager = new ZipPackager() {
            @Override
            protected ZipOutputStream createZipOutputStream(OutputStream out) {
                return mockZipOutputStream;
            }
        };
    }

    @Test
    public void testPackageDependenciesIntoZip() throws Exception {
        List<String> dependencies = Arrays.asList("group1:artifact1:1.0.0", "group2:artifact2:2.0.0");
        File localMavenRepo = new File("local-repo");
        String zipFileName = "output.zip";

        zipPackager.packageDependenciesIntoZip(dependencies, zipFileName, localMavenRepo);
    }

    @Test
    public void testCreateZipOutputStream() {
        OutputStream mockOutputStream = mock(OutputStream.class);
        ZipOutputStream mockZipOutputStream = zipPackager.createZipOutputStream(mockOutputStream);

        assertNotNull(mockZipOutputStream);
    }
}