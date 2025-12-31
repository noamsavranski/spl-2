package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {

      if (args.length != 3) {
        System.out.println("Usage: java -jar LAE.jar <numThreads> <inputFilePath> <outputFilePath>");
        return;
      }
      
      String inputPath = args[1];
      String outputPath = args[2];

      //parse the input file
      try {
        InputParser inputParser = new InputParser();
        ComputationNode computationRoot = inputParser.parse(inputPath);

        //create LinearAlgebraEngine with args[0] threads
        int numThreads = Integer.parseInt(args[0]);

        //handle associative nesting
        recursiveAssociativeNesting(computationRoot);

        LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);

        //run the computation
        ComputationNode result = engine.run(computationRoot);

        //report
        System.out.println(engine.getWorkerReport());

        //write the result to output file - success
        OutputWriter.write(result.getMatrix(), outputPath);
        }

        catch (Exception e){
          e.printStackTrace();
          //write the result to output file - failure
          OutputWriter.write(e.getMessage(), outputPath);
        }
      }

      /**
     * Recursive helper function to associativeNesting.
     * Traverses the tree bottom-up (Post-Order) and applies associativeNesting to every node.
     * This ensures that nested operations (like A+B+C) are correctly structured before execution.
     */
    private static void recursiveAssociativeNesting(ComputationNode node) {
        if (node == null) {
            return;
        }

        if (node.getChildren() != null) {
            for (ComputationNode child : node.getChildren()) {
                recursiveAssociativeNesting(child);
            }
        }

        node.associativeNesting();
    }
}