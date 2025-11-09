package br.com.fiap.mottu.yard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MottuYardWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MottuYardWebApplication.class, args);
    }

}