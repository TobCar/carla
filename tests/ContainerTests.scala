package carla.tests

import org.junit.Test
import junit.framework.TestCase
import org.junit.Assert._

import carla.{NullArgumentException, Container, Step}

class ContainerTests extends TestCase {
  
  var container: Container = null
  
  override def setUp() {
    container = new Container("this_is_my_name")
    assertEquals( container.name, "this_is_my_name" )
    assertNotEquals( container.name, "" )
    assertNotEquals( container.name, null )
    assertFalse(container.isSuperContainer)
    assertFalse(container.hasInternalContainer)
  }
  
  def testInsertContainerFunctionality() {
    var previousSize = container.internalContainers.size
    
    //Inserting
    assertFalse(container.hasInternalContainer)
    val internal = new Container("internal")
    container.insert(internal)
    assertTrue(container.hasInternalContainer)
    assertTrue(container.internalContainers.size == previousSize+1)
    assertTrue(container.internalContainers.contains(internal))

    //Removing
    assertEquals(internal, container.getInternalContainer())
    assertFalse(container.hasInternalContainer)
    assertTrue(container.internalContainers.size == previousSize)
    assertFalse(container.internalContainers.contains(internal))
  }
  
  def testInsertContainerFIFO() {
    val internal1 = new Container("internal1")
    val internal2 = new Container("internal2")
    val internal3 = new Container("internal3")
    container.insert(internal1)
    container.insert(internal2)
    container.insert(internal3)
    assertEquals(internal1, container.getInternalContainer())
    assertEquals(internal2, container.getInternalContainer())
    assertEquals(internal3, container.getInternalContainer())
  }
  
  def testInsertContainerExceptions() {
    try {
      container.insert(null: Container)
      fail("Should have through exception")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      container.getInternalContainer()
      fail("Should have through exception")
    } catch {
      case _: NoSuchElementException => //Expected
    }
  }
  
  def testInsertStepFunctionality() {
    var previousSize = container.steps.size
    val step = new Step("internal")
    container.insert(step)
    assertEquals(step, container.steps.get(step.name).get)
  }
  
  def testInsertStepExceptions() {
    try {
      container.insert(null: Step)
      fail("Should have through exception")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
  }
  
  def testCreateSuperContainer() {
    container = Container.createSuperContainer()
    assertTrue( container.isSuperContainer )
  }
}