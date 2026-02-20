package com.example.materiallib.repo;

import com.example.materiallib.model.Curve;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CurveRepository {

    private final JdbcTemplate jdbc;

    public CurveRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Curve> mapper = (rs, rowNum) -> new Curve(
            rs.getLong("id"),
            rs.getLong("condition_id"),
            rs.getString("curve_type"),
            rs.getString("x_label"),
            rs.getString("y_label"),
            rs.getString("x_unit"),
            rs.getString("y_unit"),
            (Double) rs.getObject("test_temperature_c"),
            (Double) rs.getObject("strain_rate"),
            (Double) rs.getObject("frequency_hz"),
            rs.getString("environment"),
            rs.getString("standard"),
            rs.getString("notes"),
            rs.getString("created_at"),
            rs.getString("updated_at")
    );

    public Optional<Curve> findById(long id) {
        var list = jdbc.query("SELECT * FROM curves WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public List<Curve> findByConditionId(long conditionId) {
        return jdbc.query("SELECT * FROM curves WHERE condition_id = ? ORDER BY curve_type, id", mapper, conditionId);
    }
}
