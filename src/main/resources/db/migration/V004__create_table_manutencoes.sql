CREATE TABLE manutencoes (
    id BIGSERIAL PRIMARY KEY,
    moto_id BIGINT NOT NULL REFERENCES motos(id),
    tipo_manutencao VARCHAR(20) NOT NULL,
    status_manutencao VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
    data_agendada TIMESTAMP,
    data_iniciada TIMESTAMP,
    data_concluida TIMESTAMP,
    descricao TEXT,
    valor_total DECIMAL(10, 2) CHECK (valor_total >= 0),
    pecas_utilizadas TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_tipo_manutencao CHECK (
        tipo_manutencao IN ('PREVENTIVA', 'CORRETIVA', 'REVISAO', 'EMERGENCIAL')
    ),
    CONSTRAINT chk_status_manutencao CHECK (
        status_manutencao IN ('AGENDADA', 'EM_ANDAMENTO', 'CONCLUIDA')
    )
);

CREATE INDEX idx_manutencoes_moto ON manutencoes(moto_id);
CREATE INDEX idx_manutencoes_status ON manutencoes(status_manutencao);
CREATE INDEX idx_manutencoes_data_agendada ON manutencoes(data_agendada);
CREATE INDEX idx_manutencoes_tipo ON manutencoes(tipo_manutencao);