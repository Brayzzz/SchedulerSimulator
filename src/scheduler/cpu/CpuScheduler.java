package scheduler.cpu;

import java.util.List;

/** Shared interface: every CPU algorithm plugs into the program through this. */
public interface CpuScheduler {
    String getName();
    boolean usesPriority();   // show the Priority column?
    boolean usesQuantum();    // show the Time Quantum field?
    CpuResult schedule(List<Proc> input, int quantum);
}
