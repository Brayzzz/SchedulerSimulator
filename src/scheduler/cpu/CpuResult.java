package scheduler.cpu;

import java.util.ArrayList;
import java.util.List;

/** Everything a CPU scheduler produces: the Gantt chart, per-process times and the averages. */
public class CpuResult {
    public final List<Proc> processes;            // in the order the user entered them
    public final List<Slice> gantt = new ArrayList<>();
    public int totalTime;

    CpuResult(List<Proc> processes) { this.processes = processes; }

    /** Adds a block; back-to-back blocks of the same process (or idle) are merged into one. */
    void addSlice(int pid, int start, int end) {
        if (!gantt.isEmpty()) {
            Slice last = gantt.get(gantt.size() - 1);
            if (last.pid == pid && last.end == start) {
                gantt.set(gantt.size() - 1, new Slice(pid, last.start, end));
                return;
            }
        }
        gantt.add(new Slice(pid, start, end));
    }

    public double avgCompletion() {
        double sum = 0;
        for (Proc p : processes) sum += p.completion;
        return sum / processes.size();
    }
    public double avgTurnaround() {
        double sum = 0;
        for (Proc p : processes) sum += p.turnaround();
        return sum / processes.size();
    }
    public double avgWaiting() {
        double sum = 0;
        for (Proc p : processes) sum += p.waiting();
        return sum / processes.size();
    }
    public double avgResponse() {
        double sum = 0;
        for (Proc p : processes) sum += p.response();
        return sum / processes.size();
    }
    /** Utilization = busy time / total time x 100. */
    public double utilization() {
        int busy = 0;
        for (Proc p : processes) busy += p.burst;
        return busy * 100.0 / totalTime;
    }
    /** Throughput = processes / total time. */
    public double throughput() { return processes.size() / (double) totalTime; }
}
