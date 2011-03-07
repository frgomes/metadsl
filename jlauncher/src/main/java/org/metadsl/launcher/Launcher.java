package org.metadsl.launcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import org.metadsl.bootstrap.MavenBootstrap;
import org.metadsl.bootstrap.MinimalistCmdParser;
import org.metadsl.bootstrap.PostDelegationClassLoader;


/**
 * This provides a small application intended to retrieve dependencies from
 * Maven2 repositories, build the corresponding CLASSPATH and finally launch the
 * application represented by bundle description(s) passed as argument.
 * <p>
 * This is the general calling convention:
 *
 * <pre>
 * java \
 *   <VM_ARGS> \
 *   -jar launcher.jar \
 *   <JLAUNCHER_ARGS>
 *   -V <LAUNCHED_VM_ARGS> \
 *   -- <APP_ARGS>
 * </pre>
 *
 * where:
 *
 * <li>VM_ARGS - arguments to the bootstrap JVM, which are not parsed by Launcher.
 * These arguments may affect Launcher itself and should never affect applications launched
 * by Launcher. Possibly you will never have to care about these arguments</li>
 *
 * <li>JLAUNCHER_ARGS - You may find useful to define special location for a local
 * Maven2 repository, using <code>-M &lt;local-repository&gt;</code> or a list of remote Maven2
 * repositories using <code>[ -R &lt;remote-repository&gt; ]*</code>.
 * JLauncher is pre-configured with a list of
 * <a href="http://www.mvnbrowser.com/repositories.html">popular public Maven2 repositories</a>
 * so, chances are you don't need to specify anything in particular. Notice that, if you specify
 * your own remote repositories, all pre-defined repositories are not considered anymore.</li>
 *
 * <li>LAUNCHED_VM_ARGS - arguments to the launched JVM (processed by
 * Launcher) : once Launcher will end up launching a new JVM, you have
 * opportunity to specify arguments to this new JVM here. Launcher simply
 * forwards arguments to the new created JVM, without doing any validation of
 * them. Typical uses are: additional memory for large applications, specialised
 * garbage collectors, etc;</li>
 *
 * <li>BUNDLE - is a complete groupId:artifactId:version bundle specification.
 * You can specify multiple bundles separated by commas;</li>
 *
 * <li>mainClass - this is an optional argument which specifies the class name
 * to be launched, preceded by a equal sign. If this argument is not specified,
 * the last bundle is considered as the main bundle and the main class specified
 * in its MANIFEST.MF file is launched, if any. Notice that the mainClass is searched
 * in order of declaration of bundles.</li>
 *
 * <li>APP_ARGS - arguments to the launched application. </li>
 * <p>
 * Examples:
 *
 * <pre>
 * java \
 *   -jar jlauncher.jar \
 *     -M C:/HOME/.m2/repository \
 *     -R http://example.com/repos \
 *     -R http://acme.com/repos \
 *     -b group1:artifact1:0.1.10 \
 *     -b group2:artifact2:0.1.10 \
 *     -V -Xms80m -Xmx512m -DDEBUG \
 *     -- config.properties
 * </pre>
 *
 * In the example above, there's no option <code>-m</code>, which means that the main class
 * needs to be searched in all bundles, in sequence.
 * JVM parameters JVM arguments <code>-Xms80m -Xmx512m -DDEBUG</code> MUST BE prefixed by
 * option <code>-V</code> if they are specified.
 * If the launched application requires arguments, they MUST BE necessarily prefixed by
 * option <code>--</code>.
 * <p>
 *
 * <pre>
 * java \
 *   -jar jlauncher.jar \
 *     -M C:/HOME/.m2/repository \
 *     -R http://example.com/repos \
 *     -R http://acme.com/repos \
 *     -b group1:artifact1:0.1.10 \
 *     -b group2:artifact2:0.1.10 \
 *     -m com.metatrader.nonexistent.start
 *     -V -Xms80m -Xmx512m -DDEBUG \
 *     -- config.properties
 * </pre>
 *
 * The example above specifies that the main class <code>com.baml.cube.start</code> in
 * addition to what was presented in the previous example.
 * <p>
 *
 * @see http://jira.codehaus.org/browse/MNGECLIPSE-1180
 * @see http://www.mvnbrowser.com/repositories.html
 *
 * @author Richard Gomes <rgomes1997@yahoo.co.uk>
 */
public class Launcher implements Runnable {

    private final String[] args;

