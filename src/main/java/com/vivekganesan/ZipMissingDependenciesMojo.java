package com.vivekganesan;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

@Mojo(name = "zipMissingDependencies", defaultPhase = LifecyclePhase.PACKAGE)
public class ZipMissingDependenciesMojo extends AbstractMojo {

    @Parameter(property = "publicRepoUrl", required = true)
    String publicRepoUrl;

    @Parameter(property = "jfrogUrl", required = true)
    String jfrogUrl;

    @Parameter(property = "username", required = true)
    String username;

    @Parameter(property = "password", required = true)
    String password;

    @Parameter(defaultValue = "${project.basedir}/pom.xml", required = true)
    File pomFile;

    @Parameter(defaultValue = "${project.build.directory}/missing_dependencies.zip", required = true)
    File outputZip;

    @Parameter(defaultValue = "${user.home}/.m2/repository", required = true)
    File localMavenRepo;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    File workingDirectory;

    private DependencyResolver dependencyResolver;
    private ArtifactoryChecker artifactoryChecker;
    private PomParser pomParser;
    private ZipPackager zipPackager;

    public ZipMissingDependenciesMojo() {
        this.dependencyResolver = new DependencyResolver();
        this.artifactoryChecker = new ArtifactoryChecker();
        this.pomParser = new PomParser();
        this.zipPackager = new ZipPackager();
    }

    public void execute() throws MojoExecutionException {
        try {
            getLog().info("Resolving dependencies from public repository...");
            dependencyResolver.resolveDependenciesFromPublicRepo(publicRepoUrl, localMavenRepo, workingDirectory);

            getLog().info("Checking dependencies against JFrog Artifactory...");
            List<String> dependencies = pomParser.getDependenciesFromPom(pomFile.getPath());
            List<String> missingDependencies = new ArrayList<>();

            for (String dependency : dependencies) {
                String[] parts = dependency.split(":");
                String groupId = parts[0];
                String artifactId = parts[1];
                String version = parts[2];

                if (!artifactoryChecker.checkDependencyInArtifactory(jfrogUrl, username, password, groupId, artifactId, version)) {
                    missingDependencies.add(dependency);
                }
            }

            if (missingDependencies.isEmpty()) {
                getLog().info("All dependencies are available in Artifactory.");
            } else {
                getLog().info("Missing dependencies found. Downloading and packaging...");
                for (String dep : missingDependencies) {
                    getLog().info("Downloading dependency: " + dep);
                    dependencyResolver.downloadDependency(dep, localMavenRepo, workingDirectory);
                }
                zipPackager.packageDependenciesIntoZip(missingDependencies, outputZip.getPath(), localMavenRepo);
                getLog().info("Missing dependencies have been downloaded and packaged into " + outputZip.getPath());
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error during dependency resolution and packaging", e);
        }
    }

    // Setters for dependency injection in tests
    public void setDependencyResolver(DependencyResolver dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
    }

    public void setArtifactoryChecker(ArtifactoryChecker artifactoryChecker) {
        this.artifactoryChecker = artifactoryChecker;
    }

    public void setPomParser(PomParser pomParser) {
        this.pomParser = pomParser;
    }

    public void setZipPackager(ZipPackager zipPackager) {
        this.zipPackager = zipPackager;
    }
}
