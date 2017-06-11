package carla.tests

import org.junit.Test
import junit.framework.TestCase
import org.junit.Assert._

import carla.{Configurable, NullArgumentException, Container}

class ConfigurableTests extends TestCase {
  
  var configurable: Configurable = null
  
  override def setUp() {
    configurable = new Container("this_is_my_name")
    assertEquals( configurable.name, "this_is_my_name" )
    assertNotEquals( configurable.name, "" )
    assertNotEquals( configurable.name, null )
  }
  
  def testAddUsingFunctionality() {
    var previousSize = configurable.usingName.size
    
    //Inserting    
    configurable.addUsing("name_of_using")
    assertTrue(configurable.usingName.size == previousSize+1)
    assertTrue(configurable.usingName.contains("name_of_using"))
    
    //Removing
    configurable.usingName -= "name_of_using"
    assertTrue(configurable.usingName.size == previousSize)
    assertFalse(configurable.usingName.contains("name_of_using"))
  }
  
  def testAddUsingExceptions() {
    try {
      configurable.addUsing("")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      configurable.addUsing(null)
    } catch {
      case _: NullArgumentException => //Expected
    }
  }
  
  def testAddPassingFunctionality() {
    var previousSize = configurable.passingName.size
    
    //Inserting
    configurable.addPassing("name_of_passing")
    assertTrue(configurable.passingName.size == previousSize+1)
    assertTrue(configurable.passingName.contains("name_of_passing"))
    
    //Removing
    configurable.passingName -= "name_of_passing"
    assertTrue(configurable.passingName.size == previousSize)
    assertFalse(configurable.passingName.contains("name_of_passing"))
  }
  
  def testAddPassingExceptions() {
    try {
      configurable.addPassing("")
      fail("Should have through exception")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      configurable.addPassing(null)
      fail("Should have through exception")
    } catch {
      case _: NullArgumentException => //Expected
    }
  }
}