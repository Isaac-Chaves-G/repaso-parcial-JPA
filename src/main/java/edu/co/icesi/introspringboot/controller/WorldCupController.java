package edu.co.icesi.introspringboot.controller;

import edu.co.icesi.introspringboot.entity.Club;
import edu.co.icesi.introspringboot.entity.Match;
import edu.co.icesi.introspringboot.entity.Player;
import edu.co.icesi.introspringboot.repository.ClubRepository;
import edu.co.icesi.introspringboot.repository.MatchRepository;
import edu.co.icesi.introspringboot.repository.PlayerRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

// Recibe peticiones HTTP y convierte el resultado Java en JSON.
@RestController
@RequestMapping("/worldcup") // Prefijo común: se combina con cada @GetMapping.
public class WorldCupController {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final ClubRepository clubRepository;

    // Spring entrega los repositorios: no se crean con new ni necesitan @Autowired con un único constructor.
    public WorldCupController(PlayerRepository playerRepository,
                              MatchRepository matchRepository,
                              ClubRepository clubRepository) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.clubRepository = clubRepository;
    }

    // Consulta 1: /worldcup/ej1?confederacion=CONMEBOL
    // @RequestParam lee lo que viene después de ?; el repositorio filtra y ordena en la base de datos.
    @GetMapping("/ej1")
    public List<Player> ej1(@RequestParam("confederacion") String confederacion) {
        return playerRepository.findByCountry_ConfederationOrderByFifaScoreDesc(confederacion);
    }

    // Consulta 2: clubes actuales del jugador. "lionel messi" encuentra también "Lionel Messi".
    @GetMapping("/ej2")
    public List<Club> ej2(@RequestParam("nombre") String nombre) {
        return clubRepository.findDistinctByPlayerClubs_Player_NameIgnoreCaseAndPlayerClubs_EndDateIsNull(nombre);
    }

    // Consulta 3: basta con que UN jugador LOCAL supere el puntaje; no se exige que todos lo superen.
    @GetMapping("/ej3")
    public List<Match> ej3(@RequestParam("estadio") String estadio,
                           @RequestParam("puntaje") Integer puntaje) {
        return matchRepository.findDistinctByStadiumAndHomeCountry_Players_FifaScoreGreaterThan(estadio, puntaje);
    }

    // Consulta 4: fechas de PARTIDOS, no fechas de contrato; ambos días están incluidos.
    // DateTimeFormat convierte texto como 2026-06-11 en LocalDate.
    @GetMapping("/ej4")
    public List<Club> ej4(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam("estadio") String estadio) {
        if (desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "desde debe ser anterior o igual a hasta");
        }
        return clubRepository.findDistinctByPlayerClubs_Player_Country_HomeMatches_MatchDateBetweenAndPlayerClubs_Player_Country_HomeMatches_StadiumOrPlayerClubs_Player_Country_AwayMatches_MatchDateBetweenAndPlayerClubs_Player_Country_AwayMatches_Stadium(
                desde, hasta, estadio, desde, hasta, estadio);
    }

    // Consulta 5: delanteros (FW) del club indicado, sea su selección local o visitante.
    @GetMapping("/ej5")
    public List<Match> ej5(@RequestParam("club") String club) {
        return matchRepository.findDistinctByHomeCountry_Players_PositionAndHomeCountry_Players_PlayerClubs_Club_NameOrAwayCountry_Players_PositionAndAwayCountry_Players_PlayerClubs_Club_Name(
                "FW", club, "FW", club);
    }
}
