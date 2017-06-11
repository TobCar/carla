package carla

import scala.collection.mutable.Queue

//Modified by the word "after"
class Step(override val name: String) extends Configurable(name) {
  val after = new Queue[String]()
  val tokens = new Queue[String]()
  
  //Thread relationships
  var dependents = Set[Step]() 
  var dependsOn = Set[Step]()
  
  /**
   * Pre: dependentName is not empty
   * Post: This Step will be executed after another Step with the name dependentName.
   */
  def addAfter( dependentName: String ) {
    //Assert preconditions
    if( dependentName.isEmpty() ) {
      println("ERROR: dependentName cannot be empty")
    } else {
      println("Configured 'after' for "+name+" with name: "+dependentName)
      after.enqueue(dependentName)
    }
  }
  
  /**
   * Pre: token is not empty
   */
  def insert( token: String ) {
    if( token.isEmpty() ) {
      println("ERROR: Token cannot be empty")
    } else {
      tokens.enqueue(token)
      println("Inserted Token: "+token)
    }
  }
  
  /**
   * Returning: Will getToken() succeed
   */
  def hasToken(): Boolean = {
    tokens.isEmpty == false
  }
  
  /**
   * Returning: A token in this Container following a FIFO policy
   */
  def getToken(): String = {
    if( hasToken() == false )
      println("ERROR: There are no more tokens to get from step '"+name+"'")
    tokens.dequeue()
  }
  
  /**
   * Returns: True if this Step will execute its code after all other Step objects.
   */
  def isLastStep(): Boolean = {
    name == Step.lastKeyword
  }
}

object Step {
  private val lastKeyword = "___LAST"
  
  /**
   * Returns: A newly created Step object that executes its code after all other
   * 					Step objects.
   */
  def createLastStep(): Step = {
    new Step(lastKeyword)
  }
  
  /**
   * Pre: first and second are not null
   * Post: The two objects can access each other
   */
  def connect( first: Step, second: Step ) {
    first.dependents += second
    second.dependsOn += first
  }
}