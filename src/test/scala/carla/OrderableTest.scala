package carla

import org.scalatest.FlatSpec

class OrderableTest extends FlatSpec {

  it should "throw TypeMismatchException when expecting different types for the same dependent variable" in {
    val first = new Step("firstName")
    val second = new Step("secondName")
    first.addPassing("variableName", "typeA")
    second.addUsing("variableName", "typeB")
    assertThrows[TypeMismatchException] {
      Orderable.connect(first, second)
    }
  }
}