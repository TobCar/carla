/* 
 * This file has to be minified and its contents included in OrderableRelationshipActor.txt
 * in order for it to be included in compiled code.
 * 
 * The package will be overriden so it is not necessary to include that line.
 */

package carla.including

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Handles the relationships between threads. 
 * Specifically: what thread needs to be executed after
 * what thread and what parameters need to be passed between them.
 */
class OrderableRelationshipActor( 
    processName: String,
    processInputs: collection.immutable.Map[String, Any],
    dependsOn: collection.immutable.Map[String, Set[String]], 
    dependents: collection.immutable.Map[String, Set[String]],
    processOutputKeys: collection.immutable.Map[String, String],
    parentActor: OrderableRelationshipActor) {
  
  val runnables = collection.mutable.Map[String, OrderableRunnable]()
  val runnableOutputs = collection.mutable.Map[String, Map[String, Any]]()
  val processOutputs = collection.mutable.Map[String, Any]()
  
  private val isDone = collection.mutable.Map[String, Boolean]()
  private val toFinishQueue = new ConcurrentLinkedQueue[String]()
  
  // Actor thread
  var orderablesDone = 0
  (new Thread {
    override def run {
      while(true) {
        if( toFinishQueue.size != 0 ) {
          val orderableName = toFinishQueue.poll() 
          finish(orderableName)
          
          if( orderablesDone == isDone.size ) {
            if( parentActor != null ) {
              // Different processes require different key names
              val mappedOutput = collection.mutable.Map[String, Any]()
              for( (key, value) <- processOutputs ) {
                mappedOutput.put(processOutputKeys.get(key).get, value)
              }
              
              val immutableOutput: collection.immutable.Map[String, Any] = collection.immutable.Map() ++ mappedOutput
              parentActor.addToFinishQueue(processName, immutableOutput)
            }

            println("Stopping Relationship Actor '"+processName+"'")
            return
          }
        }
      }
    }
  }).start
  
  /**
   * Add an Orderable for other Orderables to have a relationship with.
   */
  def addOrderable( name:String, runnable: OrderableRunnable ) {
    runnables.put(name,runnable)
    isDone.update(name, false)
    println("Added Orderable: "+name)
  }

  
  /**
   * Assign a task to this actor.
   */
  def addToFinishQueue( orderableName: String, outputs: collection.immutable.Map[String, Any] ) {
    runnableOutputs.put(orderableName, outputs)
    toFinishQueue.add(orderableName)
  }
  
  /**
   * Post: isDone.get(orderableName) == true and any dependents of that Orderable
   * 			 have started to be run if all of that dependent's dependencies
   * 			 have all finished running.
   */
  private def finish( orderableName: String ) {
    isDone.update(orderableName, true)
    
    val dependentsSet = dependents.getOrElse(orderableName, Set())
    
    if( dependentsSet.isEmpty ) {
      // This Orderable doesn't lead to any others, pass its output to the process itself
      val outputs = runnableOutputs.getOrElse(orderableName, collection.mutable.Map[String, Any]())
      putAll(outputs, processOutputs)
    } else {
      // Attempt to activate the next Orderable
      for( dependent <- dependents.getOrElse(orderableName, Set()) ) {
        val inputs = runnables.get(dependent).get.inputs
        val outputs = runnableOutputs.getOrElse(orderableName, collection.mutable.Map[String, Any]())
        
        // It is possible for multiple Orderables to come before an Orderable is activated.
        // This is collecting the inputs from all the Orderables before passing them in activate()
        putAll(outputs, inputs)
        
        activate(dependent)
      }
    }
    
    orderablesDone += 1
  }
  
  /**
   * Pre: map and addingTo are not null
   * Post: Every key-value pair in map is now in addingTo
   * 
   * Throws: IllegalArgumentException if map or addingTo is null
   * 				 IllegalStateException if addingTo already contains a value at the same key as map
   */
  def putAll(map: collection.Map[String, Any], addingTo: collection.mutable.Map[String, Any]) {
    if( map == null ) {
      throw new IllegalArgumentException("map cannot be null")
    } else if( addingTo == null ) {
      throw new IllegalArgumentException("addingTo cannot be null")
    }
    
    for( (key, value) <- map ) {
      if( !addingTo.get(key).isEmpty )
        throw new IllegalStateException("'"+key+"' is already in addingTo")
      addingTo.put(key, value)
    }
  }
  
  /**
   * Pre: All of the Orderables in dependsOn.get(toActivateName) have been finished.
   * 			toActivateName has not been run before
   * Post: runnables.get(toActivateName) has started to be run in a thread.
   */
  private def activate( toActivateName: String ) {
    if( isDone.getOrElse(toActivateName, false) == false ) {
      var canActivate = true
      for( previous <- dependsOn.getOrElse(toActivateName, Set()) ) {
        if( isDone.get(previous).get == false ) {
          canActivate = false
          return
        }
      }
      
      if( canActivate ) {
        run(toActivateName)
      }
    }
  }
  
  /**
   * Pre: runnables is not null
   * Post: A new thread has been created with the runnable at the key specified.
   * 
   * Throws: IllegalStateException if there is no runnable at the specified key.
   */
  def run( toActivateName: String ) {
    if( runnables.get(toActivateName).isEmpty ) {
      throw new IllegalStateException("No runnable at key '"+toActivateName+"'")
    }
    
    putAll(processInputs, runnables.get(toActivateName).get.inputs) 
    new Thread(runnables.get(toActivateName).get).start
    println("Activating Orderable: " + toActivateName)
  }
}

object OrderableRelationshipActor {
  def createActorName( processName: String ): String = {
    processName+"RelationshipActor"
  }
}