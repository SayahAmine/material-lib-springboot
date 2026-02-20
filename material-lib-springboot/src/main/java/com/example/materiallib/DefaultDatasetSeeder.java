package com.example.materiallib;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultDatasetSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final ResourceLoader resourceLoader;
    private final TransactionTemplate tx;

    public DefaultDatasetSeeder(JdbcTemplate jdbc,
                                ResourceLoader resourceLoader,
                                PlatformTransactionManager txManager) {
        this.jdbc = jdbc;
        this.resourceLoader = resourceLoader;
        this.tx = new TransactionTemplate(txManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        // Seed only if empty
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM materials", Long.class);
        if (count != null && count > 0) return;

        tx.execute(status -> {
            try {
                jdbc.execute("PRAGMA foreign_keys=ON;");

                Map<Long, Long> defaultCondByMaterial = seedMaterials("classpath:data/materials.csv");
                seedConditions("classpath:data/conditions.csv");
                applyDefaultConditions(defaultCondByMaterial);

                seedConditionProperties("classpath:data/condition_properties.csv");
                seedCurves("classpath:data/curves.csv");
                seedCurvePoints("classpath:data/curve_points.csv");

                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Failed seeding dataset", e);
            }
        });
    }

    private Map<Long, Long> seedMaterials(String path) throws Exception {
        Resource csv = resourceLoader.getResource(path);
        Map<Long, Long> defaultCond = new HashMap<>();

        String sql = """
                INSERT INTO materials(
                  id, name, category, family, grade,
                  standard_system, standard_designation, uns, en_number,
                  tags, notes, source_type, source_name, confidence,
                  density, youngs_modulus, poisson_ratio, yield_strength, ultimate_strength,
                  toughness, thermal_expansion, melting_point, thermal_conductivity,
                  created_at, updated_at, default_condition_id
                )
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (var reader = new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            List<Object[]> batch = new ArrayList<>(500);

            for (CSVRecord r : records) {
                long id = Long.parseLong(r.get("id"));
                // Store for later (we can't set FK until conditions exist)
                defaultCond.put(id, parseLongOrNull(r.get("default_condition_id")));

                batch.add(new Object[]{
                        id,
                        emptyToNull(r.get("name")),
                        emptyToNull(r.get("category")),
                        emptyToNull(r.get("family")),
                        emptyToNull(r.get("grade")),
                        emptyToNull(r.get("standard_system")),
                        emptyToNull(r.get("standard_designation")),
                        emptyToNull(r.get("uns")),
                        emptyToNull(r.get("en_number")),
                        emptyToNull(r.get("tags")),
                        emptyToNull(r.get("notes")),
                        emptyToNull(r.get("source_type")),
                        emptyToNull(r.get("source_name")),
                        emptyToNull(r.get("confidence")),
                        parseDoubleOrNull(r.get("density")),
                        parseDoubleOrNull(r.get("youngs_modulus")),
                        parseDoubleOrNull(r.get("poisson_ratio")),
                        parseDoubleOrNull(r.get("yield_strength")),
                        parseDoubleOrNull(r.get("ultimate_strength")),
                        parseDoubleOrNull(r.get("toughness")),
                        parseDoubleOrNull(r.get("thermal_expansion")),
                        parseDoubleOrNull(r.get("melting_point")),
                        parseDoubleOrNull(r.get("thermal_conductivity")),
                        emptyToNull(r.get("created_at")),
                        emptyToNull(r.get("updated_at")),
                        null // default_condition_id set later
                });

                if (batch.size() >= 500) {
                    jdbc.batchUpdate(sql, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) jdbc.batchUpdate(sql, batch);
        }
        return defaultCond;
    }

    private void seedConditions(String path) throws Exception {
        Resource csv = resourceLoader.getResource(path);

        String sql = """
                INSERT INTO conditions(
                  id, material_id, condition_name, process_route, product_form,
                  heat_treatment, notes, is_default, created_at, updated_at
                )
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;

        try (var reader = new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            List<Object[]> batch = new ArrayList<>(500);

            for (CSVRecord r : records) {
                batch.add(new Object[]{
                        Long.parseLong(r.get("id")),
                        Long.parseLong(r.get("material_id")),
                        emptyToNull(r.get("condition_name")),
                        emptyToNull(r.get("process_route")),
                        emptyToNull(r.get("product_form")),
                        emptyToNull(r.get("heat_treatment")),
                        emptyToNull(r.get("notes")),
                        parseBooleanToInt(r.get("is_default")),
                        emptyToNull(r.get("created_at")),
                        emptyToNull(r.get("updated_at"))
                });

                if (batch.size() >= 500) {
                    jdbc.batchUpdate(sql, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) jdbc.batchUpdate(sql, batch);
        }
    }

    private void applyDefaultConditions(Map<Long, Long> defaultCondByMaterial) {
        String sql = "UPDATE materials SET default_condition_id = ? WHERE id = ?";
        for (var e : defaultCondByMaterial.entrySet()) {
            Long materialId = e.getKey();
            Long condId = e.getValue();
            if (condId != null) {
                jdbc.update(sql, condId, materialId);
            }
        }
    }

    private void seedConditionProperties(String path) throws Exception {
        Resource csv = resourceLoader.getResource(path);

        String sql = """
                INSERT INTO condition_properties(
                  condition_id, prop_key, prop_name, value_num, value_text,
                  unit, basis, method, standard, notes,
                  temperature_c, strain_rate, frequency_hz, environment, uncertainty, confidence
                )
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (var reader = new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            List<Object[]> batch = new ArrayList<>(1000);

            for (CSVRecord r : records) {
                batch.add(new Object[]{
                        Long.parseLong(r.get("condition_id")),
                        emptyToNull(r.get("prop_key")),
                        emptyToNull(r.get("prop_name")),
                        parseDoubleOrNull(r.get("value_num")),
                        emptyToNull(r.get("value_text")),
                        emptyToNull(r.get("unit")),
                        emptyToNull(r.get("basis")),
                        emptyToNull(r.get("method")),
                        emptyToNull(r.get("standard")),
                        emptyToNull(r.get("notes")),
                        parseDoubleOrNull(r.get("temperature_c")),
                        parseDoubleOrNull(r.get("strain_rate")),
                        parseDoubleOrNull(r.get("frequency_hz")),
                        emptyToNull(r.get("environment")),
                        parseDoubleOrNull(r.get("uncertainty")),
                        emptyToNull(r.get("confidence"))
                });

                if (batch.size() >= 1000) {
                    jdbc.batchUpdate(sql, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) jdbc.batchUpdate(sql, batch);
        }
    }

    private void seedCurves(String path) throws Exception {
        Resource csv = resourceLoader.getResource(path);

        String sql = """
                INSERT INTO curves(
                  id, condition_id, curve_type, x_label, y_label,
                  x_unit, y_unit, test_temperature_c, strain_rate, frequency_hz,
                  environment, standard, notes, created_at, updated_at
                )
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (var reader = new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            List<Object[]> batch = new ArrayList<>(500);

            for (CSVRecord r : records) {
                batch.add(new Object[]{
                        Long.parseLong(r.get("id")),
                        Long.parseLong(r.get("condition_id")),
                        emptyToNull(r.get("curve_type")),
                        emptyToNull(r.get("x_label")),
                        emptyToNull(r.get("y_label")),
                        emptyToNull(r.get("x_unit")),
                        emptyToNull(r.get("y_unit")),
                        parseDoubleOrNull(r.get("test_temperature_c")),
                        parseDoubleOrNull(r.get("strain_rate")),
                        parseDoubleOrNull(r.get("frequency_hz")),
                        emptyToNull(r.get("environment")),
                        emptyToNull(r.get("standard")),
                        emptyToNull(r.get("notes")),
                        emptyToNull(r.get("created_at")),
                        emptyToNull(r.get("updated_at"))
                });

                if (batch.size() >= 500) {
                    jdbc.batchUpdate(sql, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) jdbc.batchUpdate(sql, batch);
        }
    }

    private void seedCurvePoints(String path) throws Exception {
        Resource csv = resourceLoader.getResource(path);

        String sql = """
                INSERT INTO curve_points(curve_id, idx, x, y, z)
                VALUES (?,?,?,?,?)
                """;

        try (var reader = new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            List<Object[]> batch = new ArrayList<>(2000);

            for (CSVRecord r : records) {
                batch.add(new Object[]{
                        Long.parseLong(r.get("curve_id")),
                        Integer.parseInt(r.get("idx")),
                        Double.parseDouble(r.get("x")),
                        Double.parseDouble(r.get("y")),
                        parseDoubleOrNull(r.get("z"))
                });

                if (batch.size() >= 2000) {
                    jdbc.batchUpdate(sql, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) jdbc.batchUpdate(sql, batch);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static Double parseDoubleOrNull(String s) {
        s = emptyToNull(s);
        if (s == null) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long parseLongOrNull(String s) {
        s = emptyToNull(s);
        if (s == null) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseBooleanToInt(String s) {
        s = emptyToNull(s);
        if (s == null) return 0;
        return Boolean.parseBoolean(s) ? 1 : 0;
    }
}
