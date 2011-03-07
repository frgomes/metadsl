package org.metadsl.compiler

import java.io.Writer
import java.lang.Class

import scala.collection._


abstract class AbstractSyntaxTree {

	private val map2D : mutable.Map[Class[Node], mutable.Map[String,Node]] = new mutable.HashMap

	def visit(node:Node) : Unit


	protected def store(node : Node) : Node = {
		store(node.name, node)
	}

	protected def store(name : String, node : Node) : Node = {
		val mapRef = map2D.getOrElseUpdate(node.getClass.asInstanceOf[Class[Node]], new mutable.HashMap )
		val allowdups = node.isInstanceOf[Ref]
		if (!allowdups && mapRef.contains(name)) {
			throw new RuntimeException("duplicated name: %s".format(name))
		}
		mapRef.getOrElseUpdate(name, node)
	}

	def dump(wr:Writer) : Unit = {
		for (map <- map2D) {
			val className = map._1
			// println(className)
			for (entry <- map._2) {
				wr.write("    " + entry._1.toString + entry._2.getClass.getName)
			}
		}
	}

	def find(klass:Class[Node], name:String) : Option[Node] = {
		val refs = map2D.get(klass)
		if (refs==None) {
			None
		} else {
			refs.get.get(name)
		}
	}


	def resolve() : Option[mutable.Map[String,Node]] = {
		val refs = map2D.get(classOf[Ref].asInstanceOf[Class[Node]])
		if (refs==None) {
			None
		} else {
			for (entry <- refs.get) {
				val name = entry._1
				val ref = entry._2.asInstanceOf[Ref]
				val map = map2D.get(ref.klass).getOrElse(
						throw new RuntimeException("class not found: %s".format(ref.klass.getName)))
				ref.nodeRef = map.get(name).getOrElse(
						throw new RuntimeException("name not found: %s (%s)".format(name, ref.klass.getName)))
				// println(name, ref.klass.getName, ref.nodeRef)
			}
			return refs
		}
	}
}
