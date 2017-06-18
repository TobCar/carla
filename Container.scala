package carla

import scala.collection.mutable.Queue

//Can contain other Configurable objects
class Container(containerName: String) extends Configurable(containerName) {  
  lazy val internalContainers = new Queue[Container]()
  lazy val orderables = collection.mutable.Map[String, Orderable]()
  lazy val importTokens = collection.mutable.Set[String]()
  
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
   * Pre: orderable is not null
   * Post: orderables contains orderable with the orderable's name as the key
   */
  def insert( orderable: Orderable ) {
    if( orderable == null ) {
      throw new NullArgumentException("orderable cannot be null")
    }
    
    orderables.update(orderable.name, orderable)
    println("Inserted Orderable: " + orderable.name + " into " + name)
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