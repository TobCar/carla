package carla

import scala.collection.mutable.Queue

//Can contain other Configurable objects
class Container(containerName: String) extends Configurable(containerName) {  
  lazy val internalContainers = new Queue[Container]()
  lazy val steps = collection.mutable.Map[String, Step]()
  
  /**
   * Pre: internalContainer is not null
   * Post: internalContainers contains internalContainer
   * 			 hasInternalContainer() == true
   * 
   * Throws NoSuchElementException if hasInternalContainer() == false
   */
  def insert( internalContainer: Container ) {
    if( internalContainer == null ) {
      throw new NullArgumentException("internalContainer cannot be null")
    }
    
    internalContainers.enqueue(internalContainer)
    println("Inserted Container: " + internalContainer.name + " into " + name)
  }
  
  /**
   * Pre: step is not null
   * Post: steps contains step with the step's name as the key
   */
  def insert( step: Step ) {
    if( step == null ) {
      throw new NullArgumentException("step cannot be null")
    }
    
    steps.update(step.name, step)
    println("Inserted Step: " + step.name + " into " + name)
  }
  
  /**
   * Pre: The container inserted following a FIFO policy.
   * Post: internalContainers has one less element
   * Returns: A Container object or null if there are no more internal
   * 					containers.
   * 
   * Throws NoSuchElementException if hasInternalContainer() == false
   */
  def getInternalContainer(): Container = {
    try {
      println("Removed internal container from " + name)
      internalContainers.dequeue()
    } catch {
      case ex: NoSuchElementException => {
        throw ex
        println("There are no internal containers in Container '" + name + "'")
        null
      }
    }
  }
  
  def hasInternalContainer(): Boolean = {
    internalContainers.isEmpty == false
  }
  
  /**
   * Returns: True if this Container was created with Container.createSuperContainer()
   */
  def isSuperContainer: Boolean = {
    name == Container.superContainerKeyword
  }
}

object Container {
  private val superContainerKeyword = "___SUPERCONTAINER"
  
  /**
   * Returns: A newly created Container object intended to hold other Containers.
   */
  def createSuperContainer(): Container = {
    new Container(superContainerKeyword)
  }
}