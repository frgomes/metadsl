package org.metadsl.bootstrap;

/**
 * This class is intended to parser command line options during the very
 * early stages of application bootstrap process. This parser is neither intended
 * to be complete nor support all possible options the application supports.
 * It's only intended to parse a very minimum set of command line options which
 * are necessary to feed the first stage of the bootstrap process itself.
 * <p>
 * After the initial bootstrap, the application will be able to employ a more
 * advanced command line interpreter and parse again all command line arguments.
 * <p>
 * Supported options are:
 * <li>-h :: help</li>
 * <li>-v :: verbose</li>
 * <li>-d :: debug (also implies 'verbose')</li>
 * <li>-M &lt;local Maven2 repository&gt; :: Defaults to ${user.home}/.m2/repository</li>
 * <li>-R &lt;remote Maven2 repository&gt; :: Argument defaults to nothing, which means that remote repositories are disabled.
 * <li>-S &lt;eclipse-root&gt; :: Defaults to <code>target/classes</code>. Equivalent -M shadow:<&lt;eclipse-root&gt;</li>
 * If this option is not specified at all, a list of default remote repositories is assumed.
 * See: <a href="http://www.mvnbrowser.com/repositories.html">Public Maven2 repositories</a></li>
 * <p>
 * You must specify only one option at a time, eventually followed by an argument, if required or
 * supported by such option. Several no-argument options are not supported, like for instance
 * <code>-dv<</code>; you should specify <code>-d -v<</code> instead.
 * <p>
 * A special option <code>"--"</code> is intended to separate arguments consumed by the launcher from
 * arguments intended to be passed to the launched application.
 * <p>
 */
public class MinimalistCmdParser {
    private final String[] args;

    private int opt = 0;
    private String optarg = null;
    private boolean opterr = false;
    private boolean debug = false;


    //
    // public constructors
    //

    public MinimalistCmdParser(final String[] args) {
        this.args = args;
    }

    //
    // public methods
    //

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public int getopt() {
        optarg = null; // no arguments
        for (;;) {
            if (opt >= args.length) return -1;
            String arg = args[opt++];
            if (arg.charAt(0) == '-') {
                final char ch = parse(arg);
                if (opterr) {
                    System.out.println(String.format("MinimalistCmdParser.ch : ERROR: %c", ch));
                    return ch;
                }
                if ((ch!='?' && ch!='-')) {
                    if (debug) System.out.println(String.format("MinimalistCmdParser.ch : %c", ch));
                    return ch;
                }
            }
            if (debug) System.out.println(String.format("MinimalistCmdParser : DISCARDED: %s", arg));
        }
    }

    private char parse(final String arg) {
        final char ch = arg.charAt(1);
        if (ch=='h' || ch=='v' || ch=='d') return ch;
        if (ch=='M') return mavenRepo('M', System.getProperty("user.home").concat("/.m2/repository"));
        if (ch=='R') return mavenRepo('R', "");
//      if (ch=='E') return mavenRepo('M', "shadow:target/classes");
        if (ch=='-') return ch;
        return '?';
    }

    public String getOptarg() {
        if (debug) System.out.println(String.format("MinimalistCmdParser.getOptarg : %s", optarg));
        return optarg;
    }

    public void setOpterr(boolean opterr) {
        this.opterr = opterr;
    }


    //
    // private methods
    //

    private char mavenRepo(final char ch, final String optarg) {
        if ((opt < args.length) && (args[opt].charAt(0) != '-')) {
            this.optarg = args[opt];
            opt++;
        } else {
            this.optarg = optarg;
        }
        return ch;
    }

}
