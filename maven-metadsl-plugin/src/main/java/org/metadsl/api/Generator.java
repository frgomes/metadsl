package org.metadsl.api;

import java.io.File;
import org.slf4j.Logger;

public interface Generator {
    public Logger getLogger();
    public void setLogger(Logger logger);

    public String getName();
    public void setName(String name);

    public File getBaseDir();
    public void setBaseDir(File baseDir);

    public File getOutputDir();
    public void setOutputDir(File outputDir);

    public void process(File source);
}
