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

// Manages membership renewal operations (Black and White GUI only)
public class RenewMembershipPanel extends JPanel {

    private final GymService gymService;

    private JTable dueTable;
    private DefaultTableModel tableModel;

    private JTextField txtMemberId;
    private JLabel lblMemberName;
    private JLabel lblCurrentExpiry;
    private JComboBox<String> cmbNewDuration;
    private JLabel lblCalculatedRenewalFee;
    private JTextField txtAmountPaid;
    private JComboBox<String> cmbPaymentMode;

    public RenewMembershipPanel(GymService gymService) {
        this.gymService = gymService;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshDueTable();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("RENEW GYM MEMBERSHIP");
        lblTitle.setForeground(Color.BLACK);
        add(lblTitle, BorderLayout.NORTH);

        // Due Members Table
        String[] columns = {"Member ID", "Member Name", "Plan", "Expiry Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        dueTable = new JTable(tableModel);
        dueTable.setBackground(Color.WHITE);
        dueTable.setForeground(Color.BLACK);
        dueTable.setGridColor(Color.BLACK);
        dueTable.setSelectionBackground(Color.BLACK);
        dueTable.setSelectionForeground(Color.WHITE);
        dueTable.getTableHeader().setBackground(Color.WHITE);
        dueTable.getTableHeader().setForeground(Color.BLACK);
        dueTable.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));

        dueTable.getSelectionModel().addListSelectionListener(e -> {
            int row = dueTable.getSelectedRow();
            if (row != -1) {
                txtMemberId.setText((String) tableModel.getValueAt(row, 0));
                loadMemberForRenewal();
            }
        });

        JScrollPane scrollTable = new JScrollPane(dueTable);
        scrollTable.getViewport().setBackground(Color.WHITE);
        scrollTable.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Members Due for Renewal / Expired", 0, 0, null, Color.BLACK));

        // Renewal Form
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Renewal Process Form", 0, 0, null, Color.BLACK));

        txtMemberId = new JTextField();
        txtMemberId.setBackground(Color.WHITE);
        txtMemberId.setForeground(Color.BLACK);
        txtMemberId.setBorder(new LineBorder(Color.BLACK, 1));

        JPanel idSearchPanel = new JPanel(new BorderLayout(5, 0));
        idSearchPanel.setBackground(Color.WHITE);
        idSearchPanel.add(txtMemberId, BorderLayout.CENTER);
        JButton btnLoad = createButton("Load");
        btnLoad.addActionListener(e -> loadMemberForRenewal());
        idSearchPanel.add(btnLoad, BorderLayout.EAST);

        lblMemberName = new JLabel("-");
        lblMemberName.setForeground(Color.BLACK);

        lblCurrentExpiry = new JLabel("-");
        lblCurrentExpiry.setForeground(Color.BLACK);

        String[] durations = {"Monthly", "Quarterly", "Yearly"};
        cmbNewDuration = new JComboBox<>(durations);
        cmbNewDuration.setBackground(Color.WHITE);
        cmbNewDuration.setForeground(Color.BLACK);
        cmbNewDuration.setBorder(new LineBorder(Color.BLACK, 1));
        cmbNewDuration.addActionListener(e -> updateRenewalFee());

        lblCalculatedRenewalFee = new JLabel("Rs. 0.0");
        lblCalculatedRenewalFee.setForeground(Color.BLACK);

        txtAmountPaid = new JTextField();
        txtAmountPaid.setBackground(Color.WHITE);
        txtAmountPaid.setForeground(Color.BLACK);
        txtAmountPaid.setBorder(new LineBorder(Color.BLACK, 1));

        String[] paymentModes = {"Cash", "Card", "UPI", "Bank Transfer"};
        cmbPaymentMode = new JComboBox<>(paymentModes);
        cmbPaymentMode.setBackground(Color.WHITE);
        cmbPaymentMode.setForeground(Color.BLACK);
        cmbPaymentMode.setBorder(new LineBorder(Color.BLACK, 1));

        form.add(new JLabel("Member ID:"));
        form.add(idSearchPanel);
        form.add(new JLabel("Member Name:"));
        form.add(lblMemberName);
        form.add(new JLabel("Current Expiry:"));
        form.add(lblCurrentExpiry);
        form.add(new JLabel("Renewal Duration:"));
        form.add(cmbNewDuration);
        form.add(new JLabel("Discounted Renewal Fee:"));
        form.add(lblCalculatedRenewalFee);
        form.add(new JLabel("Amount Paid * (Rs.):"));
        form.add(txtAmountPaid);
        form.add(new JLabel("Payment Mode *:"));
        form.add(cmbPaymentMode);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton btnProcess = createButton("Process Renewal");
        btnProcess.addActionListener(e -> handleRenewal());
        btnPanel.add(btnProcess);

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.add(form, BorderLayout.CENTER);
        formContainer.add(btnPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(scrollTable);
        centerPanel.add(formContainer);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadMemberForRenewal() {
        String id = txtMemberId.getText().trim();
        if (id.isEmpty()) return;
        try {
            Member m = gymService.getMember(id);
            lblMemberName.setText(m.getFullName());
            lblCurrentExpiry.setText(m.getExpiryDate().toString());
            cmbNewDuration.setSelectedItem(m.getMembership().getDuration());
            updateRenewalFee();
        } catch (GymException e) {
            lblMemberName.setText("-");
            lblCurrentExpiry.setText("-");
            lblCalculatedRenewalFee.setText("Rs. 0.0");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRenewalFee() {
        String id = txtMemberId.getText().trim();
        if (id.isEmpty()) return;
        try {
            String duration = (String) cmbNewDuration.getSelectedItem();
            double fee = gymService.calculateRenewalFee(id, duration);
            lblCalculatedRenewalFee.setText("Rs. " + fee);
            txtAmountPaid.setText(String.valueOf(fee));
        } catch (GymException e) {
            lblCalculatedRenewalFee.setText("Rs. 0.0");
        }
    }

    private void handleRenewal() {
        String id = txtMemberId.getText().trim();
        String duration = (String) cmbNewDuration.getSelectedItem();
        String amountStr = txtAmountPaid.getText().trim();
        String mode = (String) cmbPaymentMode.getSelectedItem();

        if (id.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a member and provide payment amount.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            gymService.renewMembership(id, duration, amount, mode);
            JOptionPane.showMessageDialog(this, "Membership renewed successfully for Member: " + id, "Renewal Success", JOptionPane.INFORMATION_MESSAGE);
            refreshDueTable();
            loadMemberForRenewal();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Renewal Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshDueTable() {
        tableModel.setRowCount(0);
        try {
            List<Member> list = gymService.getMembersDueForRenewal();
            for (Member m : list) {
                tableModel.addRow(new Object[]{
                        m.getMemberId(),
                        m.getFullName(),
                        m.getMembership().getType(),
                        m.getExpiryDate(),
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