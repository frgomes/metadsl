package org.metadsl.plugins.pageflow

import java.io._
import java.net._
import java.util.UUID

import scala.collection._
import scala.util.parsing.combinator._

import org.slf4j.Logger

import org.metadsl.compiler._
import org.metadsl.extensions._
import org.metadsl.util._


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
    	( rulePackage ~ rulePageflows ) ^^ { case pkg ~ pageflows => new Model(pkg, pageflows) }

	private def rulePackage : Parser[Package] =
		( "package" ~ javaName ) ^^ { case "package" ~ name => new Package(name) }

	private def rulePageflows : Parser[List[Pageflow]] =
		( rulePageflow* ) ^^ { (pageflows : List[Pageflow]) => pageflows }

	private def rulePageflow : Parser[Pageflow] =
		( rulePageflow_1 | rulePageflow_2 )

	private def rulePageflow_1 : Parser[Pageflow] =
		( "pageflow" ~ ident ~ "{" ~ ruleStates ~ "}" ) ^^ {
			case "pageflow" ~ name ~ "{" ~ states ~ "}" =>
				Pageflow(name, Nil, states) }

	private def rulePageflow_2 : Parser[Pageflow] =
		( "pageflow" ~ ident ~ "{" ~ ruleActions ~ ruleStates ~ "}" ) ^^ {
			case "pageflow" ~ name ~ "{" ~ actions ~ states ~ "}" =>
				Pageflow(name, actions, states) }

	private def ruleActions : Parser[List[Action]] =
		( "actions" ~ ( ruleAction* ) ~ "end" ) ^^ { case "actions" ~ actions ~ "end" => actions }

    private def ruleAction : Parser[Action] =
    	( ident ~ stringLiteral ) ^^ { case name ~ literal => new Action(name, literal) }

	private def ruleStates : Parser[List[State]] =
		( ruleState* ) ^^ { (states : List[State]) => states }

	private def ruleState : Parser[State] =
		( "state" ~ ident ~ ruleTransitions ~ "end" ) ^^ { case "state" ~ name ~ transitions ~ "end" => new State(name, transitions) }

	private def ruleTransitions : Parser[List[Transition]] =
		( ruleTransition* ) ^^ { (transitions : List[Transition]) => transitions }

	private def ruleTransition : Parser[Transition] =
		( refAction ~ "=>" ~ target ) ^^ { case action ~ "=>" ~ target => new Transition(action, target) }

	private def target : Parser[Target] =
		reserved ^^ { case reserved => new Target(reserved) } | refState ^^ { case state => new Target(state) }



	private def refAction : Parser[Ref] =
		ident ^^ { case name => new Ref(name, classOf[Action].asInstanceOf[Class[Node]]) }

	private def refState : Parser[Ref] =
		ident ^^ { case name => new Ref(name, classOf[State].asInstanceOf[Class[Node]]) }



	private def reserved : Parser[Action] =
		( reservedHome | reservedBack | reservedReset )

    private def reservedHome  : Parser[Action] =
    	"HOME"  ^^ { case literal => new Action(literal, literal) }

    private def reservedBack  : Parser[Action] =
    	"BACK"  ^^ { case literal => new Action(literal, literal) }

    private def reservedReset : Parser[Action] =
    	"RESET" ^^ { case literal => new Action(literal, literal) }



    private def javaName: Parser[String] =
    	"""[a-zA-Z_]\w*(\.[a-zA-Z_]\w*)*""".r

}



private case class Model(pkg:Package, pageflows:List[Pageflow]) extends Node(UUID.randomUUID.toString)
private case class Package(override val name:String) extends Node(UUID.randomUUID.toString)
private case class Pageflow(override val name:String, actions:List[Action], states:List[State]) extends Node(name)
private case class Action(override val name:String, literal:String) extends Node(name)
private case class State(override val name:String, transitions:List[Transition]) extends Node(name)
private case class Transition(action:Ref, target:Target) extends Node(UUID.randomUUID.toString)
private case class Target(action:Action, ref:Ref) extends Node(UUID.randomUUID.toString) {
	def this(action:Action)  = { this(action, null) }
	def this(ref:Ref) = { this(null, ref) }
}


private class SyntaxTree extends AbstractSyntaxTree {

