/* 
 * This file has to be minified and its contents included in OrderableRunnable.txt
 * in order for it to be included in compiled code.
 * 
 * The package will be overriden so it is not necessary to include that line.
 */

package carla.including

abstract class OrderableRunnable(name: String, relationshipActor: OrderableRelationshipActor) extends Runnable {
 
  val inputs = collection.mutable.Map[String, Any]()
  
  def run() {
    done(customRun())
  }
  
  /**
   * Returns the output of the runnable
   */
  def customRun(): scala.collection.immutable.Map[String, Any]
  
  private def done(outputs: collection.immutable.Map[String, Any]) {
    // null can be used in runnables that add themselves to the finish queue manually
    // An example of this would be when another process is called from within a process
    if( relationshipActor != null )
      relationshipActor.addToFinishQueue(name, outputs)
  }
}