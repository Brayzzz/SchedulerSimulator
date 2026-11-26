package scheduler.ui;

import scheduler.cpu.CpuResult;
import scheduler.cpu.Slice;

import javax.swing.*;
import java.awt.*;

/** Draws the Gantt chart on a time scale: block width is proportional to run time. IDLE is grey. */
class GanttPanel extends JPanel {
    private static final Color[] COLORS = {
        new Color(0x9EC9F5), new Color(0xF5C48E), new Color(0xA8E0A0), new Color(0xF2A6B3),
        new Color(0xD0B3F0), new Color(0xF2E58A), new Color(0x8FDAD2), new Color(0xE0B59B)
    };
    private static final int LEFT = 15, TOP = 15, BAR_H = 44;
    private CpuResult result;

    void setResult(CpuResult r) {
        result = r;
        int w = r == null ? 100 : LEFT * 2 + r.totalTime * pxPerUnit(r.totalTime);
        setPreferredSize(new Dimension(w, BAR_H + 50));
        revalidate();
        repaint();
    }

    private static int pxPerUnit(int total) { return Math.max(14, Math.min(40, 900 / Math.max(1, total))); }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (result == null) return;
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g.getFontMetrics();
        int ppu = pxPerUnit(result.totalTime);
        int lastLabelRight = -100;
        for (Slice s : result.gantt) {
            int x1 = LEFT + s.start * ppu, x2 = LEFT + s.end * ppu;
            g.setColor(s.isIdle() ? new Color(0xDDDDDD) : COLORS[s.pid % COLORS.length]);
            g.fillRect(x1, TOP, x2 - x1, BAR_H);
            g.setColor(Color.BLACK);
            g.drawRect(x1, TOP, x2 - x1, BAR_H);
            String label = s.label();
            int lw = fm.stringWidth(label);
            if (lw + 4 <= x2 - x1) g.drawString(label, x1 + (x2 - x1 - lw) / 2, TOP + BAR_H / 2 + fm.getAscent() / 2 - 2);
            lastLabelRight = drawTime(g, fm, s.start, x1, lastLabelRight);
        }
        drawTime(g, fm, result.totalTime, LEFT + result.totalTime * ppu, lastLabelRight);
    }

    /** Tick mark plus time label under the bar; the label is skipped if it would overlap the previous one. */
    private int drawTime(Graphics2D g, FontMetrics fm, int time, int x, int lastRight) {
        g.setColor(Color.BLACK);
        g.drawLine(x, TOP + BAR_H, x, TOP + BAR_H + 5);
        String t = String.valueOf(time);
        int tw = fm.stringWidth(t);
        if (x - tw / 2 > lastRight + 4) {
            g.drawString(t, x - tw / 2, TOP + BAR_H + 20);
            return x + tw / 2;
        }
        return lastRight;
    }
}