	private boolean debug = false;
    private boolean verbose = false;
    private String localRepository = null;

    // This variable is tree-state:
    //     null      : means that defaults are assumed
    //     empty     : no remote repositories
    //     populated : list of remote repositories
    private List<String> remoteRepositories = null;


    public static void main(final String[] args) {
        new Launcher(args).run();
    }

    public Launcher(final String args[]) {
        this.args = args;
    }


    public void run() {

    	final String defaultLocalRepository = System.getProperty("user.home").concat("/.m2/repository");

    	// *****************************************************************************************
        //
        // Parse command line options using the MinimalistCmdParser
        // --------------------------------------------------------
        //
        // The MinimalistCmdParser is designed and intended to support only a very restricted
        // subset of arguments, skipping silently everything it does not recognises.
        //
        // In a nutshell, we are only interested to know which repositories need to be considered
        // during the bootstrap process and that's all.
        //
        // After the bootstrap succeeds, it will be necessary to parse arguments again using a
        // more powerful and more flexible parser, which will be available at that time. Then,
        // possibly some arguments very important during the bootstrap will not be that important
        // later because all repositories will be already configured and ready to be used via a
        // custom ClassLoader configured during the bootstrap.
        //
        // *****************************************************************************************

        final MinimalistCmdParser cli = new MinimalistCmdParser(args);
        cli.setOpterr(false); // ignore unknown arguments
        int ch = cli.getopt();
        while (ch != -1) {
            switch (ch) {
            	case '?': /* ignore unknown arguments */ break;
	            case LauncherDelegate.opt_jvmArgs: break;

                case LauncherDelegate.opt_help:        usage(); break;
                case LauncherDelegate.opt_verbose:     verbose = true; break;
                case LauncherDelegate.opt_debug:       verbose = debug = true; cli.setDebug(true); break;

                case LauncherDelegate.opt_localRepo:
                	defineLocalRepository(cli.getOptarg(), defaultLocalRepository);
                	break;
                case LauncherDelegate.opt_remoteRepos:
                	defineRemoteRepositories(cli.getOptarg(), null);
                	break;
//                case LauncherDelegate.opt_eclipseRepo:
//                	defineRemoteRepositories(cli.getOptarg(), "shadow:target/classes");
//                	break;

                default:
                    System.out.println(String.format("The option '%c' is not valid", ch));
                    usage();
                    System.exit(1);
            }
            if (ch==LauncherDelegate.opt_jvmArgs) break;
            ch = cli.getopt();
        }

        // make sure local repository is defined
        if (localRepository==null || localRepository.trim().isEmpty()) {
        	defineLocalRepository(null, defaultLocalRepository);
        }


        // Launches LauncherDelegate on a separate ClassLoader and executes "run()" method.
        // This step is intended to configure a ClassLoader which gives access to specified
        // repositories, both local and remote repositories.
        final MavenBootstrap bootstrap = new MavenBootstrap(localRepository, remoteRepositories);
        bootstrap.setDebug(debug);
        bootstrap.setVerbose(verbose);
        
        final URL[] packages = bootstrap.resolve(dependencies);
        launch(packages, "org.metadsl.launcher.LauncherDelegate", "main", new Object[] { args }, debug);
    }
    
    
    /**
     * Instantiates and runs a given class on a separate ClassLoader 
     * <p>
     * This custom ClassLoader loads classes in a different policy when compared with
     * default class loaders provided by Java runtime. This different strategy is needed
     * in order to isolate classes in a sort of separate context, which could be thought
     * as something similar of launching a separate application without having to launch
     * a separate JVM.
     * <p>
     * This way, this "sort of separate application" will think it was launched by a
     * separate JVM, pretty much like when an application is launched from a single JAR file
     * (an ubber JAR containing everything needed) when, in fact, it was launched by a
     * very small application, with a minimalist context, without the need of packaging
     * all dependencies in a single JAR file.
     *
     * @param classpath is an array of URLs to dependency .jar files
     * @param className is the class to be instantiated
     * @param constructorArgs is an array of arguments to be passed to the constructor
     * @param methodName is a method name to be executed, if any. Otherwise, null.
     * @param methodArgs is an array of arguments to be passed to the method to be run
     * @param debug indicates if debugging information is needed
     */
    public static void launch(
    		final URL[]    classpath,
    		final String   className,
    		final String   methodName,
    		final Object[] methodArgs,
    		final boolean debug) {
    	final Thread t = Thread.currentThread();
        final ClassLoader original = t.getContextClassLoader();
        try {
            // Configure a custom ClassLoader
            final PostDelegationClassLoader delegated = new PostDelegationClassLoader(classpath, original); // TODO: protocol handler
            delegated.setDebug(debug);
            t.setContextClassLoader(delegated);
            // Load class for a given class name
            final Class<?> klass = delegated.loadClass(className);
            if (debug) dumpClass(klass);
            // Obtain Method for a given method name, if any
    		final Method m = klass.getMethod(methodName, getTypes(methodArgs));
            // obtain an instance of LauncherDelegate
            m.invoke(null, methodArgs);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            t.setContextClassLoader(original);
        }
    }
    
    
	private static void dumpClass(Class<?> klass) {
		System.out.println("=======================================");
		System.out.println(String.format("=== %s ===", klass.getName()));
		System.out.println("=======================================");
		try {
			Constructor<?>[] clist = klass.getDeclaredConstructors();
			for (int i = 0; i < clist.length; i++) {
				Constructor<?> c = clist[i];
				System.out.println(c.toString());
			}
			Method mlist[] = klass.getDeclaredMethods();
			for (int i = 0; i < mlist.length; i++) {
				Method m = mlist[i];
				System.out.println(m.toString());
			}
		} catch (Throwable e) {
			System.err.println(e);
		}
		System.out.println("=======================================");
		System.out.println("=======================================");
		System.out.println("=======================================");
	}    
    
    
    public static void launchJVM(
    		final String classpath, 
    		final List<String> jvmArgs, 
    		final String jarfile, 
    		final String mainClass, 
    		final List<String> appargs) {

    	throw new UnsupportedOperationException("not implemented yet");
    }

    
    
