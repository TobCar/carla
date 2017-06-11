package carla.including

class StepRunnable( name: String, relationshipActor: StepRelationshipActor ) extends Runnable { 
 
  val inputs = collection.mutable.Map[String, Any]()
  
  def run() {
    done(customRun())
  }
  
  
  def customRun(): scala.collection.immutable.Map[String, Any] = {
    //Expected to be overridden by the compiler
    null
  }
  
  private def done(outputs: collection.immutable.Map[String, Any]) {
    relationshipActor.addToFinishQueue(name, outputs)
  }
}