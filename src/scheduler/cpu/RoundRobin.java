package scheduler.cpu;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;

/**
 * Round Robin (preemptive) CPU Scheduling.
 * Owner: Jeiron Carl Ramos (Process Zero)
 * If a new arrival and a preempted process reach the queue at the same instant, the new arrival goes first.
 */
public class RoundRobin implements CpuScheduler {
    public String getName()      { return "Round Robin"; }
    public boolean usesPriority() { return false; }
    public boolean usesQuantum()  { return true; }

    public CpuResult schedule(List<Proc> input, int quantum) {
        List<Proc> procs = Proc.copyAll(input);
        CpuResult result = new CpuResult(procs);
        int n = procs.size();

        // Arrival order: earlier arrival first, then lower PID
        List<Proc> order = new ArrayList<>(procs);
        order.sort(Comparator.comparingInt((Proc p) -> p.arrival).thenComparingInt(p -> p.pid));
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) remaining[i] = order.get(i).burst;

        Queue<Integer> ready = new LinkedList<>();      // indexes into 'order'
        int time = 0, finished = 0, arrived = 0;
        while (finished < n) {
            arrived = enqueueArrivals(order, ready, arrived, time);
            if (ready.isEmpty()) {                      // nobody is ready: CPU is idle
                int next = order.get(arrived).arrival;
                result.addSlice(Slice.IDLE, time, next);
                time = next;
                continue;
            }
            int i = ready.poll();                       // first process in the queue
            Proc p = order.get(i);
            if (p.firstRun == -1) p.firstRun = time;    // remember first CPU time
            int used = Math.min(quantum, remaining[i]); // run one quantum or until done
            result.addSlice(p.pid, time, time + used);
            time += used;
            remaining[i] -= used;
            arrived = enqueueArrivals(order, ready, arrived, time);  // new arrivals go in BEFORE...
            if (remaining[i] > 0) ready.add(i);                      // ...the preempted process
            else { p.completion = time; finished++; }
        }
        result.totalTime = time;
        return result;
    }

    /** Queues every process that has arrived by 'time'; returns how many have arrived so far. */
    private int enqueueArrivals(List<Proc> order, Queue<Integer> ready, int arrived, int time) {
        while (arrived < order.size() && order.get(arrived).arrival <= time) ready.add(arrived++);
        return arrived;
    }
}
