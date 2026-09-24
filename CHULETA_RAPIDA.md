# Chuleta rápida — JPA y consultas del parcial

Lee esta primero. [Guía con escenarios y ejemplos](APUNTES_JPA_PARCIAL.md) · [Diagrama corregido](model.md) · [Parcial](<Examen JPA FIFA Wolrdcup.pdf>).

## 1. Del diagrama al código

1. Marco TODAS las columnas PK; distingo las FK.
2. Miro el extremo de la OTRA entidad: uno → objeto; muchos → colección.
3. Leo la relación desde la clase donde escribo: muchos jugadores → un país = ManyToOne.
4. FK en mi tabla → JoinColumn. Lista inversa → mappedBy con el atributo de la otra clase.
5. Si cambio objeto/lista, ajusto constructor, getter y setter al mismo tipo.

```java
// Match: cada partido tiene UN visitante; ese país puede visitar en MUCHOS partidos.
@ManyToOne
@JoinColumn(name = "away_country_id") // Columna SQL: contiene el ID del país.
private Country awayCountry;

// Country: lista de partidos que ya apuntan a este país mediante Match.awayCountry.
@OneToMany(mappedBy = "awayCountry") // Atributo Java de Match; NO nombre SQL.
@JsonIgnore // Omite la lista en JSON; la relación JPA sigue existiendo.
private List<Match> awayMatches;
```

**ManyToOne = tipo de relación. JoinColumn = dónde está la FK. mappedBy = quién ya la mapea.** Las relaciones no seleccionan qué campos devuelve una consulta; permiten navegar entre entidades. Dos líneas distintas en el diagrama pueden ser dos roles (local/visitante); representarlas en ambos sentidos no crea relaciones nuevas.

## 2. Clave compuesta: copiar la estructura, cambiar los componentes

**Pista:** varias columnas PK en UNA tabla. Aquí jugador + club + inicio identifican UNA etapa; otro inicio permite volver al mismo club.

```java
// PlayerClubId.java (estructura abreviada; clase completa comentada en entity).
@Embeddable
public class PlayerClubId implements Serializable {
    @Column(name = "player_id") private Integer playerId;
    @Column(name = "club_id") private Integer clubId;
    @Column(name = "start_date") private LocalDate startDate;
    public PlayerClubId() {}
    // Constructor con los TRES datos, getters/setters y equals/hashCode con los TRES.
}

// PlayerClub.java
@EmbeddedId
private PlayerClubId id;

@ManyToOne
@MapsId("playerId") // Componente de PlayerClubId que comparte la FK.
@JoinColumn(name = "player_id") // Columna SQL de esa relación.
private Player player;

@ManyToOne
@MapsId("clubId")
@JoinColumn(name = "club_id")
private Club club;

@Column(name = "end_date")
private LocalDate endDate; // NO forma parte de la PK.
```

- **Embeddable:** define el grupo de datos; no crea tabla.
- **EmbeddedId:** ese grupo es la PK de la entidad. No agregar otro Id/GeneratedValue.
- **MapsId:** vincula una relación con una pieza de la PK; usa nombre Java dentro de la clase ID.
- **Embedded:** usa un grupo que NO es PK; no sustituye EmbeddedId.
- `startDate` se guarda SOLO en `id`; consultar con `Id_StartDate`, no `StartDate`.
- Constructor de etapa: `id = new PlayerClubId(player.getId(), club.getId(), startDate);`.
- Guarda primero jugador/club para tener IDs. No cambies una PK ya persistida.
- Repositorio: `JpaRepository<PlayerClub, PlayerClubId>`; el segundo tipo siempre es el de la PK.
- `equals` compara los tres valores; `hashCode` usa `Objects.hash(playerId, clubId, startDate)`.

## 3. Etiquetas: cuándo y ejemplo mínimo

