# Solución comentada del parcial JPA

Modelo corregido y las cinco consultas resueltas con Query Methods, sin @Query ni SQL nativo.

- [Empieza aquí: chuleta rápida](CHULETA_RAPIDA.md).
- [Guía completa por escenarios y consultas](APUNTES_JPA_PARCIAL.md).
- [Diagrama corregido](model.md).
- [Controlador con las cinco rutas](src/main/java/edu/co/icesi/introspringboot/controller/WorldCupController.java).

Desde esta carpeta: `mvn test` y `mvn spring-boot:run`. Servidor en `http://localhost:8081`.
Primera consulta: `http://localhost:8081/worldcup/ej1?confederacion=CONMEBOL`.

Validación: ocho pruebas de integración (consultas 1–5, duplicados, fechas, locales/visitantes, contratos, PK compuesta y parámetros HTTP).

Las consultas 4 y 5 infieren participación por selección, ya que el modelo no registra alineaciones, e incluyen el historial jugador-club. La consulta 2 exige contrato actual (`endDate IS NULL`). La guía explica cómo adaptar los filtros si el enunciado exige otra interpretación.
