package org.metadsl.extensions

class JavaExtension {

	/**
	 * Convert package names to directory paths.
	 * Example: 'foo.bar.baz' => 'foo/bar/baz'
	 */
	def packageToDir(pkg:String) : String = {
	    pkg.replaceAll("\\.","/")
	}
	
	/**
	 * Returns the relative Java file path given a package name and a class name.
	 * <p>
	 * Example: ('foo.bar.baz','Geography') => 'foo/bar/baz/Geography.java'
	 */
	def relativeFilePath(pkg:String, element:String) : String = {
	    packageToDir(pkg) + "/" + element + ".java"
	}
	
	/**
	 * Capitalizes a string
	 * <p>
	 * Example: 'countryName' => 'CountryName'
	 */
	def capitalize(s : String) = { s(0).toUpperCase + s.substring(1, s.length) }
	
	
	/**
	 * Returns a method's getter name given the name of a property.
	 * <p>
	 * Example: 'name' => 'getName' 
	 */
	def getterName(property:String) : String = {
	    "get" + capitalize(property)
	}
	
	
	/**
	 * Returns a method's setter name given the name of a property.
	 * <p>
	 * Example: 'name' => 'setName' 
	 */
	def setterName(property:String) : String = {
	    "set" + capitalize(property)
	}

}