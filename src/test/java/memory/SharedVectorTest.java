package memory;
import org.junit.jupiter.api.Test;

import memory.SharedVector;
import memory.VectorOrientation;

import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    // Constructor tests
    //checking if someone tries to creat a vector with null array or null orientation
    @Test
    void constructor_nullArray_throws() {
        assertThrows(IllegalArgumentException.class,() -> new SharedVector(null, VectorOrientation.ROW_MAJOR));
    }
    @Test
    void constructor_nullOrientation_throws() {
        assertThrows(IllegalArgumentException.class,() -> new SharedVector(new double[]{1,2}, null));
    }
    @Test
    void constructor_valid_setsOrientationAndLength() {
        SharedVector v = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        assertEquals(3, v.length());
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }
    // Checking if the system handles a vector with 0 elements without crashing
    @Test
    void constructor_emptyArray_isAllowed() {
        SharedVector v = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);
        assertEquals(0, v.length());
        assertDoesNotThrow(() -> v.negate());
        assertDoesNotThrow(() -> v.transpose());
    }

    // Get method tests
    @Test
    void get_validIndex_returnsValue() {
        SharedVector v = new SharedVector(new double[]{5,6,7}, VectorOrientation.ROW_MAJOR);
        assertEquals(6, v.get(1));
    }
    @Test
    void get_outOfBounds_throws() {
        SharedVector v = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        assertThrows(IndexOutOfBoundsException.class, () -> v.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> v.get(2));
    }

    // Method tests
    @Test
    void transpose_flipsOrientation() {
        SharedVector v = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }
    @Test
    void negate_changesAllSigns() {
        SharedVector v = new SharedVector(new double[]{1,-2,3}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertEquals(-1, v.get(0));
        assertEquals(2, v.get(1));
        assertEquals(-3, v.get(2));
    }

    @Test
    void add_addsElementWise() {
        SharedVector a = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{10,20,30}, VectorOrientation.ROW_MAJOR);
                a.add(b);
                assertEquals(11, a.get(0));
                assertEquals(22, a.get(1));
                assertEquals(33, a.get(2));
    }
    //checks that adding a vector to itself works correctly
    @Test
    void add_selfAddition_works() {
        SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        assertDoesNotThrow(() -> v.add(v));
        assertEquals(2.0, v.get(0), 0.001);
        assertEquals(4.0, v.get(1), 0.001);
        assertEquals(6.0, v.get(2), 0.001);
    }

    @Test
    //checks that adding vectors of different orientation throws exception
    void add_differentOrientation_throws() {
        SharedVector a = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{1,2}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }
    @Test
    void add_differentLength_throws() {
        SharedVector a = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }

    ////dot product error cases
    @Test
    void dot_computesCorrectly() {
        SharedVector a = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{4,5,6}, VectorOrientation.ROW_MAJOR);
        double res = a.dot(b);
        assertEquals(1*4 + 2*5 + 3*6, res);
    }

    @Test
    void dot_null_throws() {
        SharedVector a = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> a.dot(null));
    }

    // Deadlock tests
    //thread 1 locks A then tries to lock B while Thread 2 locks B and tries to lock A
    @Test
    void add_noDeadlock_twoThreadsOppositeOrder() throws Exception {
        SharedVector a = new SharedVector(new double[]{1,1,1}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{2,2,2}, VectorOrientation.ROW_MAJOR);
        Thread t1 = new Thread(() -> a.add(b));
        Thread t2 = new Thread(() -> b.add(a));
        t1.start();
        t2.start();
        t1.join(1000);//wait for T1 to finish for 1s
        t2.join(1000);//wait for T2 to finish for 1s
        assertFalse(t1.isAlive(), "t1 deadlocked");
        assertFalse(t2.isAlive(), "t2 deadlocked");
    }

    //stress test with many threads
    @Test
    void add_concurrentIntegrity() throws Exception {
        final SharedVector a = new SharedVector(new double[]{0,0,0}, VectorOrientation.ROW_MAJOR);
        final SharedVector b = new SharedVector(new double[]{1,1,1}, VectorOrientation.ROW_MAJOR);
    
        int numThreads = 100;
        Thread[] threads = new Thread[numThreads];
    
        // 100 threads all trying to add 'b' to 'a' at the same time
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> a.add(b));
            threads[i].start();
        }
    
        for (Thread t : threads) t.join();
    
        // If locks work, each element should be exactly 100
        assertEquals(100.0, a.get(0), 0.001);
    }

    //checks the vector interacts correctly with a matrix multiplication
    @Test
    void vecMatMul_multipliesCorrectly() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
       
        double[][] matrixData = {{1, 2}, {3, 4}};
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(matrixData);
        
        v.vecMatMul(matrix);
        
        assertEquals(7.0, v.get(0), 0.001);
        assertEquals(10.0, v.get(1), 0.001);
    }
}
    

    