package scheduler.disk;

/**
 * Shortest Seek Time First disk scheduling.
 * Owner: [member name]
 * The request closest to the head is served next. Ties go to the lower cylinder.
 * Duplicate requests are served one after another (zero movement).
 */
public class SSTF implements DiskScheduler {
    public String getName()       { return "Shortest Seek Time First (SSTF)"; }
    public boolean usesDirection() { return false; }

    public DiskResult schedule(int[] requests, int head, int diskSize, boolean movingUp) {
        int n = requests.length;
        DiskResult result = new DiskResult(head, n);
        boolean[] served = new boolean[n];
        int pos = head;
        for (int step = 0; step < n; step++) {
            int pick = -1;
            for (int i = 0; i < n; i++) {
                if (served[i]) continue;
                if (pick == -1) { pick = i; continue; }
                int d = Math.abs(requests[i] - pos), best = Math.abs(requests[pick] - pos);
                if (d < best || (d == best && requests[i] < requests[pick])) pick = i;  // tie: lower cylinder
            }
            served[pick] = true;
            pos = requests[pick];
            result.addStep(pos, DiskResult.REQUEST);
        }
        return result;
    }
}
