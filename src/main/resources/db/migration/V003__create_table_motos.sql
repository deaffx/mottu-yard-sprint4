CREATE TABLE motos (
    id BIGSERIAL PRIMARY KEY,
    modelo VARCHAR(50) NOT NULL,
    placa VARCHAR(10) UNIQUE NOT NULL,
    marca VARCHAR(30) NOT NULL,
    ano_fabricacao INTEGER NOT NULL CHECK (ano_fabricacao > 1900),
    cor VARCHAR(20),
    quilometragem INTEGER DEFAULT 0 CHECK (quilometragem >= 0),
    status_moto VARCHAR(30) NOT NULL DEFAULT 'PARA_REGULARIZAR',
    patio_atual_id BIGINT NOT NULL REFERENCES patios(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_placa_format CHECK (
        placa ~ '^[A-Z]{3}[0-9][A-Z][0-9]{2}$' OR 
        placa ~ '^[A-Z]{3}[0-9]{4}$'
    ),
    CONSTRAINT chk_status_moto CHECK (
        status_moto IN ('PARA_REGULARIZAR', 'PARA_MANUTENCAO', 'NA_OFICINA', 'PARA_ALUGAR')
    )
);