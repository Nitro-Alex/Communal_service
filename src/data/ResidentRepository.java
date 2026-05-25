package data;

import data.dto.PaymentRecord;
import data.dto.ReadingRecord;
import data.dto.ResidentProfile;
import data.dto.ResidentSearchResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ResidentRepository {

    protected Connection getConnection() throws SQLException {
        Connection connection = AppSession.getInstance().getConnection();
        if (connection == null) {
            throw new SQLException("Нет активного подключения к базе данных");
        }
        return connection;
    }

    public List<ResidentSearchResult> searchResidents(String lastNamePart, String firstNamePart) throws SQLException {
        List<ResidentSearchResult> result = new ArrayList<ResidentSearchResult>();

        String sql =
                "SELECT r.resident_id, r.last_name, r.first_name, b.address, a.apartment_number " +
                "FROM resident r " +
                "JOIN apartment a ON r.apartment_id = a.apartment_id " +
                "JOIN building b ON a.building_id = b.building_id " +
                "WHERE LOWER(r.last_name) LIKE LOWER(?) " +
                "AND LOWER(r.first_name) LIKE LOWER(?) " +
                "ORDER BY r.last_name, r.first_name, b.address, a.apartment_number";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + safeString(lastNamePart) + "%");
            ps.setString(2, "%" + safeString(firstNamePart) + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new ResidentSearchResult(
                            rs.getInt("resident_id"),
                            rs.getString("last_name"),
                            rs.getString("first_name"),
                            rs.getString("address"),
                            rs.getInt("apartment_number")
                    ));
                }
            }
        }

        return result;
    }

    public ResidentProfile loadResidentProfile(int residentId) throws SQLException {
        String sql =
                "SELECT r.resident_id, r.apartment_id, r.last_name, r.first_name, " +
                "b.address, a.apartment_number, a.area, " +
                "bt.category, bt.discount_percent " +
                "FROM resident r " +
                "JOIN apartment a ON r.apartment_id = a.apartment_id " +
                "JOIN building b ON a.building_id = b.building_id " +
                "LEFT JOIN benefit_type bt ON r.category = bt.category " +
                "WHERE r.resident_id = ?";

        String debtSql =
                "SELECT " +
                "COALESCE((SELECT SUM(charge) FROM reading WHERE resident_id = ?), 0) - " +
                "COALESCE((SELECT SUM(amount) FROM payment WHERE resident_id = ?), 0) AS debt";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, residentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                BigDecimal debt = BigDecimal.ZERO;
                try (PreparedStatement debtPs = getConnection().prepareStatement(debtSql)) {
                    debtPs.setInt(1, residentId);
                    debtPs.setInt(2, residentId);

                    try (ResultSet debtRs = debtPs.executeQuery()) {
                        if (debtRs.next()) {
                            debt = debtRs.getBigDecimal("debt");
                            if (debt == null) {
                                debt = BigDecimal.ZERO;
                            }
                        }
                    }
                }

                BigDecimal discount = rs.getBigDecimal("discount_percent");
                if (discount == null) {
                    discount = BigDecimal.ZERO;
                }

                return new ResidentProfile(
                        rs.getInt("resident_id"),
                        rs.getInt("apartment_id"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getString("address"),
                        rs.getInt("apartment_number"),
                        rs.getBigDecimal("area"),
                        rs.getString("category"),
                        discount,
                        debt.setScale(2, RoundingMode.HALF_UP)
                );
            }
        }
    }

    public BigDecimal getCurrentDebt(int residentId) throws SQLException {
        String sql =
                "SELECT " +
                "COALESCE((SELECT SUM(charge) FROM reading WHERE resident_id = ?), 0) - " +
                "COALESCE((SELECT SUM(amount) FROM payment WHERE resident_id = ?), 0) AS debt";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, residentId);
            ps.setInt(2, residentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal debt = rs.getBigDecimal("debt");
                    return debt == null ? BigDecimal.ZERO : debt.setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public List<ReadingRecord> loadReadings(int residentId,
                                            YearMonth from,
                                            YearMonth to,
                                            String serviceName) throws SQLException {
        List<ReadingRecord> result = new ArrayList<ReadingRecord>();

        StringBuilder sql = new StringBuilder(
                "SELECT reading_id, service_name, month, year, value, charge " +
                "FROM reading WHERE resident_id = ?"
        );

        List<Object> params = new ArrayList<Object>();
        params.add(Integer.valueOf(residentId));

        if (serviceName != null && serviceName.trim().length() > 0 && !"Все".equalsIgnoreCase(serviceName)) {
            sql.append(" AND service_name = ?");
            params.add(serviceName);
        }

        if (from != null) {
            sql.append(" AND (year > ? OR (year = ? AND month >= ?))");
            params.add(Integer.valueOf(from.getYear()));
            params.add(Integer.valueOf(from.getYear()));
            params.add(Integer.valueOf(from.getMonthValue()));
        }

        if (to != null) {
            sql.append(" AND (year < ? OR (year = ? AND month <= ?))");
            params.add(Integer.valueOf(to.getYear()));
            params.add(Integer.valueOf(to.getYear()));
            params.add(Integer.valueOf(to.getMonthValue()));
        }

        sql.append(" ORDER BY year, month, service_name");

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Integer) {
                    ps.setInt(i + 1, ((Integer) value).intValue());
                } else {
                    ps.setString(i + 1, String.valueOf(value));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new ReadingRecord(
                            rs.getInt("reading_id"),
                            rs.getString("service_name"),
                            rs.getInt("month"),
                            rs.getInt("year"),
                            rs.getBigDecimal("value"),
                            rs.getBigDecimal("charge")
                    ));
                }
            }
        }

        return result;
    }

    public List<PaymentRecord> loadPayments(int residentId, LocalDate from, LocalDate to) throws SQLException {
        List<PaymentRecord> result = new ArrayList<PaymentRecord>();

        StringBuilder sql = new StringBuilder(
                "SELECT payment_id, payment_date, amount FROM payment WHERE resident_id = ?"
        );

        List<Object> params = new ArrayList<Object>();
        params.add(Integer.valueOf(residentId));

        if (from != null) {
            sql.append(" AND payment_date >= ?");
            params.add(Date.valueOf(from));
        }

        if (to != null) {
            sql.append(" AND payment_date <= ?");
            params.add(Date.valueOf(to));
        }

        sql.append(" ORDER BY payment_date DESC");

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Integer) {
                    ps.setInt(i + 1, ((Integer) value).intValue());
                } else if (value instanceof Date) {
                    ps.setDate(i + 1, (Date) value);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PaymentRecord(
                            rs.getInt("payment_id"),
                            rs.getDate("payment_date").toLocalDate(),
                            rs.getBigDecimal("amount")
                    ));
                }
            }
        }

        return result;
    }

    public void registerPayment(int residentId, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO payment (resident_id, payment_date, amount) VALUES (?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, residentId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setBigDecimal(3, amount.setScale(2, RoundingMode.HALF_UP));
            ps.executeUpdate();
        }
    }

    public void addReading(int residentId, String serviceName,
                           int month, int year, BigDecimal value) throws SQLException {

        String sql =
                "SELECT s.price, a.area, COALESCE(bt.discount_percent, 0) AS discount_percent " +
                "FROM resident r " +
                "JOIN apartment a ON r.apartment_id = a.apartment_id " +
                "JOIN service s ON s.name = ? " +
                "LEFT JOIN benefit_type bt ON r.category = bt.category " +
                "WHERE r.resident_id = ?";

        BigDecimal price;
        BigDecimal area;
        BigDecimal discount;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, serviceName);
            ps.setInt(2, residentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Невозможно вычислить начисление: нет данных жильца или услуги");
                }
                price = rs.getBigDecimal("price");
                area = rs.getBigDecimal("area");
                discount = rs.getBigDecimal("discount_percent");
                if (discount == null) {
                    discount = BigDecimal.ZERO;
                }
            }
        }

        BigDecimal base;
        if ("Отопление".equalsIgnoreCase(serviceName)) {
            base = price.multiply(area);
        } else {
            base = price.multiply(value);
        }

        BigDecimal factor = BigDecimal.ONE.subtract(
                discount.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
        );

        BigDecimal charge = base.multiply(factor).setScale(2, RoundingMode.HALF_UP);

        String insert = "INSERT INTO reading (resident_id, service_name, month, year, value, charge) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(insert)) {
            ps.setInt(1, residentId);
            ps.setString(2, serviceName);
            ps.setInt(3, month);
            ps.setInt(4, year);
            ps.setBigDecimal(5, value.setScale(2, RoundingMode.HALF_UP));
            ps.setBigDecimal(6, charge);
            ps.executeUpdate();
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }
}