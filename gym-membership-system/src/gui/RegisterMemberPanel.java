package gui;

import exceptions.GymException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import model.Locker;
import model.Member;
import model.Membership;
import service.GymService;
import service.MembershipFactory;

// Form to register new members (Black and White GUI only)
public class RegisterMemberPanel extends JPanel {

    private final GymService gymService;

    private JTextField txtMemberId;
    private JTextField txtFullName;
    private JTextField txtContact;
    private JTextField txtEmergencyName;
    private JTextField txtEmergencyContact;
    private JTextField txtAddress;
    private JComboBox<String> cmbMembershipType;
    private JComboBox<String> cmbDuration;
    private JTextField txtTrainer;
    private JComboBox<String> cmbLockerZone;
    private JTextField txtLockerRow;
    private JTextField txtLockerNo;
    private JLabel lblCalculatedFee;

    public RegisterMemberPanel(GymService gymService) {
        this.gymService = gymService;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        loadNextMemberId();
        updateCalculatedFee();
    }

    private void initComponents() {
        // Title banner
        JLabel lblTitle = new JLabel("REGISTER NEW GYM MEMBER");
        lblTitle.setForeground(Color.BLACK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Center form panel
        JPanel formContainer = new JPanel(new GridLayout(13, 2, 8, 8));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.BLACK, 1), "Member & Locker Enrollment Form", 0, 0, null, Color.BLACK));

        txtMemberId = createTextField();
        txtFullName = createTextField();
        txtContact = createTextField();
        txtEmergencyName = createTextField();
        txtEmergencyContact = createTextField();
        txtAddress = createTextField();

        String[] types = {"Basic", "Premium", "Student"};
        cmbMembershipType = new JComboBox<>(types);
        styleComboBox(cmbMembershipType);

        String[] durations = {"Monthly", "Quarterly", "Yearly", "Occasional"};
        cmbDuration = new JComboBox<>(durations);
        styleComboBox(cmbDuration);

        txtTrainer = createTextField();
        txtTrainer.setText("None");

        String[] zones = {"Men's", "Women's", "Unisex"};
        cmbLockerZone = new JComboBox<>(zones);
        styleComboBox(cmbLockerZone);

        txtLockerRow = createTextField();
        txtLockerRow.setText("Row-1");
        txtLockerNo = createTextField();
        txtLockerNo.setText("101");

        lblCalculatedFee = new JLabel("Rs. 0.0");
        lblCalculatedFee.setForeground(Color.BLACK);

        // Recalculate fee dynamically when plan or duration changes
        cmbMembershipType.addActionListener(e -> updateCalculatedFee());
        cmbDuration.addActionListener(e -> updateCalculatedFee());

        // ID row with Auto Generate button
        JPanel idPanel = new JPanel(new BorderLayout(5, 0));
        idPanel.setBackground(Color.WHITE);
        idPanel.add(txtMemberId, BorderLayout.CENTER);
        JButton btnAutoId = createButton("Auto ID");
        btnAutoId.addActionListener(e -> loadNextMemberId());
        idPanel.add(btnAutoId, BorderLayout.EAST);

        addFormField(formContainer, "Member ID *:", idPanel);
        addFormField(formContainer, "Full Name *:", txtFullName);
        addFormField(formContainer, "Contact Number *:", txtContact);
        addFormField(formContainer, "Emergency Contact Name *:", txtEmergencyName);
        addFormField(formContainer, "Emergency Contact Phone *:", txtEmergencyContact);
        addFormField(formContainer, "Address *:", txtAddress);
        addFormField(formContainer, "Membership Plan *:", cmbMembershipType);
        addFormField(formContainer, "Plan Duration *:", cmbDuration);
        addFormField(formContainer, "Assigned Trainer (Optional):", txtTrainer);
        addFormField(formContainer, "Locker Zone *:", cmbLockerZone);
        addFormField(formContainer, "Locker Row *:", txtLockerRow);
        addFormField(formContainer, "Locker Number *:", txtLockerNo);
        addFormField(formContainer, "Membership Fee:", lblCalculatedFee);

        JScrollPane scroll = new JScrollPane(formContainer);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(Color.BLACK, 1));
        add(scroll, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnRegister = createButton("Register Member");
        btnRegister.addActionListener(e -> handleRegister());

        JButton btnClear = createButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnClear);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, String labelText, Object component) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.BLACK);
        panel.add(label);
        if (component instanceof java.awt.Component) {
            panel.add((java.awt.Component) component);
        }
    }

    private void updateCalculatedFee() {
        try {
            String type = (String) cmbMembershipType.getSelectedItem();
            String dur = (String) cmbDuration.getSelectedItem();
            if ("Occasional".equalsIgnoreCase(dur)) {
                lblCalculatedFee.setText("Pay as you go");
            } else {
                double fee = gymService.calculateFee(type, dur);
                lblCalculatedFee.setText("Rs. " + fee);
            }
        } catch (Exception e) {
            lblCalculatedFee.setText("Rs. 0.0");
        }
    }

    private void loadNextMemberId() {
        try {
            txtMemberId.setText(gymService.generateNextMemberId());
        } catch (GymException e) {
            txtMemberId.setText("M1001");
        }
    }

    private void handleRegister() {
        try {
            String memberId = txtMemberId.getText().trim();
            String fullName = txtFullName.getText().trim();
            String contact = txtContact.getText().trim();
            String emName = txtEmergencyName.getText().trim();
            String emContact = txtEmergencyContact.getText().trim();
            String address = txtAddress.getText().trim();
            String type = (String) cmbMembershipType.getSelectedItem();
            String duration = (String) cmbDuration.getSelectedItem();
            String trainer = txtTrainer.getText().trim();
            String zone = (String) cmbLockerZone.getSelectedItem();
            String row = txtLockerRow.getText().trim();
            String lockerNo = txtLockerNo.getText().trim();

            Membership membership = MembershipFactory.createMembership(type, duration);
            LocalDate joiningDate = LocalDate.now();
            LocalDate expiryDate = null;
            if (!"Occasional".equalsIgnoreCase(duration)) {
                int months = membership.getDurationMonths();
                expiryDate = joiningDate.plusMonths(months);
            }

            Locker locker = new Locker(memberId, zone, row, lockerNo);

            Member member = new Member(
                    memberId,
                    fullName,
                    contact,
                    emName,
                    emContact,
                    address,
                    membership,
                    trainer,
                    joiningDate,
                    expiryDate,
                    "Active",
                    locker
            );

            gymService.registerMember(member);
            JOptionPane.showMessageDialog(this,
                    "Member '" + fullName + "' successfully registered with Member ID: " + memberId +
                    "\nAssigned Locker: " + locker.getLocationDisplay(),
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
        } catch (GymException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        loadNextMemberId();
        txtFullName.setText("");
        txtContact.setText("");
        txtEmergencyName.setText("");
        txtEmergencyContact.setText("");
        txtAddress.setText("");
        txtTrainer.setText("None");
        txtLockerRow.setText("Row-1");
        txtLockerNo.setText("101");
        cmbMembershipType.setSelectedIndex(0);
        cmbDuration.setSelectedIndex(0);
        cmbLockerZone.setSelectedIndex(0);
        updateCalculatedFee();
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(new LineBorder(Color.BLACK, 1));
        return field;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setBackground(Color.WHITE);
        box.setForeground(Color.BLACK);
        box.setBorder(new LineBorder(Color.BLACK, 1));
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
