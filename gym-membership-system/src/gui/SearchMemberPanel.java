package gui;

import exceptions.GymException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
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
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import model.Member;
import service.GymService;

// Search interface allowing multi-criteria member lookup (Black and White GUI only)
public class SearchMemberPanel extends JPanel {

    private final GymService gymService;
    private JComboBox<String> cmbCriteria;
    private JTextField txtSearchKeyword;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    public SearchMemberPanel(GymService gymService) {
        this.gymService = gymService;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("SEARCH GYM MEMBERS");
        lblTitle.setForeground(Color.BLACK);
        add(lblTitle, BorderLayout.NORTH);

        // Search Control Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(new LineBorder(Color.BLACK, 1));

        JLabel lblSearchBy = new JLabel("Search By:");
        lblSearchBy.setForeground(Color.BLACK);

        String[] criteriaOptions = {"All Fields", "Member ID", "Member Name", "Membership Type", "Locker Location", "Status"};
        cmbCriteria = new JComboBox<>(criteriaOptions);
        cmbCriteria.setBackground(Color.WHITE);
        cmbCriteria.setForeground(Color.BLACK);
        cmbCriteria.setBorder(new LineBorder(Color.BLACK, 1));

        txtSearchKeyword = new JTextField(15);
        txtSearchKeyword.setBackground(Color.WHITE);
        txtSearchKeyword.setForeground(Color.BLACK);
        txtSearchKeyword.setCaretColor(Color.BLACK);
        txtSearchKeyword.setBorder(new LineBorder(Color.BLACK, 1));

        JButton btnSearch = createButton("Search");
        btnSearch.addActionListener(e -> executeSearch());

        JButton btnReset = createButton("Show All");
        btnReset.addActionListener(e -> {
            txtSearchKeyword.setText("");
            executeSearch();
        });

        searchBar.add(lblSearchBy);
        searchBar.add(cmbCriteria);
        searchBar.add(new JLabel("Keyword:"));
        searchBar.add(txtSearchKeyword);
        searchBar.add(btnSearch);
        searchBar.add(btnReset);

        // Results Table
        String[] columns = {"Member ID", "Full Name", "Contact", "Plan", "Status", "Expiry Date", "Locker Location"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setBackground(Color.WHITE);
        resultTable.setForeground(Color.BLACK);
        resultTable.setGridColor(Color.BLACK);
        resultTable.setSelectionBackground(Color.BLACK);
        resultTable.setSelectionForeground(Color.WHITE);
        resultTable.getTableHeader().setBackground(Color.WHITE);
        resultTable.getTableHeader().setForeground(Color.BLACK);
        resultTable.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));

        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(Color.BLACK, 1));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(searchBar, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnDetails = createButton("View Full Details");
        btnDetails.addActionListener(e -> {
            int row = resultTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a member from results first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            try {
                Member m = gymService.getMember(id);
                Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
                new MemberDetailsDialog(frame, m, gymService).setVisible(true);
            } catch (GymException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnDetails);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void executeSearch() {
        tableModel.setRowCount(0);
        String criteria = (String) cmbCriteria.getSelectedItem();
        String keyword = txtSearchKeyword.getText().trim();

        try {
            List<Member> results = gymService.searchMembers(criteria, keyword);
            for (Member m : results) {
                String loc = (m.getLocker() != null) ? m.getLocker().getLocationDisplay() : "None";
                String expDisplay = (m.getExpiryDate() != null) ? m.getExpiryDate().toString() : "N/A";
                tableModel.addRow(new Object[]{
                        m.getMemberId(),
                        m.getFullName(),
                        m.getContactNumber(),
                        m.getMembership().getType(),
                        m.getStatus(),
                        expDisplay,
                        loc
                });
            }
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No gym members matched your search criteria.", "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (GymException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
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
