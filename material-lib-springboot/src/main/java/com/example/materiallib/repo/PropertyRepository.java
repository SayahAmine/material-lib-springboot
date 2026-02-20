package com.example.materiallib.repo;

import com.example.materiallib.model.ConditionProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PropertyRepository {

    private final JdbcTemplate jdbc;

    public PropertyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<ConditionProperty> mapper = (rs, rowNum) -> new ConditionProperty(
            rs.getLong("id"),
            rs.getLong("condition_id"),
            rs.getString("prop_key"),
            rs.getString("prop_name"),
            (Double) rs.getObject("value_num"),
            rs.getString("value_text"),
            rs.getString("unit"),
            rs.getString("basis"),
            rs.getString("method"),
            rs.getString("standard"),
            rs.getString("notes"),
            (Double) rs.getObject("temperature_c"),
            (Double) rs.getObject("strain_rate"),
            (Double) rs.getObject("frequency_hz"),
            rs.getString("environment"),
            (Double) rs.getObject("uncertainty"),
            rs.getString("confidence")
    );

    public List<ConditionProperty> findByConditionId(long conditionId, int limit) {
        return jdbc.query("SELECT * FROM condition_properties WHERE condition_id = ? ORDER BY id LIMIT ?", mapper, conditionId, limit);
    }

    public List<ConditionProperty> findByConditionIdAndKey(long conditionId, String propKey, int limit) {
        return jdbc.query(
                "SELECT * FROM condition_properties WHERE condition_id = ? AND prop_key = ? ORDER BY id LIMIT ?",
                mapper,
                conditionId, propKey, limit
        );
    }
}