| Si ves/necesitas… | Usa / ejemplo |
|---|---|
| Clase persistente | `@Entity` y constructor vacío público/protegido |
| Nombre de tabla distinto | `@Table(name="match_game")` sobre la clase |
| PK simple | `@Id private Integer id;` |
| ID generado por columna identidad | `@GeneratedValue(strategy=GenerationType.IDENTITY)` |
| Nombre/configuración de columna | `@Column(name="fifa_score")` sobre `fifaScore` |
| Muchos → uno | `@ManyToOne @JoinColumn(name="country_id") Country country;` |
| Uno → muchos, sentido inverso | `@OneToMany(mappedBy="country") List<Player> players;` |
| Uno → uno | `@OneToOne @JoinColumn(name="perfil_id") Perfil perfil;` |
| Muchos ↔ muchos SIN datos propios en unión | `@ManyToMany Set<Rol> roles;` y `@JoinTable(...)` |
| Tabla unión CON fechas/otros datos | Entidad intermedia + dos ManyToOne, como PlayerClub |
| Omitir propiedad en JSON | `@JsonIgnore` sobre la colección; no cambia las consultas |
| Campo auxiliar que no va a BD | `@Transient String resumen;` |
| Marcar acceso a datos | `@Repository` sobre interfaz que extiende CrudRepository/JpaRepository |
| Respuesta HTTP en JSON | `@RestController` sobre clase |
| Prefijo de rutas | `@RequestMapping("/worldcup")` |
| Método para GET | `@GetMapping("/ej1")` |
| Dato tras ? en URL | `@RequestParam("nombre") String nombre` |
| Dato dentro de ruta /jugadores/{id} | `@PathVariable("id") Integer id` |
| Fecha por URL | `@DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate desde` |
| Dependencias | Constructor único: Spring lo usa sin `@Autowired` |
| Arranque | `@SpringBootApplication` sobre la clase principal |
| Reescribir método Java heredado | `@Override` sobre equals/hashCode; no es JPA |

`@JoinTable(name="usuario_rol", joinColumns=@JoinColumn(name="usuario_id"), inverseJoinColumns=@JoinColumn(name="rol_id"))` configura una unión ManyToMany. No copiarla sobre PlayerClub: su unión tiene fechas propias. Más variantes e imports en sección 13 de la guía.

## 4. Cómo inventar un Query Method

**Qué devuelve → repositorio de esa entidad → camino de atributos Java → condición → parámetros.**

```java
// PlayerRepository: devuelve jugadores y empieza a navegar desde Player.
List<Player> findByCountry_ConfederationOrderByFifaScoreDesc(String confederation);
// Player -> country -> confederation. Ordena fifaScore de mayor a menor.
```

Solo declaras firma + `;`: Spring construye la implementación. No escribir SQL ni cuerpo. Métodos heredados: `findById(id)`, `findAll()`, `save(objeto)`, `deleteById(id)` ya existen.

| Si pide… | Fragmento y parámetros |
|---|---|
| Igual | `Name(String valor)` |
| Mayor / menor estricto | `FifaScoreGreaterThan(Integer x)` / `LessThan` |
| Mayor/menor o igual | `GreaterThanEqual` / `LessThanEqual` |
| Distinto | `NameNot(String x)` |
| Entre fechas, incluye extremos | `MatchDateBetween(LocalDate desde, LocalDate hasta)` |
| Antes / después | `MatchDateBefore(fecha)` / `After(fecha)` |
| Texto sin importar mayúsculas | `NameIgnoreCase(String nombre)` |
| Contiene fragmento | `NameContainingIgnoreCase(String parte)` |
| Empieza / termina | `NameStartingWith` / `NameEndingWith` |
| Patrón SQL con % y _ | `NameLike(String patron)` |
| Nulo / no nulo | `EndDateIsNull()` / `EndDateIsNotNull()`; sin parámetro |
| Uno de varios valores | `PositionIn(Collection<String> valores)` / `NotIn` |
| Booleano | `ActivoTrue()` / `ActivoFalse()`; requiere ese atributo booleano |
| Colección vacía / no vacía | `PlayerClubsIsEmpty()` / `PlayerClubsIsNotEmpty()` |
| Ambas condiciones | `NameAndPosition(nombre, posicion)` |
| Una u otra | `NameOrPosition(nombre, posicion)` |
| Orden mayor primero | `OrderByFifaScoreDesc` al final |
| Orden menor primero | `OrderByNameAsc` al final |
| Evitar repetir entidad raíz | `findDistinctBy...` al recorrer colecciones |
| Primeros 5 | `findTop5By...OrderByFifaScoreDesc(...)` |
| Contar / existencia | `long countByPosition(String p)` / `boolean existsByName(String n)` |
| Campo dentro de PK | `findById_StartDateBetween(desde, hasta)` en PlayerClubRepository |

