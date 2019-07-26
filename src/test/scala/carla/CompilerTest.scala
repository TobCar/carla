package carla

import org.scalatest.FlatSpec

class CompilerTest extends FlatSpec {

  it should "load all content between brackets" in {
    val scanner = new LexicalScanner()
    scanner.readFile("[ something, collection.mutable.Set[ String] ] content not in brackets".iterator)
    val loaded = carla.Compiler.loadAllBetweenBrackets(scanner)
    assert(loaded == "[something,collection.mutable.Set[String]]")
  }
}