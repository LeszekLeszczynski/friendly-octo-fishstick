package com.decerto.leszek.cr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyReportService {

    @Autowired
    private DataSource dataSource;

    public List<PolicySummary> getActivePoliciesForAgent(Long agentId) {
        List<PolicySummary> results = new ArrayList<>();

        try {
            Connection conn = dataSource.getConnection();

            Statement stmt = conn.createStatement();
            String sql = "SELECT p.id, p.number, p.premium, c.name " +
                         "FROM policies p JOIN clients c ON p.client_id = c.id " +
                         "WHERE p.agent_id = " + agentId +
                         " AND p.status = 'ACTIVE'";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                PolicySummary summary = new PolicySummary();
                summary.setId(rs.getLong("id"));
                summary.setNumber(rs.getString("number"));
                summary.setPremium(rs.getBigDecimal("premium"));
                summary.setClientName(rs.getString("name"));
                results.add(summary);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public PolicySummary getPolicyDetails(String policyNumber) {
        List<PolicySummary> all = getActivePoliciesForAgent(null);
        return all.stream()
                  .filter(p -> p.getNumber().equals(policyNumber))
                  .findFirst()
                  .orElse(null);
    }
}
