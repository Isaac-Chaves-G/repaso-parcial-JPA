package edu.co.icesi.introspringboot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

// Estudio: JPA gestiona esta clase como entidad; @Table indica la tabla donde se guardan sus datos.
@Entity
@Table(name = "match_game")
public class Match {

    // Estudio: Clave primaria simple. IDENTITY hace que la base de datos genere el número al insertar.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Estudio: Relaciona este atributo con la columna SQL match_date.
    @Column(name = "match_date")
    private LocalDate matchDate;

    // Estudio: Muchos registros de Match pueden compartir un mismo homeCountry; aquí se guarda un objeto.
    @ManyToOne
    // Estudio: name es la columna SQL que guarda la FK; no es el nombre del atributo Java.
    @JoinColumn(name = "home_country_id")
    private Country homeCountry;

    // se modifica porque  el diagrama indica un pais visitante por partido     
    // Estudio: Muchos registros de Match pueden compartir un mismo awayCountry; aquí se guarda un objeto.
    @ManyToOne
    // Estudio: name es la columna SQL que guarda la FK; no es el nombre del atributo Java.
    @JoinColumn(name = "away_country_id")
    private Country awayCountry;

    private String stadium;

    // Estudio: Constructor vacío: JPA puede crear el objeto y después cargar sus campos.
    public Match() {
    }

    // Estudio: Constructor con datos: cada this.campo = parámetro asigna un valor inicial al objeto.
    public Match(LocalDate matchDate, Country homeCountry, Country awayCountry, String stadium) {
        this.matchDate = matchDate;
        this.homeCountry = homeCountry;
        this.awayCountry = awayCountry;
        this.stadium = stadium;
    }

    // Estudio: Devuelve el identificador.
    public Integer getId() { return id; }
    // Estudio: Asigna el identificador al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setId(Integer id) { this.id = id; }

    // Estudio: Devuelve la fecha del partido.
    public LocalDate getMatchDate() { return matchDate; }
    // Estudio: Asigna la fecha del partido al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }

    // Estudio: Devuelve el país local.
    public Country getHomeCountry() { return homeCountry; }
    // Estudio: Asigna el país local al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setHomeCountry(Country homeCountry) { this.homeCountry = homeCountry; }

// Estudio: Devuelve el país visitante (un objeto, no una lista).
public Country getAwayCountry() {
    return awayCountry;
}

// Estudio: Asigna el país visitante (un objeto, no una lista) al objeto; por sí solo no ejecuta un UPDATE inmediato.
public void setAwayCountry(Country awayCountry) {
    this.awayCountry = awayCountry;
}
    // Estudio: Devuelve el estadio.
    public String getStadium() { return stadium; }
    // Estudio: Asigna el estadio al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setStadium(String stadium) { this.stadium = stadium; }
}
