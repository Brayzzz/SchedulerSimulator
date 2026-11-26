package scheduler.ui;

import scheduler.disk.DiskResult;
import scheduler.disk.DiskScheduler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** One screen that serves any DiskScheduler: inputs, step-by-step computation and the head-path graph. */
class DiskPanel extends JPanel {
    private static final int MAX_REQUESTS = 50;

    private final DiskScheduler scheduler;
    private final String sampleHead, sampleSize, sampleRequests;
    private final boolean sampleUp;

    private final JTextField headField = new JTextField(5);
    private final JTextField sizeField = new JTextField("200", 5);
    private final JTextField reqField = new JTextField(30);
    private final JComboBox<String> direction = new JComboBox<>(new String[]{"Toward higher cylinders", "Toward lower cylinders"});
    private final JTextArea output = new JTextArea();
    private final DiskGraph graph = new DiskGraph();

    DiskPanel(DiskScheduler scheduler, String head, String size, String requests, boolean up, Runnable onBack) {
        this.scheduler = scheduler;
        this.sampleHead = head;
        this.sampleSize = size;
        this.sampleRequests = requests;
        this.sampleUp = up;

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Disk Scheduling: " + scheduler.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controls = inputs;
        controls.add(new JLabel("Head:"));        controls.add(headField);
        controls.add(new JLabel("Disk size:"));   controls.add(sizeField);
        controls.add(new JLabel("Requests:"));    controls.add(reqField);
        if (scheduler.usesDirection()) {
            controls.add(new JLabel("Direction:"));
            controls.add(direction);
        }
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton compute = new JButton("Compute");
        compute.addActionListener(e -> compute());
        JButton load = new JButton("Load Sample Data");
        load.addActionListener(e -> loadSample());
        JButton reset = new JButton("Reset / Clear");
        reset.addActionListener(e -> reset());
        JButton back = new JButton("Back to Menu");
        back.addActionListener(e -> onBack.run());
        buttons.add(compute);
        buttons.add(load);
        buttons.add(reset);
        buttons.add(back);

        JPanel north = new JPanel(new BorderLayout());
        north.add(title, BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(2, 1));
        rows.add(inputs);
        rows.add(buttons);
        north.add(rows, BorderLayout.CENTER);

        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(output), new JScrollPane(graph));
        center.setResizeWeight(0.4);
        center.setDividerLocation(430);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void compute() {
        int head, size;
        int[] req;
        try {
            size = Validator.readInt(sizeField.getText(), "Disk size", 1, 10000);
            head = Validator.readInt(headField.getText(), "Head position", 0, size - 1);
            String[] parts = reqField.getText().trim().split("[,\\s]+");
            if (parts.length > MAX_REQUESTS) throw new IllegalArgumentException("Enter 1 to " + MAX_REQUESTS + " requests.");
            req = new int[parts.length];
            for (int i = 0; i < parts.length; i++) req[i] = Validator.readInt(parts[i], "Request " + (i + 1), 0, size - 1);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean up = direction.getSelectedIndex() == 0;
        DiskResult r = scheduler.schedule(req, head, size, up);
        showText(r, req, up);
        graph.setResult(r, size);
    }

    private void showText(DiskResult r, int[] req, boolean up) {
        StringBuilder sb = new StringBuilder("Movement = |To - From| for each step\n");
        if (scheduler.usesDirection()) sb.append("Initial direction: ").append(up ? "toward higher" : "toward lower").append(" cylinders\n");
        sb.append("\nSeek sequence: ");
        int[] path = r.path();
        for (int i = 0; i < path.length; i++) sb.append(i == 0 ? "" : " -> ").append(path[i]);
        sb.append("\n\n");
        int running = 0;
        StringBuilder sum = new StringBuilder();
        for (int i = 0; i < r.steps.size(); i++) {
            DiskResult.Step s = r.steps.get(i);
            running += s.movement;
            String note = s.type == DiskResult.EDGE ? "  [end of disk]" : s.type == DiskResult.JUMP ? "  [jump]" : "";
            sb.append("Step ").append(i + 1).append(": |").append(s.to).append(" - ").append(s.from).append("| = ")
              .append(s.movement).append("  (total = ").append(running).append(")").append(note).append("\n");
            sum.append(i == 0 ? "" : " + ").append(s.movement);
        }
        sb.append("\nTotal head movement:\n  = ").append(sum).append("\n  = ").append(r.totalMovement()).append(" cylinders\n");
        sb.append(String.format("%nAverage seek distance:%n  = %d / %d = %.2f cylinders", r.totalMovement(), req.length, r.averageSeek()));
        output.setText(sb.toString());
        output.setCaretPosition(0);
    }

    private void loadSample() {
        headField.setText(sampleHead);
        sizeField.setText(sampleSize);
        reqField.setText(sampleRequests);
        direction.setSelectedIndex(sampleUp ? 0 : 1);
        compute();
    }

    private void reset() {
        headField.setText("");
        sizeField.setText("200");
        reqField.setText("");
        direction.setSelectedIndex(0);
        output.setText("");
        graph.setResult(null, 0);
    }
}
