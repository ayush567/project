package gui;

import exceptions.GymException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import model.Member;
import service.GymService;

// Interface to update member membership status (Black and White GUI only)
public class UpdateStatusPanel extends JPanel {

    private final GymService gymService;

    private JTextField txtMemberId;
    private JLabel lblName;
    private JLabel lblCurrentStatus;
    private JLabel lblExpiryDate;
    private JComboBox<String> cmbNewStatus;
    private JTable statusTable;
    private DefaultTableModel tableModel;

    public UpdateStatusPanel(GymService gymService) {
        this.gymService = gymService;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshStatusTable();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("UPDATE MEMBERSHIP STATUS");
        lblTitle.setForeground(Color.BLACK);
        add(lblTitle, BorderLayout.NORTH);

        // Top Form Panel
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Update Status Form", 0, 0, null, Color.BLACK));

        txtMemberId = new JTextField();
        txtMemberId.setBackground(Color.WHITE);
        txtMemberId.setForeground(Color.BLACK);
        txtMemberId.setBorder(new LineBorder(Color.BLACK, 1));

        JPanel idSearchPanel = new JPanel(new BorderLayout(5, 0));
        idSearchPanel.setBackground(Color.WHITE);
        idSearchPanel.add(txtMemberId, BorderLayout.CENTER);
        JButton btnLoad = createButton("Load");
        btnLoad.addActionListener(e -> loadMemberData());
        idSearchPanel.add(btnLoad, BorderLayout.EAST);

        lblName = new JLabel("-");
        lblName.setForeground(Color.BLACK);

        lblCurrentStatus = new JLabel("-");
        lblCurrentStatus.setForeground(Color.BLACK);

        lblExpiryDate = new JLabel("-");
        lblExpiryDate.setForeground(Color.BLACK);

        String[] statusOptions = {"Registered", "Active", "Due for Renewal", "Renewed", "Expired"};
        cmbNewStatus = new JComboBox<>(statusOptions);
        cmbNewStatus.setBackground(Color.WHITE);
        cmbNewStatus.setForeground(Color.BLACK);
        cmbNewStatus.setBorder(new LineBorder(Color.BLACK, 1));

        form.add(new JLabel("Member ID:"));
        form.add(idSearchPanel);
        form.add(new JLabel("Member Name:"));
        form.add(lblName);
        form.add(new JLabel("Expiry Date:"));
        form.add(lblExpiryDate);
        form.add(new JLabel("Current Status:"));
        form.add(lblCurrentStatus);
        form.add(new JLabel("New Status:"));
        form.add(cmbNewStatus);

        // Action button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton btnSave = createButton("Update Status");
        btnSave.addActionListener(e -> handleUpdateStatus());
        btnPanel.add(btnSave);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.add(form, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);

        // Bottom Table
        String[] columns = {"Member ID", "Member Name", "Plan", "Expiry Date", "Current Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        statusTable = new JTable(tableModel);
        statusTable.setBackground(Color.WHITE);
        statusTable.setForeground(Color.BLACK);
        statusTable.setGridColor(Color.BLACK);
        statusTable.setSelectionBackground(Color.BLACK);
        statusTable.setSelectionForeground(Color.WHITE);
        statusTable.getTableHeader().setBackground(Color.WHITE);
        statusTable.getTableHeader().setForeground(Color.BLACK);
        statusTable.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));

        statusTable.getSelectionModel().addListSelectionListener(e -> {
            int row = statusTable.getSelectedRow();
            if (row != -1) {
                txtMemberId.setText((String) tableModel.getValueAt(row, 0));
                loadMemberData();
            }
        });

        JScrollPane scroll = new JScrollPane(statusTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Member Status Overview", 0, 0, null, Color.BLACK));

        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setBackground(Color.WHITE);
        centerContainer.add(topContainer, BorderLayout.NORTH);
        centerContainer.add(scroll, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    private void loadMemberData() {
        String id = txtMemberId.getText().trim();
        if (id.isEmpty()) return;
        try {
            Member m = gymService.getMember(id);
            lblName.setText(m.getFullName());
            lblCurrentStatus.setText(m.getStatus());
            lblExpiryDate.setText(m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A");
            cmbNewStatus.setSelectedItem(m.getStatus());
        } catch (GymException e) {
            lblName.setText("-");
            lblCurrentStatus.setText("-");
            lblExpiryDate.setText("-");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateStatus() {
        String id = txtMemberId.getText().trim();
        String newStatus = (String) cmbNewStatus.getSelectedItem();

        try {
            gymService.updateMemberStatus(id, newStatus);
            JOptionPane.showMessageDialog(this, "Status updated to '" + newStatus + "' for member: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMemberData();
            refreshStatusTable();
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshStatusTable() {
        tableModel.setRowCount(0);
        try {
            List<Member> list = gymService.getAllMembers();
            for (Member m : list) {
                String expDisplay = m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A";
                tableModel.addRow(new Object[]{
                        m.getMemberId(),
                        m.getFullName(),
                        m.getMembership().getType(),
                        expDisplay,
                        m.getStatus()
                });
            }
        } catch (GymException e) {
            // Ignore on load
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
