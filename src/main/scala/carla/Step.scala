package carla

import scala.collection.mutable.Queue

class Step(name: String) extends Orderable(name) {
  val tokens = new Queue[String]()
  
  /**
   * Pre: token is not empty
   */
  def insert(token: String) {
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
  def hasToken(): Boolean =
    tokens.isEmpty == false
  
  /**
   * Returning: A token in this Container following a FIFO policy
   */
  def getToken(): String = {
    if( hasToken() == false )
      throw new IllegalStateException("hasToken() == false, cannot call getToken()")
    tokens.dequeue()
  }
  
  /**
   * Returns: True if this carla.Step will execute its code after all other Step objects.
   */
  def isLastStep(): Boolean =
    name == Step.lastKeyword
}

object Step {
  private val lastKeyword = "___LAST"
  
  /**
   * Returns: A newly created carla.Step object that executes its code after all other Step objects.
   */
  def createLastStep(): Step =
    new Step(lastKeyword)
}