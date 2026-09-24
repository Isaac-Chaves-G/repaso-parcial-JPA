package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.Country;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends CrudRepository<Country, Integer> {

    /**
     * Top 10 de países con mejores jugadores acorde al score del jugador.
     * Navegación: Country -> players -> fifaScore
     * Se usa Distinct para no repetir el país si tiene varios jugadores en el top.
     * Se usa Top10 para limitar el resultado.
     */
    // 5
    List<Country> findTop10ByOrderByPlayers_FifaScoreDesc();
}
