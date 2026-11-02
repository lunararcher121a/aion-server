-- Silkroad Remastered v001: additive schema; run only against a backed-up Aion GS database.
-- Original players and player_skills tables remain unchanged.
CREATE TABLE IF NOT EXISTS sro_characters (
  player_id INT NOT NULL,
  strength INT NOT NULL DEFAULT 20,
  intelligence INT NOT NULL DEFAULT 20,
  unspent_stat_points INT NOT NULL DEFAULT 0,
  skill_points BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (player_id),
  CONSTRAINT fk_sro_characters_player FOREIGN KEY (player_id)
    REFERENCES players(id) ON DELETE CASCADE,
  CONSTRAINT chk_sro_strength CHECK (strength >= 0),
  CONSTRAINT chk_sro_intelligence CHECK (intelligence >= 0),
  CONSTRAINT chk_sro_stat_points CHECK (unspent_stat_points >= 0),
  CONSTRAINT chk_sro_sp CHECK (skill_points >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sro_masteries (
  player_id INT NOT NULL,
  mastery_id TINYINT NOT NULL,
  mastery_level SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (player_id, mastery_id),
  CONSTRAINT fk_sro_masteries_character FOREIGN KEY (player_id)
    REFERENCES sro_characters(player_id) ON DELETE CASCADE,
  CONSTRAINT chk_sro_mastery_id CHECK (mastery_id BETWEEN 1 AND 7),
  CONSTRAINT chk_sro_mastery_level CHECK (mastery_level >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sro_learned_skills (
  player_id INT NOT NULL,
  skill_id INT NOT NULL,
  skill_level SMALLINT NOT NULL DEFAULT 1,
  PRIMARY KEY (player_id, skill_id),
  CONSTRAINT fk_sro_skills_character FOREIGN KEY (player_id)
    REFERENCES sro_characters(player_id) ON DELETE CASCADE,
  CONSTRAINT chk_sro_skill_level CHECK (skill_level >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