**Trampas:** `fifaScore`, NO `fifa_score`; los `_` separan recorridos Java. IsNull no consume argumentos; Between consume dos. And tiene prioridad: `(A O B) Y C` se escribe `AAndCOrBAndC` y se repite C. Una lista no exige una consulta especial: la navegación encuentra filas que cumplen; Distinct evita repetir la raíz. No agregar Top, actualidad ni posición si el enunciado no lo pide.

## 5. Ubicación exacta de la solución

Desde la carpeta de `pom.xml`, abre `src/main/java/edu/co/icesi/introspringboot/`:

| Parte del parcial | Archivo y búsqueda con Ctrl+F |
|---|---|
| Modelo y relaciones | `entity/Player.java`, `Country.java`, `Match.java`, `Club.java` |
| Clave de tres partes | `entity/PlayerClubId.java` → `@Embeddable`; `PlayerClub.java` → `@EmbeddedId` |
| 1: confederación + orden | `repository/PlayerRepository.java` → `ConfederationOrderBy` |
| 2: clubes actuales del jugador | `repository/ClubRepository.java` → `NameIgnoreCaseAnd` |
| 3: estadio + score LOCAL | `repository/MatchRepository.java` → `ByStadiumAnd` |
| 4: fechas de partidos + estadio | `repository/ClubRepository.java` → `MatchDateBetween` |
| 5: delantero + club, ambos equipos | `repository/MatchRepository.java` → `Players_PositionAnd` |
| Las cinco rutas | `controller/WorldCupController.java` → `ej1` hasta `ej5` |

**4 y 5:** se infiere participación por selección, pues no hay alineaciones. Se incluyen asociaciones históricas jugador-club; no dicen «actualmente». Si te exigen actuales, agrega el filtro EndDateIsNull en cada rama. Consulta 2 sí lo incluye.

## 6. Compilar, probar y ejecutar

Abre terminal EN la carpeta de `pom.xml` (la carpeta interior repetida).

```powershell
mvn compile         # Comprueba compilación.
mvn test            # Ejecuta las 8 pruebas de integración.
mvn spring-boot:run # Arranca; espera Started y puerto 8081. Detener: Ctrl+C.
```

Si no tienes Maven: sustituye `mvn` por `.\mvnw.cmd`. Necesitas JDK 17 o compatible (aquí se verificó con JDK 21).

Para aprovechar las dependencias YA descargadas en este equipo:

```powershell
$repoEstudio = 'C:\Users\ichav\Documents\Repaso parcia JPA\tmp\maven-repository'
mvn "-Dmaven.repo.local=$repoEstudio" spring-boot:run
```

Abrir en navegador:

```text
http://localhost:8081/worldcup/ej1?confederacion=CONMEBOL
http://localhost:8081/worldcup/ej2?nombre=lionel%20messi
http://localhost:8081/worldcup/ej3?estadio=Estadio%20Kansas%20City&puntaje=90
http://localhost:8081/worldcup/ej4?desde=2026-06-11&hasta=2026-06-27&estadio=Estadio%20Kansas%20City
http://localhost:8081/worldcup/ej5?club=Real%20Madrid
```

Para generar archivo ejecutable: `mvn package`; luego `java -jar target/IntroSpringBoot-0.0.1-SNAPSHOT.war`. No arrancar dos instancias en 8081. La raíz `/` no tiene ruta: 404 allí no significa que falló la aplicación.

**Si falla:** `cannot find symbol` → nombre/import; `No property` → recorrido Java; error `mappedBy` → atributo en otra clase; columna repetida → revisar EmbeddedId/MapsId; puerto ocupado → detener ejecución anterior. Compilar no basta: errores de consultas y mapeo aparecen al arrancar.
