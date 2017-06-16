package carla.tests

import org.junit.Test
import junit.framework.TestCase
import org.junit.Assert._
import carla.including.OrderableRelationshipActor

import carla.{NullArgumentException, UnexpectedTokenException, LexicalScanner}

class OrderableRelationshipActorTests extends TestCase {
  
  def testPutAllPreconditions() {
    val actor = new OrderableRelationshipActor("actorName", collection.immutable.Map(),collection.immutable.Map(),collection.immutable.Map(),collection.immutable.Map(), null)
    try {
      actor.putAll(null, collection.mutable.Map())
      fail("Expected an IllegalArgumentException")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      actor.putAll(collection.mutable.Map(), null)
      fail("Expected an IllegalArgumentException")
    } catch {
      case _: IllegalArgumentException => //Expected
    }
    
    try {
      actor.putAll(collection.mutable.Map("duplicate_key"->"someValue"), collection.mutable.Map("duplicate_key"->"someOtherValue"))
      fail("Expected an IllegalStateException")
    } catch {
      case _: IllegalStateException => //Expected
    }
  }
  
  def testPutAllFunctionality() {
    val actor = new OrderableRelationshipActor("actorName", collection.immutable.Map(),collection.immutable.Map(),collection.immutable.Map(),collection.immutable.Map(), null)
    val map = collection.mutable.Map[String,Any]("key"->"value", "otherKey"->"otherValue")
    val addingTo = collection.mutable.Map[String,Any]()
    actor.putAll(map, addingTo)
    assertEquals(map.size, addingTo.size)
    assertTrue(addingTo.contains("key"))
    assertTrue(addingTo.contains("otherKey"))
  }
}