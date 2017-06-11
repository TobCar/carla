package carla

import java.io.{File, FileWriter, BufferedWriter}
import scala.collection.mutable.Map

object ScalaWriter {
  
  private val maxNumOfThreads = Runtime.getRuntime().availableProcessors() //TODO OPTIMIZE FOR CPU COUNT
  private var startingSteps = Set[Step]() 
  
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
   
    //Add the generic start of the file
    bw.write("package "+libraryName+"\nobject "+programName+" {\ndef main( args: Array[String] ) {\n")
    
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
    for( (stepName, step) <- process.steps ) {
      //Keep track of how many entries have been added to the Map literal
      dependsOnCount += 1
      
      //Add contents to the Map literal
      dependsOnLiteral += "\""+stepName+"\"->"+createRelationshipSetLiteral(step.dependsOn)
      
      //Add a comma to keep adding entries if there's more left
      if( dependsOnCount != process.steps.size ) {
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
    for( (stepName, step) <- process.steps ) {
      //Keep track of how many entries have been added to the Map literal
      dependentsCount += 1
      
      //Add contents to the Map literal
      dependentsLiteral += "\""+stepName+"\"" + "->" + createRelationshipSetLiteral(step.dependents)
      
      //Add a comma to keep adding entries if there's more left
      if( dependentsCount != process.steps.size ) {
        dependentsLiteral += ","
      }
    }
    dependentsLiteral + ")"
  }
  
  /**
   * Pre: relationships is not null
   * Returns: An immutable Set literal in the form collection.immutable.Set("Entry")
   */
  private def createRelationshipSetLiteral(relationships: Set[Step]): String =  {
    var setLiteral = "collection.immutable.Set("
    
    for( (dependentsStep, index) <- relationships.zipWithIndex ) {
      setLiteral += "\""+dependentsStep.name+"\""
      if( index < relationships.size-1 ) {
        setLiteral += ","
      }
    }
    
    setLiteral + ")"
  }
  
  /**
   * Pre: process contains Steps
   * 			bw writes to a valid file
   * Post: Code allowing the process and its steps to run has been written in a Scala file.
   * 			 The control flow between Steps in process has been computed.
   */
  private def compile( process: Container, bw: BufferedWriter ) {
    determineControlFlow(process.steps)
    
    //Create the Step relationship actor to manage the flow between threads
    val nameOfRelationshipActor = process.name+"RelationshipActor"
    val dependsOn = createDependsOnLiteral(process)
    val dependents = createDependentsLiteral(process)
    bw.write("val "+nameOfRelationshipActor+" = new StepRelationshipActor("+dependsOn+","+dependents+")\n")
    
    if( process.steps.isEmpty == false ) {
      for( (stepName, step) <- process.steps ) {
        createRunnable(step, bw, nameOfRelationshipActor)
      }
      
      //Create Thread  
      for( startingStep <- startingSteps ) {
        bw.write("new Thread("+nameOfRelationshipActor+".runnables.get(\""+startingStep.name+"\").get).start\n")
      }
    } else {
      println("The process \""+process.name+"\" has no steps")
    }
  }
  
  /**
   * Outputs the Scala code in the step to a Scala file as part of a StepRunnable.
   */
  def createRunnable( step: Step, bw: BufferedWriter, nameOfRelationshipActor: String ) {
    //Create runnable
    val runnableName = step.name + "Runnable"
    
    bw.write("val "+runnableName+" = new StepRunnable(\""+step.name+"\","+nameOfRelationshipActor+") {\n")
    bw.write("override def customRun(): collection.immutable.Map[String, Any] = {\n")
    
    //Instantiate "using" variables so the user defined code works
    for( usingName <- step.usingName ) {
      bw.write("val "+usingName+" = inputs.get(\""+usingName+"\").get\n")
    }
    
    //User defined code
    while( step.hasToken() ) {
      bw.write(step.getToken()+" ")
    }
    
    //"passing" variables
    var passingMapContents = ""
    var variablesAdded = 0
    for( passingName <- step.passingName ) {
      passingMapContents += "\""+passingName+"\"->"+passingName
      variablesAdded += 1
      if( variablesAdded < step.passingName.size ) {
        passingMapContents += ","
      }
    }
    bw.write("collection.immutable.Map("+passingMapContents+")")
    
    bw.write("\n}\n}\n")
    bw.write(nameOfRelationshipActor+".addStep(\""+step.name+"\","+runnableName+")\n")
  }
  
  /**
   * Pre: steps is not null
   * Post: All Steps in steps have the connections necessary for the compiler
   * 			 to execute the threads in the right order.
   */
  def determineControlFlow( steps: Map[String, Step] ) {
    for( (name, step) <- steps ) {
      if( step.isLastStep() ) {
        //Special case, run after all other steps
        for( (dependentOnName, dependentOn) <- steps ) {
          if( name != dependentOnName )
            Step.connect(dependentOn, step)
        }
      } else if( step.after.isEmpty ) {
        //Run a step first by default
        startingSteps += step
      } else {
        //Let any steps with dependents know what steps depend on them
        for( dependentOnName <- step.after ) {
          val dependentOn = steps.getOrElse(dependentOnName, null)
          if( dependentOn == null ) {
            //Could be improved by throwing an exception instead
            println("ERROR: Step \""+name+"\" is after an unknown Step \""+dependentOnName+"\"")
          } else {
            Step.connect(dependentOn, step)
          }
        }
      }
    }
  }
}