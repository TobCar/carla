package carla

import org.scalatest.FlatSpec

class ConfigurableTest extends FlatSpec {

  it should "create a Container" in {
    val configurable = new Container("this_is_my_name")
    assert(configurable.name == "this_is_my_name")
    assert(configurable.name != "")
    assert(configurable.name != null)
  }

  it should "add dependents" in {
    val configurable = new Container("this_is_my_name")
    val previousSize = configurable.using.size
    configurable.addUsing("name_of_using", "SomeType")
    assert(configurable.using.size == previousSize + 1)
    assert(configurable.using.get("name_of_using").get == "SomeType")
  }

  it should "remove dependents" in {
    val configurable = new Container("this_is_my_name")
    val previousSize = configurable.using.size
    configurable.using.remove("name_of_using")
    assert(configurable.using.size == previousSize)
    assert(!configurable.using.contains("name_of_using"))
  }

  it should "throw IllegalArgumentException when there is an empty dependentType when calling addUsing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[IllegalArgumentException] {
      configurable.addUsing("", "A")
    }
  }

  it should "throw IllegalArgumentException when there is an empty dependentName when calling addUsing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[IllegalArgumentException] {
      configurable.addUsing("A", "")
    }
  }

  it should "throw NullArgumentException when there is a null dependentName when calling addUsing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[NullArgumentException] {
      configurable.addUsing(null, "A")
    }
  }

  it should "throw NullArgumentException when there is a null dependentType when calling addUsing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[NullArgumentException] {
      configurable.addUsing("A", null)
    }
  }

  it should "add passing variable information" in {
    val configurable = new Container("this_is_my_name")
    val previousSize = configurable.passing.size
    configurable.addPassing("name_of_passing", "SomeType")
    assert(configurable.passing.size == previousSize + 1)
    assert(configurable.passing.get("name_of_passing").get == "SomeType")
  }

  it should "remove passing variable information" in {
    val configurable = new Container("this_is_my_name")
    val previousSize = configurable.passing.size
    configurable.passing.remove("name_of_passing")
    assert(configurable.passing.size == previousSize)
    assert(!configurable.passing.contains("name_of_passing"))
  }

  it should "throw IllegalArgumentException when there is an empty dependentName when calling addPassing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[IllegalArgumentException] {
      configurable.addPassing("", "A")
    }
  }

  it should "throw IllegalArgumentException when there is an empty dependentType when calling addPassing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[IllegalArgumentException] {
      configurable.addPassing("A", "")
    }
  }

  it should "throw NullArgumentException when there is a null dependentName to addPassing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[NullArgumentException] {
      configurable.addPassing(null, "A")
    }
  }

  it should "throw NullArgumentException when there is a null dependentType to addPassing" in {
    val configurable = new Container("this_is_my_name")
    assertThrows[NullArgumentException] {
      configurable.addPassing("A", null)
    }
  }
}