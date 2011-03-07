package org.metadsl.compiler


case class Ref(override val name:String, klass:Class[Node]) extends Node(name) {
	var nodeRef : Node = null
}
