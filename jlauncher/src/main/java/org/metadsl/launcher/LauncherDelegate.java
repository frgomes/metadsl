package org.metadsl.launcher;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.metadsl.resolvers.Bundle;
import org.metadsl.resolvers.BundleResolver;
import org.metadsl.resolvers.DependencyResolver;
import org.metadsl.resolvers.PlexusManager;
import org.metadsl.util.Settings;



public class LauncherDelegate implements Runnable {

	public final static void main(String[] args) {
		new LauncherDelegate(args).run();
	}

	
    //
    // private fields
    //

    private String[] args;

    private boolean debug = false;
    private boolean verbose = false;

    private String localRepository = null;

    // This variable is tree-state:
    //     null      :  means that defaults are assumed
    //     empty     : no remote repositories
    //     populated : list of remote repositories
    private List<String> remoteRepositories;

    private List<Bundle> bundles;
    private String mainClass;


    //
    // constructors
    //

    public LauncherDelegate(final String[] args) {
        this.args = args;
        this.remoteRepositories = new ArrayList<String>();
        this.bundles = new ArrayList<Bundle>();
    }

    // Relevant arguments for both bootstrap time and regular jlaundher time
    public static final char opt_help        = 'h';
    public static final char opt_verbose     = 'v';
    public static final char opt_debug       = 'd';

    // Relevant arguments during bootstrap time only
    public static final char opt_localRepo   = 'M';
    public static final char opt_remoteRepos = 'R';
//    public static final char opt_eclipseRepo = 'E';

    // Relevant arguments during regular jlauncher time only
    public static final char opt_bundle      = 'b';
    public static final char opt_mainClass   = 'm';

    // "-V" marks the start of JVM options
    public static final char opt_jvmArgs     = 'V';


    //
    // implements Runnable
    //

