package org.metadsl.resolvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.metadsl.util.Settings;



/**
 * This class resolve direct and transitive dependencies of both
 * <li>bundles: <code>groupId:artifactId:version</code></li>
 * <li>artifact: <code>groupId:artifactId:type:version:scope</code></li>
 */
public class DependencyResolver {

    private final PlexusContainer container;
    private final ArtifactRepository localRepository;
    private final List<ArtifactRepository> remoteRepositories;
    private final ArtifactFactory artifactFactory;
    private final ArtifactResolver artifactResolver;
    private final ArtifactMetadataSource metadataSource;
    private final MavenProjectBuilder projectBuilder;

    private boolean debug = false;


    public DependencyResolver(final String localRepo, final String remoteRepoList) {
        this(localRepo, Arrays.asList(remoteRepoList.split("[,;]")) );
    }

    public DependencyResolver(final String localRepo, final List<String> remoteRepoList) {
        this(new RepositoryResolver(), localRepo, remoteRepoList);
    }

    public DependencyResolver(final RepositoryResolver rr, final String localRepo, final List<String> remoteRepoList) {
        this(rr.getLocalRepository(localRepo), rr.getRemoteArtifactRepositories(remoteRepoList));
    }

    public DependencyResolver(final ArtifactRepository localRepo, final List<ArtifactRepository> remoteRepoList) {
        Settings settings = new Settings();
        this.container = settings.getPlexusContainer();
        try {
            this.artifactFactory  = (ArtifactFactory) container.lookup(ArtifactFactory.class.getName());
            this.artifactResolver = (ArtifactResolver) container.lookup(ArtifactResolver.class.getName());
            this.metadataSource   = (ArtifactMetadataSource) container.lookup(ArtifactMetadataSource.class.getName(), "maven");
            this.projectBuilder   = (MavenProjectBuilder) container.lookup(MavenProjectBuilder.class.getName());

            //TODO: parallel threads should be supported when migrating to Maven 3.x

            this.localRepository = localRepo;
            this.remoteRepositories = remoteRepoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Smart resolver for projects and artifacts
     * <p>
     * This resolver parses strings separated by ':', trying to identify if a project of an artifact was specified.
     * Projects typically conform to <code>groupId:artifactId:version</code> whilst artifacts typically conform to
     * <code>groupId:artifactId:type:version:scope</code>
     *
     * @param name
     */
    public ArtifactResolutionResult resolve(final String name) {
        final StringTokenizer st = new StringTokenizer(name, ":");
        if (st.countTokens() == 3) {
            if (debug) System.out.println(String.format("Resolving dependencies for project %s", name));
            return resolve(st.nextToken(), st.nextToken(), st.nextToken());
        } else if (st.countTokens() == 5) {
            if (debug) System.out.println(String.format("Resolving dependencies for artifact %s", name));
            return resolve(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken());
        } else {
            throw new IllegalArgumentException(String.format("Neither a project name nor an artifact name could be parsed: %s", name));
        }
    }

    /**
     * Resolves dependencies of an artifact <code>groupId:artifactId:type:version:scope</code>
     *
     * @param groupId
     * @param artifactId
     * @param type
     * @param version
     * @param scope
     */
    private ArtifactResolutionResult resolve(
            final String groupId,
            final String artifactId,
            final String type,
            final String version,
            final String scope) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Resolves dependencies of a bundle <code>groupId:artifactId:version</code>
     *
     * @param groupId
     * @param artifactId
     * @param version
     */
    @SuppressWarnings("unchecked")
    public ArtifactResolutionResult resolve(
            final String groupId,
            final String artifactId,
            final String version) {

        // final Artifact artifact = artifactFactory.createDependencyArtifact(groupId, artifactId, VersionRange.createFromVersion(version), type, "", scope);
        final Artifact pomArtifact = getPomArtifact(groupId, artifactId, version);

        try {
            // load the pom as a MavenProject and get all of the dependencies for the project
            final MavenProject project = loadPomAsProject(projectBuilder, pomArtifact);
            final List dependencies = project.getDependencies();

            // make Artifacts of all the dependencies and the project itself
            final Set<Artifact> dependencyArtifacts = MavenMetadataSource.createArtifacts(artifactFactory, dependencies, null, null, null);
            dependencyArtifacts.add(project.getArtifact());

            // create listener for monitoring the resolution process
            final List<RuntimeResolutionListener> listeners = new ArrayList<RuntimeResolutionListener>();
            listeners.add(new RuntimeResolutionListener());

            // resolve all dependencies transitively to obtain a comprehensive list of jars
            final ArtifactResolutionResult result =
                    artifactResolver.resolveTransitively(dependencyArtifacts, pomArtifact,
                    Collections.EMPTY_MAP,
                    localRepository, remoteRepositories,
                    metadataSource, null, listeners);

            if (debug) {
                for (final Object o : result.getArtifactResolutionNodes()) {
                    System.out.println(o.toString());
                }
            }

            return result;
        } catch (final Exception e) {
            throw new RuntimeException("Dependency Resolver failed", e);
        }

    }

    // FIXME: this method should called implicitly
    public void shutdown() {
        try {
            container.release(artifactResolver);
            container.release(projectBuilder);
            container.release(metadataSource);
            container.release(artifactFactory);
        } catch (final ComponentLifecycleException e) {
            e.printStackTrace();
        }
    }

    private Artifact getPomArtifact(final String groupId, final String artifactId, final String versionId) {
        return this.artifactFactory.createBuildArtifact(groupId, artifactId, versionId, "pom");
    }

    private MavenProject loadPomAsProject(final MavenProjectBuilder projectBuilder, final Artifact pomArtifact) throws ProjectBuildingException {
        return projectBuilder.buildFromRepository(pomArtifact, remoteRepositories, localRepository);
    }


    //
    // private inner classes
    //

    private class RuntimeResolutionListener implements ResolutionListener {

        public void testArtifact(final Artifact arg0) {
            if (debug) System.out.println("TESTING ARTIFACT " + arg0);
        }

        public void startProcessChildren(final Artifact arg0) {
            if (debug) System.out.println("STARTING CHILDREN " + arg0);
        }

        public void endProcessChildren(final Artifact arg0) {
            if (debug) System.out.println("ENDING CHILDREN " + arg0);
        }

        public void includeArtifact(final Artifact arg0) {
            if (debug) System.out.println("INCLUDE ARTIFACT " + arg0);
        }

        public void omitForNearer(final Artifact arg0, final Artifact arg1) {
            if (debug) System.out.println("OMITTING " + arg0 + " for NEARER " + arg1);
        }

        public void updateScope(final Artifact arg0, final String arg1) {
            if (debug) System.out.println("UPDATE of SCOPE " + arg0 + "=" + arg1);
        }

        public void manageArtifact(final Artifact arg0, final Artifact arg1) {
            if (debug) System.out.println("MANAGE ARTIFACT " + arg0 + " and " + arg1);
        }

        public void omitForCycle(final Artifact arg0) {
            if (debug) System.out.println("OMIT FOR CYCLE " + arg0);
        }

        public void updateScopeCurrentPom(final Artifact arg0, final String arg1) {
            if (debug) System.out.println("UPDATE SCOPE CURRENT POM " + arg0 + "=" + arg1);
        }

        public void selectVersionFromRange(final Artifact arg0) {
            if (debug) System.out.println("SELECT VERSION FROM RANGE " + arg0);
        }

        public void restrictRange(final Artifact arg0, final Artifact arg1, final VersionRange arg2) {
            if (debug) System.out.println("RESTRICT RANGE " + arg0 + " " + arg1 + " range=" + arg2);
        }
    }
}
