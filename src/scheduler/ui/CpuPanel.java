package scheduler.ui;

import scheduler.cpu.CpuResult;
import scheduler.cpu.CpuScheduler;
import scheduler.cpu.Proc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** One screen that serves any CpuScheduler: input table, Gantt chart, results table and averages. */
class CpuPanel extends JPanel {
    private final CpuScheduler scheduler;
    private final int[][] sample;          // rows of {pid, arrival, burst, priority}
    private final int sampleQuantum;

    private final DefaultTableModel inputModel;
    private final JTable inputTable;
    private final JTextField countField = new JTextField("5", 4);
    private final JTextField quantumField = new JTextField(4);
    private final GanttPanel gantt = new GanttPanel();
    private final JTable resultTable = new JTable();
    private final JTextArea metrics = new JTextArea(6, 30);

    CpuPanel(CpuScheduler scheduler, int[][] sample, int sampleQuantum, Runnable onBack) {
        this.scheduler = scheduler;
        this.sample = sample;
        this.sampleQuantum = sampleQuantum;

        String[] cols = scheduler.usesPriority()
                ? new String[]{"PID", "Arrival Time", "Burst Time", "Priority"}
                : new String[]{"PID", "Arrival Time", "Burst Time"};
        inputModel = new DefaultTableModel(cols, 0);
        inputTable = new JTable(inputModel);
        inputTable.setRowHeight(22);

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("CPU Scheduling: " + scheduler.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("No. of processes:"));
        controls.add(countField);
        JButton setRows = new JButton("Set Rows");
        setRows.addActionListener(e -> applyRowCount());
        controls.add(setRows);
        if (scheduler.usesQuantum()) {
            controls.add(new JLabel("   Time quantum:"));
            controls.add(quantumField);
        }
        JButton run = new JButton("Run");
        run.addActionListener(e -> run());
        JButton load = new JButton("Load Sample Data");
        load.addActionListener(e -> loadSample());
        JButton reset = new JButton("Reset / Clear");
        reset.addActionListener(e -> reset());
        JButton back = new JButton("Back to Menu");
        back.addActionListener(e -> onBack.run());
        controls.add(Box.createHorizontalStrut(10));
        controls.add(run);
        controls.add(load);
        controls.add(reset);
        controls.add(back);

        JPanel north = new JPanel(new BorderLayout());
        north.add(title, BorderLayout.NORTH);
        north.add(controls, BorderLayout.CENTER);
        JScrollPane inputScroll = new JScrollPane(inputTable);
        inputScroll.setPreferredSize(new Dimension(400, 170));
        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(north, BorderLayout.NORTH);
        top.add(inputScroll, BorderLayout.CENTER);

        JScrollPane ganttScroll = new JScrollPane(gantt, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScroll.setBorder(new TitledBorder("Gantt Chart"));
        ganttScroll.setPreferredSize(new Dimension(400, 130));
        metrics.setEditable(false);
        metrics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JPanel results = new JPanel(new BorderLayout(6, 6));
        results.add(ganttScroll, BorderLayout.NORTH);
        results.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        results.add(new JScrollPane(metrics), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(results, BorderLayout.CENTER);
        setRowCount(5);
    }

    // ---- input table helpers ----

    private void stopEditing() {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
    }

    private String cell(int row, int col) {
        Object v = inputModel.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    /** Resizes the table; new rows get the next PID. Existing entries are kept. */
    private void setRowCount(int n) {
        stopEditing();
        inputModel.setRowCount(n);
        for (int r = 0; r < n; r++) if (inputModel.getValueAt(r, 0) == null) inputModel.setValueAt(r + 1, r, 0);
        countField.setText(String.valueOf(n));
    }

    private boolean applyRowCount() {
        try {
            setRowCount(Validator.readInt(countField.getText(), "Number of processes", 1, 100));
            return true;
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            return false;
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }

    // ---- buttons ----

    private void run() {
        if (!applyRowCount()) return;       // the field and the table always agree
        List<Proc> list = new ArrayList<>();
        int quantum = 1;
        try {
            Set<Integer> seen = new HashSet<>();
            for (int r = 0; r < inputModel.getRowCount(); r++) {
                String row = "Row " + (r + 1);
                int pid = Validator.readInt(cell(r, 0), row + " PID", 1, 9999);
                if (!seen.add(pid)) throw new IllegalArgumentException(row + " PID: PID " + pid + " is already used. PIDs must be unique.");
                int at = Validator.readInt(cell(r, 1), row + " Arrival Time", 0, 1000);
                int bt = Validator.readInt(cell(r, 2), row + " Burst Time", 1, 1000);
                int pr = scheduler.usesPriority() ? Validator.readInt(cell(r, 3), row + " Priority", 1, 100) : 0;
                list.add(new Proc(pid, at, bt, pr));
            }
            if (scheduler.usesQuantum()) quantum = Validator.readInt(quantumField.getText(), "Time quantum", 1, 1000);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            return;
        }
        show(scheduler.schedule(list, quantum));
    }

    private void loadSample() {
        setRowCount(sample.length);
        for (int r = 0; r < sample.length; r++)
            for (int c = 0; c < inputModel.getColumnCount(); c++) inputModel.setValueAt(sample[r][c], r, c);
        if (scheduler.usesQuantum()) quantumField.setText(String.valueOf(sampleQuantum));
        clearResults();
    }

    private void reset() {
        stopEditing();
        inputModel.setRowCount(0);
        setRowCount(5);
        quantumField.setText("");
        clearResults();
    }

    private void clearResults() {
        gantt.setResult(null);
        resultTable.setModel(new DefaultTableModel());
        metrics.setText("");
    }

    // ---- output ----

    private void show(CpuResult r) {
        gantt.setResult(r);
        boolean pr = scheduler.usesPriority();
        String[] cols = pr
                ? new String[]{"Process ID", "Arrival Time", "Burst Time", "Priority", "Completion Time", "Turn Around Time", "Waiting Time", "Response Time"}
                : new String[]{"Process ID", "Arrival Time", "Burst Time", "Completion Time", "Turn Around Time", "Waiting Time", "Response Time"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for (Proc p : r.processes) {
            List<Object> row = new ArrayList<>();
            row.add("P" + p.pid);
            row.add(p.arrival);
            row.add(p.burst);
            if (pr) row.add(p.priority);
            row.add(p.completion);
            row.add(p.turnaround());
            row.add(p.waiting());
            row.add(p.response());
            m.addRow(row.toArray());
        }
        resultTable.setModel(m);
        metrics.setText(String.format(
                "Avg. Completion Time:  %.2f ms%nAvg. Turn Around Time: %.2f ms%nAvg. Waiting Time:     %.2f ms%n"
              + "Avg. Response Time:    %.2f ms%nCPU Utilization:       %.2f%%%nThroughput:            %.4f processes/ms",
                r.avgCompletion(), r.avgTurnaround(), r.avgWaiting(), r.avgResponse(), r.utilization(), r.throughput()));
    }
}
