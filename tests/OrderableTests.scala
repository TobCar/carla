package carla.tests

import org.junit.Test
import junit.framework.TestCase
import org.junit.Assert._

import carla.{Step, Orderable, LexicalScanner, Compiler, TypeMismatchException}

class OrderableTests extends TestCase {
  
  def testTypeMismatchException() {
    val first = new Step("firstName")
    val second = new Step("secondName")
    first.addPassing("variableName", "typeA")
    second.addUsing("variableName", "typeB")
    try {
      Orderable.connect(first, second)
      fail("Expected a TypeMismatchException")
    } catch {
      case _: TypeMismatchException => //Expected
    }
  }
}