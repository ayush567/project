package gui;

import exceptions.GymException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import model.Member;
import service.GymService;

// Displays table of all registered gym members (Black and White GUI only)
public class ViewMembersPanel extends JPanel {

    private final GymService gymService;
    private JTable table;
    private DefaultTableModel tableModel;

    public ViewMembersPanel(GymService gymService) {
        this.gymService = gymService;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("ALL ENROLLED GYM MEMBERS");
        lblTitle.setForeground(Color.BLACK);
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {
                "Member ID", "Full Name", "Contact", "Plan", "Duration",
                "Fee (Rs.)", "Status", "Expiry Date", "Assigned Locker"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setGridColor(Color.BLACK);
        table.setSelectionBackground(Color.BLACK);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(new LineBorder(Color.BLACK, 1));
        add(scrollPane, BorderLayout.CENTER);

        // Button toolbar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton btnRefresh = createButton("Refresh List");
        btnRefresh.addActionListener(e -> refreshTable());

        JButton btnViewDetails = createButton("View Full Details");
        btnViewDetails.addActionListener(e -> handleViewDetails());

        JButton btnDelete = createButton("Delete Member");
        btnDelete.addActionListener(e -> handleDeleteMember());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnViewDetails);
        btnPanel.add(btnDelete);

        add(btnPanel, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        try {
            List<Member> members = gymService.getAllMembers();
            for (Member m : members) {
                String lockerDisplay = (m.getLocker() != null) ? m.getLocker().getLocationDisplay() : "None";
                String feeDisplay = m.isOccasional() ? "Pay as you go" : String.valueOf(m.getMembershipFee());
                String expDisplay = (m.getExpiryDate() != null) ? m.getExpiryDate().toString() : "N/A";
                tableModel.addRow(new Object[]{
                        m.getMemberId(),
                        m.getFullName(),
                        m.getContactNumber(),
                        m.getMembership().getType(),
                        m.getMembership().getDuration(),
                        feeDisplay,
                        m.getStatus(),
                        expDisplay,
                        lockerDisplay
                });
            }
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String memberId = (String) tableModel.getValueAt(selectedRow, 0);
        try {
            Member member = gymService.getMember(memberId);
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            MemberDetailsDialog dialog = new MemberDetailsDialog(topFrame, member, gymService);
            dialog.setVisible(true);
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteMember() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String memberId = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete member: " + memberId + "?\nThis will also remove locker assignment and renewal history.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                gymService.deleteMember(memberId);
                JOptionPane.showMessageDialog(this, "Member deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (GymException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(new LineBorder(Color.BLACK, 1));
        btn.setFocusPainted(false);
        return btn;
    }
}
