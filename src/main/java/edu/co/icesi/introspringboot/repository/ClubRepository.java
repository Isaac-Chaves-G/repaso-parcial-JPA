package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.Club;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

// Devuelve clubes: el recorrido del nombre siempre empieza en Club.
@Repository
public interface ClubRepository extends CrudRepository<Club, Integer> {

    // Consulta 2: Club -> etapas -> jugador -> nombre; misma etapa sin fecha de fin.
    // IgnoreCase ignora mayúsculas; IsNull no recibe parámetro; Distinct evita clubes repetidos.
    List<Club> findDistinctByPlayerClubs_Player_NameIgnoreCaseAndPlayerClubs_EndDateIsNull(String playerName);

    // Consulta 4: Club -> etapas -> jugador -> selección -> partidos (local O visitante).
    // Between incluye ambos extremos. Fecha y estadio deben corresponder al MISMO partido.
    // And tiene prioridad sobre Or: (fecha local Y estadio local) O (fecha visitante Y estadio visitante).
    // Por eso repetimos los tres valores al invocar este método desde el controlador.
    // El enunciado no pide contrato vigente: se consideran las asociaciones del historial.
    List<Club> findDistinctByPlayerClubs_Player_Country_HomeMatches_MatchDateBetweenAndPlayerClubs_Player_Country_HomeMatches_StadiumOrPlayerClubs_Player_Country_AwayMatches_MatchDateBetweenAndPlayerClubs_Player_Country_AwayMatches_Stadium(
            LocalDate homeFrom, LocalDate homeTo, String homeStadium,
            LocalDate awayFrom, LocalDate awayTo, String awayStadium);

    // Ejemplo original de la plantilla: clubes relacionados con cualquiera de las dos selecciones de un partido.
    List<Club> findDistinctByPlayerClubs_Player_Country_HomeMatches_IdOrPlayerClubs_Player_Country_AwayMatches_Id(
            Integer matchId, Integer matchIdAgain);
}
