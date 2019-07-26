package carla

import org.scalatest.FlatSpec

class LexicalScannerTest extends FlatSpec {

  it should "extract tokens" in {
    var lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("com.some.package.Class anotherToken".iterator)
    assert(2 == lexicalScanner.tokens.size)
    
    lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("collection.mutable.Map[String, String] anotherToken".iterator)
    // Tokens should be "collection.mutable.Map" "[" "String" "," "String" "]" "anotherToken"
    assert(7 == lexicalScanner.tokens.size)
  }

  it should "stop reading when the end of the file is reached" in {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("import com.example.package\nimport @@@@ }}} step process {{{ process import".iterator)
    assert(4 == lexicalScanner.tokens.size)
  }

  it should "recognise pairs" in {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("(someName->someOtherName)".iterator)
    assert(5 == lexicalScanner.tokens.size)
  }

  it should "skip spacing" in {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("\t\ntoken\t token".iterator)
    assert(2 == lexicalScanner.tokens.size)
  }

  it should "test for valid names" in {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("name_name name-otherName name,otherName".iterator)
    assert(7 == lexicalScanner.tokens.size)
  }

  it should "skip block comments" in {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("/* Abc 123 - + @ \n */ token".iterator)
    assert(1 == lexicalScanner.tokens.size)
  }

  it should "skip line comments" in {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("//A Comment\ntoken".iterator)
    assert(1 == lexicalScanner.tokens.size)
  }
  
  it should "read all content in brackets" in {
    val lexicalScanner = new LexicalScanner()
    // In a real scenario, the last token saved was a "{" hence why it is excluded from the test string
    val content = lexicalScanner.readAllContentInBrackets("@@{{content}}@@\t\t\n}".iterator)
    assert(content == "@@{{content}}@@\t\t\n")
  }
}