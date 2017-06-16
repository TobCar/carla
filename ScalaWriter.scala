package carla

import java.io.{File, FileWriter, BufferedWriter}
import scala.collection.mutable.Map
import carla.including.OrderableRelationshipActor.createActorName

object ScalaWriter {
  
  private val maxNumOfThreads = Runtime.getRuntime().availableProcessors() //TODO OPTIMIZE FOR CPU COUNT
  private var startingOrderables = Set[Orderable]() 
  
  /**
   * Pre: superContainer contains all other containers.
   * 			superContainer has at least one internal container.
   * Post: A Scala file has been created for every process in superContainer
   */
  def createScalaFilesFrom( superContainer: Container ) {
    while( superContainer.hasInternalContainer() ) {
       //TODO UPDATE LIBRARY NAME
      createScalaFile(superContainer.getInternalContainer(), "LIBRARYNAMEGOESHERE")
    }
  }
  
  /**
   * Pre: process contains 
   * Post: A file has been created and its name is the name
   * 			 of the container.
   */
  def createScalaFile( process: Container, libraryName: String ) {
    val programName = process.name
    
    //Create a file based on the container's name
    val file = new File(programName+".scala")
    val fw = new FileWriter(file)
    val bw = new BufferedWriter(fw)
   
    //Write the generic start of the file
    bw.write("package "+libraryName+"\nobject "+programName+" {\n")
    
    //Make it possible to run this process directly
    bw.write("def main( args: Array[String] ) {\nactivate(collection.immutable.Map(), collection.immutable.Map(), null)\n}\n")
    
    //Make it possible for other files to run this process
    bw.write("def activate(processInputs: collection.immutable.Map[String, Any], processOutputKeys: collection.immutable.Map[String, String], parentActor: OrderableRelationshipActor) {\n")
    
    //Compile to the newly created file
    compile(process, bw)
    
    //Write the closing for the object / start of the file
    bw.write("}\n}\n")
    
    //Close the writer objects
    bw.close()
    fw.close()
    
    println("File created at: "+file.getAbsolutePath)
    
    //TODO PASS RELATIVE URLS
    copyFilesToInclude("/Users/TobiasC/Scala/Test/src/LIBRARYNAMEGOESHERE/", "LIBRARYNAMEGOESHERE")
  }
  
  /**
   * Pre: Destination is a String URL pointing to a directory. That is, the last 
   * 			character is a '/'
   * Post: All files in carla.including have been copied to the compiling location
   */
  private def copyFilesToInclude( destination: String, packageName: String ) {
    //TODO FIND INCLUDING FOLDER RELATIVE TO THE USER RUNNING THIS CODE
    val includingFolder = new java.io.File("/Users/TobiasC/Scala/Carla/src/carla/including")
    val listOfFiles = includingFolder.listFiles()
    for( file <- listOfFiles ) {
      val fw = new FileWriter(new File(destination+file.getName))
      val bw = new BufferedWriter(fw)
      
      for( line <- scala.io.Source.fromFile(file).getLines() ) {
        if( line.startsWith("package") ) {
          bw.write("package "+packageName+"\n")
        } else {
          bw.write(line+"\n")
        }
      }
      
      bw.close
      fw.close
      
      println("Copied carla.include file to "+destination+file.getName)
    }
  }
  
  /**
   * Pre: process is not null
   * Returns: A Map literal in the form [String, Set[String]
   */
  private def createDependsOnLiteral(process: Container): String = {
    var dependsOnLiteral = "collection.immutable.Map("
    var dependsOnCount = 0
    for( (orderableName, orderable) <- process.orderables ) {
      //Keep track of how many entries have been added to the Map literal
      dependsOnCount += 1
      
      //Add contents to the Map literal
      dependsOnLiteral += "\""+orderableName+"\"->"+createRelationshipSetLiteral(orderable.dependsOn)
      
      //Add a comma to keep adding entries if there's more left
      if( dependsOnCount != process.orderables.size ) {
        dependsOnLiteral += ","
      }
    }
    dependsOnLiteral + ")"
  }
  
  /**
   * Pre: process is not null
   * Returns: An immutable Map literal in the form collection.immutable.Map("Key", Set["Entry"])
   */
  private def createDependentsLiteral(process: Container): String = {
    var dependentsLiteral = "collection.immutable.Map("
    var dependentsCount = 0
    for( (orderableName, orderable) <- process.orderables ) {
      //Keep track of how many entries have been added to the Map literal
      dependentsCount += 1
      
      //Add contents to the Map literal
      dependentsLiteral += "\""+orderableName+"\"" + "->" + createRelationshipSetLiteral(orderable.dependents)
      
      //Add a comma to keep adding entries if there's more left
      if( dependentsCount != process.orderables.size ) {
        dependentsLiteral += ","
      }
    }
    dependentsLiteral + ")"
  }
  
