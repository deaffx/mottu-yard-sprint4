INSERT INTO usuarios (id, username, email, name, avatar_url, role, perfil_confirmado, created_at, updated_at) VALUES
(1, 'operador-test', 'operador@mottu.com', 'Operador Teste', 'https://avatars.githubusercontent.com/u/1', 'OPERADOR', true, NOW(), NOW()),
(2, 'mecanico-test', 'mecanico@mottu.com', 'Mecânico Teste', 'https://avatars.githubusercontent.com/u/2', 'MECANICO', true, NOW(), NOW());

SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios));

INSERT INTO patios (id, nome, endereco, capacidade_maxima, latitude, longitude) VALUES
(1, 'Pátio Central São Paulo', 'Av. Paulista, 1000 - Bela Vista, São Paulo - SP', 100, -23.5616, -46.6565),
(2, 'Pátio Zona Sul', 'Av. Ibirapuera, 500 - Ibirapuera, São Paulo - SP', 75, -23.5935, -46.6523),
(3, 'Pátio ABC Santo André', 'R. das Indústrias, 200 - Centro, Santo André - SP', 50, -23.6684, -46.5395),
(4, 'Pátio Vila Madalena', 'R. Harmonia, 100 - Vila Madalena, São Paulo - SP', 60, -23.5442, -46.6929),
(5, 'Pátio Moema', 'Av. Moema, 300 - Moema, São Paulo - SP', 80, -23.6018, -46.6636);

SELECT setval('patios_id_seq', (SELECT MAX(id) FROM patios));

INSERT INTO motos (id, modelo, placa, marca, ano_fabricacao, cor, quilometragem, status_moto, patio_atual_id, created_at, updated_at) VALUES
(1, 'CG 160 Start', 'MOT0001', 'Honda', 2023, 'Vermelho', 1500, 'PARA_ALUGAR', 1, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(2, 'PCX 150', 'MOT0002', 'Honda', 2023, 'Branco', 2500, 'NA_OFICINA', 2, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),
(3, 'Biz 125', 'MOT0003', 'Honda', 2022, 'Azul', 8500, 'PARA_ALUGAR', 1, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(4, 'XRE 300', 'MOT0004', 'Honda', 2023, 'Preto', 500, 'PARA_REGULARIZAR', 3, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(5, 'CB 600F Hornet', 'MOT0005', 'Honda', 2022, 'Amarelo', 12000, 'PARA_ALUGAR', 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(6, 'YBR 125 Factor', 'YBR1234', 'Yamaha', 2023, 'Azul', 3200, 'PARA_REGULARIZAR', 2, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(7, 'Fazer 250', 'FZR2023', 'Yamaha', 2023, 'Vermelho', 1800, 'PARA_ALUGAR', 4, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(8, 'MT-03', 'MTR3456', 'Yamaha', 2022, 'Preto', 7500, 'PARA_ALUGAR', 3, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(9, 'Duke 200', 'KTM2001', 'KTM', 2023, 'Laranja', 900, 'PARA_ALUGAR', 5, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(10, 'TNT 135', 'TNT1357', 'Benelli', 2022, 'Vermelho', 15000, 'NA_OFICINA', 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

SELECT setval('motos_id_seq', (SELECT MAX(id) FROM motos));


INSERT INTO manutencoes (moto_id, tipo_manutencao, status_manutencao, data_agendada, data_iniciada, descricao, valor_total, pecas_utilizadas) VALUES
(2, 'PREVENTIVA', 'EM_ANDAMENTO', NOW() + INTERVAL '1 day', NOW() - INTERVAL '2 hours', 'Revisão dos 5000km - troca de óleo e filtros', 250.00, 'Óleo motor 10W40, Filtro de óleo, Filtro de ar'),
(4, 'CORRETIVA', 'AGENDADA', NOW() + INTERVAL '3 days', NULL, 'Troca de pneu traseiro - desgaste excessivo', 180.00, NULL),
(10, 'CORRETIVA', 'EM_ANDAMENTO', NOW() + INTERVAL '2 days', NOW() - INTERVAL '1 hour', 'Reparo do sistema de freios', 320.00, 'Pastilhas de freio, Fluido de freio DOT 4'),
(5, 'REVISAO', 'AGENDADA', NOW() + INTERVAL '5 days', NULL, 'Revisão dos 15000km - manutenção completa', 450.00, NULL),
(1, 'PREVENTIVA', 'CONCLUIDA', NOW() - INTERVAL '5 days', NOW() - INTERVAL '6 days', 'Troca de óleo preventiva', 120.00, 'Óleo motor 10W40'),
(3, 'CORRETIVA', 'CONCLUIDA', NOW() - INTERVAL '10 days', NOW() - INTERVAL '12 days', 'Troca de vela de ignição', 45.00, 'Vela de ignição NGK'),
(6, 'PREVENTIVA', 'AGENDADA', NOW() + INTERVAL '7 days', NULL, 'Limpeza e lubrificação da corrente', 80.00, NULL);

SELECT setval('manutencoes_id_seq', (SELECT MAX(id) FROM manutencoes));

UPDATE manutencoes SET data_concluida = NOW() - INTERVAL '4 days' WHERE status_manutencao = 'CONCLUIDA' AND id = 5;
UPDATE manutencoes SET data_concluida = NOW() - INTERVAL '9 days' WHERE status_manutencao = 'CONCLUIDA' AND id = 6;