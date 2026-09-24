package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.Match;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Devuelve partidos: cada recorrido empieza en Match, aunque filtre atributos de jugadores o clubes.
@Repository
public interface MatchRepository extends CrudRepository<Match, Integer> {

    // Consulta 3: estadio del partido Y algún jugador de la selección LOCAL con score > límite.
    // GreaterThan es estricto: un score igual al límite no sirve. Distinct devuelve cada partido una vez.
    List<Match> findDistinctByStadiumAndHomeCountry_Players_FifaScoreGreaterThan(String stadium, Integer score);

    // Consulta 5: (delantero local Y su club) O (delantero visitante Y su club).
    // En data.sql los delanteros se identifican con FW; el controlador pasa FW en ambos lados.
    // Los filtros de posición y club deben cumplirse en el MISMO jugador.
    // Incluye el historial del club: aquí no se pidió "actualmente", a diferencia de la consulta 2.
    // Participar se deduce por selección: el modelo no tiene alineaciones por partido.
    List<Match> findDistinctByHomeCountry_Players_PositionAndHomeCountry_Players_PlayerClubs_Club_NameOrAwayCountry_Players_PositionAndAwayCountry_Players_PlayerClubs_Club_Name(
            String homePosition, String homeClub, String awayPosition, String awayClub);

    // Ejemplo de la plantilla (NO es la consulta 5): solo locales, cualquier posición, club actual.
    List<Match> findDistinctByHomeCountry_Players_PlayerClubs_Club_NameAndHomeCountry_Players_PlayerClubs_EndDateIsNull(String clubName);
}
