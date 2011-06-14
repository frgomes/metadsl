package org.metadsl.plugins.json

import java.io._
import java.net._
import java.util.UUID

import scala.collection._
import scala.util.parsing.combinator._

import org.slf4j.{LoggerFactory,Logger}

import org.metadsl.compiler._
import org.metadsl.extensions._
import org.metadsl.util._


object Main extends Application {
   val name:String = "metadsl-pageflow-plugin"
   val dir:String  = "./src/main/resources/model/"

   val plugin = new Plugin
   plugin.setName(name)
   plugin.setLogger(LoggerFactory.getLogger(name))
   plugin.setBaseDir(new File("."))
   plugin.setOutputDir(new File(dir))

   plugin.process(new File(dir+"Example.pageflow"))
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
		    	logger.debug("Building syntax tree...")
		    	val symtab = new SyntaxTree
		    	symtab.visit(model)
		    	logger.debug("Resolving forward references...")
		    	symtab.resolve
		    	logger.debug("Generating code...")
		    	new CodeGenerator(model, symtab, outputDirectory, logger)
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

    def start : Parser[Model] =
        ( ruleEntries ) ^^ { entries => new Model(entries) }

    private def ruleEntries : Parser[List[Entry]] =
        ( ruleEntry ~ "," ~ ruleEntries ) ^^ { case entry ~ "," ~ list => new ::[Entry](entry, list) } |
        ( ruleEntry                     ) ^^ { case entry              => new ::[Entry](entry, null) }

    private def ruleEntry : Parser[Entry] =
        ( comments  ) ^^ { case text:String => new Entry(text, null, null) } |
        ( ruleGroup ) ^^ { case group:Group => new Entry(null, group, null)    } |
        ( ruleBlock ) ^^ { case block:Block => new Entry(null, null, block)    }

    private def ruleGroup : Parser[Group] =
        ( "[" ~ stringLiteral ~ "]" ) ^^ { case "[" ~ text ~ "]" => new Group(text) }

    private def ruleBlock : Parser[Block] =
        ( ruleNamedBlock   ) ^^ { case namedBlock   => new Block(namedBlock, null, null) } |
        ( ruleUnnamedBlock ) ^^ { case unnamedBlock => new Block(null, unnamedBlock, null) } |
        ( rulePair         ) ^^ { case pair         => new Block(null, null, pair)       }

    private def ruleNamedBlock : Parser[NamedBlock] =
        ( ident ~ "{" ~ rulePairs    ~ "}" ) ^^ { case name ~ "{" ~ pairs    ~ "}" => new NamedBlock(name, pairs, null)    } |
        ( ident ~ "{" ~ ruleLiterals ~ "}" ) ^^ { case name ~ "{" ~ literals ~ "}" => new NamedBlock(name, null, literals) }

    private def ruleUnnamedBlock : Parser[UnnamedBlock] =
        ( "{" ~ rulePairs    ~ "}" ) ^^ { case "{" ~ pairs    ~ "}" => new UnnamedBlock(pairs, null)    } |
        ( "{" ~ ruleLiterals ~ "}" ) ^^ { case "{" ~ literals ~ "}" => new UnnamedBlock(null, literals) }

    private def rulePairs : Parser[List[Pair]] =
        ( rulePair ~ "," ~ rulePairs ) ^^ { case pair ~ "," ~ list => new ::[Pair](pair, list) } |
        ( rulePair                   ) ^^ { case pair              => new ::[Pair](pair, null) }

    private def rulePair : Parser[Pair] =
        ( identifier ~ stringLiteral ) ^^ { case key ~ value => new Pair(key, value) }

    private def ruleLiterals : Parser[List[String]] =
        ( ruleLiteral ~ "," ~ ruleLiterals ) ^^ { case text ~ "," ~ list => new ::[String](text, list) } |
        ( ruleLiteral                      ) ^^ { case text              => new ::[String](text, null) }

    private def ruleLiteral : Parser[String] =
        ( stringLiteral ) ^^ { text : String => text }

    val comments   : Parser[String] = """(\s|;.*)+""".r
    val identifier : Parser[String] = """[a-zA-Z_-]\w*""".r
}


private class Pair(val key:String, val value:String)

