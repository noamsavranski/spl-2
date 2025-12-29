package scheduling;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;


public class TiredThreadTest {
    @Test
    void newTask_null_throwsIllegalArgument() {
        TiredThread t = new TiredThread(0, 1.0);
        assertThrows(IllegalArgumentException.class, () -> t.newTask(null));
    }


    @Test
    void newTask_runsTask() throws Exception {
        TiredThread t = new TiredThread(0, 1.0);
        t.start();
        CountDownLatch latch = new CountDownLatch(1);
        t.newTask(latch::countDown);
        assertTrue(latch.await(1, java.util.concurrent.TimeUnit.SECONDS),"Task did not run in time");
        t.shutdown();
        t.join(1000);
        assertFalse(t.isAlive(), "Thread did not terminate after shutdown");
    }

   @Test
    void newTask_whenQueueFull_throwsIllegalState() {
        TiredThread t = new TiredThread(1, 1.0);
        t.newTask(() -> {});
        assertThrows(IllegalStateException.class, () -> t.newTask(() -> {}));
    }

    @Test
    void shutdown_terminatesIdleThread() throws Exception {
        TiredThread t = new TiredThread(2, 1.0);
        t.start();
        t.shutdown();
        t.join(1000);
        assertFalse(t.isAlive(), "Thread did not terminate after shutdown");
        }
    @Test
    void compareTo_tieBreaksByIdWhenFatigueEqual() {
        TiredThread a = new TiredThread(0, 1.0);
        TiredThread b = new TiredThread(1, 1.0);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }
    
    @Test
    void run_updatesMetricsCorrectly() throws InterruptedException {
        TiredThread t = new TiredThread(3, 1.0);
        t.start();
        long initialIdle = t.getTimeIdle();

        // Run a task that takes 100ms
        CountDownLatch latch = new CountDownLatch(1);
        t.newTask(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            latch.countDown();
        });
        
        assertTrue(latch.await(1, java.util.concurrent.TimeUnit.SECONDS));
        
        // Ensure metrics moved
        assertTrue(t.getTimeUsed() > 0, "TimeUsed should increase after task");
        assertTrue(t.getTimeIdle() >= initialIdle, "TimeIdle should be tracked");
        t.shutdown();
    }
    

}
