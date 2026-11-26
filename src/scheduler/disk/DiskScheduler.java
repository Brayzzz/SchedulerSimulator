package scheduler.disk;

/** Shared interface: every disk algorithm plugs into the program through this. */
public interface DiskScheduler {
    String getName();
    boolean usesDirection();   // enable the initial-direction choice?
    DiskResult schedule(int[] requests, int head, int diskSize, boolean movingUp);
}
