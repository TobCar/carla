package carla.tests

import org.junit.Test
import junit.framework.TestCase
import org.junit.Assert._

import carla.{NullArgumentException, UnexpectedTokenException, LexicalScanner}

class LexicalScannerTests extends TestCase {
  
  def testPairScanning() {
    var lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("(someName->someOtherName)".iterator)
    assertEquals(5, lexicalScanner.tokens.size)
  }
  
  def testSpacingSkipping() {
    var lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("\11\ntoken\11 token".iterator)
    assertEquals(2, lexicalScanner.tokens.size)
  }
  
  def testValidName() {
    var lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("name_name name-otherName name,otherName".iterator)
    assertEquals(7, lexicalScanner.tokens.size)
  }
  
  def testBlockCommentSkipping() {
    var lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("/* Abc 123 - + @ \n */ token".iterator)
    assertEquals(1, lexicalScanner.tokens.size)
  }
  
  def testLineCommentSkipping() {
    var lexicalScanner = new LexicalScanner()
    lexicalScanner.readFile("//A Comment\ntoken".iterator)
    assertEquals(1, lexicalScanner.tokens.size)
  }
}