package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // creating executor with given thread count
        this.executor = new TiredExecutor(numThreads);
    }

    //resolve computation tree step by step until final matrix is produced
    //we will use it using recursion
    public ComputationNode run(ComputationNode computationRoot) {
        if (computationRoot == null) 
            return null;

        List<ComputationNode> children = computationRoot.getChildren();

        //if children is null or empty, we are at a leaf node (MATRIX), so we return it as is
        if (children == null || children.isEmpty()) {
            return computationRoot;
        }

        for (ComputationNode kid : children) {
            run(kid); // Recursive call
        }

        loadAndCompute(computationRoot);//all the children are solved and now we can solve us, the current node

        return computationRoot;
    }

    // Load operand matrices and compute the result
    //an assumption is that the children of the node are already computed and solved
    public void loadAndCompute(ComputationNode node) {
        ComputationNodeType type = node.getNodeType();
        //if the type is matrix we have nothing to do, bc we need an action like add or negate
        if (type == ComputationNodeType.MATRIX) {
            return;
        }
        List<ComputationNode> children = node.getChildren();
        if (children.size() > 0) {
            this.leftMatrix.loadRowMajor(children.get(0).getMatrix());//loading the left child matrix as rows
        }
        if (children.size() > 1) {
            this.rightMatrix.loadRowMajor(children.get(1).getMatrix());//loadint the right child matrix as rows
        }
        List<Runnable> tasks = new ArrayList<>();
        if (type == ComputationNodeType.ADD) {
            tasks = createAddTasks();
        } 
        else if (type == ComputationNodeType.MULTIPLY) {
            tasks = createMultiplyTasks();
        } 
        else if (type == ComputationNodeType.NEGATE) {
            tasks = createNegateTasks();
        } 
        else if (type == ComputationNodeType.TRANSPOSE) {
            tasks = createTransposeTasks();
        } 
        else {
            throw new IllegalArgumentException("Unknown operation: " + type);
        }
        //they are exsiting tasks to execute
        if (!tasks.isEmpty()) {
            executor.submitAll(tasks); //submit tasks one by one and to executer and wait until all finish
        }
        double[][] resultData = this.leftMatrix.readRowMajor();
        node.resolve(resultData);//Resolves turn operator node to matrix node and deltes childten
    }

     // return tasks that perform row-wise addition
    public List<Runnable> createAddTasks() {
        List<Runnable> tasks = new ArrayList<>();
        //our assumption is that the num of rows is equal in both matrixes, we check that in SharedVector
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i; // must be final to be used in Lambda
            tasks.add(() -> {
                SharedVector v1 = leftMatrix.get(rowIndex);
                SharedVector v2 = rightMatrix.get(rowIndex);
                v1.add(v2); 
            });
        }
        return tasks;
    }

    //return tasks that perform row × matrix multiplication
    public List<Runnable> createMultiplyTasks() {
        List<Runnable> tasks = new ArrayList<>();
        //our assumption is that the num of rows is equal in both matrixes, we check that in SharedVector
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i; // must be final to be used in Lambda
            tasks.add(() -> {
                leftMatrix.get(rowIndex).vecMatMul(rightMatrix); 
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
       List<Runnable> tasks = new ArrayList<>();
        //our assumption is that the num of rows is equal in both matrixes, we check that in SharedVector
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i; // must be final to be used in Lambda
            tasks.add(() -> {
                leftMatrix.get(rowIndex).negate();
            });
        }
        return tasks;
    }
    

    //return tasks that transpose rows
    public List<Runnable> createTransposeTasks() {
        List<Runnable> tasks = new ArrayList<>();
        //our assumption is that the num of rows is equal in both matrixes, we check that in SharedVector
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i; // must be final to be used in Lambda
            tasks.add(() -> {
                leftMatrix.get(rowIndex).transpose();
            });
        }
        return tasks;
    }

    // return summary of worker activity
    //helps us check the programm is parallel
    public String getWorkerReport() {
       return executor.getWorkerReport();
    }

    //Helper method we did
    //shutdown the executor and its threads
    public void shutdown() {
        try {
            this.executor.shutdown();
        } 
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}