-- Optional Silkroad monster prototype mapping. Run on a disposable database first.
-- No native Aion NPC, spawn, loot or world tables are modified.
CREATE TABLE IF NOT EXISTS sro_monster_prototypes (
  sro_monster_code VARCHAR(128) NOT NULL,
  display_name VARCHAR(128) NOT NULL,
  level SMALLINT UNSIGNED NOT NULL,
  max_hp INT UNSIGNED NOT NULL,
  physical_attack INT UNSIGNED NOT NULL,
  physical_defense INT UNSIGNED NOT NULL,
  aion_npc_id INT UNSIGNED DEFAULT NULL,
  enabled BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (sro_monster_code),
  CONSTRAINT chk_sro_monster_level CHECK (level > 0),
  CONSTRAINT chk_sro_monster_hp CHECK (max_hp > 0),
  CONSTRAINT chk_sro_monster_attack CHECK (physical_attack > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sro_monster_spawn_prototypes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  sro_monster_code VARCHAR(128) NOT NULL,
  aion_world_id INT UNSIGNED NOT NULL,
  x FLOAT NOT NULL,
  y FLOAT NOT NULL,
  z FLOAT NOT NULL,
  respawn_seconds INT UNSIGNED NOT NULL DEFAULT 60,
  enabled BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  KEY idx_sro_monster_spawns_code (sro_monster_code),
  CONSTRAINT fk_sro_monster_spawn_code FOREIGN KEY (sro_monster_code)
    REFERENCES sro_monster_prototypes(sro_monster_code) ON DELETE CASCADE,
  CONSTRAINT chk_sro_monster_respawn CHECK (respawn_seconds > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
