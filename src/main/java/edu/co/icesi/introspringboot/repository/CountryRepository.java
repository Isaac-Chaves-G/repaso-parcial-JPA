package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.Country;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends CrudRepository<Country, Integer> {

    // Ejemplo original de la plantilla; no corresponde a las cinco consultas del examen.
    // Ordena por el score de jugadores relacionados y limita filas: NO calcula un promedio por país.
    // Un ranking agregado por país requiere definir primero qué medida se quiere comparar.
    List<Country> findTop10ByOrderByPlayers_FifaScoreDesc();
}
