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
        readLock();
        try {
           if (index < 0 || index >= vector.length) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds.");
            }
            return vector[index];
        } 
        finally {
            readUnlock(); // Release read lock
        }
    }

    public int length() {
        readLock();
        try {
            return vector.length;
        } 
        finally {
            readUnlock(); // Release read lock
        }
    }

    public VectorOrientation getOrientation() {
        readLock();
        try {
            return orientation;
        } 
        finally {
            readUnlock(); // Release read lock
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
        writeLock();
        try{
            if (orientation == VectorOrientation.ROW_MAJOR) {
                orientation = VectorOrientation.COLUMN_MAJOR;
            } 
            else {
                orientation = VectorOrientation.ROW_MAJOR;
            }
        } 
        finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    public void add(SharedVector other) {
        if (other == null) {
                throw new IllegalArgumentException("Cannot add a null vector.");
            }
        
        if (System.identityHashCode(this) > System.identityHashCode(other)) {
            other.readLock();
            try {
                writeLock();
                try{
                    if (this.orientation != other.orientation) {
                        throw new IllegalArgumentException("Vectors must have the same orientation to add.");
                    }
                    if (this.length() != other.length()) {
                        throw new IllegalArgumentException("Vectors must be of the same length to add.");
                    }
                    for (int i = 0; i < this.length(); i++) {
                        this.vector[i] = this.vector[i] + other.get(i);
                    }
                } 
                finally {
                    writeUnlock(); // Release write lock
                }
            }
            finally {
                other.readUnlock(); // Release read lock
                }
        }
        else if (System.identityHashCode(this) < System.identityHashCode(other)) { 
            writeLock();
            try {
                other.readLock();
                try{
                    if (this.orientation != other.orientation) {
                        throw new IllegalArgumentException("Vectors must have the same orientation to add.");
                    }
                    if (this.length() != other.length()) {
                        throw new IllegalArgumentException("Vectors must be of the same length to add.");
                    }
                    for (int i = 0; i < this.length(); i++) {
                        this.vector[i] = this.vector[i] + other.get(i);
                    }
                } 
                finally {
                    other.readUnlock(); // Release read lock
                }
            }
            finally {
                writeUnlock(); // Release write lock
            }
        }
        else  {
        synchronized (SharedVector.class) {
            writeLock();
            try {
                other.readLock();
                try {
                    
                    if (this.orientation != other.orientation) {
                        throw new IllegalArgumentException("Vectors must have the same orientation to add.");
                    }
                    if (this.length() != other.length()) {
                        throw new IllegalArgumentException("Vectors must be of the same length to add.");
                    }
                    for (int i = 0; i < this.length(); i++) {
                        this.vector[i] = this.vector[i] + other.get(i);
                    }
                } finally {
                    other.readUnlock();
                }
            } finally {
                writeUnlock();
            }
        }
    }
}


    public void negate() {
        writeLock();
        try{
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] = -1 * this.vector[i];
            }
        } 
        finally {
            writeUnlock(); // Release write lock
        }
    }

    public double dot(SharedVector other) {
        if (other == null) {
                throw new IllegalArgumentException("Cannot compute dot product with a null vector.");
            }
        if (System.identityHashCode(this) > System.identityHashCode(other)) {
            other.readLock();
            try {
                writeLock();
                try{
                    if (this.orientation != other.orientation) {
                        throw new IllegalArgumentException("Vectors must have the same orientation to add.");
                    }
                    if (this.length() != other.length()) {
                        throw new IllegalArgumentException("Vectors must be of the same length to add.");
                    }
                    double sum = 0.0;
                    for (int i = 0; i < this.length(); i++) {
                        sum = sum + (this.vector[i] * other.get(i));
                    }
                    return sum;
                } 
                finally {
                    writeUnlock(); // Release write lock
                }
            }
            finally {
                other.readUnlock(); // Release read lock
                }
        }else if (System.identityHashCode(this) < System.identityHashCode(other)) { 
            writeLock();
            try {
                other.readLock();
                try{
                    if (this.orientation != other.orientation) {
                        throw new IllegalArgumentException("Vectors must have the same orientation to add.");
                    }
                    if (this.length() != other.length()) {
                        throw new IllegalArgumentException("Vectors must be of the same length to add.");
                    }
                    double sum = 0.0;
                    for (int i = 0; i < this.length(); i++) {
                        sum = sum + (this.vector[i] * other.get(i));
                    }
                    return sum;
                } 
                finally {
                    other.readUnlock(); // Release read lock
                }
            }
            finally {
                writeUnlock(); // Release write lock
            }
        }
        else  {
        synchronized (SharedVector.class) {
            writeLock();
            try {
                other.readLock();
                try {
                    
                    if (this.orientation != other.orientation) {
                        throw new IllegalArgumentException("Vectors must have the same orientation to add.");
                    }
                    if (this.length() != other.length()) {
                        throw new IllegalArgumentException("Vectors must be of the same length to add.");
                    }
                    double sum = 0.0;
                    for (int i = 0; i < this.length(); i++) {
                        sum = sum + (this.vector[i] * other.get(i));
                    }
                    return sum;
                } finally {
                    other.readUnlock();
                }
            } finally {
                writeUnlock();
            }
        }
    }


    //compute row-vector × matrix
}    public void vecMatMul(SharedMatrix matrix) {
        if (matrix == null) {
        throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (matrix.length() == 0) {
            throw new IllegalArgumentException("Matrix cannot be empty");
        }
        if (System.identityHashCode(this) < System.identityHashCode(matrix)) {
            this.writeLock();
            try{
             for (int i = 0; i < matrix.length(); i++) matrix.get(i).readLock();
                try{
                    if (this.length() != matrix.length()) {
                        throw new IllegalArgumentException("Vector length must match the number of matrix rows for multiplication.");
                    }
                    double[] result = new double[matrix.get(0).length()];
                    for (int j = 0; j < matrix.get(0).length(); j++) {
                        double sum = 0.0;
                        for (int i = 0; i < matrix.length(); i++) {
                            sum += this.vector[i] * matrix.get(i).get(j);
                        }
                        result[j] = sum;
                    }
                    this.vector = result;
                    this.orientation = VectorOrientation.ROW_MAJOR;
                } 
                finally {
                  for (int i = 0; i < matrix.length(); i++) matrix.get(i).readUnlock();
                }
            }
            finally {
                this.writeUnlock();
            }
        } 
        else if (System.identityHashCode(this) > System.identityHashCode(matrix)) {
           for (int i = 0; i < matrix.length(); i++) matrix.get(i).readLock();
            try{
                this.writeLock();
                try{
                    if (this.length() != matrix.length()) {
                        throw new IllegalArgumentException("Vector length must match the number of matrix rows for multiplication.");
                    }
                    double[] result = new double[matrix.get(0).length()];
                    for (int j = 0; j < matrix.get(0).length(); j++) {
                        double sum = 0.0;
                        for (int i = 0; i < matrix.length(); i++) {
                            sum += this.vector[i] * matrix.get(i).get(j);
                        }
                        result[j] = sum;
                    }
                    this.vector = result;
                    this.orientation = VectorOrientation.ROW_MAJOR;
                } 
                finally {
                    this.writeUnlock();
                }
            }
            finally {
               for (int i = 0; i < matrix.length(); i++) matrix.get(i).readUnlock();
            }
        }
        else {
            synchronized(SharedVector.class) {
                this.writeLock();
                try{
                   for (int i = 0; i < matrix.length(); i++) matrix.get(i).readLock();
                    try{
                        if (this.length() != matrix.length()) {
                            throw new IllegalArgumentException("Vector length must match the number of matrix rows for multiplication.");
                        }
                        double[] result = new double[matrix.get(0).length()];
                        for (int j = 0; j <matrix.get(0).length(); j++) {
                            double sum = 0.0;
                            for (int i = 0; i < matrix.length(); i++) {
                                sum += this.vector[i] * matrix.get(i).get(j);
                            }
                            result[j] = sum;
                        }
                        this.vector = result;
                        this.orientation = VectorOrientation.ROW_MAJOR;
                    }
                    finally {
                       for (int i = 0; i < matrix.length(); i++) matrix.get(i).readUnlock();
                    }
                }
                finally {
                    this.writeUnlock();
                }
            }
        }
    }
}

