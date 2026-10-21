package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import service.GymService;

// Main Dashboard Window (Black and White GUI only)
public class DashboardFrame extends JFrame {

    private final GymService gymService;

    private CardLayout cardLayout;
    private JPanel centerCardPanel;

    private RegisterMemberPanel registerPanel;
    private ViewMembersPanel viewPanel;
    private SearchMemberPanel searchPanel;
    private LockerManagementPanel lockerPanel;
    private UpdateStatusPanel statusPanel;
    private RenewMembershipPanel renewPanel;

    public DashboardFrame() {
        super("GYM MEMBERSHIP TRACKING AND MANAGEMENT SYSTEM");
        this.gymService = new GymService();

        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initGUI();

        // Safely close DB connection when window exits
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                database.DatabaseConnection.getInstance().closeConnection();
            }
        });
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header Panel (Black border, white background, black text)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new LineBorder(Color.BLACK, 1));

        JLabel lblHeading = new JLabel("GYM MEMBERSHIP TRACKING AND MANAGEMENT SYSTEM");
        lblHeading.setForeground(Color.BLACK);
        headerPanel.add(lblHeading);
        add(headerPanel, BorderLayout.NORTH);

        // Navigation Bar
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new LineBorder(Color.BLACK, 1));

        JButton btnRegister = createNavButton("Register Member");
        JButton btnView = createNavButton("View Members");
        JButton btnSearch = createNavButton("Search Member");
        JButton btnLocker = createNavButton("Locker Management");
        JButton btnStatus = createNavButton("Update Status");
        JButton btnRenew = createNavButton("Renew Membership");

        navPanel.add(btnRegister);
        navPanel.add(btnView);
        navPanel.add(btnSearch);
        navPanel.add(btnLocker);
        navPanel.add(btnStatus);
        navPanel.add(btnRenew);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(navPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center Multi-Panel (CardLayout)
        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setBackground(Color.WHITE);

        registerPanel = new RegisterMemberPanel(gymService);
        viewPanel = new ViewMembersPanel(gymService);
        searchPanel = new SearchMemberPanel(gymService);
        lockerPanel = new LockerManagementPanel(gymService);
        statusPanel = new UpdateStatusPanel(gymService);
        renewPanel = new RenewMembershipPanel(gymService);

        centerCardPanel.add(registerPanel, "REGISTER");
        centerCardPanel.add(viewPanel, "VIEW");
        centerCardPanel.add(searchPanel, "SEARCH");
        centerCardPanel.add(lockerPanel, "LOCKER");
        centerCardPanel.add(statusPanel, "STATUS");
        centerCardPanel.add(renewPanel, "RENEW");

        add(centerCardPanel, BorderLayout.CENTER);

        // Navigation Button Action Listeners with Auto-Refresh
        btnRegister.addActionListener(e -> showCard("REGISTER"));
        btnView.addActionListener(e -> {
            viewPanel.refreshTable();
            showCard("VIEW");
        });
        btnSearch.addActionListener(e -> {
            searchPanel.executeSearch();
            showCard("SEARCH");
        });
        btnLocker.addActionListener(e -> {
            lockerPanel.refreshLockerTable();
            showCard("LOCKER");
        });
        btnStatus.addActionListener(e -> {
            statusPanel.refreshStatusTable();
            showCard("STATUS");
        });
        btnRenew.addActionListener(e -> {
            renewPanel.refreshDueTable();
            showCard("RENEW");
        });

        // Default to Register card
        showCard("REGISTER");
    }

    private void showCard(String cardName) {
        cardLayout.show(centerCardPanel, cardName);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setFocusPainted(false);
        return btn;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}