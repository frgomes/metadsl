package org.metadsl.util

import java.io.File
import scala.reflect.BeanProperty

import org.slf4j.Logger

import org.metadsl.api.Generator


abstract class AbstractGenerator extends Generator {
    @BeanProperty var logger    : Logger = null
    @BeanProperty var name      : String = null
    @BeanProperty var baseDir   : File = null
    @BeanProperty var outputDir : File = null

    def process(source : File) : Unit
}
