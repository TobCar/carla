package carla.tests

import org.junit.Test
import junit.framework.TestCase
import org.junit.Assert._

import carla.{LexicalScanner, Compiler}

class CompilerTests extends TestCase {
  
  def testLoadAllBetweenBrackets() {
    val scanner = new LexicalScanner()
    scanner.readFile("[ something, collection.mutable.Set[ String] ] content not in brackets".iterator)
    val loaded = carla.Compiler.loadAllBetweenBrackets(scanner)
    assertEquals("[something,collection.mutable.Set[String]]", loaded)
  }
}