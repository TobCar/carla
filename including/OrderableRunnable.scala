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
    relationshipActor.addToFinishQueue(name, outputs)
  }
}