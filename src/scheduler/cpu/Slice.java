package scheduler.cpu;

/** One block of the Gantt chart: a process (or IDLE when pid is -1) running from start to end. */
public class Slice {
    public static final int IDLE = -1;
    public final int pid, start, end;

    public Slice(int pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }

    public boolean isIdle() { return pid == IDLE; }
    public String label()   { return isIdle() ? "IDLE" : "P" + pid; }
}
