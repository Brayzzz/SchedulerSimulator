package scheduler.ui;

import scheduler.disk.DiskResult;

import javax.swing.*;
import java.awt.*;

/** Graph of the head's path: x = cylinder, y = step (top to bottom). Jumps are dashed grey. */
class DiskGraph extends JPanel {
    private static final Stroke DASHED = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 5f}, 0f);
    private DiskResult result;
    private int size;

    void setResult(DiskResult r, int diskSize) {
        result = r;
        size = diskSize;
        setPreferredSize(r == null ? new Dimension(300, 100) : new Dimension(300, 100 + r.steps.size() * 40));
        revalidate();
        repaint();
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (result == null) return;
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke normal = g.getStroke();
        int w = getWidth() - 80;
        int span = Math.max(1, size - 1);

        // Axis line with a tick and label every few cylinders
        int tick = Math.max(10, ((size / 20 + 9) / 10) * 10);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 10f));
        g.setColor(Color.BLACK);
        g.drawLine(40, 28, 40 + w, 28);
        for (int c = 0; c < size; c += tick) {
            int tx = 40 + c * w / span;
            g.setColor(Color.BLACK);
            g.drawLine(tx, 23, tx, 33);
            g.setColor(Color.ORANGE.darker());
            g.drawString(String.valueOf(c), tx - 8, 18);
        }
        g.setColor(Color.BLACK);
        g.drawLine(40 + w, 23, 40 + w, 33);                 // end of the disk (size - 1)
        g.drawString(String.valueOf(size - 1), 40 + w - 8, 18);

        g.setFont(g.getFont().deriveFont(Font.PLAIN, 12f));
        int[] path = result.path();
        int[] types = result.pathTypes();
        for (int i = 0; i < path.length; i++) {
            int x = 40 + path[i] * w / span;
            int y = 60 + i * 40;
            if (i > 0) {
                int px = 40 + path[i - 1] * w / span;
                boolean jump = types[i] == DiskResult.JUMP;
                g.setColor(jump ? Color.GRAY : Color.BLUE);
                g.setStroke(jump ? DASHED : normal);
                g.drawLine(px, y - 40, x, y);
                g.setStroke(normal);
                g.setColor(jump ? Color.GRAY : Color.RED);
                g.drawString((jump ? "jump +" : "+") + Math.abs(path[i] - path[i - 1]), (px + x) / 2 + 5, y - 15);
            }
            Color dot = types[i] == DiskResult.START ? Color.GREEN.darker()
                      : types[i] == DiskResult.EDGE ? Color.ORANGE
                      : types[i] == DiskResult.JUMP ? Color.GRAY : Color.RED;
            g.setColor(dot);
            g.fillOval(x - 4, y - 4, 8, 8);
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(path[i]), x + 8, y - 5);
        }
    }
}
