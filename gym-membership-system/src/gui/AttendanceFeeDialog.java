package gui;

import exceptions.GymException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import model.AttendanceFeeSummary;
import service.GymService;

// Modal dialog showing on-demand fee calculation and payment option (Black and White GUI only)
public class AttendanceFeeDialog extends JDialog {

    private final AttendanceFeeSummary summary;
    private final GymService gymService;
    private boolean paymentProcessed = false;

    public AttendanceFeeDialog(Frame parent, AttendanceFeeSummary summary, GymService gymService) {
        super(parent, "Attendance Fee Calculation - " + summary.getMemberId(), true);
        this.summary = summary;
        this.gymService = gymService;

        setSize(550, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Title
        JLabel lblHeading = new JLabel("CALCULATED ATTENDANCE FEE TILL DATE");
        lblHeading.setForeground(Color.BLACK);
        lblHeading.setFont(lblHeading.getFont().deriveFont(Font.BOLD, 13f));
        mainPanel.add(lblHeading, BorderLayout.NORTH);

        // Center container with Summary info and Present Dates list
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setBackground(Color.WHITE);

        // Details Grid Panel
        JPanel detailsPanel = new JPanel(new GridLayout(8, 2, 6, 6));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Fee Breakdown", 0, 0, null, Color.BLACK));

        addDetailRow(detailsPanel, "Member Name:", summary.getMemberName() + " (" + summary.getMemberId() + ")");
        addDetailRow(detailsPanel, "Plan Type:", summary.getPlanType() + " (Occasional)");
        addDetailRow(detailsPanel, "From Date (Earliest Unpaid):", summary.getFromDate().toString());
        addDetailRow(detailsPanel, "Till Date:", summary.getTillDate().toString());
        addDetailRow(detailsPanel, "Number of Present Days:", String.valueOf(summary.getPresentDaysCount()));
        addDetailRow(detailsPanel, "Number of Absent Days:", String.valueOf(summary.getAbsentDaysCount()));
        addDetailRow(detailsPanel, "Per-Day Rate:", "Rs. " + summary.getDailyRate() + " / day");
        addDetailRow(detailsPanel, "Total Amount Payable:", "Rs. " + summary.getTotalAmount());

        centerContainer.add(detailsPanel, BorderLayout.NORTH);

        // Present Dates List Panel
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Dates Marked Present (" + summary.getPresentDaysCount() + ")", 0, 0, null, Color.BLACK));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        int count = 1;
        for (LocalDate date : summary.getPresentDates()) {
            listModel.addElement(count++ + ". " + date.toString());
        }

        JList<String> dateList = new JList<>(listModel);
        dateList.setBackground(Color.WHITE);
        dateList.setForeground(Color.BLACK);
        dateList.setSelectionBackground(Color.BLACK);
        dateList.setSelectionForeground(Color.WHITE);

        JScrollPane scrollDates = new JScrollPane(dateList);
        scrollDates.getViewport().setBackground(Color.WHITE);
        scrollDates.setBorder(new LineBorder(Color.BLACK, 1));
        scrollDates.setPreferredSize(new Dimension(500, 150));
        listPanel.add(scrollDates, BorderLayout.CENTER);

        centerContainer.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnMarkPaid = createButton("Mark as Paid");
        btnMarkPaid.addActionListener(e -> handleMarkPaid());

        JButton btnClose = createButton("Close");
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnMarkPaid);
        buttonPanel.add(btnClose);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void handleMarkPaid() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm receipt of Rs. " + summary.getTotalAmount() + " from " + summary.getMemberName() + "?\n" +
                "This will set all attendance days up to " + summary.getTillDate() + " as PAID.\n" +
                "They cannot be modified or charged again.",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                gymService.markAttendanceAsPaid(summary.getMemberId(), summary.getTillDate());
                paymentProcessed = true;
                JOptionPane.showMessageDialog(this,
                        "Attendance fee of Rs. " + summary.getTotalAmount() + " for "
                        + summary.getMemberName() + " has been recorded as PAID.",
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (GymException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isPaymentProcessed() {
        return paymentProcessed;
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.BLACK);
        JLabel val = new JLabel(value);
        val.setForeground(Color.BLACK);
        panel.add(lbl);
        panel.add(val);
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
