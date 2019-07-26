package carla

import org.scalatest.FlatSpec

import carla.including.OrderableRelationshipActor

class OrderableRelationshipActorTests extends FlatSpec {

  it should "throw IllegalArgumentException when trying to put null preconditions" in {
    val actor = new OrderableRelationshipActor("actorName", collection.immutable.Map(), collection.immutable.Map(), collection.immutable.Map(), collection.immutable.Map(), null)
    assertThrows[IllegalArgumentException] {
      actor.putAll(null, collection.mutable.Map())
    }
  }

  it should "throw IllegalArgumentException when trying to put preconditions when the existing map is null" in {
    val actor = new OrderableRelationshipActor("actorName", collection.immutable.Map(), collection.immutable.Map(), collection.immutable.Map(), collection.immutable.Map(), null)
    assertThrows[IllegalArgumentException] {
      actor.putAll(collection.mutable.Map(), null)
    }
  }

  it should "throw IllegalStateException when trying to set an existing precondition" in {
    val actor = new OrderableRelationshipActor("actorName", collection.immutable.Map(), collection.immutable.Map(), collection.immutable.Map(), collection.immutable.Map(), null)
    assertThrows[IllegalStateException] {
      actor.putAll(collection.mutable.Map("duplicate_key" -> "someValue"), collection.mutable.Map("duplicate_key" -> "someOtherValue"))
    }
  }

  it should "put all preconditions onto the map" in {
    val actor = new OrderableRelationshipActor("actorName", collection.immutable.Map(),collection.immutable.Map(),collection.immutable.Map(),collection.immutable.Map(), null)
    val map = collection.mutable.Map[String,Any]("key"->"value", "otherKey"->"otherValue")
    val addingTo = collection.mutable.Map[String,Any]()
    actor.putAll(map, addingTo)
    assert(map.size == addingTo.size)
    assert(addingTo.contains("key"))
    assert(addingTo.contains("otherKey"))
  }
}