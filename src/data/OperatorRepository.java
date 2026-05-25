package data;

import data.dto.ApartmentItem;
import data.dto.BenefitTypeItem;
import data.dto.BuildingItem;
import data.dto.ServiceItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperatorRepository extends ResidentRepository {

    public List<BuildingItem> loadBuildings() throws SQLException {
        List<BuildingItem> result = new ArrayList<BuildingItem>();

        String sql = "SELECT building_id, address, apartments_count FROM building ORDER BY address, building_id";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new BuildingItem(
                        rs.getInt("building_id"),
                        rs.getString("address"),
                        rs.getInt("apartments_count")
                ));
            }
        }

        return result;
    }

    public List<ApartmentItem> loadApartments() throws SQLException {
        List<ApartmentItem> result = new ArrayList<ApartmentItem>();

        String sql =
                "SELECT a.apartment_id, a.building_id, b.address, a.apartment_number, a.area " +
                "FROM apartment a " +
                "JOIN building b ON a.building_id = b.building_id " +
                "ORDER BY b.address, a.apartment_number";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new ApartmentItem(
                        rs.getInt("apartment_id"),
                        rs.getInt("building_id"),
                        rs.getString("address"),
                        rs.getInt("apartment_number"),
                        rs.getBigDecimal("area")
                ));
            }
        }

        return result;
    }

    public List<ServiceItem> loadServices() throws SQLException {
        List<ServiceItem> result = new ArrayList<ServiceItem>();

        String sql = "SELECT name, unit, price FROM service ORDER BY name";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new ServiceItem(
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getBigDecimal("price")
                ));
            }
        }

        return result;
    }

    public List<BenefitTypeItem> loadBenefitTypes() throws SQLException {
        List<BenefitTypeItem> result = new ArrayList<BenefitTypeItem>();

        String sql = "SELECT category, discount_percent FROM benefit_type ORDER BY category";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new BenefitTypeItem(
                        rs.getString("category"),
                        rs.getBigDecimal("discount_percent")
                ));
            }
        }

        return result;
    }

    public int addBuilding(String address, int apartmentsCount) throws SQLException {
        String sql = "INSERT INTO building (address, apartments_count) VALUES (?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, address);
            ps.setInt(2, apartmentsCount);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Не удалось получить ID нового дома");
    }

    public void updateBuilding(int buildingId, String address, int apartmentsCount) throws SQLException {
        String sql = "UPDATE building SET address = ?, apartments_count = ? WHERE building_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, address);
            ps.setInt(2, apartmentsCount);
            ps.setInt(3, buildingId);
            ps.executeUpdate();
        }
    }

    public void deleteBuilding(int buildingId) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            List<Integer> apartmentIds = new ArrayList<Integer>();

            String selectApartments = "SELECT apartment_id FROM apartment WHERE building_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectApartments)) {
                ps.setInt(1, buildingId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        apartmentIds.add(Integer.valueOf(rs.getInt("apartment_id")));
                    }
                }
            }

            for (Integer apartmentId : apartmentIds) {
                deleteApartmentInternal(conn, apartmentId.intValue());
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM building WHERE building_id = ?")) {
                ps.setInt(1, buildingId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    public int addApartment(int buildingId, int apartmentNumber, BigDecimal area) throws SQLException {
        String sql = "INSERT INTO apartment (building_id, apartment_number, area) VALUES (?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buildingId);
            ps.setInt(2, apartmentNumber);
            ps.setBigDecimal(3, area.setScale(2, RoundingMode.HALF_UP));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Не удалось получить ID новой квартиры");
    }

    public void updateApartment(int apartmentId, int buildingId, int apartmentNumber, BigDecimal area) throws SQLException {
        String sql = "UPDATE apartment SET building_id = ?, apartment_number = ?, area = ? WHERE apartment_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, buildingId);
            ps.setInt(2, apartmentNumber);
            ps.setBigDecimal(3, area.setScale(2, RoundingMode.HALF_UP));
            ps.setInt(4, apartmentId);
            ps.executeUpdate();
        }
    }

    public void deleteApartment(int apartmentId) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            deleteApartmentInternal(conn, apartmentId);
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    private void deleteApartmentInternal(Connection conn, int apartmentId) throws SQLException {
        Integer residentId = null;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT resident_id FROM resident WHERE apartment_id = ?")) {
            ps.setInt(1, apartmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    residentId = Integer.valueOf(rs.getInt("resident_id"));
                }
            }
        }

        if (residentId != null) {
            deleteResidentInternal(conn, residentId.intValue());
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM apartment WHERE apartment_id = ?")) {
            ps.setInt(1, apartmentId);
            ps.executeUpdate();
        }
    }

    public int addResident(String lastName, String firstName, int apartmentId, String category) throws SQLException {
        String sql = "INSERT INTO resident (last_name, first_name, apartment_id, category) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setInt(3, apartmentId);

            if (category == null || category.trim().length() == 0 || "Без льготы".equalsIgnoreCase(category)) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, category);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Не удалось получить ID нового жильца");
    }

    public void updateResident(int residentId, String lastName, String firstName, int apartmentId, String category) throws SQLException {
        String sql = "UPDATE resident SET last_name = ?, first_name = ?, apartment_id = ?, category = ? WHERE resident_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setInt(3, apartmentId);

            if (category == null || category.trim().length() == 0 || "Без льготы".equalsIgnoreCase(category)) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, category);
            }

            ps.setInt(5, residentId);
            ps.executeUpdate();
        }
    }

    public void deleteResident(int residentId) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            deleteResidentInternal(conn, residentId);
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    private void deleteResidentInternal(Connection conn, int residentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM reading WHERE resident_id = ?")) {
            ps.setInt(1, residentId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM payment WHERE resident_id = ?")) {
            ps.setInt(1, residentId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM resident WHERE resident_id = ?")) {
            ps.setInt(1, residentId);
            ps.executeUpdate();
        }
    }

    public void updateServicePrice(String serviceName, BigDecimal price) throws SQLException {
        String sql = "UPDATE service SET price = ? WHERE name = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBigDecimal(1, price.setScale(2, RoundingMode.HALF_UP));
            ps.setString(2, serviceName);
            ps.executeUpdate();
        }
    }

    public void updateBenefitPercent(String category, BigDecimal percent) throws SQLException {
        String sql = "UPDATE benefit_type SET discount_percent = ? WHERE category = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBigDecimal(1, percent.setScale(2, RoundingMode.HALF_UP));
            ps.setString(2, category);
            ps.executeUpdate();
        }
    }
}