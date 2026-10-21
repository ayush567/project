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

// Manages locker locations and assignments (Black and White GUI only)
public class LockerManagementPanel extends JPanel {

    private final GymService gymService;

    private JTextField txtSearchMemberId;
    private JLabel lblMemberName;
    private JLabel lblCurrentLocker;
    private JComboBox<String> cmbNewZone;
    private JTextField txtNewRow;
    private JTextField txtNewLockerNo;
    private JTable lockerTable;
    private DefaultTableModel tableModel;

    public LockerManagementPanel(GymService gymService) {
        this.gymService = gymService;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshLockerTable();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("LOCKER LOCATION MANAGEMENT");
        lblTitle.setForeground(Color.BLACK);
        add(lblTitle, BorderLayout.NORTH);

        // Top form panel for reassigning lockers
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Assign / Update Member Locker", 0, 0, null, Color.BLACK));

        txtSearchMemberId = new JTextField();
        txtSearchMemberId.setBackground(Color.WHITE);
        txtSearchMemberId.setForeground(Color.BLACK);
        txtSearchMemberId.setBorder(new LineBorder(Color.BLACK, 1));

        JPanel idSearchPanel = new JPanel(new BorderLayout(5, 0));
        idSearchPanel.setBackground(Color.WHITE);
        idSearchPanel.add(txtSearchMemberId, BorderLayout.CENTER);
        JButton btnLoad = createButton("Load");
        btnLoad.addActionListener(e -> loadMemberLockerInfo());
        idSearchPanel.add(btnLoad, BorderLayout.EAST);

        lblMemberName = new JLabel("-");
        lblMemberName.setForeground(Color.BLACK);

        lblCurrentLocker = new JLabel("-");
        lblCurrentLocker.setForeground(Color.BLACK);

        String[] zones = {"Men's", "Women's", "Unisex"};
        cmbNewZone = new JComboBox<>(zones);
        cmbNewZone.setBackground(Color.WHITE);
        cmbNewZone.setForeground(Color.BLACK);
        cmbNewZone.setBorder(new LineBorder(Color.BLACK, 1));

        txtNewRow = new JTextField("Row-1");
        txtNewRow.setBackground(Color.WHITE);
        txtNewRow.setForeground(Color.BLACK);
        txtNewRow.setBorder(new LineBorder(Color.BLACK, 1));

        txtNewLockerNo = new JTextField("101");
        txtNewLockerNo.setBackground(Color.WHITE);
        txtNewLockerNo.setForeground(Color.BLACK);
        txtNewLockerNo.setBorder(new LineBorder(Color.BLACK, 1));

        formPanel.add(new JLabel("Member ID:"));
        formPanel.add(idSearchPanel);
        formPanel.add(new JLabel("Member Name:"));
        formPanel.add(lblMemberName);
        formPanel.add(new JLabel("Current Locker:"));
        formPanel.add(lblCurrentLocker);
        formPanel.add(new JLabel("New Locker Zone:"));
        formPanel.add(cmbNewZone);
        formPanel.add(new JLabel("New Locker Row:"));
        formPanel.add(txtNewRow);
        formPanel.add(new JLabel("New Locker Number:"));
        formPanel.add(txtNewLockerNo);

        // Update button
        JPanel updateBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        updateBtnPanel.setBackground(Color.WHITE);
        JButton btnUpdateLocker = createButton("Save Locker Update");
        btnUpdateLocker.addActionListener(e -> handleUpdateLocker());
        updateBtnPanel.add(btnUpdateLocker);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(updateBtnPanel, BorderLayout.SOUTH);

        // Table showing all lockers
        String[] columns = {"Member ID", "Member Name", "Zone", "Row", "Locker No"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        lockerTable = new JTable(tableModel);
        lockerTable.setBackground(Color.WHITE);
        lockerTable.setForeground(Color.BLACK);
        lockerTable.setGridColor(Color.BLACK);
        lockerTable.setSelectionBackground(Color.BLACK);
        lockerTable.setSelectionForeground(Color.WHITE);
        lockerTable.getTableHeader().setBackground(Color.WHITE);
        lockerTable.getTableHeader().setForeground(Color.BLACK);
        lockerTable.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));

        // When a row is clicked, populate the update form
        lockerTable.getSelectionModel().addListSelectionListener(e -> {
            int row = lockerTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                txtSearchMemberId.setText(id);
                loadMemberLockerInfo();
            }
        });

        JScrollPane scroll = new JScrollPane(lockerTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "All Locker Allocations", 0, 0, null, Color.BLACK));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(topContainer, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadMemberLockerInfo() {
        String id = txtSearchMemberId.getText().trim();
        if (id.isEmpty()) {
            return;
        }
        try {
            Member m = gymService.getMember(id);
            lblMemberName.setText(m.getFullName());
            if (m.getLocker() != null) {
                lblCurrentLocker.setText(m.getLocker().getLocationDisplay());
                cmbNewZone.setSelectedItem(m.getLocker().getZone());
                txtNewRow.setText(m.getLocker().getRowNumber());
                txtNewLockerNo.setText(m.getLocker().getLockerNumber());
            } else {
                lblCurrentLocker.setText("None Assigned");
            }
        } catch (GymException e) {
            lblMemberName.setText("-");
            lblCurrentLocker.setText("-");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lookup Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateLocker() {
        String id = txtSearchMemberId.getText().trim();
        String zone = (String) cmbNewZone.getSelectedItem();
        String row = txtNewRow.getText().trim();
        String lockerNo = txtNewLockerNo.getText().trim();

        try {
            gymService.updateLocker(id, zone, row, lockerNo);
            JOptionPane.showMessageDialog(this, "Locker assigned successfully for member: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMemberLockerInfo();
            refreshLockerTable();
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshLockerTable() {
        tableModel.setRowCount(0);
        try {
            List<Member> members = gymService.getAllMembers();
            for (Member m : members) {
                if (m.getLocker() != null) {
                    tableModel.addRow(new Object[]{
                            m.getMemberId(),
                            m.getFullName(),
                            m.getLocker().getZone(),
                            m.getLocker().getRowNumber(),
                            m.getLocker().getLockerNumber()
                    });
                }
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