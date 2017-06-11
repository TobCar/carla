package carla.including

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Handles the relationships between threads. Specifically: what thread needs to be executed after
 * what thread and what parameters need to be passed between them.
 */
class StepRelationshipActor( 
    dependsOn: collection.immutable.Map[String, Set[String]], 
    dependents: collection.immutable.Map[String, Set[String]]) {
  
  val runnables = collection.mutable.Map[String, StepRunnable]()
  val runnableOutputs = collection.mutable.Map[String, Map[String, Any]]()
  
  private val isDone = collection.mutable.Map[String, Boolean]()
  private val toFinishQueue = new ConcurrentLinkedQueue[String]()
  
  var stepsDone = 0
  (new Thread {
    override def run {
      while(true) {
        if( toFinishQueue.size != 0 ) {
          val stepName = toFinishQueue.poll() 
          finish(stepName)
          
          if( stepsDone == isDone.size ) {
            println("Stopping Relationship Actor")
            return
          }
        }
      }
    }
  }).start
  
  /**
   * Add a step for other steps to have a relationship with.
   */
  def addStep( name:String, runnable: StepRunnable ) {
    runnables.put(name,runnable)
    isDone.update(name, false)
    println("Added step: "+name)
  }

  
  /**
   * Assign a task to this actor.
   */
  def addToFinishQueue( stepName: String, outputs: collection.immutable.Map[String, Any] ) {
    runnableOutputs.put(stepName, outputs)
    toFinishQueue.add(stepName)
    //TODO PROCESS OUTPUTS AND MERGE OUTPUTS FROM MULTIPLE STEPS
  }
  
  /**
   * Post: isDone.get(stepName) == true and any dependents of that step
   * 			 have started to be run if all of that dependent's dependencies
   * 			 have all finished running.
   */
  private def finish( stepName: String ) {
    isDone.update(stepName, true)
    
    //Attempt to activate the next Step
    for( dependent <- dependents.getOrElse(stepName, Set()) ) {
      //Prevent duplicate inputs for the same variable
      val inputs = runnables.get(dependent).get.inputs
      val outputs = runnableOutputs.getOrElse(stepName, collection.mutable.Map[String, Any]())
      for( key <- inputs.keySet ) {
        if( !outputs.get(key).isEmpty ) {
          println("ERROR: Duplicate key '"+key+"'")
        }
      }
      
      //It is possible for multiple Steps to come before a Step is activate.
      //This is collecting the inputs from all the Steps before passing them in activate()
      for( (key, value) <- outputs ) {
        runnables.get(dependent).get.inputs.put(key, value)
      }
      
      activate(dependent)
    }
    
    stepsDone += 1
  }
  
  /**
   * Pre: All of the steps in dependsOn.get(toActivateName) have been finished.
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
        new Thread(runnables.get(toActivateName).get).start
        println("Activating Step/Thread: " + toActivateName)
      }
    }
  }
}