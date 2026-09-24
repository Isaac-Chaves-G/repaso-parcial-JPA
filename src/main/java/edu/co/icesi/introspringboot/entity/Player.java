package edu.co.icesi.introspringboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;


// Estudio: JPA gestiona esta clase como entidad; @Table indica la tabla donde se guardan sus datos.
@Entity //SE AGREGA LA ETIQUETA ENTITY PORQUE ANTES NO LA TENIA Y ASI JPA LA RECONOCE COMO ENTIDAD
@Table(name = "player")
public class Player {

    // Estudio: Clave primaria simple. IDENTITY hace que la base de datos genere el número al insertar.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    // Estudio: Relaciona este atributo con la columna SQL birth_date.
    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String position;

    // Estudio: Relaciona este atributo con la columna SQL fifa_score.
    @Column(name = "fifa_score")
    private Integer fifaScore;

    // Estudio: Muchos registros de Player pueden compartir un mismo country; aquí se guarda un objeto.
    @ManyToOne
    // Estudio: name es la columna SQL que guarda la FK; no es el nombre del atributo Java.
    @JoinColumn(name = "country_id")
    private Country country;

    // Estudio: Omite esta propiedad en JSON para evitar recorridos repetidos; no elimina la relación JPA.
    @JsonIgnore
    // Estudio: La colección usa el atributo Java "player" de la otra clase para recorrer la misma relación.
    @OneToMany(mappedBy = "player")
    private List<PlayerClub> playerClubs;

    //Constructor vacio agregado porque JPA necesita un constructor sin parametros para crear los objetos al recuperar datos
    // Estudio: Constructor vacío: JPA puede crear el objeto y después cargar sus campos.
    public Player() {
    }

    // Estudio: Constructor con datos: cada this.campo = parámetro asigna un valor inicial al objeto.
    public Player(String name, LocalDate birthDate, String position, Integer fifaScore, Country country) {
        this.name = name;
        this.birthDate = birthDate;
        this.position = position;
        this.fifaScore = fifaScore;
        this.country = country;
    }

    // Estudio: Devuelve el identificador.
    public Integer getId() {
        return id;
    }
    // Los getters permiten que la respuesta JSON muestre estos datos; no son constructores.
    public String getName() { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPosition() { return position; }
    public Integer getFifaScore() { return fifaScore; }
    public Country getCountry() { return country; }


}
