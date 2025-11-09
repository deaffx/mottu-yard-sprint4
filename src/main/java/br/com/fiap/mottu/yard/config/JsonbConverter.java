package br.com.fiap.mottu.yard.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Converter
@Slf4j
public class JsonbConverter implements AttributeConverter<String, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            // Valida se é um JSON válido
            objectMapper.readTree(attribute);
            return attribute;
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter para JSONB: {}", attribute, e);
            return null;
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        
        try {
            // Valida se é um JSON válido
            objectMapper.readTree(dbData);
            return dbData;
        } catch (IOException e) {
            log.error("Erro ao converter de JSONB: {}", dbData, e);
            return null;
        }
    }
}
