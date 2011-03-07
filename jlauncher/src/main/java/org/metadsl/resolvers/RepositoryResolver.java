package org.metadsl.resolvers;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.metadsl.resolvers.RepositoryManager.RepositoryInfo;

import org.metadsl.util.Settings;



public class RepositoryResolver {

    private final PlexusContainer   container;
    private final RepositoryManager repoManager;

    public RepositoryResolver() {
        this.container   = new Settings().getPlexusContainer();
        this.repoManager = new RepositoryManager();
    }

    public DefaultArtifactRepository getLocalRepository(final String localRepo) {
        final RepositoryInfo ri = repoManager.getLocalRepository(localRepo);
        final ArtifactRepositoryLayout layout = getDefaultLayout();
        return new DefaultArtifactRepository(ri.getName(), ri.getURL(), layout);
    }

    public List<ArtifactRepository> getRemoteArtifactRepositories(List<String> remoteRepoList) {
        final RepositoryInfo[] ri = repoManager.getRemoteRepositories(remoteRepoList);
        final ArtifactRepositoryLayout layout = getDefaultLayout();
        final List<ArtifactRepository> result = new ArrayList<ArtifactRepository>();
        for (RepositoryInfo repo : ri) {
            result.add(new DefaultArtifactRepository(repo.getName(), repo.getURL(), layout));
        }
        return result;
    }

    private ArtifactRepositoryLayout getDefaultLayout() {
        try {
            return (ArtifactRepositoryLayout) container.lookup(ArtifactRepositoryLayout.class.getName(), "default");
        } catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

}
