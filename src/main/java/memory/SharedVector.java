package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    //constructor
    public SharedVector(double[] vector, VectorOrientation orientation) {
        // Check if the array is null
    if (vector == null) {
        throw new IllegalArgumentException("SharedVector cannot be initialized with a null array.");
    }
    // Check if the orientation is null
    if (orientation == null) {
        throw new IllegalArgumentException("SharedVector orientation cannot be null.");
    }
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        lock.readLock();
        try {
           if (index < 0 || index >= vector.length) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds.");
            }
            return vector[index];
        } 
        finally {
            lock.readUnlock(); // Release read lock
        }
    }

    public int length() {
        lock.readLock();
        try {
            return vector.length;
        } 
        finally {
            lock.readUnlock(); // Release read lock
        }
    }

    public VectorOrientation getOrientation() {
        lock.readLock();
        try {
            return orientation;
        } 
        finally {
            lock.readUnlock(); // Release read lock
        }
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock(); 
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        lock.writeLock();
        try{
            if (orientation == VectorOrientation.ROW) {
                orientation = VectorOrientation.COLUMN;
            } 
            else {
                orientation = VectorOrientation.ROW;
            }
        } 
        finally {
            lock.writeUnlock(); // Release write lock
        }
    }

    public void add(SharedVector other) {
        if (other == null) {
                throw new IllegalArgumentException("Cannot add a null vector.");
            }
        lock.writeLock();
        try{
            if (this.orientation != other.orientation) {
                throw new IllegalArgumentException("Vectors must have the same orientation to add.");
            }
            if (this.length() != other.length()) {
                throw new IllegalArgumentException("Vectors must be of the same length to add.");
            }
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] = this.vecotr[i] + other.get(i);
            }
        } 
        finally {
            lock.writeUnlock(); // Release write lock
        }
        
    }

    public void negate() {
        lock.writeLock();
        try{
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] = -1 * this.vector[i];
            }
        } 
        finally {
            lock.writeUnlock(); // Release write lock
        }
    }

    public double dot(SharedVector other) {
        if (other == null) {
                throw new IllegalArgumentException("Cannot compute dot product with a null vector.");
            }
        try{
           
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
    }
}
