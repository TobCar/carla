package carla

import org.scalatest.FlatSpec

class ContainerTest extends FlatSpec {
  
  it should "create a container" in {
    val container = new Container("this_is_my_name")
    assert(container.name == "this_is_my_name")
    assert(container.name != "")
    assert(container.name != null)
    assert(!container.isSuperContainer)
    assert(!container.hasInternalContainer)
  }

  it should "insert container into container" in {
    val container = new Container("this_is_my_name")
    val previousSize = container.internalContainers.size

    assert(!container.hasInternalContainer)
    val internal = new Container("internal")
    container.insert(internal)
    assert(container.hasInternalContainer)
    assert(container.internalContainers.size == previousSize + 1)
    assert(container.internalContainers.contains(internal))
  }

  it should "remove container from container" in {
    val container = new Container("this_is_my_name")
    val previousSize = container.internalContainers.size
    val internal = new Container("internal")
    container.insert(internal)

    assert(internal == container.getInternalContainer())
    assert(!container.hasInternalContainer)
    assert(container.internalContainers.size == previousSize)
    assert(!container.internalContainers.contains(internal))
  }

  it should "insert containers in a first in first out manner" in {
    val container = new Container("this_is_my_name")
    val internal1 = new Container("internal1")
    val internal2 = new Container("internal2")
    val internal3 = new Container("internal3")
    container.insert(internal1)
    container.insert(internal2)
    container.insert(internal3)
    assert(internal1 == container.getInternalContainer())
    assert(internal2 == container.getInternalContainer())
    assert(internal3 == container.getInternalContainer())
  }

  it should "throw IllegalArgumentException when trying to insert a null object as a container" in {
    val container = new Container("this_is_my_name")
    assertThrows[IllegalArgumentException] {
      container.insert(null: Container)
    }
  }

  it should "throw NoSuchElementException when calling getInternalContainer on an empty container" in {
    val container = new Container("this_is_my_name")
    assertThrows[NoSuchElementException] {
      container.getInternalContainer()
    }
  }
  
  it should "insert step" in {
    val container = new Container("this_is_my_name")
    val previousSize = container.orderables.size
    val step = new Step("internal")
    container.insert(step)
    assert(step == container.orderables.get(step.name).get)
  }

  it should "throw IllegalArgumentException when trying to insert a null object as an Orderable" in {
    val container = new Container("this_is_my_name")
    assertThrows[IllegalArgumentException] {
      container.insert(null: Orderable)
    }
  }

  "Container class" should "create a super container" in {
    val container = Container.createSuperContainer()
    assert(container.isSuperContainer)
  }
}