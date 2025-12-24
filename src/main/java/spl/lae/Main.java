package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
  public static void main(String[] args) throws IOException {
      //args is an array of strings where each string is a line from the input file
      //args[0] is the path to the input file
      //args[1] is the number of threads to use
      //at minimun we need 2 arguments so
    if (args.length < 2) {
      System.out.println("Error: not enough arguments");
      return;
    }
    String inputFilePath = args[0];
    //we get a string and we need to convert it to an integer
    int numThreads = Integer.parseInt(args[1]);
    System.out.println("Input file path: " + inputFilePath);
    System.out.println("Number of threads: " + numThreads);
      
    // Create the Engine (which creates the TiredExecutor + Thread Pool)
    LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);
        
    // Create the Parser (which reads the file)
    //input parse returns a single root note and not a list of nodes
    InputParser parser = new InputParser();

    try {
      // Parse the file into a Computation Tree
      System.out.println("Parsing script file.");
      ComputationNode command = parser.parse(inputFilePath);

      // Run the Engine
      System.out.println("Executing computation.");
      ComputationNode result = engine.run(command);

      // Check if it worked
      if (result != null && result.getMatrix() != null) {
      int rows = result.getMatrix().length;
      int cols = result.getMatrix()[0].length;
      System.out.println("Success! Final result matrix dimensions: " + rows + "x" + cols);
      } 
      else {
        System.out.println("Computation failed or resulted in null matrix.");
      }
      // Print the Worker Report
      System.out.println("--------------------------------------------------");
      System.out.println("Worker Activity Report:");
      System.out.println(engine.getWorkerReport());
      System.out.println("--------------------------------------------------");

    //handling errors
    } catch (Exception e) {
      System.err.println("An error occurred during execution:");
      e.printStackTrace(); 
    }
  }
}    