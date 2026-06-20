package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.util.DBConnection;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDAO {

    private static final String DEFAULT_IFSC = "DKSB0001886";
    private static final SecureRandom random = new SecureRandom();

    /* =====================================================
       DASHBOARD COUNTS
    ===================================================== */

    public int getTotalUsers() {
        return getCount("SELECT COUNT(*) FROM users WHERE role = 'USER'");
    }

    public int getTotalAccounts() {
        return getCount("SELECT COUNT(*) FROM accounts");
    }

    public int getTotalTransactions() {
        return getCount("SELECT COUNT(*) FROM transactions");
    }

    private int getCount(String sql) {
        int count = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    /* =====================================================
       RECENT DASHBOARD DATA
    ===================================================== */

    public List<User> getRecentUsers(int limit) {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id,
                       customer_id,
                       first_name,
                       last_name,
                       full_name,
                       dob,
                       address,
                       email,
                       phone,
                       password,
                       role,
                       online_banking_enabled
                FROM users
                ORDER BY user_id DESC
                LIMIT ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public List<Account> getRecentAccounts(int limit) {
        List<Account> accounts = new ArrayList<>();

        String sql = """
                SELECT account_id,
                       user_id,
                       account_number,
                       COALESCE(ifsc_code, ?) AS ifsc_code,
                       account_type,
                       balance,
                       COALESCE(status, 'ACTIVE') AS status,
                       created_at
                FROM accounts
                ORDER BY account_id DESC
                LIMIT ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, DEFAULT_IFSC);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapAccount(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return accounts;
    }

    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> transactions = new ArrayList<>();

        String sql = """
                SELECT transaction_id,
                       sender_account,
                       receiver_account,
                       amount,
                       transaction_type,
                       status,
                       transaction_date
                FROM transactions
                ORDER BY transaction_date DESC
                LIMIT ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /* =====================================================
       ALL DATA
    ===================================================== */

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id,
                       customer_id,
                       first_name,
                       last_name,
                       full_name,
                       dob,
                       address,
                       email,
                       phone,
                       password,
                       role,
                       online_banking_enabled
                FROM users
                ORDER BY user_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();

        String sql = """
                SELECT account_id,
                       user_id,
                       account_number,
                       COALESCE(ifsc_code, ?) AS ifsc_code,
                       account_type,
                       balance,
                       COALESCE(status, 'ACTIVE') AS status,
                       created_at
                FROM accounts
                ORDER BY account_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, DEFAULT_IFSC);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapAccount(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return accounts;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        String sql = """
                SELECT transaction_id,
                       sender_account,
                       receiver_account,
                       amount,
                       transaction_type,
                       status,
                       transaction_date
                FROM transactions
                ORDER BY transaction_date DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /* =====================================================
       SEARCH
    ===================================================== */

    public List<User> searchUsers(String keyword) {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id,
                       customer_id,
                       first_name,
                       last_name,
                       full_name,
                       dob,
                       address,
                       email,
                       phone,
                       password,
                       role,
                       online_banking_enabled
                FROM users
                WHERE full_name LIKE ?
                   OR email LIKE ?
                   OR phone LIKE ?
                   OR customer_id LIKE ?
                   OR role LIKE ?
                ORDER BY user_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            ps.setString(5, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public List<Account> searchAccounts(String keyword) {
        List<Account> accounts = new ArrayList<>();

        String sql = """
                SELECT account_id,
                       user_id,
                       account_number,
                       COALESCE(ifsc_code, ?) AS ifsc_code,
                       account_type,
                       balance,
                       COALESCE(status, 'ACTIVE') AS status,
                       created_at
                FROM accounts
                WHERE account_number LIKE ?
                   OR ifsc_code LIKE ?
                   OR account_type LIKE ?
                   OR status LIKE ?
                   OR CAST(balance AS CHAR) LIKE ?
                ORDER BY account_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setString(1, DEFAULT_IFSC);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            ps.setString(5, search);
            ps.setString(6, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapAccount(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return accounts;
    }

    public List<Transaction> searchTransactions(String keyword) {
        List<Transaction> transactions = new ArrayList<>();

        String sql = """
                SELECT transaction_id,
                       sender_account,
                       receiver_account,
                       amount,
                       transaction_type,
                       status,
                       transaction_date
                FROM transactions
                WHERE sender_account LIKE ?
                   OR receiver_account LIKE ?
                   OR transaction_type LIKE ?
                   OR status LIKE ?
                   OR CAST(amount AS CHAR) LIKE ?
                ORDER BY transaction_date DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            ps.setString(5, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /* =====================================================
       ADD USER - AddUserServlet Fix
    ===================================================== */

    public boolean addUser(String fullName,
                           String email,
                           String phone,
                           String password,
                           String role) {

        String customerId = generateCustomerId();

        String[] nameParts = splitName(fullName);
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        String sql = """
                INSERT INTO users
                (customer_id, first_name, last_name, full_name, dob, address, email, phone, password, role, online_banking_enabled)
                VALUES (?, ?, ?, ?, NULL, '', ?, ?, ?, ?, 1)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, customerId);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, fullName);
            ps.setString(5, email);
            ps.setString(6, phone);
            ps.setString(7, password);
            ps.setString(8, role);

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                logAction("ADD_USER", "Admin added user: " + fullName + " (" + email + ")");
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =====================================================
       CREATE CUSTOMER + ACCOUNT - CreateAccountServlet Fix
    ===================================================== */

    public String createCustomerAndAccount(String firstName,
                                           String lastName,
                                           String dob,
                                           String phone,
                                           String email,
                                           String address,
                                           String accountType,
                                           double openingBalance) {

        Connection con = null;

        try {
            con = DBConnection.getConnection();

            if (con == null) {
                return null;
            }

            con.setAutoCommit(false);

            String customerId = generateCustomerId();
            String accountNumber = generateAccountNumber();
            String fullName = (firstName + " " + lastName).trim();

            String userSql = """
                    INSERT INTO users
                    (customer_id, first_name, last_name, full_name, dob, address, email, phone, password, role, online_banking_enabled)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL, 'USER', 0)
                    """;

            int userId;

            try (PreparedStatement ps = con.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, customerId);
                ps.setString(2, firstName);
                ps.setString(3, lastName);
                ps.setString(4, fullName);
                ps.setDate(5, Date.valueOf(dob));
                ps.setString(6, address);
                ps.setString(7, email);
                ps.setString(8, phone);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    con.rollback();
                    return null;
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    } else {
                        con.rollback();
                        return null;
                    }
                }
            }

            String accountSql = """
                    INSERT INTO accounts
                    (user_id, account_number, ifsc_code, account_type, balance, status)
                    VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                    """;

            try (PreparedStatement ps = con.prepareStatement(accountSql)) {
                ps.setInt(1, userId);
                ps.setString(2, accountNumber);
                ps.setString(3, DEFAULT_IFSC);
                ps.setString(4, accountType);
                ps.setDouble(5, openingBalance);
                ps.executeUpdate();
            }

            con.commit();

            logAction("CREATE_ACCOUNT", "Created account " + accountNumber + " for " + fullName);

            return accountNumber;

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /* =====================================================
       FREEZE / UNFREEZE ACCOUNT - FreezeAccountServlet Fix
    ===================================================== */

    public boolean changeAccountStatus(String accountNumber, String newStatus) {
        String sql = """
                UPDATE accounts
                SET status = ?
                WHERE account_number = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, accountNumber);

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                logAction("ACCOUNT_STATUS", "Account " + accountNumber + " status changed to " + newStatus);
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =====================================================
       ADD BALANCE - AddBalanceServlet Fix
    ===================================================== */

    public boolean addBalance(String accountNumber, double amount, String note) {
        String sql = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE account_number = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setString(2, accountNumber);

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                logAction("ADD_BALANCE", "Added ₹" + amount + " to " + accountNumber + ". Note: " + note);
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean addBalance(String accountNumber, double amount) {
        return addBalance(accountNumber, amount, "");
    }

    public boolean addBalanceByAccountNumber(String accountNumber, double amount) {
        return addBalance(accountNumber, amount, "");
    }

    public boolean addBalanceByCustomerId(String customerId, double amount) {
        String sql = """
                UPDATE accounts a
                INNER JOIN users u ON a.user_id = u.user_id
                SET a.balance = a.balance + ?
                WHERE u.customer_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setString(2, customerId);

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                logAction("ADD_BALANCE", "Added ₹" + amount + " by customer ID " + customerId);
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =====================================================
       GET ACCOUNT BY ACCOUNT/CUSTOMER
    ===================================================== */

    public Account getAccountByAccountNumber(String accountNumber) {
        Account account = null;

        String sql = """
                SELECT account_id,
                       user_id,
                       account_number,
                       COALESCE(ifsc_code, ?) AS ifsc_code,
                       account_type,
                       balance,
                       COALESCE(status, 'ACTIVE') AS status,
                       created_at
                FROM accounts
                WHERE account_number = ?
                LIMIT 1
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, DEFAULT_IFSC);
            ps.setString(2, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    account = mapAccount(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return account;
    }

    public Account getAccountByCustomerId(String customerId) {
        Account account = null;

        String sql = """
                SELECT a.account_id,
                       a.user_id,
                       a.account_number,
                       COALESCE(a.ifsc_code, ?) AS ifsc_code,
                       a.account_type,
                       a.balance,
                       COALESCE(a.status, 'ACTIVE') AS status,
                       a.created_at
                FROM accounts a
                INNER JOIN users u ON a.user_id = u.user_id
                WHERE u.customer_id = ?
                LIMIT 1
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, DEFAULT_IFSC);
            ps.setString(2, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    account = mapAccount(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return account;
    }

    /* =====================================================
       AUDIT LOGS - GenerateReportServlet/AuditLogsServlet Fix
    ===================================================== */

    public void logAction(String action, String description) {
        String sql = """
                INSERT INTO audit_logs
                (action, description)
                VALUES (?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, action);
            ps.setString(2, description);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAuditLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();

        String sql = """
                SELECT log_id,
                       action,
                       description,
                       created_at
                FROM audit_logs
                ORDER BY created_at DESC
                LIMIT 100
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();

                log.put("logId", rs.getInt("log_id"));
                log.put("action", rs.getString("action"));
                log.put("description", rs.getString("description"));
                log.put("createdAt", rs.getTimestamp("created_at"));

                logs.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }

    /* =====================================================
       HELPERS
    ===================================================== */

    private String generateCustomerId() {
        long number = 1000000000L + Math.floorMod(random.nextLong(), 9000000000L);
        return String.valueOf(number);
    }

    private String generateAccountNumber() {
        long number = 100000000000L + Math.floorMod(random.nextLong(), 900000000000L);
        return "ACC" + number;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }

        String trimmed = fullName.trim();
        int index = trimmed.indexOf(" ");

        if (index == -1) {
            return new String[]{trimmed, ""};
        }

        String firstName = trimmed.substring(0, index).trim();
        String lastName = trimmed.substring(index + 1).trim();

        return new String[]{firstName, lastName};
    }

    /* =====================================================
       MAPPERS
    ===================================================== */

    private User mapUser(ResultSet rs) throws Exception {
        User user = new User();

        user.setUserId(rs.getInt("user_id"));
        user.setCustomerId(rs.getString("customer_id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setFullName(rs.getString("full_name"));
        user.setDob(rs.getDate("dob"));
        user.setAddress(rs.getString("address"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setOnlineBankingEnabled(rs.getInt("online_banking_enabled") == 1);

        return user;
    }

    private Account mapAccount(ResultSet rs) throws Exception {
        Account account = new Account();

        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setIfscCode(rs.getString("ifsc_code"));
        account.setAccountType(rs.getString("account_type"));
        account.setBalance(rs.getDouble("balance"));
        account.setStatus(rs.getString("status"));

        try {
            account.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (Exception ignored) {
            account.setCreatedAt(null);
        }

        return account;
    }

    private Transaction mapTransaction(ResultSet rs) throws Exception {
        Transaction transaction = new Transaction();

        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setSenderAccount(rs.getString("sender_account"));
        transaction.setReceiverAccount(rs.getString("receiver_account"));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setStatus(rs.getString("status"));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date"));

        return transaction;
    }
}