    @Override
    public void run() {

    	final String defaultLocalRepository = System.getProperty("user.home").concat("/.m2/repository");

        // *****************************************************************************************
        //
        // Parse command line options using a powerful parser
        // --------------------------------------------------
        //
    	// This step is intended to parse all command line options, as opposed to the first command
    	// line parsing performed at bootstrap time by MinimalistCmdParser.
    	//
        // *****************************************************************************************

        final LongOpt[] longopts = {
            new LongOpt("help",               LongOpt.NO_ARGUMENT,       null, opt_help,        "this help text"),
            new LongOpt("verbose",            LongOpt.NO_ARGUMENT,       null, opt_verbose,     "verbose"),
            new LongOpt("debug",              LongOpt.NO_ARGUMENT,       null, opt_debug,       "debug (implies verbose)"),
            // ---
            new LongOpt("localRepository",    LongOpt.OPTIONAL_ARGUMENT, null, opt_localRepo,   "local Maven directory"),
            new LongOpt("remoteRepositories", LongOpt.OPTIONAL_ARGUMENT, null, opt_remoteRepos, "remote Maven directories"),
            // ---
            new LongOpt("bundle",             LongOpt.REQUIRED_ARGUMENT, null, opt_bundle,      "bundle"),
            new LongOpt("mainClass",          LongOpt.REQUIRED_ARGUMENT, null, opt_mainClass,   "main class"),
            // ---
            new LongOpt("jvmArgs",            LongOpt.REQUIRED_ARGUMENT, null, opt_jvmArgs,     "JVM arguments"),
        };

        final List<String> appArgs   = new ArrayList<String>();
        List<String> jvmArgs = null;

        final String opts = "hvdMRb:m:V:" /* + "ES:" */ ;
        final Getopt cli = new Getopt(Constants.APP_NAME, args, opts, longopts);
        cli.setOpterr(false);

        int ch = cli.getopt();
        while (ch != -1) {
            switch (ch) {
            	case '?': break;
	            case LauncherDelegate.opt_jvmArgs:
	                jvmArgs = new ArrayList<String>();
	            	boolean jvmPhase = true;
	            	for (int index = cli.getOptind(); index<args.length; index++) {
	            		final String arg = args[index];
	            		if (debug) System.out.println(String.format("jvmArgs: |%s| |%s|", arg, cli.getOptarg()));
	            		if ("--".equals(arg)) {
	            			jvmPhase = false;
	            			continue;
	            		}
	            		if (jvmPhase) {
		            		jvmArgs.add("-".concat(arg));
	            		} else {
	                    	appArgs.add(arg);
	            		}
	            	}
	            	break;

                case LauncherDelegate.opt_help:        usage(longopts); System.exit(0);
                case LauncherDelegate.opt_verbose:     verbose = true; break;
                case LauncherDelegate.opt_debug:       debug = verbose = true; break;

                case LauncherDelegate.opt_localRepo:
                	defineLocalRepository(cli.getOptarg(), defaultLocalRepository);
                	break;
                case LauncherDelegate.opt_remoteRepos:
                	defineRemoteRepositories(cli.getOptarg(), "");
                	break;
//                case LauncherDelegate.opt_eclipseRepo:
//                	defineRemoteRepositories(cli.getOptarg(), "shadow:target/classes");
//                	break;

                case LauncherDelegate.opt_bundle:
                	bundles.addAll(Arrays.asList(Bundle.create(cli.getOptarg())));
                	break;
                case LauncherDelegate.opt_mainClass:
                	mainClass = cli.getOptarg();
                	break;

                default:
                    System.out.println(String.format("The option '%s' is not valid", ch));
                    usage(longopts);
                    System.exit(1);
            }
            ch = cli.getopt();
        }

        if (appArgs.isEmpty()) {
        	for (int index = cli.getOptind(); index<args.length; index++) {
        		final String arg = args[index];
        		if (debug) System.out.println(String.format("appArgs: |%s|", arg));
            	appArgs.add(arg);
        	}
        }

        // make sure local repository is defined
        if (localRepository==null || localRepository.trim().isEmpty()) {
        	defineLocalRepository(null, defaultLocalRepository);
        }

        // creating and initializing BundleResolver
        final PlexusManager plexus = PlexusManager.getInstance();
        final Settings settings = new Settings();
        settings.setPlexusContainer(plexus.getContainer());

        // resolve dependencies and run main class
        final DependencyResolver deps = new DependencyResolver(localRepository, remoteRepositories);
        final BundleResolver resolver = new BundleResolver(bundles, deps);
        if (mainClass==null || mainClass.trim().isEmpty()) {
            mainClass = resolver.mainClass();
        }

        if (verbose) {
        	System.out.println(String.format("bundles    = %s", bundles));
        	System.out.println(String.format("Main-Class = %s", mainClass));
        	System.out.println(String.format("jvmArgs    = %s", jvmArgs));
        	System.out.println(String.format("appArgs    = %s", appArgs));
        }

        if (jvmArgs==null) {
        	final URL[] packages = resolver.resolveAsURL();
            deps.shutdown();
            
            final String[] args = appArgs.toArray(new String[appArgs.size()]);
            Launcher.launch(packages, mainClass, "main", new Object[] { args }, debug);
        } else {
			final String classpath = resolver.classpath();
	        deps.shutdown();
	        StringBuilder sb = new StringBuilder();
	        if (File.pathSeparatorChar==':') {
	        	// Unix
	        	sb.append("java");
	        } else {
	        	// Windoze
	        	sb.append("javaw.exe");
	        }
	        for (String jvmArg : jvmArgs) {
	        	sb.append(' ').append(jvmArg);
	        }
	        sb.append(' ').append("-cp").append(' ').append(classpath);
	        sb.append(' ').append(mainClass);
	        for (String appArg : appArgs) {
	        	sb.append(' ').append(appArg);
	        }
	        String cmd = sb.toString();
	        if (verbose) System.out.println(cmd);
	        try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
        }
    }

    

    //
    // private methods
    //

    private void usage(final LongOpt[] longopts) {
        System.out.println(String.format("%s [options...] sources", Constants.APP_NAME));
        System.out.println(Constants.APP_DESC);
        System.out.println("where:");
        for (final LongOpt o : longopts) {
            System.out.println(String.format("    -%c, --%s    %s",
                    o.getVal(), o.getName(), o.getHelp()));
        }
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


    //
    // private inner classes
    //

    private class LongOpt extends gnu.getopt.LongOpt {
        private final String help;

        public LongOpt(final String name, final int has_arg, final StringBuffer flag, final int val, final String help) throws IllegalArgumentException {
            super(name, has_arg, flag, val);
            this.help = help;
        }

        public String getHelp() {
            return help;
        }
    }

}
