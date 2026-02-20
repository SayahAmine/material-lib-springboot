package com.example.materiallib.repo;

import com.example.materiallib.model.Material;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MaterialRepository {

    private final JdbcTemplate jdbc;

    public MaterialRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Material> mapper = (rs, rowNum) -> new Material(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("family"),
            rs.getString("grade"),
            rs.getString("standard_system"),
            rs.getString("standard_designation"),
            rs.getString("uns"),
            rs.getString("en_number"),
            rs.getString("tags"),
            rs.getString("notes"),
            rs.getString("source_type"),
            rs.getString("source_name"),
            rs.getString("confidence"),
            (Double) rs.getObject("density"),
            (Double) rs.getObject("youngs_modulus"),
            (Double) rs.getObject("poisson_ratio"),
            (Double) rs.getObject("yield_strength"),
            (Double) rs.getObject("ultimate_strength"),
            (Double) rs.getObject("toughness"),
            (Double) rs.getObject("thermal_expansion"),
            (Double) rs.getObject("melting_point"),
            (Double) rs.getObject("thermal_conductivity"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            (Long) rs.getObject("default_condition_id")
    );

    public long count() {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM materials", Long.class);
        return c == null ? 0 : c;
    }

    public List<Material> findAll(int limit) {
        return jdbc.query("SELECT * FROM materials ORDER BY name LIMIT ?", mapper, limit);
    }

    public Optional<Material> findById(long id) {
        var list = jdbc.query("SELECT * FROM materials WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public List<Material> searchByName(String q, int limit) {
        String like = "%" + q + "%";
        return jdbc.query("SELECT * FROM materials WHERE name LIKE ? ORDER BY name LIMIT ?", mapper, like, limit);
    }

    public List<Material> byCategory(String category, int limit) {
        return jdbc.query("SELECT * FROM materials WHERE category = ? ORDER BY name LIMIT ?", mapper, category, limit);
    }
}
