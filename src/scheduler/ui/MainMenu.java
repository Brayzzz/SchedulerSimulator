package scheduler.ui;

import scheduler.cpu.PriorityNonPreemptive;
import scheduler.cpu.RoundRobin;
import scheduler.disk.CScan;
import scheduler.disk.SSTF;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/** Main window: a menu card plus one card per algorithm, all in the same frame. */
public class MainMenu extends JFrame {
    // TODO: fill these in before submitting (shown on the About screen)
    private static final String GROUP_NO = "[Group No.]";
    private static final String[] MEMBERS = {"[Member 1]", "[Member 2]", "[Member 3]", "[Member 4]"};

    // Practice data. Replace with the facilitator's practice problem sets.
    private static final int[][] PRIORITY_SAMPLE = {{1, 0, 8, 3}, {2, 1, 4, 1}, {3, 2, 9, 4}, {4, 3, 5, 2}, {5, 30, 3, 1}, {6, 31, 2, 2}};
    private static final int[][] RR_SAMPLE = {{1, 0, 5}, {2, 1, 3}, {3, 2, 8}, {4, 3, 2}, {5, 30, 4}};
    private static final String DISK_REQUESTS = "98, 183, 37, 122, 14, 124, 65, 67";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public MainMenu() {
        super("CPU and Disk Scheduling Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Runnable back = () -> cards.show(root, "menu");

        root.add(menuCard(), "menu");
        root.add(new CpuPanel(new PriorityNonPreemptive(), PRIORITY_SAMPLE, 0, back), "priority");
        root.add(new CpuPanel(new RoundRobin(), RR_SAMPLE, 3, back), "rr");
        root.add(new DiskPanel(new SSTF(), "53", "200", DISK_REQUESTS, true, back), "sstf");
        root.add(new DiskPanel(new CScan(), "53", "200", DISK_REQUESTS, true, back), "cscan");
        add(root);
        setSize(1150, 720);
        setLocationRelativeTo(null);
    }

    private JPanel menuCard() {
        JPanel menu = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel("CPU and Disk Scheduling Simulator", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        c.gridy = 0;
        menu.add(title, c);

        c.gridy = 1;
        menu.add(group("CPU Scheduling",
                button("Priority (Non-Preemptive)", "priority"), button("Round Robin", "rr")), c);
        c.gridy = 2;
        menu.add(group("Disk Scheduling",
                button("SSTF (Shortest Seek Time First)", "sstf"), button("C-SCAN", "cscan")), c);

        JButton about = new JButton("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Group No.: " + GROUP_NO + "\n\nMembers:\n" + String.join("\n", MEMBERS),
                "About", JOptionPane.INFORMATION_MESSAGE));
        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> System.exit(0));
        JPanel bottom = new JPanel(new GridLayout(1, 2, 10, 10));
        bottom.add(about);
        bottom.add(exit);
        c.gridy = 3;
        menu.add(bottom, c);
        return menu;
    }

    private JPanel group(String name, JButton... buttons) {
        JPanel p = new JPanel(new GridLayout(buttons.length, 1, 8, 8));
        p.setBorder(new TitledBorder(name));
        for (JButton b : buttons) p.add(b);
        return p;
    }

    private JButton button(String text, String card) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(15f));
        b.addActionListener(e -> cards.show(root, card));
        return b;
    }
}
