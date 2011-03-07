package org.metadsl.bootstrap;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.metadsl.resolvers.Artifact;
import org.metadsl.resolvers.Bundle;
import org.metadsl.resolvers.RepositoryManager;
import org.metadsl.resolvers.RepositoryManager.RepositoryInfo;

/**
 * This class in intended to provide a minimalist infrastructure necessary to
 * bootstrap the following dependencies:
 */
public class MavenBootstrap {

    private final String localRepo;
    private final List<String> remoteRepos;
    private final RepositoryInfo localList;
    private final RepositoryInfo[] remoteList;

    private boolean debug = false;
    private boolean verbose = false;

    private final Proxy proxy;

    public MavenBootstrap(
                    final String localRepo,
                    final List<String> remoteRepos) {
        this.localRepo = localRepo;
        this.remoteRepos = remoteRepos;
        final RepositoryManager rm = new RepositoryManager();
        this.localList  = rm.getLocalRepository(localRepo);
        this.remoteList = rm.getRemoteRepositories(remoteRepos);
        // obtain proxy configuration
        final String proxyHost = System.getProperty("http.proxyHost");
        final String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost!=null && proxyHost.trim().length()>0 && proxyPort!=null && proxyPort.trim().length()>0) {
        	this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
        } else if ((proxyHost==null || proxyHost.trim().isEmpty()) && (proxyPort==null || proxyPort.trim().isEmpty())) {
        	this.proxy = Proxy.NO_PROXY;
        } else {
        	throw new IllegalArgumentException("illegal or incomplete proxy configuration");
        }
    }


    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public URL[] resolve(final String[] dependencies) {
        final List<URL> urls = new ArrayList<URL>();
        for (final String dependency : dependencies) {
            final Bundle[] bundles = Bundle.create(dependency);
            for (final Bundle bundle : bundles) {
                final String path = getBundlePath(bundle);
                // try to find dependency on local repositories
                URL url = findDependency(new RepositoryInfo[] { localList }, path);
                if (url==null) {
                    url = findDependency(remoteList, path);
                }
                if (url==null) {
                    throw new RuntimeException(String.format("could not load %s", bundle));
                }
                urls.add(url);
            }
        }
        return urls.toArray(new URL[0]);
    }

    private URL findDependency(final RepositoryInfo[] repositories, final String path) {
        URL url = null;
        try {
            for (final RepositoryInfo repo : repositories) {
                // test if InputStream exists
                url = new URL(repo.getURL().concat(path));
                if (debug) System.out.println(String.format("  -- trying %s", url.toString()));
                final URLConnection conn = url.openConnection(proxy);
                conn.connect();
                final InputStream is = conn.getInputStream();
                if (verbose) System.err.println(String.format("OK %s", url.toString()));
                break;
            }
        } catch (final Exception e) {
            url = null;
        }
        return url;
    }


    // @Override
    public String getBundlePath(final Bundle bundle) {
        final StringBuilder path = new StringBuilder();
        path.append("/").append(bundle.getGroupId().replace('.', '/'));
        path.append("/").append(bundle.getArtifactId());
        path.append("/").append(bundle.getVersion());
        path.append("/").append(bundle.getArtifactId()).append("-").append(bundle.getVersion());
        path.append('.').append("jar");
        return path.toString();
    }

    // @Override
    public String getArtifactPath(final Artifact artifact) {
        final StringBuilder path = new StringBuilder();
        path.append("/").append(artifact.getGroupId().replace('.', '/'));
        path.append("/").append(artifact.getArtifactId());
        path.append("/").append(artifact.getVersion());
        path.append("/").append(artifact.getArtifactId()).append("-").append(artifact.getVersion());
        final String classifier = artifact.getClassifier();
        if (classifier!= null && classifier.length()>0) {
            path.append("-").append(classifier);
        }
        final String packaging = artifact.getPackaging();
        if (packaging!= null && packaging.length()>0) {
            path.append(".").append(packaging);
        } else {
            path.append(".jar");
        }
        return path.toString();
    }


    private static final String[] dependencies = {
        "ch.qos.logback:logback-classic:0.9.24",
        "ch.qos.logback:logback-core:0.9.24",
        "classworlds:classworlds:1.1",
        "com.jcraft:jsch:0.1.27",
        "commons-cli:commons-cli:1.0",
        "commons-httpclient:commons-httpclient:2.0.2",
        "commons-logging:commons-logging:1.0.4",
        "de.zeigermann.xml:xml-im-exporter:1.1",
        "jdom:jdom:1.0",
        "jtidy:jtidy:4aug2000r7-dev",
        "junit:junit:3.8.1",
        "org.apache.maven:maven-artifact:2.0.9",
        "org.apache.maven:maven-artifact-manager:2.0.9",
        "org.apache.maven:maven-core:2.0.9",
        "org.apache.maven:maven-error-diagnostics:2.0.9",
        "org.apache.maven:maven-model:2.0.9",
        "org.apache.maven:maven-monitor:2.0.9",
        "org.apache.maven:maven-plugin-api:2.0.9",
        "org.apache.maven:maven-plugin-descriptor:2.0.9",
        "org.apache.maven:maven-plugin-parameter-documenter:2.0.9",
        "org.apache.maven:maven-plugin-registry:2.0.9",
        "org.apache.maven:maven-profile:2.0.9",
        "org.apache.maven:maven-project:2.0.9",
        "org.apache.maven:maven-repository-metadata:2.0.9",
        "org.apache.maven:maven-settings:2.0.9",
        "org.apache.maven.doxia:doxia-sink-api:1.0-alpha-10",
        "org.apache.maven.reporting:maven-reporting-api:2.0.9",
        "org.apache.maven.wagon:wagon-file:1.0-beta-2",
        "org.apache.maven.wagon:wagon-http-lightweight:1.0-beta-2",
        "org.apache.maven.wagon:wagon-http-shared:1.0-beta-2",
        "org.apache.maven.wagon:wagon-provider-api:1.0-beta-2",
        "org.apache.maven.wagon:wagon-ssh:1.0-beta-2",
        "org.apache.maven.wagon:wagon-ssh-common:1.0-beta-2",
        "org.apache.maven.wagon:wagon-ssh-external:1.0-beta-2",
        "org.apache.maven.wagon:wagon-webdav:1.0-beta-2",
        "org.codehaus.plexus:plexus-container-default:1.0-alpha-9-stable-1",
        "org.codehaus.plexus:plexus-interactivity-api:1.0-alpha-4",
        "org.codehaus.plexus:plexus-utils:1.5.1",
        "org.slf4j:slf4j-api:1.6.0",
        "slide:slide-webdavlib:2.1",
        "urbanophile:java-getopt:1.0.9",
        "xml-apis:xml-apis:1.0.b2",
        "org.metadsl:maven-metadsl-plugin:1.0-SNAPSHOT", // this package
    };

}
