package com.example.materiallib.repo;

import com.example.materiallib.model.MaterialCondition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConditionRepository {

    private final JdbcTemplate jdbc;

    public ConditionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<MaterialCondition> mapper = (rs, rowNum) -> new MaterialCondition(
            rs.getLong("id"),
            rs.getLong("material_id"),
            rs.getString("condition_name"),
            rs.getString("process_route"),
            rs.getString("product_form"),
            rs.getString("heat_treatment"),
            rs.getString("notes"),
            rs.getInt("is_default") != 0,
            rs.getString("created_at"),
            rs.getString("updated_at")
    );

    public List<MaterialCondition> findByMaterialId(long materialId) {
        return jdbc.query("SELECT * FROM conditions WHERE material_id = ? ORDER BY is_default DESC, id", mapper, materialId);
    }

    public Optional<MaterialCondition> findDefaultForMaterial(long materialId) {
        var list = jdbc.query("SELECT * FROM conditions WHERE material_id = ? AND is_default = 1 LIMIT 1", mapper, materialId);
        return list.stream().findFirst();
    }

    public Optional<MaterialCondition> findById(long id) {
        var list = jdbc.query("SELECT * FROM conditions WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }
}
