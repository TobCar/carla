package carla.including

abstract class OrderableRunnable( name: String, relationshipActor: OrderableRelationshipActor ) extends Runnable { 
 
  val inputs = collection.mutable.Map[String, Any]()
  
  def run() {
    done(customRun())
  }
  
  /**
   * Returns the output of the runnable
   */
  def customRun(): scala.collection.immutable.Map[String, Any]
  
  private def done(outputs: collection.immutable.Map[String, Any]) {
    //null can be used in runnables that add themselves to the finish queue manually
    //An example of this would be when another process is called from within a process
    if( relationshipActor != null )
      relationshipActor.addToFinishQueue(name, outputs)
  }
}