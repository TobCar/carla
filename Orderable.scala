package carla

import scala.collection.mutable.Queue

//Modified by the keyword "after"
abstract class Orderable(orderableName: String) extends Configurable(orderableName) {
  val after = new Queue[String]()
  
  //Thread relationships
  var dependents = Set[Orderable]() 
  var dependsOn = Set[Orderable]()
  
  /**
   * Pre: dependentName is not empty
   * Post: This Orderable will be executed after another Orderable with the name dependentName.
   */
  def addAfter( dependentName: String ) {
    //Assert preconditions
    if( dependentName.isEmpty() ) {
      throw new IllegalArgumentException("dependentName cannot be empty")
    } else {
      println("Configured 'after' for "+name+" with name: "+dependentName)
      after.enqueue(dependentName)
    }
  }
  
  /**
   * Returning: Will getToken() succeed
   */
  def hasToken(): Boolean
  
  /**
   * Returning: The contents of this Orderable to be included in a thread.
   */
  def getToken(): String
}

object Orderable {  
  /**
   * Pre: first and second are not null
   * Post: The two objects can access each other
   */
  def connect( first: Orderable, second: Orderable ) {
    first.dependents += second
    second.dependsOn += first
  }
}