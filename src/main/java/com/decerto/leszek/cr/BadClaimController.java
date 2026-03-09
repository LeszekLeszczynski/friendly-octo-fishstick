package com.decerto.leszek.cr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * REST controller for submitting insurance claims.
 *
 * <p>When a claim is submitted, the payout is calculated based on the policy type:
 * BASIC (70%), STANDARD (85%), PREMIUM (95%), other (50%) — capped at the policy's
 * coverage limit. The claim is persisted with status PENDING.
 */
@RestController
@RequestMapping("/claims")
public class BadClaimController {

    @Autowired private DataSource ds;
    private static final RestTemplate rest = new RestTemplate();
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Map<String, Object> claimCache = new HashMap<>();
    private static final String FRAUD_URL = "https://api.example.com/fraud/check?key=abc123secret&name=";
    private static double totalPayouts = 0;

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Map<String, Object> body) {
        String claimant = (String) body.get("claimant");
        String policyId = (String) body.get("policyId");
        double amount = (double) body.get("amount");
        String description = (String) body.get("description");

        Map<String, Object> result = new HashMap<>();

        try {
            Connection c = ds.getConnection();
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT coverage_limit, type FROM policies WHERE id = '" + policyId + "'");

            if (!rs.next()) {
                result.put("status", "error");
                result.put("message", "Policy not found");
                return result;
            }

            double coverageLimit = rs.getDouble("coverage_limit");
            String policyType = rs.getString("type");

            if (amount > coverageLimit) {
                amount = coverageLimit;
            }

            double payout;
            if (policyType.equals("BASIC")) {
                payout = amount * 0.7;
            } else if (policyType.equals("STANDARD")) {
                payout = amount * 0.85;
            } else if (policyType.equals("PREMIUM")) {
                payout = amount * 0.95;
            } else {
                payout = amount * 0.5;
            }

            String claimId = UUID.randomUUID().toString();
            String now = fmt.format(new java.util.Date());

            st.executeUpdate("INSERT INTO claims (id, claimant, policy_id, amount, payout, status, created_at, description) " +
                "VALUES ('" + claimId + "', '" + claimant + "', '" + policyId + "', " + amount +
                ", " + payout + ", 'PENDING', '" + now + "', '" + description + "')");

            totalPayouts += payout;
            claimCache.put(claimId, result);

            result.put("claimId", claimId);
            result.put("payout", payout);
            result.put("status", "PENDING");

            System.out.println("Claim submitted: " + claimId + " by " + claimant + " for $" + amount);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
        }

        return result;
    }
}