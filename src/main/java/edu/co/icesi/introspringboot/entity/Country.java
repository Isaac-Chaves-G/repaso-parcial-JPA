package edu.co.icesi.introspringboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

// Estudio: JPA gestiona esta clase como entidad; @Table indica la tabla donde se guardan sus datos.
@Entity
@Table(name = "country")
public class Country {

    // Estudio: Clave primaria simple. IDENTITY hace que la base de datos genere el número al insertar.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String code;
    private String confederation;

    // Estudio: Omite esta propiedad en JSON para evitar recorridos repetidos; no elimina la relación JPA.
    @JsonIgnore
    // Estudio: La colección usa el atributo Java "homeCountry" de la otra clase para recorrer la misma relación.
    @OneToMany(mappedBy = "homeCountry")
    private List<Match> homeMatches;

    // Estudio: Omite esta propiedad en JSON para evitar recorridos repetidos; no elimina la relación JPA.
    @JsonIgnore
    // Estudio: La colección usa el atributo Java "awayCountry" de la otra clase para recorrer la misma relación.
    @OneToMany(mappedBy = "awayCountry")
    private List<Match> awayMatches;

    // Estudio: Omite esta propiedad en JSON para evitar recorridos repetidos; no elimina la relación JPA.
    @JsonIgnore
    // Estudio: La colección usa el atributo Java "country" de la otra clase para recorrer la misma relación.
    @OneToMany(mappedBy = "country")
    private List<Player> players;

    // Estudio: Omite esta propiedad en JSON para evitar recorridos repetidos; no elimina la relación JPA.
    @JsonIgnore
    // Estudio: La colección usa el atributo Java "country" de la otra clase para recorrer la misma relación.
    @OneToMany(mappedBy = "country")
    private List<Club> clubs;

    // Estudio: Constructor vacío: JPA puede crear el objeto y después cargar sus campos.
    public Country() {
    }

    // Estudio: Constructor con datos: cada this.campo = parámetro asigna un valor inicial al objeto.
    public Country(String name, String code, String confederation) {
        this.name = name;
        this.code = code;
        this.confederation = confederation;
    }

    // Estudio: Devuelve el identificador.
    public Integer getId() { return id; }
    // Estudio: Asigna el identificador al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setId(Integer id) { this.id = id; }

    // Estudio: Devuelve el nombre.
    public String getName() { return name; }
    // Estudio: Asigna el nombre al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setName(String name) { this.name = name; }

    // Estudio: Devuelve el código del país.
    public String getCode() { return code; }
    // Estudio: Asigna el código del país al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setCode(String code) { this.code = code; }

    // Estudio: Devuelve la confederación.
    public String getConfederation() { return confederation; }
    // Estudio: Asigna la confederación al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setConfederation(String confederation) { this.confederation = confederation; }

    // Estudio: Devuelve la lista de partidos donde este país es local.
    public List<Match> getHomeMatches() { return homeMatches; }
    // Estudio: Asigna la lista de partidos donde este país es local al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setHomeMatches(List<Match> homeMatches) { this.homeMatches = homeMatches; }

    // Estudio: Devuelve la lista de partidos donde este país es visitante.
    public List<Match> getAwayMatches() { return awayMatches; }
    // Estudio: Asigna la lista de partidos donde este país es visitante al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setAwayMatches(List<Match> awayMatches) {
        this.awayMatches = awayMatches;
    }

    // Estudio: Devuelve la lista de jugadores del país.
    public List<Player> getPlayers() { return players; }
    // Estudio: Asigna la lista de jugadores del país al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setPlayers(List<Player> players) { this.players = players; }

    // Estudio: Devuelve la lista de clubes del país.
    public List<Club> getClubs() { return clubs; }
    // Estudio: Asigna la lista de clubes del país al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setClubs(List<Club> clubs) { this.clubs = clubs; }
}
