PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS materials (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  category TEXT,
  family TEXT,
  grade TEXT,
  standard_system TEXT,
  standard_designation TEXT,
  uns TEXT,
  en_number TEXT,
  tags TEXT,
  notes TEXT,
  source_type TEXT,
  source_name TEXT,
  confidence TEXT,
  density REAL,
  youngs_modulus REAL,
  poisson_ratio REAL,
  yield_strength REAL,
  ultimate_strength REAL,
  toughness REAL,
  thermal_expansion REAL,
  melting_point REAL,
  thermal_conductivity REAL,
  created_at TEXT,
  updated_at TEXT,
  default_condition_id INTEGER,
  FOREIGN KEY (default_condition_id) REFERENCES conditions(id)
);

CREATE TABLE IF NOT EXISTS conditions (
  id INTEGER PRIMARY KEY,
  material_id INTEGER NOT NULL,
  condition_name TEXT NOT NULL,
  process_route TEXT,
  product_form TEXT,
  heat_treatment TEXT,
  notes TEXT,
  is_default INTEGER NOT NULL DEFAULT 0,
  created_at TEXT,
  updated_at TEXT,
  FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_conditions_material_id ON conditions(material_id);
CREATE INDEX IF NOT EXISTS idx_conditions_is_default ON conditions(is_default);

CREATE TABLE IF NOT EXISTS condition_properties (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  condition_id INTEGER NOT NULL,
  prop_key TEXT,
  prop_name TEXT,
  value_num REAL,
  value_text TEXT,
  unit TEXT,
  basis TEXT,
  method TEXT,
  standard TEXT,
  notes TEXT,
  temperature_c REAL,
  strain_rate REAL,
  frequency_hz REAL,
  environment TEXT,
  uncertainty REAL,
  confidence TEXT,
  FOREIGN KEY (condition_id) REFERENCES conditions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_props_condition_id ON condition_properties(condition_id);
CREATE INDEX IF NOT EXISTS idx_props_condition_key ON condition_properties(condition_id, prop_key);

CREATE TABLE IF NOT EXISTS curves (
  id INTEGER PRIMARY KEY,
  condition_id INTEGER NOT NULL,
  curve_type TEXT NOT NULL,
  x_label TEXT,
  y_label TEXT,
  x_unit TEXT,
  y_unit TEXT,
  test_temperature_c REAL,
  strain_rate REAL,
  frequency_hz REAL,
  environment TEXT,
  standard TEXT,
  notes TEXT,
  created_at TEXT,
  updated_at TEXT,
  FOREIGN KEY (condition_id) REFERENCES conditions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_curves_condition_id ON curves(condition_id);
CREATE INDEX IF NOT EXISTS idx_curves_type ON curves(curve_type);

CREATE TABLE IF NOT EXISTS curve_points (
  curve_id INTEGER NOT NULL,
  idx INTEGER NOT NULL,
  x REAL NOT NULL,
  y REAL NOT NULL,
  z REAL,
  PRIMARY KEY (curve_id, idx),
  FOREIGN KEY (curve_id) REFERENCES curves(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_curve_points_curve_id ON curve_points(curve_id);
