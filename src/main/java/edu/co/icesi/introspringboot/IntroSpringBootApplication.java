package edu.co.icesi.introspringboot;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Activa Spring Boot y busca entidades, repositorios y controladores bajo este paquete.
@SpringBootApplication
public class IntroSpringBootApplication {

    // Punto de entrada: arranca la aplicación y el servidor web.
    public static void main(String[] args) {
        SpringApplication.run(IntroSpringBootApplication.class, args);
    }

    // Solo imprime un aviso después de inicializar este componente; no implementa consultas.
    @PostConstruct
    public void init() {
        System.out.println(">>>>>Inició la app");
    }
}
