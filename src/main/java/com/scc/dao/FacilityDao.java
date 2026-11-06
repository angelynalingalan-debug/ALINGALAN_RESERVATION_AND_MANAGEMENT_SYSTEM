package com.scc.dao;

import com.scc.model.Facility;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacilityDao {

    public List<Facility> getAllFacilities() {

        List<Facility> facilities = new ArrayList<>();

        String sql = "SELECT * FROM facilities ORDER BY facility_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                facilities.add(new Facility(
                        rs.getInt("facility_id"),
                        rs.getString("facility_name"),
                        rs.getString("location"),
                        rs.getString("availability")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Facility error: " + e.getMessage());
        }

        return facilities;
    }
}
