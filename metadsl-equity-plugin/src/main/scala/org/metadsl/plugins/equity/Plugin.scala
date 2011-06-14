package org.metadsl.plugins.equity

import java.io._
import java.net._
import java.util.Date
import java.util.UUID

import scala.collection._
import scala.util.parsing.combinator._

import org.slf4j.{Logger,LoggerFactory}

import org.metadsl.compiler._
import org.metadsl.extensions._
import org.metadsl.util._


object Main extends Application {
   val name:String = "metadsl-equity-plugin"
   val dir:String  = "./src/main/resources/model/"

   val plugin = new Plugin
   plugin.setName(name)
   plugin.setLogger(LoggerFactory.getLogger(name))
   plugin.setBaseDir(new File("."))
   plugin.setOutputDir(new File(dir))

   plugin.process(new File(dir+"Example.equity"))
}



class Plugin extends AbstractGenerator {
    def process(source : File) = {
        val p = new PluginBody(outputDir, logger)
        p.process(source)
    }
}


private class PluginBody(outputDirectory:File, logger:Logger) extends ResourceProcessor {

	override def process(is:InputStream) : Unit = {
        val reader : Reader = new InputStreamReader(is)
		val grammar : Grammar = new Grammar
		logger.debug("Parsing...")
		grammar.parseAll(grammar.start, reader) match {
		    case grammar.Success(model, _) => {

//TODO: to be implemented
//		    	logger.debug("Building syntax tree...")
//		    	val symtab = new SyntaxTree
//		    	symtab.visit(model)
//		    	logger.debug("Resolving forward references...")
//		    	symtab.resolve
//		    	logger.debug("Generating code...")
//		    	new CodeGenerator(model, symtab, outputDirectory, logger)
//
		    	new CodeGenerator(model, /*symtab,*/ outputDirectory, logger)

		    }
		    case grammar.Failure(msg, _) => {
		    	logger.error(msg)
		    	throw new RuntimeException(msg)
		    }
		    case grammar.Error(msg, _) => {
		    	logger.error(msg)
		    	throw new RuntimeException(msg)
		    }
		}
	}
}


private class Grammar extends JavaTokenParsers {

    def run(text:String) : Unit = {
        println(parseAll(start, text))
    }

    def run(reader:Reader) : Unit = {
        println(parseAll(start, reader))
    }

    //
    // grammar
    //

    def start : Parser[Equity] =
        symbol ~ short_long ~ size ~ date ~ strike ~ call_put ~ price ^^ {
            case symbol ~ sl ~ size ~ date ~ strike ~ cp ~ price =>
                new Equity(symbol, sl, size, date, strike, cp, price)
    }

    def symbol : Parser[String] =
        ident

    def price : Parser[Double] =
        "@" ~> realNumber

    def date : Parser[Date] =
        year ~ "-" ~ month ~ "-" ~ day ^^ {
            case y ~ m ~ d => new Date(y.##, m.##, d.##) } //TODO: fix deprecated code

    def call_put : Parser[String] =
        ("call" | "put")

    def short_long : Parser[String] =
        ("short" | "long")

    def buy_sell : Parser[String] =
        ("buy" | "sell")

    def size : Parser[Int] =
        wholeNumber ^^ { case n => n.toInt }

    def strike : Parser[Double] =
        realNumber

    def year : Parser[Int] =
        wholeNumber ^^ { case n => n.toInt }

    def month : Parser[Int] =
        wholeNumber ^^ { case n => n.toInt }

    def day : Parser[Int] =
        wholeNumber ^^ { case n => n.toInt }

    def realNumber : Parser[Double] =
        decimalNumber ^^ { case d => d.toDouble }

}



private case class Equity(
          symbol:String,
          sl:String,
          size:Int,
          date:Date,
          strike:Double,
          cp:String,
          price:Double) extends Node(symbol)


private class CodeGenerator(model:Equity, /*ast:SyntaxTree,*/ outputDirectory:File, logger:Logger) extends JavaExtension {

    val dir = new File(outputDirectory, "equity")
    dir.mkdirs

    val filnam = new File(dir, "equity.txt");
    val wr = new FileWriter(filnam)
    wr.write(Template("dump").format(
        model.symbol,
        model.sl,
        model.size,
        model.date.toString,
        model.strike,
        model.cp,
        model.price))
    wr.close()
}


private object Template {

	def apply(name : String) : String = map.get(name).get


	private val map = Map(
"dump" ->
"""# Equity dump:
symbol=%s
sl:%s
size:%d
date:%s
strike:%f,
cp:%s,
price:%f
""")

}
