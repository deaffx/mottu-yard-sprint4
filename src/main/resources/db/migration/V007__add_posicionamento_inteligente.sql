-- Migration para adicionar sistema de posicionamento inteligente
-- Resolve: Falta de organização e controle de localização das motos nos pátios

-- 1. Adicionar campos de posicionamento nas motos
ALTER TABLE motos
ADD COLUMN setor VARCHAR(5),
ADD COLUMN vaga INTEGER,
ADD COLUMN localizacao_observacao VARCHAR(200);

-- Comentários explicativos
COMMENT ON COLUMN motos.setor IS 'Setor do pátio onde a moto está localizada (ex: A, B, C, D)';
COMMENT ON COLUMN motos.vaga IS 'Número da vaga dentro do setor (ex: 1, 2, 3... 25)';
COMMENT ON COLUMN motos.localizacao_observacao IS 'Observações adicionais sobre localização (ex: "Próximo à porta", "Esquina esquerda")';

-- 2. Adicionar configuração de setores nos pátios
ALTER TABLE patios
ADD COLUMN configuracao_setores JSONB,
ADD COLUMN setor_oficina VARCHAR(5),
ADD COLUMN setor_saida_rapida VARCHAR(5);

-- Comentários explicativos
COMMENT ON COLUMN patios.configuracao_setores IS 'Configuração dos setores do pátio em formato JSON. Ex: {"A": 25, "B": 25, "C": 20, "D": 30}';
COMMENT ON COLUMN patios.setor_oficina IS 'Setor mais próximo da oficina (para motos em manutenção)';
COMMENT ON COLUMN patios.setor_saida_rapida IS 'Setor com saída mais rápida (para motos prontas para alugar)';

-- 3. Criar índices para otimizar buscas por localização
CREATE INDEX idx_motos_setor_vaga ON motos(patio_atual_id, setor, vaga) WHERE deleted_at IS NULL;
CREATE INDEX idx_motos_setor ON motos(patio_atual_id, setor) WHERE deleted_at IS NULL;

-- 4. Atualizar dados existentes com configuração padrão
UPDATE patios
SET configuracao_setores = '{"A": 25, "B": 25, "C": 25, "D": 25}'::jsonb,
    setor_oficina = 'D',
    setor_saida_rapida = 'A'
WHERE configuracao_setores IS NULL;

-- 5. Distribuir motos existentes em setores (simulação de organização inicial)
WITH motos_numeradas AS (
    SELECT 
        id,
        patio_atual_id,
        ROW_NUMBER() OVER (PARTITION BY patio_atual_id ORDER BY id) as num_moto
    FROM motos
    WHERE deleted_at IS NULL
      AND setor IS NULL
)
UPDATE motos m
SET 
    setor = CASE 
        WHEN mn.num_moto % 4 = 1 THEN 'A'
        WHEN mn.num_moto % 4 = 2 THEN 'B'
        WHEN mn.num_moto % 4 = 3 THEN 'C'
        ELSE 'D'
    END,
    vaga = ((mn.num_moto - 1) % 25) + 1,
    localizacao_observacao = 'Localização definida automaticamente'
FROM motos_numeradas mn
WHERE m.id = mn.id;

-- 6. Criar tabela de histórico de movimentações
CREATE TABLE historico_movimentacoes (
    id BIGSERIAL PRIMARY KEY,
    moto_id BIGINT NOT NULL REFERENCES motos(id),
    patio_origem_id BIGINT REFERENCES patios(id),
    setor_origem VARCHAR(5),
    vaga_origem INTEGER,
    patio_destino_id BIGINT REFERENCES patios(id),
    setor_destino VARCHAR(5),
    vaga_destino INTEGER,
    motivo VARCHAR(200),
    usuario VARCHAR(100),
    data_movimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_moto FOREIGN KEY (moto_id) REFERENCES motos(id)
);

-- Comentário na tabela
COMMENT ON TABLE historico_movimentacoes IS 'Histórico completo de movimentações de motos entre pátios e setores';

-- Índices para histórico
CREATE INDEX idx_historico_moto ON historico_movimentacoes(moto_id);
CREATE INDEX idx_historico_data ON historico_movimentacoes(data_movimentacao DESC);
CREATE INDEX idx_historico_patio_destino ON historico_movimentacoes(patio_destino_id);

-- 7. Criar view para ocupação por setor
CREATE OR REPLACE VIEW vw_ocupacao_setores AS
SELECT 
    p.id as patio_id,
    p.nome as patio_nome,
    m.setor,
    COUNT(m.id) as motos_ocupadas,
    COALESCE((p.configuracao_setores->>m.setor)::integer, 0) as capacidade_setor,
    ROUND(
        (COUNT(m.id)::decimal / NULLIF((p.configuracao_setores->>m.setor)::integer, 0)) * 100, 
        2
    ) as percentual_ocupacao
FROM patios p
LEFT JOIN motos m ON m.patio_atual_id = p.id AND m.deleted_at IS NULL
WHERE m.setor IS NOT NULL
GROUP BY p.id, p.nome, m.setor, p.configuracao_setores;

-- Comentário na view
COMMENT ON VIEW vw_ocupacao_setores IS 'Visão consolidada da ocupação de cada setor por pátio';
