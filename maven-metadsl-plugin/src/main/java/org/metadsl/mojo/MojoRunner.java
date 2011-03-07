package org.metadsl.mojo;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.maven.plugin.MojoExecutionException;
import org.metadsl.api.Generator;
import org.metadsl.resolvers.Bundle;
import org.metadsl.resolvers.BundleResolver;
import org.metadsl.resolvers.DependencyResolver;
import org.slf4j.Logger;


public class MojoRunner {
    
    final String name;
    final File basedir;
    final File modeldir;
    final File outputdir;
    final Logger logger;

    public MojoRunner(
                final String name,
                final File baseDirectory,
                final String modelDirectory,
                final String outputDirectory,
                final Logger logger) throws MojoExecutionException {
        this(name, baseDirectory, new File(modelDirectory), new File(outputDirectory), logger);
        if (!modeldir.isDirectory()) {
            throw new MojoExecutionException(String.format("cannot find directory: %s", modeldir.getPath()));
        }
    }

    public MojoRunner(
                final String name,
                final File baseDirectory,
                final File modelDirectory,
                final File outputDirectory,
                final Logger logger) {
        this.name = name;
        this.basedir = baseDirectory;
        this.modeldir = modelDirectory;
        this.outputdir = outputDirectory;
        this.logger = logger;
    }


    public void execute(
                    final Bundle[] bundles,
                    final File[] inputs,
                    final DependencyResolver deps,
                    final BundleResolver resolver) throws MojoExecutionException {
        for (final Bundle bundle : bundles) {
            logger.debug(String.format("Resolving dependencies for bundle %s", bundle.toString()));

            final URL[] urls = resolver.resolveAsURL(bundle.toString(), deps);
            final ClassLoader thisClassLoader = this.getClass().getClassLoader();
            final ClassLoader childClassLoader = new URLClassLoader(urls, thisClassLoader);
            try {
                final String artifactId = getName(bundle.getArtifactId());
                final String mainClass = "org.metadsl.plugins." + artifactId + ".Plugin";
                logger.debug(String.format("Loading %s from bundle %s", mainClass, bundle.toString()));
                final Class<?> klass = Class.forName(mainClass, true, childClassLoader); //FIXME: hardcoded class name :(
                final Generator g = (Generator) klass.newInstance();
                g.setName(name==null ? name : artifactId);
                g.setLogger(logger);
                g.setBaseDir(basedir);
                g.setOutputDir(getOutputDirectory(bundle));
                for (File input : inputs) {
                    logger.debug(String.format("Processing source %s", input.getAbsolutePath()));
                    g.process(input);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }


    //
    // private methods
    //
    
    private File getOutputDirectory(Bundle bundle) throws MojoExecutionException {
        final File result;
        if (outputdir==null) {
            String defaultPath = "target/generated/<BUNDLE>/".replaceAll("<BUNDLE>", bundle.getArtifactId());
            result = new File(basedir, defaultPath );
        } else {
            result = outputdir;
        }

        if (!result.mkdirs() && !result.isDirectory()) {
            throw new MojoExecutionException(String.format("cannot create directory: %s", result.getPath()));
        }
        return result;
    }

    private String getName(String artifactId) {
        String[] parts = artifactId.split("-");
        return parts[Math.max(0, Math.min(1, parts.length-1))];
    }

}
