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
    var previousSize = configurable.using.size
    
    //Inserting    
    configurable.addUsing("name_of_using", "SomeType")
    assertTrue(configurable.using.size == previousSize+1)
    assertTrue(configurable.using.get("name_of_using").get == "SomeType")
    
    //Removing
    configurable.using.remove("name_of_using")
    assertTrue(configurable.using.size == previousSize)
    assertFalse(configurable.using.contains("name_of_using"))
  }
  
  def testAddUsingExceptions() {
    try {
      configurable.addUsing("", "A")
      fail("Should have thrown an exception")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      configurable.addUsing(null, "A")
      fail("Should have thrown an exception")
    } catch {
      case _: NullArgumentException => //Expected
    }
    
    try {
      configurable.addUsing("A", "")
      fail("Should have thrown an exception")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      configurable.addUsing("A", null)
      fail("Should have thrown an exception")
    } catch {
      case _: NullArgumentException => //Expected
    }
  }
  
  def testAddPassingFunctionality() {
    var previousSize = configurable.passing.size
    
    //Inserting
    configurable.addPassing("name_of_passing", "SomeType")
    assertTrue(configurable.passing.size == previousSize+1)
    assertTrue(configurable.passing.get("name_of_passing").get == "SomeType")
    
    //Removing
    configurable.passing.remove("name_of_passing")
    assertTrue(configurable.passing.size == previousSize)
    assertFalse(configurable.passing.contains("name_of_passing"))
  }
  
  def testAddPassingExceptions() {
    try {
      configurable.addPassing("", "A")
      fail("Should have thrown an exception")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      configurable.addPassing(null, "A")
      fail("Should have thrown an exception")
    } catch {
      case _: NullArgumentException => //Expected
    }
    
    try {
      configurable.addPassing("A", "")
      fail("Should have thrown an exception")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      configurable.addPassing("A", null)
      fail("Should have thrown an exception")
    } catch {
      case _: NullArgumentException => //Expected
    }
  }
}