	def visit(node : Node) : Unit = {
		// println("%s%s ( %s )".format(indent, node.name, node.getClass.getName ))
		node match {
			case (Model(pkg:Package, pageflows:List[Pageflow])) =>
				store(node)
				visit(pkg)
				for (pageflow <- pageflows) visit(pageflow)
			case (Package(name:String)) =>
				store(name, node)
			case (Pageflow(name:String, actions:List[Action], states:List[State])) =>
				store(name, node)
				for (action <- actions) visit(action)
				for (state <- states) visit(state)
			case (Action(name:String, literal:String)) =>
				store(name, node)
			case (State(name:String, transitions:List[Transition])) =>
				store(name, node)
				for (transition <- transitions) visit(transition)
			case (Transition(ref:Ref, target:Target)) =>
				visit(ref)
				visit(target)
			case (Target(null, ref:Ref)) =>
				visit(ref)
			case (Target(state:State, null)) =>
				visit(state)
			case (Ref(name:String, klass:Class[Node])) =>
				store(name, node)
			case _ =>
				store(node.name, node)
		}
	}
}


private class CodeGenerator(model:Model, ast:SyntaxTree, outputDirectory:File, logger:Logger) extends JavaExtension {

	val pkg:String = model.pkg.name
	for (pageflow <- model.pageflows) { // Extensions.getSortedPageflows(model)
		val dir = new File(outputDirectory, packageToDir(Extensions.getPackageName(model)))
		dir.mkdirs
		val filnam = new File(outputDirectory, relativeFilePath(Extensions.getPackageName(model), pageflow.name))

		logger.info("file created: %s".format(filnam.getAbsoluteFile))
		val wr = new FileWriter(filnam)
		val pageflowName = Extensions.getPageflowName(pageflow)
		wr.write(Template("declare pageflow").format(Extensions.getPackageName(model), pageflowName, pageflowName))

		for (state <- pageflow.states) { // Extensions.getSortedStates(pageflow)
			wr.write(Template("enter state").format(Extensions.getStateName(state)))

			for (transition <- state.transitions ) {
				wr.write(Template("enter transition").format(Extensions.getActionName(transition), Extensions.getActionLiteral(transition))
				)
			}
			wr.write(Template("leave transition"))
		}
		wr.write(Template("leave state"))
		wr.close()
	}


	private object Extensions {

		def actionForTransition(node : Transition) : Action = {
			if (node.action.nodeRef == null) {
				node.action.nodeRef = ast.find(classOf[Action].asInstanceOf[Class[Node]], node.action.name)
											.getOrElse(throw new RuntimeException(
													"action not found: %s".format(node.action.name)))
			}
			node.action.nodeRef.asInstanceOf[Action]
		}

		def  targetForTransition(transition : Transition) : Target = {
		    transition.target
		}

		def  getPackageName(model : Model) : String = {
		    model.pkg.name
		}

		def  getPageflowName(pageflow : Pageflow) : String = {
		    pageflow.name
		}

		def  getStateName(state : State) : String = {
		    state.name
		}

		def getActionName(transition : Transition) : String = {
			val x = actionForTransition(transition)
			if (x==null) {
				throw new NullPointerException
			}
			actionForTransition(transition).name
		}

		def  getActionLiteral(transition : Transition) : String = {
		    actionForTransition(transition).literal
		}

		/**
		 * Returns a sorted list of Pageflow(s) in a Model
		 */
		def getSortedPageflows(model : Model) : List[Pageflow] = {
		    model.pageflows.sortBy( t => t.name.toLowerCase() )
		}

		/**
		 * Returns a sorted list of Action(s) in a Pageflow
		 */
		def getSortedActions(pageflow : Pageflow) : List[Action] = {
		    pageflow.actions.sortBy( t => t.name.toLowerCase )
		}

		/**
		 * Returns a sorted list of State(s) in a Pageflow
		 */
		def getSortedStates(pageflow : Pageflow) : List[State] = {
		    pageflow.states.sortBy( t => t.name.toLowerCase() )
		}
	}

}



private object Template {

	def apply(name : String) : String = map.get(name).get


	private val map = Map(
"declare pageflow" ->
"""package %s;

//
// *************************************************************************
// ***        THIS FILE IS AUTOMATICALLY GENERATED. DO NOT EDIT !        ***
// *************************************************************************
//

import java.util.Arrays;
import java.util.List;

import com.vaadin.navigator.State;
import com.vaadin.navigator.Transition;


/**
 * This class contains pageflow %s
 *
 * @generated
 */
public final class %s {

    public List<State> getStates() {
        return Arrays.asList(new State[] {""",

"enter state" ->
"""
	        new State(%s.class,
	            new Transition[] {""",

"enter transition" ->
"""
			        new Transition(%s, %s),""",

"leave transition" ->
"""
		        } ),""",

"leave state" ->
"""
            });
    }
}""")

}
