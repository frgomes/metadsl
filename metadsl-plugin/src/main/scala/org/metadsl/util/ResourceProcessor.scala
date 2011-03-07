package org.metadsl.util

import java.io._
import java.net._


/**
 * This class offers a generic processor for local and remote resources.
 * <p>
 * Resources may be passed by name, as {@link File} or as {@link URL}.
 * Local resources may be both files or directories.
 * In case a directory is specified, it is scanned recursively until
 * all its files are processed.
 * <p>
 * TODO: Study the possibility of tweaking the thread's class loader instead.<br/>
 * http://www.javafaq.nu/java-example-code-895.html<br/>
 * http://java.sun.com/products/jndi/tutorial/beyond/misc/classloader.html
 *
 * @author Richard Gomes
 */
abstract class ResourceProcessor {

    protected def process(is : InputStream)

    /**
     * Process a resource by its name
     * <p>
     * A resource name may be its name in the file system of any URL.
     *
     * @param source
     */
    def process(source : String) : Unit = {
    	if (source==null || source.equals("-")) {
    		process(System.in)
    	} else {
    		var uri : URI = null
            try {
                uri = new URI(source);
            } catch {
            	case e : URISyntaxException => throw new RuntimeException(e)
            }
            if (uri.isAbsolute()) {
                var url : URL = null
                try {
                    url = uri.toURL()
                } catch {
                	case e : MalformedURLException => throw new RuntimeException(e)
                }
                process(url)
            } else {
            	process(new File(source))
            }
    	}
    }

    /**
     * Process a resource URL
     * <p>
     * This method simply processes the {@link InputStream} associated to such URL
     *
     * @param url
     */
    def process(url : URL) : Unit = {
        try {
        	process(url.openStream());
        } catch {
        	case e : IOException => throw new RuntimeException(e)
        }
    }

    /**
     * Process a resource File
     * <p>
     * Resources may be both files or directories.
     * In case a directory is specified, it is scanned recursively until
     * all its files are processed.
     *
     * @param file
     */
    //TODO: offer an option for filtering files
    def process(file : File) : Unit = {
        if (file.isDirectory()) {
            val files : Array[String] = file.list()
            for (entry <- files) {
            	process(new File(file, entry))
            }
        } else {
            try {
            	process(new FileInputStream(file))
            } catch {
            	case e : FileNotFoundException => throw new RuntimeException(e)
            }
        }
    }

}
