package org.metadsl.mojo;



import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.metadsl.resolvers.Bundle;
import org.metadsl.resolvers.BundleResolver;
import org.metadsl.resolvers.DependencyResolver;
import org.metadsl.util.LoggerAdapter4Plexus;
import org.metadsl.util.Settings;



/**
 * Goal which generates code given a certain DSL grammar
 *
 * @goal generate
 * @phase generate-sources
 */

public class Mojo extends AbstractMojo implements Contextualizable {

    //
    // fields below are injected by Plexus
    //
    

    /**
     * Base directory of the project.
     * @parameter expression="${basedir}"
     * @readonly
     * @required
     */
    private File basedir;

    /**
     * Default input directory for models. This is a convenience property which contains a
     * default location, allowing usage without any special configuration.
     *
     * @parameter expression="${modelDirectory}" default-value="src/main/resources/model/"
     */
    private File modelDirectory;

    /**
     * Location of output directory. The default is "target/generated/<BUNDLE>/"
     * where BUNDLE is the artifactId of the current MetaDSL plugin being executed.
     *
     *
     * @parameter expression="${outputDirectory}"
     */
    private File outputDirectory;

    /**
     * Location of model source files or directories to be retrieved for model files.
     * This property provides more flexibility than property <i>model.directory</i> which will be ignored
     * if this property is specified.
     *
     * @parameter expression="${inputs}"
     */
    private String[] inputs;

    /**
     * File extension employed as filter for input files.
     * By default, it's assumed that the second part of the artifactId works as such filter.
     * For example, if a plugin has artifactId "metadsl-pageflow-plugin" then input files which
     * match ".pageflow" extension are selected.
     * Notice that filters are only employed when inputs are retrieved from directories
     * making it necessary to discover which files would be eligible.
     *
     * @paremeter expression=${filter}
     */
    private String filter;

    /**
     * @parameter default-value="${bundles}"
     */
    private Bundle[] bundles;

    /**
     * @parameter default-value="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     */
    private List<ArtifactRepository> remoteRepositories;

    /**
     * Plexus container can be obtained via Contextualizable interface
     */
    @Override
    public void contextualize(Context context) throws ContextException {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
    private PlexusContainer container;


    //
    // public constants
    //
    
    public final static String METADSL     = "metaDSL";
    public final static String DESCRIPTION = "Runs parser generators and code generators";

    
    //
    // private fields
    //

    private boolean debug = false;


    //
    // public constructor
    //

    public Mojo() {
        // nothing
    }


    @Override
    public void execute() throws MojoExecutionException {
        // store container into a ThreadLocal
        Settings settings = new Settings();
        settings.setPlexusContainer(container);

        // resolve dependencies and run metaDSL plugins
        final DependencyResolver deps = new DependencyResolver(localRepository, remoteRepositories);
        final BundleResolver resolver = new BundleResolver();
        final File inputFiles[] = processInputs(inputs);
        MojoRunner runner = new MojoRunner(filter, basedir, modelDirectory, outputDirectory, new LoggerAdapter4Plexus(container.getLogger()));
        runner.execute(bundles, inputFiles, deps, resolver);
    }


    private File[] processInputs(String[] inputs) throws MojoExecutionException {
        if (inputs.length==0) {
            if (debug) System.out.println(String.format("input : %s", modelDirectory.getAbsolutePath()));
            return new File[] { modelDirectory };
        } else {
            List<File> result = new ArrayList<File>();
            for (int i=0; i<inputs.length; i++) {
                File input = new File(modelDirectory, inputs[i]);
                result.add(input);
                if (debug) System.out.println(String.format("input : %s", input.getAbsolutePath()));
            }
            return result.toArray(new File[result.size()]);
        }
    }

}
