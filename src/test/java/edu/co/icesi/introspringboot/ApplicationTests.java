package edu.co.icesi.introspringboot;

import edu.co.icesi.introspringboot.entity.*;
import edu.co.icesi.introspringboot.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// H2 de pruebas: cada caso crea sus datos y se revierte al terminar, sin alterar data.sql.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationTests {
    @Autowired CountryRepository countries;
    @Autowired PlayerRepository players;
    @Autowired ClubRepository clubs;
    @Autowired MatchRepository matches;
    @Autowired PlayerClubRepository stages;
    @Autowired EntityManager em;
    @Autowired MockMvc http;
    private static final LocalDate DAY = LocalDate.of(2026, 6, 15);

    // Helpers: construyen datos pequeños y conocidos para comprobar cada condición del enunciado.
    private Country country(String name, String confederation) {
        return countries.save(new Country(name, name, confederation));
    }
    private Club club(String name, Country country) {
        return clubs.save(new Club(name, "Ciudad", LocalDate.of(1900, 1, 1), country));
    }
    private Player player(String name, String position, int score, Country country) {
        return players.save(new Player(name, LocalDate.of(2000, 1, 1), position, score, country));
    }
    private PlayerClub stage(Player player, Club club, LocalDate start, LocalDate end) {
        return stages.save(new PlayerClub(player, club, start, end));
    }
    private Match match(Country home, Country away, LocalDate date, String stadium) {
        return matches.save(new Match(date, home, away, stadium));
    }

    @Test
    void consulta1FiltraConfederacionYOrdenaDescendente() throws Exception {
        Country a = country("Argentina", "CONMEBOL");
        Country b = country("España", "UEFA");
        player("Menor", "FW", 70, a);
        player("Mayor", "FW", 95, a);
        player("Ajeno", "FW", 99, b);
        assertThat(players.findByCountry_ConfederationOrderByFifaScoreDesc("CONMEBOL"))
                .extracting(Player::getName).containsExactly("Mayor", "Menor");
        assertThat(players.findByCountry_ConfederationOrderByFifaScoreDesc("NO_EXISTE")).isEmpty();
        http.perform(get("/worldcup/ej1").param("confederacion", "CONMEBOL"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Mayor"))
                .andExpect(jsonPath("$[0].fifaScore").value(95))
                .andExpect(jsonPath("$[0].country.confederation").value("CONMEBOL"));
    }

    @Test
    void consulta2ExigeNombreYContratoActualEnLaMismaEtapa() throws Exception {
        Country c = country("Argentina", "CONMEBOL");
        Player messi = player("Lionel Messi", "FW", 93, c);
        Player other = player("Otro", "FW", 80, c);
        Club old = club("Anterior", c);
        Club current = club("Actual", c);
        stage(messi, old, DAY.minusYears(5), DAY.minusYears(1));
        stage(other, old, DAY.minusYears(1), null); // No debe hacer vigente la etapa de Messi.
        stage(messi, current, DAY.minusYears(1), null);
        assertThat(clubs.findDistinctByPlayerClubs_Player_NameIgnoreCaseAndPlayerClubs_EndDateIsNull("LIONEL messi"))
                .extracting(Club::getName).containsExactly("Actual");
        http.perform(get("/worldcup/ej2").param("nombre", "lionel messi"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Actual"));
    }

    @Test
    void consulta3SoloLocalMayorEstrictoSinDuplicados() throws Exception {
        Country strong = country("Fuerte", "X");
        Country weak = country("Limite", "X");
        player("Uno", "FW", 91, strong);
        player("Dos", "FW", 92, strong);
        player("Igual", "FW", 90, weak);
        Match expected = match(strong, weak, DAY, "Estadio");
        match(weak, strong, DAY, "Estadio");
        match(strong, weak, DAY, "Otro estadio");
        assertThat(matches.findDistinctByStadiumAndHomeCountry_Players_FifaScoreGreaterThan("Estadio", 90))
                .extracting(Match::getId).containsExactly(expected.getId());
        http.perform(get("/worldcup/ej3").param("estadio", "Estadio").param("puntaje", "90"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void consulta4IncluyeLocalVisitanteExtremosYEvitaCruzarPartidos() throws Exception {
        Country a = country("Local", "X");
        Country b = country("Visitante", "X");
        Country empty = country("Sin jugadores", "X");
        Country wrong = country("Fuera", "X");
        Club home = club("Club local", a);
        Club away = club("Club visitante", b);
        Club excluded = club("No cumple", wrong);
        stage(player("A", "FW", 90, a), home, DAY.minusYears(1), null);
        stage(player("A2", "FW", 90, a), home, DAY.minusYears(1), null);
        stage(player("B", "FW", 90, b), away, DAY.minusYears(1), null);
        stage(player("C", "FW", 90, wrong), excluded, DAY.minusYears(1), null);
        match(a, empty, DAY, "Estadio");
        match(empty, b, DAY.plusDays(2), "Estadio");
        match(wrong, empty, DAY, "Otro");
        match(wrong, empty, DAY.plusDays(3), "Estadio");
        assertThat(clubs.findDistinctByPlayerClubs_Player_Country_HomeMatches_MatchDateBetweenAndPlayerClubs_Player_Country_HomeMatches_StadiumOrPlayerClubs_Player_Country_AwayMatches_MatchDateBetweenAndPlayerClubs_Player_Country_AwayMatches_Stadium(
                DAY, DAY.plusDays(2), "Estadio", DAY, DAY.plusDays(2), "Estadio"))
                .extracting(Club::getName).containsExactlyInAnyOrder("Club local", "Club visitante");
        http.perform(get("/worldcup/ej4").param("desde", DAY.toString())
                        .param("hasta", DAY.plusDays(2).toString()).param("estadio", "Estadio"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void consulta5MismoDelanteroYClubEnAmbosLadosSinExigirJugadoresAlRival() throws Exception {
        Country good = country("Delanteros", "X");
        Country mixed = country("Distractor", "X");
        Country empty = country("Sin jugadores", "X");
        Club target = club("Objetivo", good);
        Club other = club("Otro", mixed);
        stage(player("Delantero", "FW", 90, good), target, DAY.minusYears(1), null);
        stage(player("Segundo", "FW", 90, good), target, DAY.minusYears(1), null);
        stage(player("Portero objetivo", "GK", 90, mixed), target, DAY.minusYears(1), null);
        stage(player("Delantero otro", "FW", 90, mixed), other, DAY.minusYears(1), null);
        Match home = match(good, empty, DAY, "E");
        Match away = match(empty, good, DAY, "E");
        match(mixed, empty, DAY, "E");
        assertThat(matches.findDistinctByHomeCountry_Players_PositionAndHomeCountry_Players_PlayerClubs_Club_NameOrAwayCountry_Players_PositionAndAwayCountry_Players_PlayerClubs_Club_Name(
                "FW", "Objetivo", "FW", "Objetivo"))
                .extracting(Match::getId).containsExactlyInAnyOrder(home.getId(), away.getId());
        http.perform(get("/worldcup/ej5").param("club", "Objetivo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void consulta5IncluyeHistorialPorqueNoPideActualmente() {
        Country c = country("Pais", "X");
        Country rival = country("Rival", "X");
        Club club = club("Historico", c);
        stage(player("Delantero", "FW", 80, c), club, DAY.minusYears(3), DAY.minusYears(1));
        Match expected = match(c, rival, DAY, "Estadio");
        assertThat(matches.findDistinctByHomeCountry_Players_PositionAndHomeCountry_Players_PlayerClubs_Club_NameOrAwayCountry_Players_PositionAndAwayCountry_Players_PlayerClubs_Club_Name(
                "FW", "Historico", "FW", "Historico"))
                .extracting(Match::getId).containsExactly(expected.getId());
    }

    @Test
    void claveCompuestaPermiteDosEtapasDelMismoJugadorYClub() {
        Country c = country("Pais", "X");
        Player player = player("Jugador", "FW", 80, c);
        Club club = club("Club", c);
        stage(player, club, DAY.minusYears(2), DAY.minusYears(1));
        stage(player, club, DAY, null);
        em.flush();
        em.clear(); // Comprueba lo guardado en BD, no solo los objetos en memoria.
        PlayerClubId first = new PlayerClubId(player.getId(), club.getId(), DAY.minusYears(2));
        PlayerClubId same = new PlayerClubId(player.getId(), club.getId(), DAY.minusYears(2));
        PlayerClubId second = new PlayerClubId(player.getId(), club.getId(), DAY);
        assertThat(first).isEqualTo(same).hasSameHashCodeAs(same).isNotEqualTo(second);
        assertThat(stages.count()).isEqualTo(2);
        assertThat(stages.findById(first).orElseThrow().getStartDate()).isEqualTo(DAY.minusYears(2));
        assertThat(stages.findById(second).orElseThrow().getEndDate()).isNull();
    }

    @Test
    void parametrosInvalidosResponden400() throws Exception {
        http.perform(get("/worldcup/ej1")).andExpect(status().isBadRequest());
        http.perform(get("/worldcup/ej3").param("estadio", "E").param("puntaje", "texto"))
                .andExpect(status().isBadRequest());
        http.perform(get("/worldcup/ej4").param("desde", "2026-06-20")
                        .param("hasta", "2026-06-10").param("estadio", "E"))
                .andExpect(status().isBadRequest());
        http.perform(get("/worldcup/ej4").param("desde", "no-es-fecha")
                        .param("hasta", "2026-06-10").param("estadio", "E"))
                .andExpect(status().isBadRequest());
    }
}