  /**
   * Pre: relationships is not null
   * Returns: An immutable Set literal in the form collection.immutable.Set("Entry")
   */
  private def createRelationshipSetLiteral(relationships: Set[Orderable]): String =  {
    var setLiteral = "collection.immutable.Set("
    
    for( (dependentsOrderable, index) <- relationships.zipWithIndex ) {
      setLiteral += "\""+dependentsOrderable.name+"\""
      if( index < relationships.size-1 ) {
        setLiteral += ","
      }
    }
    
    setLiteral + ")"
  }
  
  /**
   * Pre: process contains Orderables
   * 			bw writes to a valid file
   * 			bw is writing code within a function where "processInputs" and "parentActor"
   * 			are parameters.
   * Post: Code allowing the process and its orderables to run has been written in a Scala file.
   * 			 The control flow between Orderables in process has been computed.
   */
  private def compile( process: Container, bw: BufferedWriter ) {
    determineControlFlow(process.orderables)
    
    //Create the Orderable relationship actor to manage the flow between threads
    val nameOfRelationshipActor = createActorName(process.name)
    val dependsOn = createDependsOnLiteral(process)
    val dependents = createDependentsLiteral(process)
    
    bw.write("val "+nameOfRelationshipActor+" = new OrderableRelationshipActor(\""+process.name+"\",processInputs,"+dependsOn+","+dependents+",processOutputKeys,parentActor)\n")
    
    if( process.orderables.isEmpty == false ) {
      for( (orderableName, orderable) <- process.orderables ) {
        createRunnable(orderable, bw, nameOfRelationshipActor)
      }
      
      //Create Thread  
      for( startingOrderable <- startingOrderables ) {
        bw.write(nameOfRelationshipActor+".run(\""+startingOrderable.name+"\")\n")
      }
    } else {
      println("The process \""+process.name+"\" has no orderables")
    }
  }
  
  /**
   * Outputs the Scala code in the orderable to a Scala file as part of a OrderableRunnable.
   */
  def createRunnable( orderable: Orderable, bw: BufferedWriter, nameOfRelationshipActor: String ) {
    //Create runnable
    val runnableName = orderable.name + "Runnable"
    
    /* Pass a null actor to the constructor if this Orderable is calling another process since
     * that process adds itself to the finish queue manually. */
    orderable match {
      case processToRun: ProcessToRun => bw.write("val "+runnableName+" = new OrderableRunnable(\""+orderable.name+"\",null) {\n")
      case _ => bw.write("val "+runnableName+" = new OrderableRunnable(\""+orderable.name+"\","+nameOfRelationshipActor+") {\n")
    }
    
    bw.write("override def customRun(): collection.immutable.Map[String, Any] = {\n")
    
    //Instantiate "using" variables so the user defined code works
    for( usingName <- orderable.usingName ) {
      bw.write("val "+usingName+" = inputs.get(\""+usingName+"\").get\n")
    }
    
    //User defined code
    while( orderable.hasToken() ) {
      bw.write(orderable.getToken()+" ")
    }
    
    var passingMapContents = ""
    orderable match {
      case processToRun: ProcessToRun => //Do nothing. Outputs are passed manually by the new process when it's done.
      case _ => //"passing" variables
                var variablesAdded = 0
                for( passingName <- orderable.passingName ) {
                  passingMapContents += "\""+passingName+"\"->"+passingName
                  variablesAdded += 1
                  if( variablesAdded < orderable.passingName.size ) {
                    passingMapContents += ","
                  }
                }
    }
    
    bw.write("collection.immutable.Map("+passingMapContents+")")
    
    bw.write("\n}\n}\n")
    bw.write(nameOfRelationshipActor+".addOrderable(\""+orderable.name+"\","+runnableName+")\n")
  }
  
  /**
   * Pre: orderables is not null
   * Post: All objects in orderables have the connections necessary for the compiler
   * 			 to execute the threads in the right order.
   */
  def determineControlFlow( orderables: Map[String, Orderable] ) {
    for( (name, orderable) <- orderables ) {
      if( orderable.isInstanceOf[Step] && orderable.asInstanceOf[Step].isLastStep() ) {
        //Special case, run after all other Orderables
        for( (dependentOnName, dependentOn) <- orderables ) {
          if( name != dependentOnName )
            Orderable.connect(dependentOn, orderable)
        }
      } else if( orderable.after.isEmpty ) {
        //Run an Orderable first by default
        startingOrderables += orderable
      } else {
        //Let any Orderables with dependents know what Orderables depend on them
        for( dependentOnName <- orderable.after ) {
          val dependentOn = orderables.getOrElse(dependentOnName, null)
          if( dependentOn == null ) {
            throw new IllegalControlFlowException("Orderable \""+name+"\" is after an unknown Orderable \""+dependentOnName+"\"")
          } else {
            Orderable.connect(dependentOn, orderable)
          }
        }
      }
    }
  }
}