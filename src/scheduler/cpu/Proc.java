package scheduler.cpu;

import java.util.ArrayList;
import java.util.List;

/** One process: the input values plus the times the scheduler fills in. */
public class Proc {
    public final int pid, arrival, burst, priority;
    public int firstRun = -1;   // first time the CPU runs this process
    public int completion;

    public Proc(int pid, int arrival, int burst, int priority) {
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
    }

    public int turnaround() { return completion - arrival; }
    public int waiting()    { return turnaround() - burst; }
    public int response()   { return firstRun - arrival; }

    /** Fresh copies with results cleared, so a scheduler never changes the caller's list. */
    static List<Proc> copyAll(List<Proc> in) {
        List<Proc> out = new ArrayList<>();
        for (Proc p : in) out.add(new Proc(p.pid, p.arrival, p.burst, p.priority));
        return out;
    }
}
