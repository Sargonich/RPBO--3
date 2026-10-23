-- Небольшой набор демонстрационных данных для локального запуска.
-- Две команды допущены к турниру, поэтому демонстрационный матч согласован с бизнес-правилами.
INSERT INTO users (id, username, email) VALUES
 (1, 'captain_red', 'captain.red@example.test'),
 (2, 'captain_blue', 'captain.blue@example.test'),
 (3, 'player_green', 'player.green@example.test'),
 (4, 'player_yellow', 'player.yellow@example.test')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tournaments (id, name, team_limit, status) VALUES
 (1, 'Демонстрационный турнир', 4, 'PLANNED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO teams (id, name, captain_id) VALUES
 (1, 'Красные', 1),
 (2, 'Синие', 2),
 (3, 'Зелёные', 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO team_members (team_id, user_id) VALUES
 (1, 1), (2, 2), (3, 3)
ON CONFLICT DO NOTHING;

INSERT INTO team_applications (id, team_id, tournament_id, admission_status) VALUES
 (1, 1, 1, 'APPROVED'),
 (2, 2, 1, 'APPROVED'),
 (3, 3, 1, 'PENDING')
ON CONFLICT (id) DO NOTHING;

INSERT INTO matches (id, tournament_id, team1_id, team2_id, score1, score2, status) VALUES
 (1, 1, 1, 2, 0, 0, 'SCHEDULED')
ON CONFLICT (id) DO NOTHING;

-- Синхронизируем identity sequences после явных демонстрационных ID.
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('tournaments', 'id'), COALESCE((SELECT MAX(id) FROM tournaments), 1));
SELECT setval(pg_get_serial_sequence('teams', 'id'), COALESCE((SELECT MAX(id) FROM teams), 1));
SELECT setval(pg_get_serial_sequence('team_applications', 'id'), COALESCE((SELECT MAX(id) FROM team_applications), 1));
SELECT setval(pg_get_serial_sequence('matches', 'id'), COALESCE((SELECT MAX(id) FROM matches), 1));
