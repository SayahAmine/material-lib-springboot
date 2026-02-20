package com.example.materiallib.repo;

import com.example.materiallib.model.CurvePoint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CurvePointRepository {

    private final JdbcTemplate jdbc;

    public CurvePointRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<CurvePoint> mapper = (rs, rowNum) -> new CurvePoint(
            rs.getLong("curve_id"),
            rs.getInt("idx"),
            rs.getDouble("x"),
            rs.getDouble("y"),
            (Double) rs.getObject("z")
    );

    public List<CurvePoint> pointsForCurve(long curveId) {
        return jdbc.query("SELECT * FROM curve_points WHERE curve_id = ? ORDER BY idx", mapper, curveId);
    }
}
