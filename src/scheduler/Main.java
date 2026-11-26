package scheduler;

import scheduler.ui.MainMenu;

import javax.swing.*;

/** Entry point of the executable JAR. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
