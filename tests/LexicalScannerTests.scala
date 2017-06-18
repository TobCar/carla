package carla.tests

import org.junit.Test
import junit.framework.TestCase
import org.junit.Assert._

import carla.{NullArgumentException, UnexpectedTokenException, LexicalScanner}

class LexicalScannerTests extends TestCase {
  
  def testInlineImportScanning() {
    var lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("com.some.package.Class anotherToken".iterator)
    assertEquals(2, lexicalScanner.tokens.size)
    
    lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("collection.mutable.Map[String, String] anotherToken".iterator)
    //Tokens should be "collection.mutable.Map" "[" "String" "," "String" "]" "anotherToken"
    assertEquals(7, lexicalScanner.tokens.size)
  }
  
  def testReadUntilEndLine() {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("import com.example.package\nimport @@@@ }}} step process {{{ process import".iterator)
    assertEquals(4, lexicalScanner.tokens.size)
  }
  
  def testPairScanning() {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("(someName->someOtherName)".iterator)
    assertEquals(5, lexicalScanner.tokens.size)
  }
  
  def testSpacingSkipping() {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("\11\ntoken\11 token".iterator)
    assertEquals(2, lexicalScanner.tokens.size)
  }
  
  def testValidName() {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("name_name name-otherName name,otherName".iterator)
    assertEquals(7, lexicalScanner.tokens.size)
  }
  
  def testBlockCommentSkipping() {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("/* Abc 123 - + @ \n */ token".iterator)
    assertEquals(1, lexicalScanner.tokens.size)
  }
  
  def testLineCommentSkipping() {
    val lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("//A Comment\ntoken".iterator)
    assertEquals(1, lexicalScanner.tokens.size)
  }
  
  def testReadAllContentInBrackets() {
    val lexicalScanner = new LexicalScanner()
    //In a real scenario, the last token saved was a "{"
    val content = lexicalScanner.readAllContentInBrackets("@@{{content}}@@\11\11\n}".iterator)
    assertTrue(content == "@@{{content}}@@\11\11\n")
  }
}