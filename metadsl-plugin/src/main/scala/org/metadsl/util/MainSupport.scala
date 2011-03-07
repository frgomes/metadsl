package org.metadsl.util

import java.io._
import java.net._


import scala.collection._

import gnu.getopt.Getopt
import gnu.getopt.LongOpt

//XXX import org.slf4j.{Logger,LoggerFactory}


import scala.reflect.BeanProperty


// THIS CLASS IS LEFT HERE FOR FUTURE PURPOSES

private abstract class MainSupport(val name: String, val args: Array[String]) { //TODO: implements Runnable

    // protected val logger : Logger = LoggerFactory.getLogger(classOf[MainSupport])

    private val SOURCE_NOT_FOUND        = "source file or directory not found: %s"
    private val MODEL_SOURCE_DEFINED    = "model source defined: %s"
    private val NO_MODEL_SOURCE_DEFINED = "no model source defined"
    private val CANNOT_FIND_DIRECTORY   = "cannot find directory: %s"
    private val CANNOT_CREATE_DIRECTORY = "cannot create directory: %s"

    // TODO: ideally, these constants should be shared with Java
    protected val DEFAULT_MODEL_DIR  = "src/main/resources/model/"
    protected val DEFAULT_OUTPUT_DIR = "target/generated/pageflow-maven-plugin/"

    protected val progName  = name

    // cmd line options
    protected var verbose	   : Boolean = false
    protected var baseDirectory   : String = null
    protected var modelDirectory  : String = null
    protected var outputDirectory : String = null
    protected var basedir    : File = null
    protected var modeldir   : File = null
    protected var outputdir  : File = null

    protected def process(sources : mutable.ArrayBuffer[File]) : Unit


    /*override*/ def run() : Unit = { //TODO: override

		// TODO: long options
		//		val sb : StringBuffer = new StringBuffer
		//    	val longopts : Array[LongOpt] = Array(
		//        		new LongOpt("baseDirectory",     LongOpt.REQUIRED_ARGUMENT, sb, 'b'),
		//        		new LongOpt("modelDirectory",    LongOpt.REQUIRED_ARGUMENT, sb, 'm'),
		//        		new LongOpt("outputDirectory",   LongOpt.REQUIRED_ARGUMENT, sb, 'o'),
		//        		new LongOpt("verbose",  LongOpt.NO_ARGUMENT, null, 'v'),
		//        		new LongOpt("help",     LongOpt.NO_ARGUMENT, null, 'h') )

        val g : Getopt = new Getopt(progName, args, "b:m:o:vh", null /*longopts*/)
        g.setOpterr(true)

        var c : Int = g.getopt
        while (c != -1) {
        	c match {
        		case 'b' => baseDirectory   = g.getOptarg
        		case 'm' => modelDirectory  = g.getOptarg
        		case 'o' => outputDirectory = g.getOptarg
			    case 'v' => verbose = true
			    case 'h' => usage(progName)
			    			return 0
			    case _   => println("The option '" + c + "' is not valid")
			    			usage(progName)
			    			return 1
        	}
        	c = g.getopt
        }

    	// define basedir
        basedir = if (baseDirectory==null) {
        	new File(".")
        } else {
        	new File(baseDirectory)
        }
        if (verbose) println("basedir=%s".format(basedir.getAbsolutePath))

        if (modelDirectory==null) modelDirectory = DEFAULT_MODEL_DIR
    	modeldir  = new File(basedir, modelDirectory)
        if (verbose) println("modeldir=%s".format(modeldir.getAbsolutePath))

        if (outputDirectory==null) outputDirectory = DEFAULT_OUTPUT_DIR
    	outputdir = new File(basedir, outputDirectory)
        if (verbose) println("outputdir=%s".format(outputdir.getAbsolutePath))

    	// validate models directory
    	if (!modeldir.isDirectory()) {
            throw new RuntimeException(String.format(CANNOT_FIND_DIRECTORY, outputdir.getPath()));
        }
    	// validate output directory
    	if (!outputdir.mkdirs() && !outputdir.isDirectory()) {
            throw new RuntimeException(String.format(CANNOT_CREATE_DIRECTORY, outputdir.getPath()));
        }

    	// define list of sources
    	var sources    = new mutable.ArrayBuffer[File]
    	if (g.getOptind() >= args.size) {
    		sources += modeldir
    		if (verbose) println(MODEL_SOURCE_DEFINED.format(modeldir.getAbsoluteFile()))
    	} else {
	        for (i <- g.getOptind() until args.size) {
            	val f : File = new File(modeldir, args(i));
            	if (f.isDirectory() || f.isFile()) {
                	sources += f
                	if (verbose) { println(String.format(MODEL_SOURCE_DEFINED, f.getAbsoluteFile())) }
            	} else {
            		if (verbose) { println(String.format(SOURCE_NOT_FOUND, f.getAbsoluteFile())) }
            	}
	       		if (sources.size == 0) {
	                throw new RuntimeException(NO_MODEL_SOURCE_DEFINED);
	       		}
	        }
    	}

        process(sources)

        return 0
    }


    private def usage(appName : String) = {
        println("%s [-h] [-v] -o output-directory sources".format(appName))
        println("Where:")
        println("  sources         optional list of input files and/or directories")
        println("Options:")
        println("  -b, --baseDirectory      baseDirectory")
        println("  -m, --modelDirectory     model directory")
        println("  -o, --outputDirectory    output directory")
        println("  -h, --help      this help text")
        println("  -v, --verbose   verbose mode")
	}

}
