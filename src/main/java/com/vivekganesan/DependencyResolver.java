package com.vivekganesan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DependencyResolver {

    public void resolveDependenciesFromPublicRepo(String publicRepoUrl, File localMavenRepo, File workingDirectory) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("mvn");
        command.add("dependency:resolve");
        command.add("-Dmaven.repo.remote=" + publicRepoUrl);
        command.add("-Dmaven.repo.local=" + localMavenRepo.getPath());

        executeCommand(command, workingDirectory);
    }

    public void downloadDependency(String dependency, File localMavenRepo, File workingDirectory) throws Exception {
        String[] parts = dependency.split(":");
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];

        List<String> getCommand = new ArrayList<>();
        getCommand.add("mvn");
        getCommand.add("dependency:get");
        getCommand.add("-DgroupId=" + groupId);
        getCommand.add("-DartifactId=" + artifactId);
        getCommand.add("-Dversion=" + version);
        getCommand.add("-Dmaven.repo.local=" + localMavenRepo.getPath());

        executeCommand(getCommand, workingDirectory);

        List<String> copyCommand = new ArrayList<>();
        copyCommand.add("mvn");
        copyCommand.add("dependency:copy");
        copyCommand.add("-Dartifact=" + groupId + ":" + artifactId + ":" + version);
        copyCommand.add("-DoutputDirectory=dependencies");
        copyCommand.add("-Dmaven.repo.local=" + localMavenRepo.getPath());

        executeCommand(copyCommand, workingDirectory);
    }

    protected void executeCommand(List<String> command, File workingDirectory) throws Exception {
        ProcessBuilder builder = createProcessBuilder(command.toArray(new String[0]));
        builder.directory(workingDirectory);
        builder.inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode + ": " + String.join(" ", command));
        }
    }

    protected ProcessBuilder createProcessBuilder(String... command) {
        return new ProcessBuilder(command);
    }
}