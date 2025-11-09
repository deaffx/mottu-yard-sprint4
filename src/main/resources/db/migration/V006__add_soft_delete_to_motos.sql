-- Migration para adicionar soft delete em motos
-- Adiciona coluna deleted_at para implementar exclusão lógica

ALTER TABLE motos
ADD COLUMN deleted_at TIMESTAMP;

-- Índice para melhorar performance nas consultas de motos ativas
CREATE INDEX idx_motos_deleted_at ON motos(deleted_at);

