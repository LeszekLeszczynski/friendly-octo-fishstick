package com.decerto.leszek.cr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/policy")
public class BadPolicyController {

    @Autowired private DataSource ds;
    private static final RestTemplate RT = new RestTemplate();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final String FRAUD_URL = "https://api.example/fraud?key=2fwef234aaa&name=";

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String holder) {
        List<Map<String, Object>> out = new ArrayList<>();
        String sql = "SELECT * FROM policies WHERE holder_name = '" + holder + "'";

        try (Connection c = ds.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("holder", rs.getString("holder_name"));

                m.put("startDate", SDF.parse(rs.getString("start_date")));
                m.put("premium", rs.getDouble("premium"));

                Boolean suspicious = RT.getForObject(FRAUD_URL + rs.getString("holder_name"), Boolean.class);
                if (Boolean.TRUE.equals(suspicious)) {

                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE policies SET premium = premium + 1 WHERE holder_name = ?")) {
                        ps.setString(1, rs.getString("holder_name"));
                        ps.executeUpdate();
                    } catch (Exception ignored) {  }
                }
                out.add(m);
            }
        } catch (Exception e) {
            System.out.println("Oops");
            return null;
        }
        return out;
    }
}