    //
    // private methods
    //

    private void usage() {
        System.out.println(String.format("%s: %s", Constants.APP_NAME, Constants.APP_DESC));
        System.out.println("Options:");
        System.out.println("  -h                             : this help text");
        System.out.println("  -v                             : verbose");
        System.out.println("  -d                             : debug (also implies 'verbose')");
        System.out.println("  -M <local Maven2 repository>   : Defaults to ${user.home}/.m2/repository");
        System.out.println("  -R <remote Maven2 repository>  : Argument defaults to nothing, which means that remote repositories are disabled.");
        System.out.println("                                   If this option is not specified at all, a list of default remote repositories is assumed.");
        System.out.println("                                   See: http://www.mvnbrowser.com/repositories.html");
//TODO        System.out.println("  -S <eclipse-root>              : Defaults to 'target/classes'. Equivalent -M shadow:<eclipse-root>");
    }

    private void defineLocalRepository(final String localRepos, final String defaultLocalRepos) {
    	localRepository =  (localRepos!=null)
    		? localRepos
    		: (defaultLocalRepos!=null)
    			? defaultLocalRepos
    			: null;
    	if (verbose) System.out.println(String.format("localRepository = %s", localRepository));
    }

    private void defineRemoteRepositories(final String remoteRepos, final String defaultRemoteRepos) {
        if (remoteRepositories==null) {
            remoteRepositories = new ArrayList<String>();
        }
        if (remoteRepos != null && remoteRepos.trim().length()>0) {
        	remoteRepositories.add(remoteRepos);
        } else if (defaultRemoteRepos != null && defaultRemoteRepos.trim().length()>0) {
        	remoteRepositories.add(defaultRemoteRepos);
        }
    	if (verbose) System.out.println(String.format("remoteRepositories = %s", remoteRepositories));
    }


    private static Class<?>[] getTypes(final Object[] args) {
        final Class<?>[]  types;
        if (args==null) {
        	types = null;
        } else {
        	types = new Class[args.length];
    		for (int i=0; i<args.length; i++) {
    			types[i] = args[i].getClass();
    		}
        }
        return types;
    }


    //
    // ============
    // DEPENDENCIES
    // ============
    //

    private static final String[] dependencies = {
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
        // obtains jlauncher G:A:V from META-INF/MANIFEST.MF
        jlauncherArtifact()
    };


    private static final String jlauncherArtifact() {
        try {
            final Manifest manifest = new Manifest(Launcher.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
            return manifest.getMainAttributes().getValue("jlauncher-artifact");
        } catch (final Exception e) {
        	return "co.itrader:jlauncher:1.0-SNAPSHOT";
        }
    }
    
}
