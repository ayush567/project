package database;

import exceptions.InvalidInputException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Locker;
import model.Member;
import model.Membership;
import model.Renewal;
import service.MembershipFactory;

// Data Access Object (DAO) Pattern: Separates MySQL CRUD operations from business logic
public class MemberDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // CREATE: Inserts new member and assigns locker using a database transaction
    public boolean addMember(Member member) throws SQLException {
        String memberSql = "INSERT INTO members (member_id, full_name, contact_number, emergency_contact_name, " +
                "emergency_contact_number, address, membership_type, duration, membership_fee, " +
                "trainer_name, joining_date, expiry_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String lockerSql = "INSERT INTO lockers (member_id, zone, `row_number`, locker_number) VALUES (?, ?, ?, ?)";

        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement psMember = conn.prepareStatement(memberSql)) {
                psMember.setString(1, member.getMemberId());
                psMember.setString(2, member.getFullName());
                psMember.setString(3, member.getContactNumber());
                psMember.setString(4, member.getEmergencyContactName());
                psMember.setString(5, member.getEmergencyContactNumber());
                psMember.setString(6, member.getAddress());
                psMember.setString(7, member.getMembership().getType());
                psMember.setString(8, member.getMembership().getDuration());
                psMember.setDouble(9, member.getMembershipFee());
                psMember.setString(10, member.getTrainerName());
                psMember.setDate(11, Date.valueOf(member.getJoiningDate()));
                if (member.getExpiryDate() != null) {
                    psMember.setDate(12, Date.valueOf(member.getExpiryDate()));
                } else {
                    psMember.setNull(12, Types.DATE);
                }
                psMember.setString(13, member.getStatus());
                psMember.executeUpdate();
            }

            if (member.getLocker() != null) {
                try (PreparedStatement psLocker = conn.prepareStatement(lockerSql)) {
                    psLocker.setString(1, member.getMemberId());
                    psLocker.setString(2, member.getLocker().getZone());
                    psLocker.setString(3, member.getLocker().getRowNumber());
                    psLocker.setString(4, member.getLocker().getLockerNumber());
                    psLocker.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    // READ: Retrieves a single member by their ID
    public Member getMemberById(String memberId) throws SQLException {
        String sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number " +
                     "FROM members m LEFT JOIN lockers l ON m.member_id = l.member_id " +
                     "WHERE m.member_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                }
            }
        }
        return null;
    }

    // READ: Retrieves all members
    public List<Member> getAllMembers() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number " +
                     "FROM members m LEFT JOIN lockers l ON m.member_id = l.member_id " +
                     "ORDER BY m.member_id ASC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMember(rs));
            }
        }
        return list;
    }

    // READ: Retrieves all members enrolled under the Occasional plan duration
    public List<Member> getOccasionalMembers() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number " +
                     "FROM members m LEFT JOIN lockers l ON m.member_id = l.member_id " +
                     "WHERE m.duration = 'Occasional' " +
                     "ORDER BY m.member_id ASC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMember(rs));
            }
        }
        return list;
    }

    // READ: Searches members by specific column criteria
    public List<Member> searchMembers(String criteria, String keyword) throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql;

        if ("Member ID".equalsIgnoreCase(criteria)) {
            sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number FROM members m " +
                  "LEFT JOIN lockers l ON m.member_id = l.member_id WHERE m.member_id LIKE ? ORDER BY m.member_id ASC";
        } else if ("Member Name".equalsIgnoreCase(criteria)) {
            sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number FROM members m " +
                  "LEFT JOIN lockers l ON m.member_id = l.member_id WHERE m.full_name LIKE ? ORDER BY m.member_id ASC";
        } else if ("Membership Type".equalsIgnoreCase(criteria)) {
            sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number FROM members m " +
                  "LEFT JOIN lockers l ON m.member_id = l.member_id WHERE m.membership_type LIKE ? ORDER BY m.member_id ASC";
        } else if ("Status".equalsIgnoreCase(criteria)) {
            sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number FROM members m " +
                  "LEFT JOIN lockers l ON m.member_id = l.member_id WHERE m.status LIKE ? ORDER BY m.member_id ASC";
        } else if ("Locker Location".equalsIgnoreCase(criteria)) {
            sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number FROM members m " +
                  "LEFT JOIN lockers l ON m.member_id = l.member_id " +
                  "WHERE (l.zone LIKE ? OR l.`row_number` LIKE ? OR l.locker_number LIKE ?) ORDER BY m.member_id ASC";
        } else {
            // General multi-field search
            sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number FROM members m " +
                  "LEFT JOIN lockers l ON m.member_id = l.member_id " +
                  "WHERE (m.member_id LIKE ? OR m.full_name LIKE ? OR m.contact_number LIKE ? " +
                  "OR m.membership_type LIKE ? OR m.status LIKE ? OR l.locker_number LIKE ?) ORDER BY m.member_id ASC";
        }

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            if ("Locker Location".equalsIgnoreCase(criteria)) {
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
            } else if (!"Member ID".equalsIgnoreCase(criteria) && !"Member Name".equalsIgnoreCase(criteria) &&
                       !"Membership Type".equalsIgnoreCase(criteria) && !"Status".equalsIgnoreCase(criteria)) {
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
                ps.setString(4, pattern);
                ps.setString(5, pattern);
                ps.setString(6, pattern);
            } else {
                ps.setString(1, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMember(rs));
                }
            }
        }
        return list;
    }

    // UPDATE: Updates member's personal and plan details
    public boolean updateMember(Member member) throws SQLException {
        String sql = "UPDATE members SET full_name = ?, contact_number = ?, emergency_contact_name = ?, " +
                     "emergency_contact_number = ?, address = ?, membership_type = ?, duration = ?, " +
                     "membership_fee = ?, trainer_name = ?, status = ? WHERE member_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, member.getFullName());
            ps.setString(2, member.getContactNumber());
            ps.setString(3, member.getEmergencyContactName());
            ps.setString(4, member.getEmergencyContactNumber());
            ps.setString(5, member.getAddress());
            ps.setString(6, member.getMembership().getType());
            ps.setString(7, member.getMembership().getDuration());
            ps.setDouble(8, member.getMembershipFee());
            ps.setString(9, member.getTrainerName());
            ps.setString(10, member.getStatus());
            ps.setString(11, member.getMemberId());

            return ps.executeUpdate() > 0;
        }
    }

    // UPDATE: Updates only the membership status
    public boolean updateStatus(String memberId, String newStatus) throws SQLException {
        String sql = "UPDATE members SET status = ? WHERE member_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, memberId);
            return ps.executeUpdate() > 0;
        }
    }

    // UPDATE: Updates or inserts locker assignment
    public boolean updateLocker(String memberId, Locker locker) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM lockers WHERE member_id = ?";
        boolean exists = false;
        try (PreparedStatement psCheck = getConnection().prepareStatement(checkSql)) {
            psCheck.setString(1, memberId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        }

        if (exists) {
            String updateSql = "UPDATE lockers SET zone = ?, `row_number` = ?, locker_number = ? WHERE member_id = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(updateSql)) {
                ps.setString(1, locker.getZone());
                ps.setString(2, locker.getRowNumber());
                ps.setString(3, locker.getLockerNumber());
                ps.setString(4, memberId);
                return ps.executeUpdate() > 0;
            }
        } else {
            String insertSql = "INSERT INTO lockers (member_id, zone, `row_number`, locker_number) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(insertSql)) {
                ps.setString(1, memberId);
                ps.setString(2, locker.getZone());
                ps.setString(3, locker.getRowNumber());
                ps.setString(4, locker.getLockerNumber());
                return ps.executeUpdate() > 0;
            }
        }
    }

    // RENEWAL: Inserts renewal history and updates member expiry date and status
    public boolean recordRenewal(Renewal renewal) throws SQLException {
        String renewalSql = "INSERT INTO renewals (member_id, renewal_date, new_expiry_date, amount_paid, payment_mode) " +
                            "VALUES (?, ?, ?, ?, ?)";
        String memberSql = "UPDATE members SET expiry_date = ?, status = 'Renewed' WHERE member_id = ?";

        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement psRen = conn.prepareStatement(renewalSql)) {
                psRen.setString(1, renewal.getMemberId());
                psRen.setDate(2, Date.valueOf(renewal.getRenewalDate()));
                psRen.setDate(3, Date.valueOf(renewal.getNewExpiryDate()));
                psRen.setDouble(4, renewal.getAmountPaid());
                psRen.setString(5, renewal.getPaymentMode());
                psRen.executeUpdate();
            }

            try (PreparedStatement psMem = conn.prepareStatement(memberSql)) {
                psMem.setDate(1, Date.valueOf(renewal.getNewExpiryDate()));
                psMem.setString(2, renewal.getMemberId());
                psMem.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    // READ: Retrieves renewal history for a specific member
    public List<Renewal> getRenewalsByMemberId(String memberId) throws SQLException {
        List<Renewal> list = new ArrayList<>();
        String sql = "SELECT * FROM renewals WHERE member_id = ? ORDER BY renewal_date DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Renewal ren = new Renewal(
                            rs.getInt("renewal_id"),
                            rs.getString("member_id"),
                            rs.getDate("renewal_date").toLocalDate(),
                            rs.getDate("new_expiry_date").toLocalDate(),
                            rs.getDouble("amount_paid"),
                            rs.getString("payment_mode")
                    );
                    list.add(ren);
                }
            }
        }
        return list;
    }

    // READ: Retrieves members who are due for renewal or expired (excludes Occasional members)
    public List<Member> getMembersDueForRenewal() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT m.*, l.locker_id, l.zone, l.`row_number`, l.locker_number " +
                     "FROM members m LEFT JOIN lockers l ON m.member_id = l.member_id " +
                     "WHERE (m.status IN ('Due for Renewal', 'Expired') OR (m.expiry_date IS NOT NULL AND m.expiry_date <= CURRENT_DATE())) " +
                     "AND m.duration != 'Occasional' " +
                     "ORDER BY m.expiry_date ASC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMember(rs));
            }
        }
        return list;
    }

    // DELETE: Removes a member record (cascades to locker, renewals, and attendance in DB)
    public boolean deleteMember(String memberId) throws SQLException {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            return ps.executeUpdate() > 0;
        }
    }

    // Checks whether a member ID already exists
    public boolean isMemberIdExists(String memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM members WHERE member_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Generates the next sequential Member ID, e.g., M1001, M1002
    public String getNextMemberId() throws SQLException {
        String sql = "SELECT member_id FROM members WHERE member_id LIKE 'M%' ORDER BY member_id DESC LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString(1);
                try {
                    int numericPart = Integer.parseInt(lastId.substring(1));
                    return "M" + (numericPart + 1);
                } catch (NumberFormatException e) {
                    // fall through
                }
            }
        }
        return "M1001";
    }

    // Helper: Maps a SQL ResultSet row to a Member model instance
    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        String memberId = rs.getString("member_id");
        String name = rs.getString("full_name");
        String contact = rs.getString("contact_number");
        String emContactName = rs.getString("emergency_contact_name");
        String emContactNo = rs.getString("emergency_contact_number");
        String address = rs.getString("address");
        String planType = rs.getString("membership_type");
        String duration = rs.getString("duration");
        double fee = rs.getDouble("membership_fee");
        String trainer = rs.getString("trainer_name");
        LocalDate joinDate = rs.getDate("joining_date").toLocalDate();
        Date expDateSql = rs.getDate("expiry_date");
        LocalDate expDate = (expDateSql != null) ? expDateSql.toLocalDate() : null;
        String status = rs.getString("status");

        Membership membership;
        try {
            membership = MembershipFactory.createMembership(planType, duration);
        } catch (InvalidInputException e) {
            membership = new model.BasicMembership(duration);
        }

        Locker locker = null;
        String zone = rs.getString("zone");
        if (zone != null) {
            locker = new Locker(
                    rs.getInt("locker_id"),
                    memberId,
                    zone,
                    rs.getString("row_number"),
                    rs.getString("locker_number")
            );
        }

        Member m = new Member(memberId, name, contact, emContactName, emContactNo, address,
                              membership, trainer, joinDate, expDate, status, locker);
        m.setMembershipFee(fee);
        return m;
    }
}
