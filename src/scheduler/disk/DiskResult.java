package scheduler.disk;

import java.util.ArrayList;
import java.util.List;

/** The head's journey: each step records where it came from, where it went and how far. */
public class DiskResult {
    public static final int START = 0, REQUEST = 1, EDGE = 2, JUMP = 3;

    public static class Step {
        public final int from, to, movement, type;
        Step(int from, int to, int type) {
            this.from = from;
            this.to = to;
            this.movement = Math.abs(to - from);
            this.type = type;
        }
    }

    public final int head, requestCount;
    public final List<Step> steps = new ArrayList<>();
    private int position, total;

    DiskResult(int head, int requestCount) {
        this.head = head;
        this.requestCount = requestCount;
        this.position = head;
    }

    int position() { return position; }

    /** Moves the head to 'to'. type: REQUEST (served), EDGE (end of disk) or JUMP (C-SCAN return). */
    void addStep(int to, int type) {
        Step s = new Step(position, to, type);
        steps.add(s);
        total += s.movement;
        position = to;
    }

    public int totalMovement() { return total; }
    public double averageSeek() { return requestCount == 0 ? 0 : (double) total / requestCount; }

    /** Head positions in order, starting with the initial head position. */
    public int[] path() {
        int[] p = new int[steps.size() + 1];
        p[0] = head;
        for (int i = 0; i < steps.size(); i++) p[i + 1] = steps.get(i).to;
        return p;
    }

    /** Type of each point in path() (START for the first). */
    public int[] pathTypes() {
        int[] t = new int[steps.size() + 1];
        t[0] = START;
        for (int i = 0; i < steps.size(); i++) t[i + 1] = steps.get(i).type;
        return t;
    }
}
