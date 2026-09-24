package edu.co.icesi.introspringboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

// Estudio: JPA gestiona esta clase como entidad; @Table indica la tabla donde se guardan sus datos.
@Entity
@Table(name = "club")
public class Club {

    // Estudio: Clave primaria simple. IDENTITY hace que la base de datos genere el número al insertar.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String city;
    private LocalDate founded;

    // Estudio: Muchos registros de Club pueden compartir un mismo country; aquí se guarda un objeto.
    @ManyToOne
    // Estudio: name es la columna SQL que guarda la FK; no es el nombre del atributo Java.
    @JoinColumn(name = "country_id")
    private Country country;

    // Estudio: Omite esta propiedad en JSON para evitar recorridos repetidos; no elimina la relación JPA.
    @JsonIgnore
    // Estudio: La colección usa el atributo Java "club" de la otra clase para recorrer la misma relación.
    @OneToMany(mappedBy = "club")
    private List<PlayerClub> playerClubs;

    // Estudio: Constructor vacío: JPA puede crear el objeto y después cargar sus campos.
    public Club() {
    }

    // Estudio: Constructor con datos: cada this.campo = parámetro asigna un valor inicial al objeto.
    public Club(String name, String city, LocalDate founded, Country country) {
        this.name = name;
        this.city = city;
        this.founded = founded;
        this.country = country;
    }

    // Estudio: Devuelve el identificador.
    public Integer getId() { return id; }
    // Estudio: Asigna el identificador al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setId(Integer id) { this.id = id; }

    // Estudio: Devuelve el nombre.
    public String getName() { return name; }
    // Estudio: Asigna el nombre al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setName(String name) { this.name = name; }

    // Estudio: Devuelve la ciudad.
    public String getCity() { return city; }
    // Estudio: Asigna la ciudad al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setCity(String city) { this.city = city; }

    // Estudio: Devuelve la fecha de fundación.
    public LocalDate getFounded() { return founded; }
    // Estudio: Asigna la fecha de fundación al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setFounded(LocalDate founded) { this.founded = founded; }

    // Estudio: Devuelve el país relacionado.
    public Country getCountry() { return country; }
    // Estudio: Asigna el país relacionado al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setCountry(Country country) { this.country = country; }

    // Estudio: Devuelve los registros de etapas de jugadores en clubes.
    public List<PlayerClub> getPlayerClubs() { return playerClubs; }
    // Estudio: Asigna los registros de etapas de jugadores en clubes al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setPlayerClubs(List<PlayerClub> playerClubs) { this.playerClubs = playerClubs; }
}
