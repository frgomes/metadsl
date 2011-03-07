package org.metadsl.resolvers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionNode;



public class BundleResolver {

    private boolean debug = false;


    //
    // public constructors
    //

    public BundleResolver() {
        // nothing
    }


    //
    // public methods
    //

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

//    public String classpath(final String[] bundles) {
//        return classpath(resolve(bundles));
//    }
//
//    public String jar(final String[] bundles) {
//        return jar(resolve(bundles));
//    }



    //
    // private methods
    //

    public String classpath(final List<ResolutionNode> artifacts) {
        final StringBuilder sb = new StringBuilder();
        try {
            for (final ResolutionNode artifact : artifacts) {
                sb.append(artifact.getArtifact().getFile().getCanonicalPath()).append(File.pathSeparatorChar);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Cannot obtain CLASSPATH", e);
        }
        if (debug) System.out.println(sb.toString());
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public String jar(final List<ResolutionNode> artifacts) {
    	if (artifacts==null || artifacts.size()==0) {
            throw new RuntimeException("Resolve process haven't found any JAR file");
    	}
        final ResolutionNode node = artifacts.get(artifacts.size()-1);
        try {
            final String jarFile = node.getArtifact().getFile().getCanonicalPath();
            if (debug) System.out.println(jarFile);
            return jarFile;
        } catch (final IOException e) {
            throw new RuntimeException("Cannot obtain JAR file location", e);
        }
    }


    //
    // public methods
    //

    // TODO: validate resolved artifacts against already resolved artifacts in order to warn when
    // the same artifact appears more than once with different versions

    public List<ResolutionNode> resolve(
            final String bundle,
            DependencyResolver resolver) {
        final List<ResolutionNode> nodes = new ArrayList<ResolutionNode>();
        final ArtifactResolutionResult result = resolver.resolve(bundle);
        for (final Object o : result.getArtifactResolutionNodes()) {
            final ResolutionNode node = (ResolutionNode) o;
            nodes.add(node);
        }
        return nodes;
    }

    public URL[] resolveAsURL(
            final String bundle,
            final DependencyResolver resolver) {
        final List<ResolutionNode> nodes = resolve(bundle.toString(), resolver);
        return logURLs(toURL(nodes));
    }


    //
    // private methods
    //

    private URL[] toURL(final List<ResolutionNode> nodes) {
        final URL[] urls = new URL[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            ResolutionNode node = nodes.get(i);
            String address = null;
            try {
                urls[i] = node.getArtifact().getFile().toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(address, e);
            }
        }
        return urls;
    }

    private List<ResolutionNode> logResolutionNodes(List<ResolutionNode> nodes) {
        for (ResolutionNode node : nodes) {
            if (debug) System.out.println(String.format("bundle resolved: %s", node.getArtifact().getFile().getAbsolutePath()));
        }
        return nodes;
    }

    private URL[] logURLs(URL[] urls) {
        for (URL url : urls) {
            if (debug) System.out.println(String.format("bundle resolved: %s", url.toString()));
        }
        return urls;
    }

}
