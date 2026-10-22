package gui;

import exceptions.GymException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import model.Attendance;
import model.AttendanceFeeSummary;
import model.Member;
import service.GymService;

// Interface for marking daily attendance and on-demand fee calculation for Occasional members (Black and White GUI only)
public class AttendancePanel extends JPanel {

    private final GymService gymService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Left Panel Components (Occasional Members)
    private JTextField txtSearch;
    private JTable tblMembers;
    private DefaultTableModel memberTableModel;
    private List<Member> currentOccasionalMembers = new ArrayList<>();

    // Selected Member State
    private Member selectedMember = null;
    private JLabel lblSelectedMember;

    // Attendance Action Components
    private JTextField txtAttendanceDate;
    private JTextField txtTillDate;

    // History Table Components
    private JTable tblHistory;
    private DefaultTableModel historyTableModel;

    public AttendancePanel(GymService gymService) {
        this.gymService = gymService;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshOccasionalMembers();
    }

    private void initComponents() {
        // Title banner
        JLabel lblTitle = new JLabel("OCCASIONAL MEMBER ATTENDANCE & ON-DEMAND BILLING");
        lblTitle.setForeground(Color.BLACK);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 13f));
        add(lblTitle, BorderLayout.NORTH);

        // Split pane: Left side Occasional Members table, Right side Attendance & Fee controls + History
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(Color.WHITE);
        splitPane.setDividerLocation(380);
        splitPane.setBorder(null);

        // ==========================================
        // LEFT PANEL: Occasional Members List & Search
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Occasional Members Only", 0, 0, null, Color.BLACK));

        // Search bar (reuses Searchable.matchesKeyword)
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchBar.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Color.BLACK);

        txtSearch = new JTextField(10);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setForeground(Color.BLACK);
        txtSearch.setCaretColor(Color.BLACK);
        txtSearch.setBorder(new LineBorder(Color.BLACK, 1));
        txtSearch.addActionListener(e -> filterOccasionalMembers());

        JButton btnSearch = createButton("Search");
        btnSearch.addActionListener(e -> filterOccasionalMembers());

        JButton btnShowAll = createButton("Show All");
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            filterOccasionalMembers();
        });

        searchBar.add(lblSearch);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        leftPanel.add(searchBar, BorderLayout.NORTH);

        // Members Table
        String[] memberCols = {"ID", "Full Name", "Plan", "Contact"};
        memberTableModel = new DefaultTableModel(memberCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblMembers = new JTable(memberTableModel);
        styleTable(tblMembers);

        tblMembers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblMembers.getSelectedRow();
                if (row != -1 && row < currentOccasionalMembers.size()) {
                    selectedMember = currentOccasionalMembers.get(row);
                    updateSelectedMemberUI();
                }
            }
        });

        JScrollPane scrollMembers = new JScrollPane(tblMembers);
        scrollMembers.getViewport().setBackground(Color.WHITE);
        scrollMembers.setBorder(new LineBorder(Color.BLACK, 1));
        leftPanel.add(scrollMembers, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // ==========================================
        // RIGHT PANEL: Action Controls & History Table
        // ==========================================
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);

        // Top Controls Panel
        JPanel controlsContainer = new JPanel(new BorderLayout(8, 8));
        controlsContainer.setBackground(Color.WHITE);

        // Selected Member Banner
        JPanel bannerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(new LineBorder(Color.BLACK, 1));

        lblSelectedMember = new JLabel("Selected Member: None (Please select an occasional member)");
        lblSelectedMember.setForeground(Color.BLACK);
        lblSelectedMember.setFont(lblSelectedMember.getFont().deriveFont(Font.BOLD, 12f));
        bannerPanel.add(lblSelectedMember);
        controlsContainer.add(bannerPanel, BorderLayout.NORTH);

        // Grid for Mark Attendance and Calculate Fee forms
        JPanel actionFormsPanel = new JPanel(new GridBagLayout());
        actionFormsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;

        // 1. Mark Attendance Section
        JPanel markPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        markPanel.setBackground(Color.WHITE);
        markPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Mark Daily Attendance", 0, 0, null, Color.BLACK));

        JLabel lblAttDate = new JLabel("Date (yyyy-MM-dd):");
        lblAttDate.setForeground(Color.BLACK);

        txtAttendanceDate = new JTextField(LocalDate.now().format(DATE_FORMATTER), 10);
        txtAttendanceDate.setBackground(Color.WHITE);
        txtAttendanceDate.setForeground(Color.BLACK);
        txtAttendanceDate.setCaretColor(Color.BLACK);
        txtAttendanceDate.setBorder(new LineBorder(Color.BLACK, 1));

        JButton btnMarkPresent = createButton("Mark Present");
        btnMarkPresent.addActionListener(e -> handleMarkAttendance("Present"));

        JButton btnMarkAbsent = createButton("Mark Absent");
        btnMarkAbsent.addActionListener(e -> handleMarkAttendance("Absent"));

        markPanel.add(lblAttDate);
        markPanel.add(txtAttendanceDate);
        markPanel.add(btnMarkPresent);
        markPanel.add(btnMarkAbsent);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        actionFormsPanel.add(markPanel, gbc);

        // 2. Calculate Fee Till Date Section
        JPanel feePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        feePanel.setBackground(Color.WHITE);
        feePanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "On-Demand Fee Calculation", 0, 0, null, Color.BLACK));

        JLabel lblTill = new JLabel("Till Date (yyyy-MM-dd):");
        lblTill.setForeground(Color.BLACK);

        txtTillDate = new JTextField(LocalDate.now().format(DATE_FORMATTER), 10);
        txtTillDate.setBackground(Color.WHITE);
        txtTillDate.setForeground(Color.BLACK);
        txtTillDate.setCaretColor(Color.BLACK);
        txtTillDate.setBorder(new LineBorder(Color.BLACK, 1));

        JButton btnCalculate = createButton("Calculate Fee Till Date");
        btnCalculate.addActionListener(e -> handleCalculateFee());

        feePanel.add(lblTill);
        feePanel.add(txtTillDate);
        feePanel.add(btnCalculate);

        gbc.gridx = 0;
        gbc.gridy = 1;
        actionFormsPanel.add(feePanel, gbc);

        controlsContainer.add(actionFormsPanel, BorderLayout.CENTER);
        rightPanel.add(controlsContainer, BorderLayout.NORTH);

        // Center / Bottom: Attendance History Table
        JPanel historyContainer = new JPanel(new BorderLayout(5, 5));
        historyContainer.setBackground(Color.WHITE);
        historyContainer.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Attendance History (Selected Member)", 0, 0, null, Color.BLACK));

        String[] historyCols = {"Date", "Status", "Payment Status"};
        historyTableModel = new DefaultTableModel(historyCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblHistory = new JTable(historyTableModel);
        styleTable(tblHistory);

        JScrollPane scrollHistory = new JScrollPane(tblHistory);
        scrollHistory.getViewport().setBackground(Color.WHITE);
        scrollHistory.setBorder(new LineBorder(Color.BLACK, 1));
        historyContainer.add(scrollHistory, BorderLayout.CENTER);

        // Refresh History Button
        JPanel historyBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        historyBottom.setBackground(Color.WHITE);
        JButton btnRefreshHistory = createButton("Refresh History");
        btnRefreshHistory.addActionListener(e -> loadAttendanceHistory());
        historyBottom.add(btnRefreshHistory);
        historyContainer.add(historyBottom, BorderLayout.SOUTH);

        rightPanel.add(historyContainer, BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    // Refreshes the list of occasional members and resets selection
    public void refreshPanel() {
        txtAttendanceDate.setText(LocalDate.now().format(DATE_FORMATTER));
        txtTillDate.setText(LocalDate.now().format(DATE_FORMATTER));
        refreshOccasionalMembers();
    }

    private void refreshOccasionalMembers() {
        memberTableModel.setRowCount(0);
        currentOccasionalMembers.clear();

        try {
            List<Member> list = gymService.getOccasionalMembers();
            currentOccasionalMembers.addAll(list);

            for (Member m : currentOccasionalMembers) {
                memberTableModel.addRow(new Object[]{
                        m.getMemberId(),
                        m.getFullName(),
                        m.getMembership().getType(),
                        m.getContactNumber()
                });
            }

            // Restore selection if previous member still exists
            if (selectedMember != null) {
                boolean found = false;
                for (int i = 0; i < currentOccasionalMembers.size(); i++) {
                    if (currentOccasionalMembers.get(i).getMemberId().equals(selectedMember.getMemberId())) {
                        tblMembers.setRowSelectionInterval(i, i);
                        selectedMember = currentOccasionalMembers.get(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    selectedMember = null;
                    updateSelectedMemberUI();
                }
            } else if (!currentOccasionalMembers.isEmpty()) {
                tblMembers.setRowSelectionInterval(0, 0);
                selectedMember = currentOccasionalMembers.get(0);
                updateSelectedMemberUI();
            } else {
                updateSelectedMemberUI();
            }
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, "Error loading occasional members: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Filters occasional members list using Searchable.matchesKeyword
    private void filterOccasionalMembers() {
        String keyword = txtSearch.getText().trim();
        memberTableModel.setRowCount(0);
        currentOccasionalMembers.clear();

        try {
            List<Member> all = gymService.getOccasionalMembers();
            for (Member m : all) {
                if (m.matchesKeyword(keyword)) {
                    currentOccasionalMembers.add(m);
                    memberTableModel.addRow(new Object[]{
                            m.getMemberId(),
                            m.getFullName(),
                            m.getMembership().getType(),
                            m.getContactNumber()
                    });
                }
            }

            if (!currentOccasionalMembers.isEmpty()) {
                tblMembers.setRowSelectionInterval(0, 0);
                selectedMember = currentOccasionalMembers.get(0);
            } else {
                selectedMember = null;
            }
            updateSelectedMemberUI();
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedMemberUI() {
        if (selectedMember != null) {
            lblSelectedMember.setText("Selected Member: " + selectedMember.getMemberId() + " - "
                    + selectedMember.getFullName() + " | Plan: " + selectedMember.getMembership().getType()
                    + " (Occasional, Rs. " + selectedMember.getMembership().getDailyRate() + "/day)");
            loadAttendanceHistory();
        } else {
            lblSelectedMember.setText("Selected Member: None (Please select an occasional member from the list)");
            historyTableModel.setRowCount(0);
        }
    }

    private void loadAttendanceHistory() {
        historyTableModel.setRowCount(0);
        if (selectedMember == null) {
            return;
        }

        try {
            List<Attendance> history = gymService.getMemberAttendanceHistory(selectedMember.getMemberId());
            for (Attendance att : history) {
                String paidText = att.isPaid() ? "Paid" : "Unpaid";
                historyTableModel.addRow(new Object[]{
                        att.getAttendanceDate().toString(),
                        att.getStatus(),
                        paidText
                });
            }
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, "Error loading attendance history: " + e.getMessage(), "History Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMarkAttendance(String status) {
        if (selectedMember == null) {
            JOptionPane.showMessageDialog(this, "Please select an occasional member first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = txtAttendanceDate.getText().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd (e.g., 2026-10-22).", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean updated = gymService.markAttendance(selectedMember.getMemberId(), date, status);
            String action = updated ? "updated to" : "recorded as";
            JOptionPane.showMessageDialog(this,
                    "Attendance for member " + selectedMember.getFullName() + " (" + selectedMember.getMemberId() + ")"
                    + "\non date " + date + " has been " + action + " '" + status + "'.",
                    "Attendance Saved",
                    JOptionPane.INFORMATION_MESSAGE);

            loadAttendanceHistory();
        } catch (GymException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Attendance Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCalculateFee() {
        if (selectedMember == null) {
            JOptionPane.showMessageDialog(this, "Please select an occasional member first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tillDateStr = txtTillDate.getText().trim();
        LocalDate tillDate;
        try {
            tillDate = LocalDate.parse(tillDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid till date format. Please use yyyy-MM-dd (e.g., 2026-10-22).", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            AttendanceFeeSummary summary = gymService.calculateFeeTillDate(selectedMember.getMemberId(), tillDate);
            if (summary == null || summary.getPresentDaysCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No unpaid Present attendance records found for member " + selectedMember.getFullName()
                        + " up to " + tillDate + ".\nNothing is payable.",
                        "Nothing Payable",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            AttendanceFeeDialog dialog = new AttendanceFeeDialog(topFrame, summary, gymService);
            dialog.setVisible(true);

            if (dialog.isPaymentProcessed()) {
                loadAttendanceHistory();
            }
        } catch (GymException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fee Calculation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable table) {
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setGridColor(Color.BLACK);
        table.setSelectionBackground(Color.BLACK);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));
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
