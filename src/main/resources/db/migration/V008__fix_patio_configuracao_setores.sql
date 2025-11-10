-- Corrigir configuração de setores baseado na capacidade real de cada pátio
-- A V007 criou todos com 25 vagas por setor (total 100), mas alguns pátios têm capacidades diferentes

UPDATE patios SET configuracao_setores = '{"A": 25, "B": 25, "C": 25, "D": 25}'::jsonb WHERE id = 1;

UPDATE patios SET configuracao_setores = '{"A": 19, "B": 19, "C": 19, "D": 18}'::jsonb WHERE id = 2;

UPDATE patios SET configuracao_setores = '{"A": 13, "B": 13, "C": 12, "D": 12}'::jsonb WHERE id = 3;

UPDATE patios SET configuracao_setores = '{"A": 15, "B": 15, "C": 15, "D": 15}'::jsonb WHERE id = 4;

UPDATE patios SET configuracao_setores = '{"A": 20, "B": 20, "C": 20, "D": 20}'::jsonb WHERE id = 5;
