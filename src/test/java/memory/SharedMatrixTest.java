package memory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

    //Constructor Tests
    @Test
    void emptyConstructor_createsEmptyMatrix() {
        SharedMatrix m = new SharedMatrix();
        assertEquals(0, m.length());
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        assertArrayEquals(new double[0][0], m.readRowMajor());
    }
    @Test
    void constructor_nullMatrix_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(null));
    }
    @Test
    void constructor_emptyMatrix_createsEmptyMatrix() {
        SharedMatrix m = new SharedMatrix(new double[][]{});
        assertEquals(0, m.length());
        assertArrayEquals(new double[0][0], m.readRowMajor());
    }
    @Test
    void constructor_nonRectangular_throws() {
        double[][] bad = {
                {1, 2},
                {3, 4, 5}
        };
        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(bad));
    }
    // Valid Constructor Tests. is data put in the same as data that comes out?
    @Test
    void constructor_validRowMajorMatrix_storesCorrectly() {
        double[][] input = {
                {1, 2, 3},
                {4, 5, 6}
        };
        SharedMatrix m = new SharedMatrix(input);

        assertEquals(2, m.length());
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());

        assertEquals(1, m.get(0).get(0));
        assertEquals(3, m.get(0).get(2));
        assertEquals(4, m.get(1).get(0));
        assertEquals(6, m.get(1).get(2));
    }

    //testing 1*1 matrix
    @Test
    void singletonMatrix_works() {
        SharedMatrix m = new SharedMatrix(new double[][]{{99}});
        assertEquals(1, m.length());
        assertEquals(99, m.get(0).get(0));
    }

    // Method Tests 
    @Test
    void get_outOfBounds_throws() {
        SharedMatrix m = new SharedMatrix(new double[][]{
                {1, 2}
        });

        assertThrows(IndexOutOfBoundsException.class, () -> m.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> m.get(1));
    }
    //confirms length checks the number of vectors according to orientation
    @Test
    void length_matchesNumberOfStoredVectors() {
        SharedMatrix m = new SharedMatrix(new double[][]{
                {1, 2},
                {3, 4},
                {5, 6}
        });
        assertEquals(3, m.length()); // 3 row vectors
    }

    @Test
    void loadRowMajor_null_throws() {
        SharedMatrix m = new SharedMatrix();
        assertThrows(IllegalArgumentException.class, () -> m.loadRowMajor(null));
    }

    @Test
    void loadRowMajor_nonRectangular_throws() {
        SharedMatrix m = new SharedMatrix();
        double[][] bad = {
                {1, 2},
                {3}
        };
        assertThrows(IllegalArgumentException.class, () -> m.loadRowMajor(bad));
    }

    // Valid loadRowMajor test
    @Test
    void loadRowMajor_replacesMatrixData() {
        SharedMatrix m = new SharedMatrix(new double[][]{
                {1, 1},
                {1, 1}
        });

        double[][] newData = {
                {2, 3},
                {4, 5}
        };

        m.loadRowMajor(newData);

        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        double[][] out = m.readRowMajor();

        assertArrayEquals(new double[]{2, 3}, out[0]);
        assertArrayEquals(new double[]{4, 5}, out[1]);
    }

    @Test
    void loadColumnMajor_null_throws() {
        SharedMatrix m = new SharedMatrix();
        assertThrows(IllegalArgumentException.class, () -> m.loadColumnMajor(null));
    }

    @Test
    void loadColumnMajor_nonRectangular_throws() {
        SharedMatrix m = new SharedMatrix();
        double[][] bad = {
                {1, 2},
                {3}
        };
        assertThrows(IllegalArgumentException.class, () -> m.loadColumnMajor(bad));
    }

    // Valid loadColumnMajor test
    @Test
    void loadColumnMajor_replacesMatrixData_andStoresAsColumns() {
        double[][] input = {
                {1, 2, 3},
                {4, 5, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(input);

        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
        assertEquals(3, m.length());
        SharedVector col0 = m.get(0);
        assertEquals(VectorOrientation.COLUMN_MAJOR, col0.getOrientation());
        assertEquals(1, col0.get(0));
        assertEquals(4, col0.get(1));
    }
    // Test switching orientations by loading different formats
    @Test
    void switchingOrientations_works() {
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{{1}, {2}}); // Load as columns
        m.loadRowMajor(new double[][]{{3, 4}});      // Load as rows
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        assertEquals(1, m.length());
    }

    //checking two threats accessing the matrix at the same time does not crash
    @Test
    void concurrentAccess_doesNotCrash() throws InterruptedException {
        SharedMatrix m = new SharedMatrix(new double[][]{{1, 2}, {3, 4}});
        Thread t1 = new Thread(() -> m.get(0));
        Thread t2 = new Thread(() -> m.get(0));
        t1.start(); t2.start();
        t1.join(); t2.join();
    }

    @Test
    void readRowMajor_fromColumnMajorMatrix_returnsCorrectRows() {
        double[][] originalData = {
            {1, 2, 3},
            {4, 5, 6}
        };
        SharedMatrix m = new SharedMatrix();
    
        // Load it as Column-Major it should store 3 vectors: [1,4], [2,5], [3,6]
        m.loadColumnMajor(originalData);
    
        //Request the data back in Row-Major format
        double[][] result = m.readRowMajor();
    
        
        assertEquals(2, result.length, "Should have 2 rows");
        assertEquals(3, result[0].length, "Should have 3 columns");
    
        
        assertArrayEquals(new double[]{1, 2, 3}, result[0], 0.001);
        assertArrayEquals(new double[]{4, 5, 6}, result[1], 0.001);
    }
}
