-- Top săn boss (2026-10-14)
-- boss_kill: số boss đã diệt; event_point_boss (có sẵn): điểm săn boss cộng dồn.
-- Server cộng 2 cột này trong BossHuntService.onKillBoss mỗi khi boss thưởng điểm cho người diệt.
ALTER TABLE player ADD COLUMN boss_kill INT NOT NULL DEFAULT 0 AFTER event_point_boss;
