import gui.DashboardFrame;
import javax.swing.SwingUtilities;

// Application entry point
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard = new DashboardFrame();
            dashboard.setVisible(true);
        });
    }
}