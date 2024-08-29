package com.vivekganesan;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipPackager {

    public void packageDependenciesIntoZip(List<String> dependencies, String zipFileName, File localMavenRepo) throws Exception {
        try (ZipOutputStream zipOut = createZipOutputStream(Files.newOutputStream(Paths.get(zipFileName)))) {
            for (String dependency : dependencies) {
                String[] parts = dependency.split(":");
                String groupId = parts[0];
                String artifactId = parts[1];
                String version = parts[2];
                String jarFileName = artifactId + "-" + version + ".jar";
                String filePath = localMavenRepo.getPath() + "/" + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + jarFileName;

                File fileToZip = new File(filePath);
                if (fileToZip.exists()) {
                    zipOut.putNextEntry(new ZipEntry("dependencies/" + jarFileName));
                    Files.copy(fileToZip.toPath(), zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }

    protected ZipOutputStream createZipOutputStream(OutputStream out) {
        return new ZipOutputStream(out);
    }
}