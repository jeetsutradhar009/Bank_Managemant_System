package com.bank.controller;

import com.bank.dao.AdminDAO;
import com.bank.model.Account;
import com.bank.util.AdminAuth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/add-balance")
public class AddBalanceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private AdminDAO adminDAO;

    @Override
    public void init() {
        adminDAO = new AdminDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!AdminAuth.requireAdmin(request, response)) {
            return;
        }

        request.setAttribute("accounts", adminDAO.getAllAccounts());

        request.getRequestDispatcher("/add-balance.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!AdminAuth.requireAdmin(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String accountNumber = firstValue(
                request.getParameter("accountNumber"),
                request.getParameter("accountNo"),
                request.getParameter("accNo")
        );

        String customerId = firstValue(
                request.getParameter("customerId"),
                request.getParameter("custId"),
                request.getParameter("cust_id")
        );

        String amountStr = firstValue(
                request.getParameter("amount"),
                request.getParameter("balance"),
                request.getParameter("addAmount")
        );

        String note = firstValue(
                request.getParameter("note"),
                request.getParameter("remarks"),
                request.getParameter("description")
        );

        if (isBlank(accountNumber) && isBlank(customerId)) {
            redirect(response, request, "err", "Please enter Account Number or Customer ID.");
            return;
        }

        if (isBlank(amountStr)) {
            redirect(response, request, "err", "Please enter amount.");
            return;
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(amountStr.trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                redirect(response, request, "err", "Amount must be greater than zero.");
                return;
            }

        } catch (NumberFormatException e) {
            redirect(response, request, "err", "Invalid amount.");
            return;
        }

        try {
            Account account = null;

            if (!isBlank(accountNumber)) {
                account = adminDAO.getAccountByAccountNumber(accountNumber.trim());
            }

            if (account == null && !isBlank(customerId)) {
                account = adminDAO.getAccountByCustomerId(customerId.trim());
            }

            if (account == null) {
                redirect(response, request, "err", "Account not found. Please check Account Number or Customer ID.");
                return;
            }

            String finalAccountNumber = account.getAccountNumber();

            boolean updated = adminDAO.addBalance(
                    finalAccountNumber,
                    amount.doubleValue(),
                    isBlank(note) ? "Admin balance credit" : note.trim()
            );

            if (updated) {
                redirect(response, request, "msg",
                        "Balance added successfully to account " + finalAccountNumber + ".");
            } else {
                redirect(response, request, "err",
                        "Balance not added. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirect(response, request, "err",
                    "Something went wrong while adding balance.");
        }
    }

    private String firstValue(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void redirect(HttpServletResponse response,
                          HttpServletRequest request,
                          String type,
                          String message) throws IOException {

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        response.sendRedirect(
                request.getContextPath()
                        + "/admin/add-balance?"
                        + type
                        + "="
                        + encodedMessage
        );
    }
}