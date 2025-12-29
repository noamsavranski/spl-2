package scheduling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
public class TiredExecuterTest {
    @Test
    void constructor_invalidNumThreads_throws() {
        assertThrows(IllegalArgumentException.class, () -> new TiredExecutor(0));
        assertThrows(IllegalArgumentException.class, () -> new TiredExecutor(-2));
    }
    @Test
    void submit_nullTask_throws() throws Exception {
        TiredExecutor executor = new TiredExecutor(2);
        assertThrows(IllegalArgumentException.class, () -> executor.submit(null));
        executor.shutdown();
    }

    @Test
    void submit_tasksExecute() throws Exception {
        TiredExecutor executor = new TiredExecutor(3);
        CountDownLatch latch = new CountDownLatch(1);
        executor.submit(latch::countDown);
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Task did not run in time");
        executor.shutdown();
    }
    @Test
    void submit_blocksWhenNoIdleWorkers() throws Exception {
        TiredExecutor ex = new TiredExecutor(1);
        CountDownLatch hold = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);

        ex.submit(() -> {
            started.countDown();
            try {
                 hold.await();
            } 
            catch (InterruptedException ignored) {}
        });

        assertTrue(started.await(1, TimeUnit.SECONDS), "First task did not start");
        CountDownLatch secondRan = new CountDownLatch(1);
        Thread submitThread = new Thread(() -> ex.submit(secondRan::countDown));
        submitThread.start();
        Thread.sleep(50);
        assertTrue(submitThread.isAlive(), "submit should be blocked (no idle workers)");
        hold.countDown();
        assertTrue(secondRan.await(1, TimeUnit.SECONDS), "Second task did not run after worker freed");
        submitThread.join(1000);
        assertFalse(submitThread.isAlive(), "submit thread did not finish");
        ex.shutdown();
    }

    @Test
    void submitAll_allTasksExecutedExactlyOnce() throws Exception {
        TiredExecutor ex = new TiredExecutor(3);
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            tasks.add(counter::incrementAndGet);
        }
        ex.submitAll(tasks);
        assertEquals(50, counter.get(), "Not all tasks executed exactly once");
        ex.shutdown();
    }
    @Test
    void getWorkerReport_returnsReadableReport() throws Exception {
        TiredExecutor ex = new TiredExecutor(2);
        String report = ex.getWorkerReport();
        assertNotNull(report);
        assertTrue(report.contains("TiredExecutor Worker Report"), "Report header missing");
        assertTrue(report.contains("inFlight"), "Report should include inFlight");
        assertTrue(report.contains("Worker #0"), "Report should include worker 0");
        assertTrue(report.contains("Worker #1"), "Report should include worker 1");
        ex.shutdown();
    }
    @Test
    void submitAll_waitsUntilFinish() throws Exception {
        TiredExecutor ex = new TiredExecutor(2);
        CountDownLatch hold = new CountDownLatch(1);
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> { try { hold.await(); } catch (InterruptedException ignored) {} });
        tasks.add(() -> {});
        tasks.add(() -> {});
        Thread t = new Thread(() -> ex.submitAll(tasks));
        t.start();
        Thread.sleep(50);
        assertTrue(t.isAlive(), "submitAll returned too early");
        hold.countDown();
        t.join(1000);
        assertFalse(t.isAlive(), "submitAll did not return after tasks finished");
        ex.shutdown();
    }
    @Test
    void shutdown_waitsForRunningTasks() throws Exception {
        TiredExecutor ex = new TiredExecutor(1);
        CountDownLatch hold = new CountDownLatch(1);
        ex.submit(() -> { try { hold.await(); } catch (InterruptedException ignored) {} });
        Thread t = new Thread(() -> {
            try { ex.shutdown(); } catch (InterruptedException ignored) {}
        });
        t.start();
        Thread.sleep(50);
        assertTrue(t.isAlive(), "shutdown returned too early");
        hold.countDown();
        t.join(2000);
        assertFalse(t.isAlive(), "shutdown did not finish");
    }
}
