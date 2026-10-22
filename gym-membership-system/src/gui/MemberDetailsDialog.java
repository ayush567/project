package gui;

import exceptions.GymException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import model.Member;
import model.Renewal;
import service.GymService;

// Modal dialog to view complete member profile and renewal history (Black and White only)
public class MemberDetailsDialog extends JDialog {

    public MemberDetailsDialog(Frame owner, Member member, GymService service) {
        super(owner, "Member Details - " + member.getMemberId(), true);
        setSize(650, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Details Panel (Grid layout)
        JPanel detailsPanel = new JPanel(new GridLayout(12, 2, 5, 5));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Member Profile", 0, 0, null, Color.BLACK));

        boolean isOccasional = member.isOccasional();
        String feeDisplay = isOccasional ? "Pay as you go" : ("Rs. " + member.getMembershipFee());
        String expDisplay = member.getExpiryDate() != null ? member.getExpiryDate().toString() : "N/A";

        addDetailRow(detailsPanel, "Member ID:", member.getMemberId());
        addDetailRow(detailsPanel, "Full Name:", member.getFullName());
        addDetailRow(detailsPanel, "Contact Number:", member.getContactNumber());
        addDetailRow(detailsPanel, "Emergency Contact:", member.getEmergencyContactName() + " (" + member.getEmergencyContactNumber() + ")");
        addDetailRow(detailsPanel, "Address:", member.getAddress());
        addDetailRow(detailsPanel, "Membership Plan:", member.getMembership().getType());
        addDetailRow(detailsPanel, "Plan Duration:", member.getMembership().getDuration());
        addDetailRow(detailsPanel, "Membership Fee:", feeDisplay);
        addDetailRow(detailsPanel, "Assigned Trainer:", member.getTrainerName());
        addDetailRow(detailsPanel, "Locker Assigned:", (member.getLocker() != null ? member.getLocker().getLocationDisplay() : "None"));
        addDetailRow(detailsPanel, "Joining Date:", member.getJoiningDate().toString());
        addDetailRow(detailsPanel, "Expiry Date:", expDisplay);

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.WHITE);
        JLabel lblStatus = new JLabel("Current Status: " + member.getStatus());
        lblStatus.setForeground(Color.BLACK);
        statusPanel.add(lblStatus);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.add(detailsPanel, BorderLayout.CENTER);
        topContainer.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Renewal History Section
        JPanel renewalHistoryPanel = new JPanel(new BorderLayout());
        renewalHistoryPanel.setBackground(Color.WHITE);
        renewalHistoryPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Renewal History", 0, 0, null, Color.BLACK));

        String[] columns = {"Renewal ID", "Renewal Date", "New Expiry Date", "Amount Paid", "Payment Mode"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setGridColor(Color.BLACK);
        table.setSelectionBackground(Color.BLACK);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));

        try {
            List<Renewal> renewals = service.getRenewals(member.getMemberId());
            for (Renewal r : renewals) {
                model.addRow(new Object[]{
                        r.getRenewalId(),
                        r.getRenewalDate(),
                        r.getNewExpiryDate(),
                        "Rs. " + r.getAmountPaid(),
                        r.getPaymentMode()
                });
            }
        } catch (GymException e) {
            // Keep table empty on error
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(new LineBorder(Color.BLACK, 1));
        scrollPane.setPreferredSize(new Dimension(600, 150));
        renewalHistoryPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(renewalHistoryPanel, BorderLayout.CENTER);

        // Bottom Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnClose = new JButton("Close");
        btnClose.setBackground(Color.WHITE);
        btnClose.setForeground(Color.BLACK);
        btnClose.setBorder(new LineBorder(Color.BLACK, 1));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.BLACK);
        JLabel val = new JLabel(value);
        val.setForeground(Color.BLACK);
        panel.add(lbl);
        panel.add(val);
    }
}
