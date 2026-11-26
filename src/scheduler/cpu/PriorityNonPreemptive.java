package scheduler.cpu;

import java.util.ArrayList;
import java.util.List;

/**
 * Priority (Non-Preemptive) CPU Scheduling.
 * Owner: [member name]
 * Lower number = higher priority. Ties: earlier arrival, then lower PID. No aging.
 */
public class PriorityNonPreemptive implements CpuScheduler {
    public String getName()      { return "Priority (Non-Preemptive)"; }
    public boolean usesPriority() { return true; }
    public boolean usesQuantum()  { return false; }

    public CpuResult schedule(List<Proc> input, int quantum) {
        List<Proc> procs = Proc.copyAll(input);
        CpuResult result = new CpuResult(procs);
        List<Proc> pending = new ArrayList<>(procs);
        int time = 0;
        while (!pending.isEmpty()) {
            Proc next = pickBest(pending, time);
            if (next == null) {                        // nobody is ready: CPU is idle
                int nextArrival = Integer.MAX_VALUE;
                for (Proc p : pending) nextArrival = Math.min(nextArrival, p.arrival);
                result.addSlice(Slice.IDLE, time, nextArrival);
                time = nextArrival;
                continue;
            }
            next.firstRun = time;
            result.addSlice(next.pid, time, time + next.burst);
            time += next.burst;
            next.completion = time;
            pending.remove(next);
        }
        result.totalTime = time;
        return result;
    }

    /** The arrived process with the best priority. Ties: earlier arrival, then lower PID. */
    private Proc pickBest(List<Proc> pending, int time) {
        Proc best = null;
        for (Proc p : pending) {
            if (p.arrival > time) continue;
            if (best == null || p.priority < best.priority
                    || (p.priority == best.priority && p.arrival < best.arrival)
                    || (p.priority == best.priority && p.arrival == best.arrival && p.pid < best.pid)) {
                best = p;
            }
        }
        return best;
    }
}
