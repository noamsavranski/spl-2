package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        //if null
        if (matrix == null) 
            throw new IllegalArgumentException("Input matrix cannot be null.");

        //if empty
        if (matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }

        //checking the array is rectangular
        int numCols = matrix[0].length;
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i].length != numCols) {
                throw new IllegalArgumentException("Input matrix must be rectangular.");
            }
        }
        //initialize vectors
        this.vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            this.vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    //no need for locks since we are creating and replacing the entire matrix
    //replace internal data with new row-major matrix
    public void loadRowMajor(double[][] matrix) {
        if (matrix == null) 
            throw new IllegalArgumentException("Input matrix cannot be null."); 
        //if empty
        if (matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }
        //checking if in the new array i have null vectors
        //CHECKING RECTANGULARITY
        int numCols = matrix[0].length;
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
             throw new IllegalArgumentException("Matrix row cannot be null.");
         }
            if (matrix[i].length != numCols) {
                throw new IllegalArgumentException("Input matrix must be rectangular.");
            }
        }
        //initialize new matrix
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
           newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        //THE SWAP!
        this.vectors = newVectors;
    }
    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
       if (matrix == null) {
        throw new IllegalArgumentException("Input matrix cannot be null.");
    }
    if (matrix.length == 0) {
        this.vectors = new SharedVector[0];
        return;
    }
    // Check rectangularity and mull rows
    int rows = matrix.length;
    int cols = matrix[0].length;
    for (int i = 0; i < rows; i++) {
        if (matrix[i] == null) {
             throw new IllegalArgumentException("Matrix row cannot be null.");
        }
        if (matrix[i].length != cols) {
            throw new IllegalArgumentException("Input matrix must be rectangular.");
        }
    }

     //create new matrix as column-major vectors
    SharedVector[] newVectors = new SharedVector[cols];
    for (int j = 0; j < cols; j++) {
        double[] colData = new double[rows];//hodling the verticle numbers
        //interate through the rown in col
        for (int i = 0; i < rows; i++) {
            colData[i] = matrix[i][j];
        }
        // Create the vector with COLUMN orientation
        newVectors[j] = new SharedVector(colData, VectorOrientation.COLUMN_MAJOR);
    }
    //SWAP
    this.vectors = newVectors;
}

    //this method returns the matrix as vectors of rows
    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
    //making a current copy a "snapshot"
        SharedVector[] currMatrix = this.vectors;
    
        if (currMatrix == null || currMatrix.length == 0) {
            return new double[0][0];
        }

        // lock the matrix for reading
        acquireAllVectorReadLocks(currMatrix);
    
        try {
            //checking oriantion of vectors in matrix
            VectorOrientation orientation = currMatrix[0].getOrientation();
        
            int rows;
            int cols;
            double[][] result;

            //CASE A
            //if the matrix is already in rows
            if (orientation == VectorOrientation.ROW_MAJOR) {
                rows = currMatrix.length;
                cols = currMatrix[0].length();
                result = new double[rows][cols];
                //direct coping
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        result[i][j] = currMatrix[i].get(j);
                    }
                }
            }

            //CASE B
            //the matrix is in columns
            else {
                cols = currMatrix.length; 
                rows = currMatrix[0].length(); // The length of one vector is the number of Rows
                result = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        result[i][j] = currMatrix[j].get(i); //transpose
                    }
                }
            }
            return result;
        } finally {
        releaseAllVectorReadLocks(currMatrix); //unlock
         }
    }

    public SharedVector get(int index) {
        SharedVector[] snap = this.vectors;
        if (index < 0 || index >= snap.length) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + snap.length);
        }
        return snap[index];
    }

    public int length() {
        SharedVector[] snap = this.vectors;
        return snap.length;
    }

    public VectorOrientation getOrientation() {
        SharedVector[] snap = this.vectors;
        if (snap.length == 0) {
        return VectorOrientation.ROW_MAJOR;
        }
        SharedVector v0 = snap[0];
        v0.readLock();
         try {
            return v0.getOrientation();
        } 
        finally {
            v0.readUnlock();
        } 
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        if (vecs == null) {
            return;
        }
        for (int i = 0; i < vecs.length; i++) {
            SharedVector v = vecs[i];
            if (v != null) {
                v.readLock();
            }
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        if (vecs == null) {
            return;
        }
        for (int i = vecs.length - 1; i >= 0; i--) {
            SharedVector v = vecs[i];
            if (v != null) {
                v.readUnlock();
            }
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
           if (vecs == null) {
            return;
        }
        for (int i = 0; i < vecs.length; i++) {
            SharedVector v = vecs[i];
            if (v != null) {
                v.writeLock();
            }
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        if (vecs == null) {
            return;
        }
        for (int i = vecs.length - 1; i >= 0; i--) {
            SharedVector v = vecs[i];
            if (v != null) {
                v.writeUnlock();
            }
        }
    }
}