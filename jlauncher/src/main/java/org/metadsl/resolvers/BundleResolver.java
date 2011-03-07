package org.metadsl.resolvers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionNode;


public class BundleResolver {

    private final String[] bundles;
    private final DependencyResolver resolver;
    private boolean debug = false;
    
    private List<ResolutionNode> artifacts = null;
    

    //
    // public constructors
    //

    public BundleResolver(
            final Bundle bundle,
            final DependencyResolver resolver) {
    	this(bundle.toString(), resolver);
    }

    public BundleResolver(
            final String bundle,
            final DependencyResolver resolver) {
    	this.bundles = new String[] { bundle };
    	this.resolver = resolver;
    }

    public BundleResolver(
            final Bundle[] bundles,
            final DependencyResolver resolver) {
    	this(convert(bundles), resolver);
    }

    public BundleResolver(
            final String[] bundles,
            final DependencyResolver resolver) {
    	this.bundles = bundles;
    	this.resolver = resolver;
    }

    public BundleResolver(
            final List<Bundle> bundles,
            final DependencyResolver resolver) {
        this(bundles.toArray(new Bundle[bundles.size()]), resolver);
    }

    
    //
    // public methods
    //

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /**
     * Resolves dependencies
     * <p>
     * Warning: This method is not synchronized
     * 
     * @return List<ResolutionNode>
     */
    public List<ResolutionNode> resolve() {
    	if (this.artifacts==null) {
        	this.artifacts = new ArrayList<ResolutionNode>();
            for (final String bundle : bundles) {
                final List<ResolutionNode> nodes = resolve(bundle, resolver);
                artifacts.addAll(nodes);
            }
    	}
        return Collections.unmodifiableList(this.artifacts);
    }

    public URL[] resolveAsURL() {
    	// guarantee that dependencies are resolved
    	resolve();
    	// converts List<ResolutionNode> into URL[]
        final URL[] urls = new URL[this.artifacts.size()];
        for (int i = 0; i < this.artifacts.size(); i++) {
            final ResolutionNode node = this.artifacts.get(i);
            final String address = null;
            try {
                urls[i] = node.getArtifact().getFile().toURI().toURL();
            } catch (final MalformedURLException e) {
                throw new RuntimeException(address, e);
            }
        }
        return urls;
    }
    
    public String classpath() {
    	// guarantee that dependencies are resolved
    	resolve();
    	// obtains classpath
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

    public String jar() {
    	// guarantee that dependencies are resolved
    	resolve();
    	// obtain last jar in the dependency chain
    	if (artifacts==null || artifacts.isEmpty()) {
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


    public String mainClass() {
    	String jarName = jar();
        String mainClass = null;
		try {
	        final Manifest manifest = new JarFile(jarName).getManifest();
	        mainClass = manifest.getMainAttributes().getValue("Main-Class");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        if (mainClass==null || mainClass.trim().length()==0)
        	throw new RuntimeException(String.format("Main-Class not found in %s", jarName));
        if (debug) System.out.println(String.format("Main-Class=%s", mainClass));
        return mainClass;
    }
    
    
    
    //
    // public static methods
    //
    
    public static final String[] convert(Bundle[] bundles) {
    	String[] result = new String[bundles.length];
    	for (int i=0; i<bundles.length; i++) {
    		result[i] = bundles[i].toString();
    	}
    	return result;
    }
    

    //
    // private methods
    //

    private List<ResolutionNode> resolve(
            final String bundle,
            final DependencyResolver resolver) {
        final List<ResolutionNode> nodes = new ArrayList<ResolutionNode>();
        final ArtifactResolutionResult result = resolver.resolve(bundle);
        for (final Object o : result.getArtifactResolutionNodes()) {
            final ResolutionNode node = (ResolutionNode) o;
            nodes.add(node);
            if (debug) System.out.println(String.format("bundle resolved: %s", node.getArtifact().getFile().getAbsolutePath()));
        }
        return nodes;
    }

}
