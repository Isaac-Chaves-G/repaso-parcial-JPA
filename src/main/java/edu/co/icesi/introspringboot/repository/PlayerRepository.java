package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Estudio: Spring crea la implementación de este repositorio; declaramos métodos, no escribimos aquí su SQL.
// Estudio: CrudRepository<Player, Integer> significa entidad Player e identificador de tipo Integer.
@Repository
public interface PlayerRepository extends CrudRepository<Player, Integer> {

    // Consulta 1: Player -> country -> confederation. OrderBy ordena por fifaScore de mayor a menor.
    // Se escribe fifaScore (atributo Java), no fifa_score (columna SQL); Spring implementa el método.
    List<Player> findByCountry_ConfederationOrderByFifaScoreDesc(String confederation);

    /**
     * Ejemplo original: jugadores que han jugado históricamente en un club por nombre del club.
     * Navegación: Player -> playerClubs (List) -> club -> name
     */
    List<Player> findByPlayerClubs_Club_Name(String clubName);

    /**
     * Ejemplo original: jugadores de un país por nombre, que hayan jugado en un club por nombre.
     * Navegación: Player -> country -> name AND Player -> playerClubs -> club -> name
     */
    List<Player> findByCountry_NameAndPlayerClubs_Club_Name(String countryName, String clubName);
}
