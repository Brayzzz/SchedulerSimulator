package scheduler.disk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * C-SCAN disk scheduling.
 * Owner: [member name]
 * The head serves requests in one direction up to the physical end of the disk, then jumps to the
 * opposite end and continues in the same direction. The jump is counted in the head movement.
 * The head always travels to the end; the jump is made only if requests are still waiting.
 */
public class CScan implements DiskScheduler {
    public String getName()       { return "C-SCAN"; }
    public boolean usesDirection() { return true; }

    public DiskResult schedule(int[] requests, int head, int diskSize, boolean movingUp) {
        DiskResult result = new DiskResult(head, requests.length);
        int[] sorted = requests.clone();
        Arrays.sort(sorted);

        int end = movingUp ? diskSize - 1 : 0;       // where the sweep stops
        int jumpTo = movingUp ? 0 : diskSize - 1;    // where the head lands after the jump

        // firstSweep: still ahead of the head, nearest first. afterJump: served after the jump.
        List<Integer> firstSweep = new ArrayList<>(), afterJump = new ArrayList<>();
        if (movingUp) {
            for (int v : sorted) (v >= head ? firstSweep : afterJump).add(v);          // both ascending
        } else {
            for (int i = sorted.length - 1; i >= 0; i--)                              // both descending
                (sorted[i] <= head ? firstSweep : afterJump).add(sorted[i]);
        }

        for (int v : firstSweep) result.addStep(v, DiskResult.REQUEST);
        if (result.position() != end) result.addStep(end, DiskResult.EDGE);
        if (!afterJump.isEmpty()) {
            result.addStep(jumpTo, DiskResult.JUMP);
            for (int v : afterJump) result.addStep(v, DiskResult.REQUEST);
        }
        return result;
    }
}
