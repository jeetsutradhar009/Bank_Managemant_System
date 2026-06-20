package com.bank.controller;

import com.bank.dao.AdminDAO;
import com.bank.util.AdminAuth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/accounts/create")
public class CreateAccountServlet extends HttpServlet {

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

        request.getRequestDispatcher("/create-account.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!AdminAuth.requireAdmin(request, response)) {
            return;
        }

        try {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String dob = request.getParameter("dob");
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");
            String address = request.getParameter("address");
            String accountType = request.getParameter("accountType");
            String openingBalanceStr = request.getParameter("openingBalance");

            if (isBlank(firstName) || isBlank(lastName) || isBlank(dob)
                    || isBlank(phone) || isBlank(email) || isBlank(address)
                    || isBlank(accountType) || isBlank(openingBalanceStr)) {

                response.sendRedirect(request.getContextPath()
                        + "/admin/accounts/create?err=All fields are required");
                return;
            }

            double openingBalance = Double.parseDouble(openingBalanceStr);

            if (openingBalance < 500) {
                response.sendRedirect(request.getContextPath()
                        + "/admin/accounts/create?err=Initial deposit must be minimum ₹500");
                return;
            }

            String accountNumber = adminDAO.createCustomerAndAccount(
                    firstName,
                    lastName,
                    dob,
                    phone,
                    email,
                    address,
                    accountType,
                    openingBalance
            );

            if (accountNumber != null) {
                response.sendRedirect(request.getContextPath()
                        + "/admin/accounts/create?msg=Account created successfully. Account No: " + accountNumber);
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/admin/accounts/create?err=Account creation failed");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/accounts/create?err=Invalid opening balance");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()
                    + "/admin/accounts/create?err=Something went wrong");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}