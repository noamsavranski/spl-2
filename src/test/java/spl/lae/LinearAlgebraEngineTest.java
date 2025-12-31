package spl.lae;
import spl.lae.LinearAlgebraEngine;
import parser.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;

    @BeforeEach
    void setUp() {
        // Initialize engine with 4 threads for parallel testing
        engine = new LinearAlgebraEngine(4);
    }

  @Test
    void run_simpleAddition_computesCorrectResult() throws Exception {
        double[][] data1 = {{1.0, 2.0}};
        double[][] data2 = {{10.0, 20.0}};
        ComputationNode node1 = new ComputationNode(data1);
        ComputationNode node2 = new ComputationNode(data2);
    
    
        List<ComputationNode> children = new ArrayList<>();
        children.add(node1);
        children.add(node2);

        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, children);
        engine.run(root);

        double[][] result = root.getMatrix();
        assertArrayEquals(new double[]{11.0, 22.0}, result[0], 0.001);
}

   @Test
    void run_recursiveDeepTree_resolvesAllLevels() throws Exception {
        ComputationNode m1 = new ComputationNode(new double[][]{{1, 1}});
        ComputationNode m2 = new ComputationNode(new double[][]{{1, 1}});
    
        List<ComputationNode> innerChildren = new ArrayList<>();
        innerChildren.add(m1);
        innerChildren.add(m2);
        ComputationNode innerAdd = new ComputationNode(ComputationNodeType.ADD, innerChildren);

        ComputationNode m3 = new ComputationNode(new double[][]{{5, 5}});
        List<ComputationNode> rootChildren = new ArrayList<>();
        rootChildren.add(innerAdd);
        rootChildren.add(m3);

        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, rootChildren);
        engine.run(root);

        assertArrayEquals(new double[]{7.0, 7.0}, root.getMatrix()[0], 0.001);
    }

    @Test
    void run_matrixMultiplication_computesRowByMatrix() throws Exception {
        ComputationNode vNode = new ComputationNode(new double[][]{{1.0, 2.0}});
        ComputationNode iNode = new ComputationNode(new double[][]{{1.0, 0.0}, {0.0, 1.0}});

        List<ComputationNode> children = new ArrayList<>();
        children.add(vNode);
        children.add(iNode);

        ComputationNode root = new ComputationNode(ComputationNodeType.MULTIPLY, children);

        engine.run(root);

        assertArrayEquals(new double[]{1.0, 2.0}, root.getMatrix()[0], 0.001);
    }

    @Test
    void run_negateAndTranspose_worksSequentially() throws Exception {
        ComputationNode mNode = new ComputationNode(new double[][]{{1.0, 2.0}});

        List<ComputationNode> negateChildren = new ArrayList<>();
        negateChildren.add(mNode);
        ComputationNode negateNode = new ComputationNode(ComputationNodeType.NEGATE, negateChildren);

        List<ComputationNode> transposeChildren = new ArrayList<>();
        transposeChildren.add(negateNode);
        ComputationNode root = new ComputationNode(ComputationNodeType.TRANSPOSE, transposeChildren);

        engine.run(root);
        double[][] result = root.getMatrix();
        assertEquals(2, result.length, "Transpose of 1x2 should result in 2 rows");
        assertEquals(-1.0, result[0][0], 0.001);
        assertEquals(-2.0, result[1][0], 0.001);
    }

    @Test
    void run_nullRoot_returnsNull() {
        assertNull(engine.run(null));
    }

    @Test
    void run_singleLeafNode_returnsImmediately() {
        double[][] data = {{5, 5}};
        ComputationNode leaf = new ComputationNode(data);
        
        ComputationNode result = engine.run(leaf);
        
        assertSame(leaf, result);
        assertArrayEquals(data[0], result.getMatrix()[0]);
    }
}