private case class Model(entries:List[Entry]) extends Node(UUID.randomUUID.toString)
private case class Entry(val comment:String, val group:Group, val block:Block) extends Node(UUID.randomUUID.toString)
private case class Group(group:String) extends Node(UUID.randomUUID.toString)
private case class Block(val namedBlock:NamedBlock, val unnamedBlock:UnnamedBlock, val pair:Pair) extends Node(UUID.randomUUID.toString)
private case class NamedBlock(override val name:String, val pairs:List[Pair], val literals:List[String]) extends Node(name)
private case class UnnamedBlock(val pairs:List[Pair], val literals:List[String]) extends Node(UUID.randomUUID.toString)


private class SyntaxTree extends AbstractSyntaxTree {

	def visit(node : Node) : Unit = {
        visit(0, node)
    }

	def visit(indent:Int, node : Node) : Unit = {

		// println("%%%ds ( %s )".format(indent*2, node.name, node.getClass.getName ))

		node match {
			case (Model(entries:List[Entry])) =>
				store(node)
				for (entry <- entries) visit(indent+1, entry)
			case (Entry(comment:String, group:Group, block:Block)) =>
				store(node)
                if (group != null) {
                    //TODO: should inject date/time from Group into the Block
                } else if (block != null) {
                    visit(indent+1, block)
                }
            case (Block(namedBlock:NamedBlock, unnamedBlock:UnnamedBlock, pair:Pair)) =>
                store(node)
                if (namedBlock != null) {
                    visit(indent+1, namedBlock)
                } else if (unnamedBlock != null) {
                    visit(indent+1, unnamedBlock)
                } else {
                    // do not visit Pair
                }
            case (NamedBlock(name:String, pairs:List[Pair], literals:List[String])) =>
                store(name, node)
                // do not visit neither List[Pair] nor List[String]
            case (UnnamedBlock(pairs:List[Pair], literals:List[String])) =>
                store(node)
                // do not visit neither List[Pair] nor List[String]
			case _ =>
				store(node.name, node)
		}
	}
}


private class CodeGenerator(model:Model, ast:SyntaxTree, outputDirectory:File, logger:Logger) extends JavaExtension {

    val dir = new File(outputDirectory.toString)
	dir.mkdirs
    val filnam = new File(outputDirectory, "t.txt") // TODO
    val wr = new FileWriter(filnam)
    visit(model)


    private def visit(node : Node) : Unit = {
		node match {
			case (Model(entries:List[Entry])) =>
				for (entry <- entries) visit(entry)
			case (Entry(comment:String, group:Group, block:Block)) =>
                if (group != null) {
                    //TODO: should inject date/time from Group into the Block
                } else if (block != null) {
                    visit(block)
                }
            case (Block(namedBlock:NamedBlock, unnamedBlock:UnnamedBlock, pair:Pair)) =>
                if (namedBlock != null) {
                    visit(namedBlock)
                } else if (unnamedBlock != null) {
                    visit(unnamedBlock)
                } else {
                    visitPair(pair)
                }
            case (NamedBlock(name:String, pairs:List[Pair], literals:List[String])) =>
                    if (pairs != null) {
                        wr.write("\"%s\":{".format("name"));
                        visitPairs(pairs);
                        wr.write("}");
                    } else if (literals != null) {
                        wr.write("\"%s\":[".format("name"));
                        visitLiterals(literals);
                        wr.write("]");
                    }
            case (UnnamedBlock(pairs:List[Pair], literals:List[String])) =>
                    if (pairs != null) {
                        wr.write("{");
                        visitPairs(pairs);
                        wr.write("}");
                    } else if (literals != null) {
                        wr.write("[");
                        visitLiterals(literals);
                        wr.write("]");
                    }
			case _ =>
				// do nothing
		}
    }

    private def visitPair(pair:Pair) : Unit = {
        wr.write("\""+pair.key+"\":\""+pair.value+"\"");
    }

    private def visitPairs(pairs:List[Pair]) : Unit = {
        var count = 0
        for (pair <- pairs) {
          if (count==0) wr.write(",")
          visitPair(pair)
          count += 1
        }
    }

    private def visitLiteral(literal:String) : Unit = {
        wr.write("\""+literal+"\"");
    }

    private def visitLiterals(literals:List[String]) : Unit = {
        var count = 0
        for (literal <- literals) {
          if (count==0) wr.write(",")
          visitLiteral(literal)
          count += 1
        }
    }

}
