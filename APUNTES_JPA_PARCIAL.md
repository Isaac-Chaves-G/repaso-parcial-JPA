# Chuleta JPA: qué hacer según lo que encuentres

**Accesos rápidos:** [clave compuesta](#9-si-varias-columnas-forman-la-pk-clave-compuesta) · [anotaciones](#13-referencia-de-etiquetas-cuándo-usarla--ejemplo) · [compilar y ejecutar](#14-cómo-compilar-y-ejecutar-powershell--windows) · [construir un Query Method](#15-si-debes-construir-un-query-method) · [diccionario de casos](#16-diccionario-de-escenarios-para-query-methods).

**Formato:** pista del enunciado/diagrama → código → detalle que debes revisar. Fragmentos de referencia, no clases completas. Solución completa: modelo corregido y cinco consultas implementadas exclusivamente con Query Methods. Ocho pruebas de integración pasan, incluyendo las cinco rutas, clave compuesta y casos límite. Para repasar rápido abre CHULETA_RAPIDA.md junto a pom.xml.

[Parcial](<Examen JPA FIFA Wolrdcup.pdf>): **40 % modelo** (página 2); **60 % consultas** (3 de 5, páginas 2–3). Abre el proyecto interior que contiene `pom.xml`. Código en `src/main/java/edu/co/icesi/introspringboot/`: `entity`, `repository`, `controller`.

## 1. Si necesitas leer una relación del diagrama

- **PK:** identifica una fila. **FK:** referencia otra tabla.
- **Dos rayitas:** exactamente uno. **Círculo + pata de gallo:** cero o muchos.
- Desde una entidad, mira el extremo de la otra; después lee al revés.
- Un destino → un objeto. Varios destinos → colección (`List<Tipo>` en esta plantilla).
- La anotación se lee **ESTA clase → OTRA clase**. Muchos jugadores → un país = ManyToOne en Player.

## 2. Si una clase representa una entidad del modelo

**Usa Entity; indica la tabla y conserva un constructor vacío:**

```java
@Entity
@Table(name = "player")
public class Player {
    public Player() {}
    // Atributos y otros métodos dentro de la clase.
}
```

**Revisa:** Table solo indica el nombre; no reemplaza a Entity. El constructor vacío debe ser público o protegido. Si escribes otro constructor, Java ya no genera el vacío automáticamente. Imports JPA de esta plantilla: `jakarta.persistence.*`.

## 3. Si la PK tiene una sola columna

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;
```

**Usa Id** para el identificador. **Agrega GeneratedValue con IDENTITY solo si la base de datos genera ese ID mediante una columna identidad**, como en esta plantilla. No lo copies automáticamente a claves asignadas manualmente o compuestas.

## 4. Si debes indicar el nombre de una columna simple

```java
@Column(name = "fifa_score")
private Integer fifaScore;
```

**Column recibe el nombre SQL.** El atributo Java se llama `fifaScore`. No hace falta anotar todos los atributos si el mapeo por defecto ya coincide.

## 5. Si muchos registros de X apuntan a uno de Y

Ejemplo: muchos partidos pueden compartir el mismo país visitante. **En Match:**

```java
@ManyToOne
@JoinColumn(name = "away_country_id")
private Country awayCountry;
```

- **ManyToOne:** muchos partidos → un país; cada partido guarda un objeto, no una lista.
- **JoinColumn:** nombre de la FK en MATCH. Guarda el ID del país, no todos sus datos. Por defecto referencia su PK.
- **Si es obligatorio** (dos rayitas): puedes expresar `@ManyToOne(optional = false)` y `@JoinColumn(name = "away_country_id", nullable = false)`.

## 6. Si necesitas una lista de los registros que apuntan a tu entidad

Ejemplo: desde un país, acceder a sus partidos como visitante. **Si Match ya tiene el atributo Country awayCountry**, agrega en Country:

```java
@OneToMany(mappedBy = "awayCountry")
private List<Match> awayMatches;
```

**Lectura:** mi lista usa la relación definida por `Match.awayCountry`.

**Para elegir mappedBy:** mira el tipo de la lista → abre esa clase → busca el atributo que apunta de regreso → copia su nombre exacto.

| Lo que quieres desde Country | Atributo correspondiente de Match | mappedBy |
|---|---|---|
| Partidos como local | `Country homeCountry` | `"homeCountry"` |
| Partidos como visitante | `Country awayCountry` | `"awayCountry"` |

**No confundas:** JoinColumn usa columna SQL (`away_country_id`); mappedBy usa atributo Java (`awayCountry`). No uses el nombre de tu lista ni el de la clase.

**No toda lista es OneToMany:** decide por la relación del diagrama. mappedBy tampoco crea listas; señala el mapeo del otro lado. También existe en OneToOne y ManyToMany.

## 7. Si dudas entre representar uno o ambos sentidos

- Solo necesitas ir de partido a visitante → atributo `awayCountry` en Match.
- También necesitas ir de país a sus partidos → colección `awayMatches` en Country.
- Si un Query Method recorre `Country → awayMatches`, ese atributo debe existir.
- Una relación en ambos sentidos sigue siendo **una relación**, con un atributo en cada clase.
- En el parcial, conserva ambos sentidos: la plantilla y sus métodos ya los utilizan.
- Las dos líneas COUNTRY–MATCH son local y visitante: 2 ManyToOne en Match + 2 OneToMany en Country. No son cuatro relaciones.

## 8. Si una tabla intermedia tiene datos propios

**Ejemplo del parcial:** PLAYER_CLUB relaciona jugador y club, pero además guarda fechas. Represéntala como entidad propia `PlayerClub`.

- Cada registro tiene un jugador y un club → ManyToOne hacia cada uno.
- Un jugador o club tiene varios registros → OneToMany hacia PlayerClub, si representas el sentido inverso.
- En Club, `mappedBy = "club"`, porque PlayerClub contiene `private Club club;`.
- No reemplaces este modelo por un ManyToMany directo que omita las fechas.

## 9. Si varias columnas forman la PK: clave compuesta

**Pista del parcial:** PLAYER_CLUB marca como PK `player_id`, `club_id` y `start_date`. Es una clave formada por los tres valores; la fecha distingue etapas de un mismo jugador en el mismo club.

### Procedimiento para resolverlo sin memorizar

1. **Miro el diagrama:** ¿qué columnas tienen PK? Las anoto TODAS; no incluyo una columna solo por ser FK.
2. **Pregunto qué identifica una fila:** aquí, «este jugador en este club desde esta fecha». No basta jugador + club si puede regresar otro año.
3. **Creo o completo una clase para ese identificador:** PlayerClubId guarda esos tres datos. `@Embeddable` dice que se integran en la tabla de otra entidad.
4. **La entidad usa ese identificador:** PlayerClub contiene `@EmbeddedId PlayerClubId id`. No es otro registro ni otra tabla: es un objeto que agrupa la PK.
5. **Si una parte también es FK:** el jugador y el club tienen sus relaciones ManyToOne. `@MapsId` une cada relación con la pieza correspondiente del ID.
6. **Evito duplicados:** startDate vive en id. PlayerClub pregunta a su id cuando necesita leer o asignar la fecha.
7. **Completo la mecánica:** constructor vacío, constructor con TODOS los componentes, acceso a datos, equals y hashCode con los componentes de la clave.

```text
Una fila de PLAYER_CLUB
  Identificador: (jugador 7, club 4, inicio 2020-01-01)
  Otros datos:   fecha de fin

En Java:
  PlayerClub.id = PlayerClubId(7, 4, 2020-01-01)
```

**Embeddable = define la clase que agrupa datos. EmbeddedId = usa ese grupo como identificador. MapsId = conecta una relación con una pieza del identificador.**

**Agrupa los componentes en una clase Embeddable:**

```java
@Embeddable
public class PlayerClubId implements Serializable {
    @Column(name = "player_id")
    private Integer playerId;
    @Column(name = "club_id")
    private Integer clubId;
    @Column(name = "start_date")
    private LocalDate startDate;
    // Completar constructores, acceso a datos, equals y hashCode.
}
```

**En la entidad, usa ese objeto como identificador:**

```java
@EmbeddedId
private PlayerClubId id;
```

- **Embeddable:** sus datos se integran en la tabla de la entidad; no tiene tabla propia.
- **EmbeddedId:** ese objeto representa la PK completa. No agregues otro Id independiente en esta estrategia.
- **Revisa:** constructor vacío y `equals`/`hashCode` considerando todos los componentes de la clave; en esta plantilla, conserva `Serializable`.
- **Al completar la clave:** agrega startDate al constructor de PlayerClubId, a sus getters/setters y a `equals`/`hashCode`. Los tres componentes determinan la identidad.

```java
// En equals, después de comprobar tipo y convertir a PlayerClubId:
return Objects.equals(playerId, that.playerId)
    && Objects.equals(clubId, that.clubId)
    && Objects.equals(startDate, that.startDate);
// En hashCode:
return Objects.hash(playerId, clubId, startDate);
```

**Si una columna pasa a formar parte de EmbeddedId:** evita mapearla también como campo independiente de la entidad. Para este parcial:

- Quita el campo startDate y su Column de PlayerClub; queda en PlayerClubId.
- En el constructor de PlayerClub: `this.id = new PlayerClubId(player.getId(), club.getId(), startDate);`. Elimina `this.startDate = startDate;`.
- Conserva getStartDate/setStartDate en PlayerClub delegando en `id.getStartDate()`/`id.setStartDate(...)`; contempla id nulo en objetos nuevos.
- Para futuras consultas, la ruta persistente será `id.startDate` (`Id_StartDate` en un Query Method). El getter delegado no crea un atributo persistente nuevo con acceso por campos.
- La PK no se cambia después de persistir el registro: otra fecha de inicio identifica otra etapa.

### Si un campo es FK y además forma parte de la PK

**Usa MapsId para conectar la relación con ese componente de EmbeddedId:**

```java
@ManyToOne
@MapsId("playerId")
@JoinColumn(name = "player_id")
private Player player;
```

`@MapsId("playerId")`: el ID del jugador corresponde a `id.playerId`. Recibe el nombre del atributo de PlayerClubId. `@JoinColumn(name = "player_id")` identifica la columna SQL. En esta plantilla ya están bien MapsId para playerId y clubId.

## 10. Si cambias objeto ↔ lista o falta un método

**Revisa juntos:** atributo, parámetro del constructor, retorno del getter y parámetro del setter.

```java
// Match: el visitante es UN país.
public Country getAwayCountry() { return awayCountry; }
public void setAwayCountry(Country nuevoVisitante) {
    this.awayCountry = nuevoVisitante;
}
```

- **Getter:** lee/devuelve. **Setter:** recibe/asigna; `void` = sin resultado.
- `this.atributo = parametro`: mi atributo recibe el valor entregado.
- Si guardas `List<Match>`, getter y setter también usan `List<Match>`.
- Si aparece `player.getId()` pero Player no define el método: agrega `public Integer getId() { return id; }`.
- **Constructor:** mismo nombre de clase y sin retorno: `public Match() {}`; se llama mediante `new Match()`.
- Un setter cambia el objeto; no garantiza un UPDATE inmediato en la base de datos.

## 11. Si el JSON repite relaciones o faltan datos al responder

- Para omitir una propiedad en la respuesta, usa `@JsonIgnore`. En las colecciones inversas de esta plantilla evita recorridos país → partidos → país. No altera el mapeo JPA.
- Si faltan datos de Player en JSON, revisa sus getters públicos. Tener atributos privados no garantiza que se serialicen con la configuración habitual.
- No confundas JSON con persistencia: con Id sobre el campo, JPA accede directamente a los campos y no exige todos los getters/setters.

## 12. Dónde estaban los errores del parcial

Estas eran las líneas de la plantilla ORIGINAL; en la versión comentada se desplazaron. Usa el enlace y busca el atributo o método indicado.

| Archivo | Qué revisar/corregir |
|---|---|
| [Player.java][player], línea 10 y constructor | Faltaban Entity, constructor vacío y getId. |
| [Match.java][match], línea 23, awayCountry | ManyToOne + JoinColumn + Country sin List; ajustar constructor/getter/setter. |
| [Country.java][country], línea 24, awayMatches | OneToMany(mappedBy = "awayCountry") + List<Match>; ajustar getter/setter. |
| [Club.java][club], línea 26 | mappedBy = "club", nombre del atributo de [PlayerClub][playerclub]. |
| [PlayerClubId.java][playerclubid], línea 10 | Table → Embeddable; incluir startDate en campos, constructor, getters/setters, equals/hashCode. En PlayerClub, almacenar la fecha en id y adaptar constructor y métodos. |

## 13. Referencia de etiquetas: cuándo usarla + ejemplo

Las del modelo y las necesarias para las consultas/controlador de este parcial están aquí. Los ejemplos son fragmentos: se colocan sobre la clase, atributo, método o parámetro correspondiente. No debes añadir todas las etiquetas a todas las clases.

| Etiqueta | Úsala cuando... | Ejemplo |
|---|---|---|
| `@Entity` | La clase es una entidad persistente. | `@Entity public class Player { ... }` |
| `@Table` | Indicas la tabla de esa entidad. | `@Table(name = "player")` |
| `@Id` | El atributo identifica la entidad. | `@Id private Integer id;` |
| `@GeneratedValue` | La base de datos/proveedor genera el ID según una estrategia. | `@GeneratedValue(strategy = GenerationType.IDENTITY)` |
| `@Column` | Configuras una columna simple. | `@Column(name = "fifa_score")` |
| `@ManyToOne` | Muchos de esta clase comparten uno de la otra. | `@ManyToOne private Country country;` |
| `@OneToMany` | Esta entidad tiene una colección de registros relacionados uno a muchos. | `@OneToMany(mappedBy = "country") private List<Player> players;` |
| `@JoinColumn` | Indicas la columna FK de una relación. | `@JoinColumn(name = "country_id")` |
| `@Embeddable` | Defines una clase cuyos campos se integran en una entidad. | `@Embeddable public class PlayerClubId implements Serializable { ... }` |
| `@EmbeddedId` | Ese objeto es la PK compuesta. | `@EmbeddedId private PlayerClubId id;` |
| `@MapsId` | Una relación comparte columna con un componente de la PK. | `@MapsId("playerId")` sobre la relación `player`. |
| `@JsonIgnore` | Debes omitir una propiedad en la respuesta JSON. | Sobre `List<Match> awayMatches`. |
| `@Override` | Reescribes un método heredado; es Java, no JPA. | Sobre `equals` y `hashCode`. |
| `@Repository` | Identificas un componente de acceso a datos. En las interfaces Spring Data detectadas no es obligatorio. | Sobre `PlayerRepository`. |
| `@RestController` | La clase atiende HTTP y devuelve datos, normalmente JSON. | Sobre `WorldCupController`. |
| `@RequestMapping` | Defines el prefijo de rutas del controlador. | `@RequestMapping("/worldcup")` |
| `@GetMapping` | Un método responde a una petición GET. | `@GetMapping("/jugadores")` |
| `@RequestParam` | Lees un valor de `?nombre=valor` en la URL. | Parámetro `@RequestParam("confederacion") String confederacion`. |
| `@PathVariable` | Lees un valor de una ruta como `/jugadores/{id}`. | Parámetro `@PathVariable("id") Integer id`. |
| `@Autowired` | Pides a Spring una dependencia; con un único constructor normalmente se omite. | `@Autowired` sobre un constructor si la configuración lo requiere. |
| `@SpringBootApplication` | Marcas la clase principal de arranque. Ya viene en la plantilla. | Sobre `IntroSpringBootApplication`. |
| `@PostConstruct` | Ejecutas un método después de crear e inicializar el componente. Ya está en la plantilla. | Sobre `public void init()`. |
| `@DateTimeFormat` | Recibes una fecha por URL con formato explícito. | Parámetro `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde`. |

**Otras variantes para reconocerlas, sin agregarlas a este modelo por defecto:**

| Etiqueta | Caso y ejemplo breve |
|---|---|
| `@OneToOne` | Uno con uno: `@OneToOne @JoinColumn(name = "perfil_id") private Perfil perfil;`. |
| `@ManyToMany` | Muchos con muchos sin datos propios en la unión: `@ManyToMany private Set<Rol> roles;`. PLAYER_CLUB sí tiene fechas: conservar entidad intermedia. |
| `@JoinTable` | Configuras una tabla de unión: `@JoinTable(name = "usuario_rol", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))`. |
| `@Embedded` | Un grupo de datos integrado que NO es la PK: `@Embedded private Direccion direccion;`; Direccion lleva Embeddable. |
| `@IdClass` | Alternativa a EmbeddedId: `@IdClass(Clave.class)` sobre la entidad, con cada componente marcado Id. No mezclar ambas estrategias. |
| `@Transient` | Un atributo auxiliar no debe persistirse: `@Transient private String resumen;`. No equivale a JsonIgnore. |

**En este parcial se piden únicamente Query Methods:** no resolver las consultas con `@Query` ni SQL nativo. El nombre del método expresará la búsqueda; no lleva una anotación por cada filtro.

**Imports:** JPA `jakarta.persistence.*`; web `org.springframework.web.bind.annotation.*`; Repository `org.springframework.stereotype.Repository`; JsonIgnore `com.fasterxml.jackson.annotation.JsonIgnore`; DateTimeFormat `org.springframework.format.annotation.DateTimeFormat`; PostConstruct `jakarta.annotation.PostConstruct`. List y Set vienen de `java.util`; LocalDate, de `java.time`.

## 14. Cómo compilar y ejecutar: PowerShell / Windows

### A. Abre una terminal en la carpeta que contiene pom.xml

En este equipo, puedes copiar:

```powershell
Set-Location -LiteralPath 'C:\Users\ichav\Documents\Repaso parcia JPA\computacion-2-classroom-075044-examen-jpa-JPAExamenTemplate-1-main\computacion-2-classroom-075044-examen-jpa-JPAExamenTemplate-1-main'
```

**No ejecutar desde la carpeta del PDF.** Comprueba que ves pom.xml y mvnw.cmd.

```powershell
java -version
mvn -version
```

El pom apunta a Java 17; el equipo tiene JDK 21, que puede compilar para ese destino. Maven usa el Java indicado por su configuración/JAVA_HOME. La primera compilación necesita internet para descargar dependencias.

### B. Compilar: comprueba el código Java

Con Maven instalado:

```powershell
mvn compile
```

Si `mvn` no se reconoce, usa el wrapper incluido:

```powershell
.\mvnw.cmd compile
```

Resultado esperado: **BUILD SUCCESS**. Compilar NO garantiza que el mapeo JPA y los Query Methods estén bien: también hay que arrancar.

### C. Ejecutar: arranca Spring Boot, JPA y el servidor

```powershell
mvn spring-boot:run
```

O con wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Espera a ver **Started IntroSpringBootApplication** y el servidor en el puerto **8081**. Deja la terminal abierta. Para detener: **Ctrl+C**. Guarda tus cambios y vuelve a ejecutar para probarlos.

### D. Probar una ruta

- Dirección base: `http://localhost:8081`.
- Prefijo del controlador: `/worldcup`.
- Primera consulta: `http://localhost:8081/worldcup/ej1?confederacion=CONMEBOL`. Las cinco rutas ya están implementadas; sus parámetros son obligatorios.
- La dirección raíz puede devolver 404 aunque la app esté funcionando; todavía no hay un controlador para `/`.
- Consola H2: `http://localhost:8081/h2`; JDBC URL `jdbc:h2:mem:testdb`, usuario `sa`, contraseña vacía. Son los valores de application.properties.
- La base es en memoria y usa create-drop: los cambios de datos no se conservan entre ejecuciones; data.sql vuelve a cargar los ejemplos al arrancar.

### E. Si aparece un error

| Mensaje / síntoma | Qué revisar |
|---|---|
| No hay pom.xml / MissingProjectException | Estás en la carpeta equivocada. |
| cannot find symbol | Falta un método, atributo o import, o su nombre está mal. |
| incompatible types | Mezclaste objeto y lista, o el constructor/getter/setter usa otro tipo. |
| Error de mappedBy / propiedad inexistente | Copia el nombre del atributo Java de la otra clase. |
| Columna repetida | La misma columna está mapeada dos veces para escritura; revisar EmbeddedId. |
| PropertyReferenceException / No property | El Query Method usa un nombre o recorrido de atributos que no existe. |
| Port 8081 already in use | Detén tu ejecución anterior con Ctrl+C; no cierres procesos ajenos. |
| No descarga dependencias / Non-resolvable parent | Revisar internet y configuración Maven; es anterior a los errores de tu código. |

Lee el primer error útil y las líneas **Caused by**, no solamente BUILD FAILURE.

### F. Reutilizar la caché de dependencias de esta sesión

Las comprobaciones del asistente descargan dependencias en una carpeta del espacio de estudio. Para reutilizarlas en lugar de descargarlas otra vez a la caché predeterminada, desde la carpeta de pom.xml:

```powershell
$repoEstudio = 'C:\Users\ichav\Documents\Repaso parcia JPA\tmp\maven-repository'
mvn "-Dmaven.repo.local=$repoEstudio" compile
mvn "-Dmaven.repo.local=$repoEstudio" spring-boot:run
```

El segundo comando mantiene la aplicación abierta. Esta opción solo cambia dónde Maven guarda dependencias; no cambia el código ni la base de datos.

## 15. Si debes construir un Query Method

**Procedimiento:** ¿qué devuelvo? → repositorio de esa entidad; ¿por qué filtro? → recorrido de atributos Java; ¿cómo comparo? → operador; ¿cómo ordeno? → OrderBy.

### Cómo construir el nombre sin adivinar

1. **Subraya qué te piden devolver:** jugadores → PlayerRepository y `List<Player>`.
2. **Localiza el atributo del filtro:** si está en Player, úsalo directamente; si está en Country, recorre `country → atributo`.
3. **Empieza por findBy y añade la ruta:** `Position` o `Country_Confederation`.
4. **Añade la comparación si no es igualdad:** `FifaScoreGreaterThan`. Para igualdad, no necesitas escribir Equals.
5. **Agrega condiciones con And/Or y el orden al final:** `OrderByFifaScoreDesc`.
6. **Declara parámetros para los valores que faltan:** String para posición/confederación; Integer para puntaje. El orden sigue al de las condiciones.

```java
// Atributo directo: la posición está en Player.
List<Player> findByPosition(String position);

// Dos condiciones, dos parámetros: posición y puntaje mínimo exclusivo.
List<Player> findByPositionAndFifaScoreGreaterThan(String position, Integer score);
```

**Declaración vs uso:** declaras el método una vez en la interfaz. Después, `playerRepository.findByPosition("FW")` busca delanteros; con `"GK"` busca porteros. El valor concreto va en el argumento, no dentro del nombre del método.

**El parámetro no define la ruta:** cambiar `String confederation` por `String confederacion` no cambia el filtro. La ruta sale de Country_Confederation en el nombre del método. Los resultados siguen siendo objetos Player completos, no solamente su confederación.

- Spring Data construye la consulta a partir del nombre del método. En la interfaz declaras su firma, terminada en `;`, sin cuerpo.
- `CrudRepository<Player, Integer>`: gestiona Player; su ID es Integer. Para PlayerClub, la plantilla usa `JpaRepository<PlayerClub, PlayerClubId>`.
- Usa **atributos Java**, no columnas SQL: FifaScore, no fifa_score. `_` separa pasos de una relación: `Country_Confederation` = `player.country.confederation`.
- Generalmente cada condición requiere un parámetro en el mismo orden. IsNull no recibe parámetro; Between recibe dos; OrderBy no recibe parámetro.

| Si el enunciado dice... | Usa... | Fragmento de ejemplo |
|---|---|---|
| Igual a un valor | Nombre del atributo | `findByName(String name)` |
| Ignorar mayúsculas | IgnoreCase | `findByNameIgnoreCase(String name)` |
| Mayor que | GreaterThan | `findByFifaScoreGreaterThan(Integer score)` |
| Al menos / mayor o igual | GreaterThanEqual | `findByFifaScoreGreaterThanEqual(Integer score)` |
| Entre dos valores, incluidos extremos | Between | `findByBirthDateBetween(LocalDate desde, LocalDate hasta)` |
| No tiene fecha de fin | IsNull | `findByEndDateIsNull()` en PlayerClubRepository |
| Cumplir ambas condiciones | And | `findByNameAndPosition(String name, String position)` |
| Cumplir una u otra | Or | `findByNameOrPosition(String name, String position)` |
| Evitar repetir entidades por relaciones con colecciones | Distinct | `findDistinctBy...` |
| Mayor a menor | OrderBy + atributo + Desc | `OrderByFifaScoreDesc` |
| Menor a mayor | OrderBy + atributo + Asc | `OrderByFifaScoreAsc` |
| Primeros N | TopN / FirstN | `findTop5ByOrderByFifaScoreDesc()` |

Los ejemplos de la tabla son patrones. El atributo debe existir en la entidad del repositorio. And tiene prioridad sobre Or: al mezclar, revisaremos cómo repetir condiciones para representar la lógica correcta.

### Consulta 1 del parcial: jugadores por confederación, ordenados por score descendente

**Diagrama:** PLAYER → COUNTRY → confederation; el score está en PLAYER. Devuelvo jugadores → PlayerRepository.

Dentro de [PlayerRepository.java][playerrepo], añade sin borrar sus otros métodos:

```java
// Filtra por la confederación del país y ordena a los jugadores por puntaje, de mayor a menor.
List<Player> findByCountry_ConfederationOrderByFifaScoreDesc(String confederation);
```

```text
findBy | Country_Confederation | OrderBy | FifaScore | Desc
buscar | país.confederación    | ordenar | puntaje   | mayor a menor
```

Implementación final en [WorldCupController.java][controller], método ej1:

```java
// Ejemplo: /worldcup/ej1?confederacion=CONMEBOL
@GetMapping("/ej1")
public List<Player> ej1(@RequestParam("confederacion") String confederacion) {
    // Entrega el valor de la URL al repositorio y devuelve su resultado como JSON.
    return playerRepository.findByCountry_ConfederationOrderByFifaScoreDesc(confederacion);
}
```

**Para ver los datos en JSON:** Player ya tiene getId; agrega getters públicos para name, birthDate, position, fifaScore y country. Conserva JsonIgnore en playerClubs para no incluir su historial circularmente. No necesitas setters nuevos para esta consulta de lectura.

```java
public String getName() { return name; }
public LocalDate getBirthDate() { return birthDate; }
public String getPosition() { return position; }
public Integer getFifaScore() { return fifaScore; }
public Country getCountry() { return country; }
```

**Prueba tras guardar y arrancar:** `http://localhost:8081/worldcup/ej1?confederacion=CONMEBOL`. Verifica que el país de cada jugador tenga esa confederación y que los fifaScore no aumenten al recorrer la lista. Una confederación inexistente debe devolver `[]`.

### Consulta 2 del parcial: clubes actuales de un jugador, ignorando mayúsculas

**Decisiones:** devolver clubes → ClubRepository; buscar el nombre → `playerClubs.player.name`; etapa actual según data.sql → `playerClubs.endDate == null`; recorrer colección sin repetir clubes → Distinct.

```text
Club → playerClubs → player → name     (IgnoreCase)
                  → endDate           (IsNull)
```

Implementación final dentro de ClubRepository, además del ejemplo original de la plantilla:

```java
// Clubes con una etapa abierta del jugador cuyo nombre recibimos.
List<Club> findDistinctByPlayerClubs_Player_NameIgnoreCaseAndPlayerClubs_EndDateIsNull(
    String playerName);
```

**Un parámetro:** NameIgnoreCase necesita el nombre; EndDateIsNull no recibe valor. El argumento es el nombre completo, no un fragmento (para fragmentos sería ContainingIgnoreCase).

Implementación final en WorldCupController, método ej2:

```java
// Ejemplo: /worldcup/ej2?nombre=lionel%20messi
@GetMapping("/ej2")
public List<Club> ej2(@RequestParam("nombre") String nombre) {
    return clubRepository
        .findDistinctByPlayerClubs_Player_NameIgnoreCaseAndPlayerClubs_EndDateIsNull(nombre);
}
```

**Prueba:** `http://localhost:8081/worldcup/ej2?nombre=lionel%20messi`. El resultado esperado según data.sql es Inter Miami; Barcelona y PSG son etapas cerradas. Repite con mayúsculas y con un nombre inexistente. `%20` representa el espacio en la URL. Esta consulta ya está implementada y tiene una prueba que descarta etapas cerradas incluso si otro jugador sigue en ese club.

### Consulta 3: partidos en un estadio con algún jugador LOCAL por encima del puntaje

**Dónde:** `repository/MatchRepository.java`, método que empieza `findDistinctByStadiumAndHomeCountry`; `controller/WorldCupController.java`, método `ej3`.

```java
List<Match> findDistinctByStadiumAndHomeCountry_Players_FifaScoreGreaterThan(
    String stadium, Integer score);
```

**Cómo pensarlo:** me piden partidos → MatchRepository. Estadio está en Match; puntaje está en Player → camino `homeCountry.players.fifaScore`. «Superior a» → GreaterThan, sin incluir igualdad. «Al menos uno» se cumple al encontrar un jugador; Distinct evita repetir el partido si cumplen varios. No buscar por awayCountry porque aquí sí dice LOCAL.

### Consulta 4: clubes cuyos jugadores tienen partidos entre fechas y en un estadio

**Dónde:** `repository/ClubRepository.java`, método que contiene `MatchDateBetween`; controlador `ej4`.

```text
Club → playerClubs → player → country → homeMatches → matchDate / stadium
                                    → awayMatches → matchDate / stadium
```

**Cómo pensarlo:** me piden clubes → ClubRepository. Busco los partidos de la selección del jugador. Debe cumplir **(fecha local entre límites Y estadio local) O (fecha visitante entre límites Y estadio visitante)**. Se repiten fechas y estadio en ambos lados del Or; And tiene prioridad. Between consume DOS argumentos e incluye ambos días. Distinct devuelve cada club una vez.

El nombre completo está en el repositorio comentado; no memorices sus 300 caracteres: escribe el recorrido para cada condición. Parámetros en orden: `(desde, hasta, estadio, desde, hasta, estadio)`.

**Interpretación adoptada:** las fechas son `Match.matchDate`, no las del contrato. Se consultan las asociaciones jugador-club del historial porque no se solicita vigencia contractual. El modelo no registra alineaciones: inferimos participación por selección. Si un nuevo enunciado exige un contrato vigente en la fecha del partido, NO basta con copiar esta consulta.

### Consulta 5: partidos con delanteros asociados al club indicado

**Dónde:** `repository/MatchRepository.java`, método que empieza `findDistinctByHomeCountry_Players_PositionAnd`; controlador `ej5`.

```text
Match → homeCountry → players → position = FW
                             → playerClubs → club → name = club recibido
    O → awayCountry → players → position = FW
                             → playerClubs → club → name = club recibido
```

**Cómo pensarlo:** partidos → MatchRepository. Delantero se representa como `FW` en data.sql. La posición y el club deben pertenecer al MISMO jugador. Se repiten ambas condiciones en cada lado del Or; argumentos `("FW", club, "FW", club)`. Distinct evita repetir partidos.

**Alcance:** incluye asociaciones históricas, ya que aquí no se dice «actualmente». Si el profesor usa «pertenecientes» para exigir club actual, agrega `AndHomeCountry_Players_PlayerClubs_EndDateIsNull` ANTES del Or y `AndAwayCountry_Players_PlayerClubs_EndDateIsNull` AL FINAL. No cambian los cuatro parámetros. La consulta 2 sí pide explícitamente actualidad y ya filtra EndDateIsNull.

### Rutas finales para probar con data.sql

Servidor: `http://localhost:8081`. Espacios en URL: `%20`.

| Consulta | Ruta que puedes pegar tras el servidor |
|---|---|
| 1 | `/worldcup/ej1?confederacion=CONMEBOL` |
| 2 | `/worldcup/ej2?nombre=lionel%20messi` |
| 3 | `/worldcup/ej3?estadio=Estadio%20Kansas%20City&puntaje=90` |
| 4 | `/worldcup/ej4?desde=2026-06-11&hasta=2026-06-27&estadio=Estadio%20Kansas%20City` |
| 5 | `/worldcup/ej5?club=Real%20Madrid` |

**Comprobar:** 1 orden descendente; 2 Inter Miami; 3 local con score estrictamente mayor; 4 incluye local y visitante; 5 delanteros FW. Sin coincidencias devuelve `[]`. Parámetros faltantes, número/fecha inválidos o intervalo invertido devuelven 400.

**Pruebas y empaquetado**, desde pom.xml:

```powershell
mvn test
mvn package
java -jar target/IntroSpringBoot-0.0.1-SNAPSHOT.war
```

`package` compila, prueba y genera el WAR ejecutable. Alternativamente usa `mvn spring-boot:run`; no arranques ambos en 8081 a la vez. Si usas la caché local, añade `"-Dmaven.repo.local=$repoEstudio"` a cada comando Maven. Las pruebas en `src/test/java/edu/co/icesi/introspringboot/ApplicationTests.java` generan datos aislados para verificar las condiciones; no necesitas modificarlas para ejecutar la app.

## 16. Diccionario de escenarios para Query Methods

**Consulta rápida:** busca con Ctrl+F la palabra del enunciado: «mayor», «contiene», «actualmente», «entre», «sin», «primeros», «contar» o «página». Estas son las familias aplicables a Spring Data JPA de este proyecto; no toda consulta SQL puede expresarse mediante un nombre de método.

**Regla de construcción:** tipo devuelto + `find[Distinct][TopN]By` + rutas y condiciones + `[OrderByCampoAsc/Desc]` + parámetros. Los corchetes indican partes opcionales: NO se escriben en Java.

### A. Si necesitas decidir qué devolver

| Necesidad | Ejemplo dentro del repositorio correspondiente | Resultado |
|---|---|---|
| Varios jugadores | `List<Player> findByPosition(String position);` | Lista; si no hay coincidencias, vacía. |
| Cero o un país por código | `Optional<Country> findByCode(String code);` en CountryRepository | Optional vacío si no existe; falla si hay más de uno. No presupongas unicidad si el modelo no la garantiza. |
| Cuántos jugadores cumplen | `long countByPosition(String position);` | Cantidad, no jugadores. |
| Saber si existe alguno | `boolean existsByFifaScoreGreaterThan(Integer score);` | true/false. |
| El de mayor puntaje | `Optional<Player> findFirstByOrderByFifaScoreDescIdAsc();` | Como máximo uno; IdAsc desempata. |
| Primeros cinco | `List<Player> findTop5ByOrderByFifaScoreDesc();` | Hasta cinco, no exige que existan cinco. |

`findBy`, `readBy`, `getBy`, `queryBy` y `searchBy` son alternativas de búsqueda derivada; para este examen usa **findBy**. Import de Optional: `java.util.Optional`.

**Ya heredados:** `findAll()`, `findById(id)`, `existsById(id)`, `count()`, `save(objeto)`, `deleteById(id)`. Con CrudRepository, findAll devuelve Iterable; con JpaRepository, List. No hace falta redeclararlos para usarlos. Los métodos personalizados sí se declaran en la interfaz, sin cuerpo.

### B. Si comparas números o un valor exacto

Ejemplos en **PlayerRepository**. Todas las filas reciben UN valor, salvo Between que recibe DOS.

| Enunciado | Método de ejemplo | Comparación |
|---|---|---|
| Posición igual a X | `findByPosition(String p)` | = |
| Posición diferente de X | `findByPositionNot(String p)` | <> |
| Puntaje mayor que X | `findByFifaScoreGreaterThan(Integer n)` | > |
| Puntaje de al menos X | `findByFifaScoreGreaterThanEqual(Integer n)` | >= |
| Puntaje menor que X | `findByFifaScoreLessThan(Integer n)` | < |
| Puntaje de como máximo X | `findByFifaScoreLessThanEqual(Integer n)` | <= |
| Puntaje entre A y B | `findByFifaScoreBetween(Integer a, Integer b)` | Incluye ambos extremos. |

**Recuerda el retorno:** delante de esos ejemplos escribe `List<Player>` y termina en `;`. Igualdad también admite `Is`/`Equals`: `findByPositionEquals(...)`. Not también admite IsNot. No necesitas memorizar los sinónimos.

### C. Si filtras texto

Ejemplos sobre **Player.name**; parámetro de tipo String.

| Enunciado | Nombre del método | Ejemplo del argumento |
|---|---|---|
| Nombre exacto | `findByName` | `"Luis Diaz"` |
| Sin distinguir mayúsculas | `findByNameIgnoreCase` | `"luis diaz"` |
| Contiene un texto | `findByNameContaining` | `"Diaz"` |
| Contiene sin distinguir mayúsculas | `findByNameContainingIgnoreCase` | `"diaz"` |
| No contiene | `findByNameNotContaining` | `"Diaz"` |
| Empieza por | `findByNameStartingWith` | `"Luis"` |
| Termina en | `findByNameEndingWith` | `"Diaz"` |
| Patrón SQL LIKE | `findByNameLike` | `"Luis%"` |
| No cumple un patrón | `findByNameNotLike` | `"Luis%"` |

- Con Containing/StartingWith/EndingWith entrega el texto **sin agregar `%`**.
- Con Like, `%` significa cualquier cantidad de caracteres y `_` un carácter. Ese `_` está en el ARGUMENTO, no en el nombre del método.
- IgnoreCase ignora mayúsculas/minúsculas; no promete ignorar tildes.
- Si varios filtros de texto deben ignorar mayúsculas: `findByNameAndPositionAllIgnoreCase(String nombre, String posicion)`.
- Aliases que puedes reconocer: Contains = Containing; StartsWith = StartingWith; EndsWith = EndingWith.

### D. Si filtras fechas

Ejemplos en **MatchRepository**, usando `LocalDate` y retorno `List<Match>`.

| Enunciado | Método | Extremos |
|---|---|---|
| En una fecha exacta | `findByMatchDate(LocalDate fecha)` | Igualdad. |
| Después de una fecha | `findByMatchDateAfter(LocalDate fecha)` | No incluye esa fecha. |
| Antes de una fecha | `findByMatchDateBefore(LocalDate fecha)` | No incluye esa fecha. |
| Desde una fecha, incluida | `findByMatchDateGreaterThanEqual(LocalDate fecha)` | Incluye la fecha. |
| Hasta una fecha, incluida | `findByMatchDateLessThanEqual(LocalDate fecha)` | Incluye la fecha. |
| Entre dos fechas | `findByMatchDateBetween(LocalDate desde, LocalDate hasta)` | Incluye ambas; entrega primero la menor. |

Fecha en Java: `LocalDate.of(2026, 6, 11)`. Si llega por URL, usa RequestParam y `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`; formato `2026-06-11`.

**Lee qué fecha pide:** partidos → Match.matchDate; nacimiento → Player.birthDate; inicio en club → PlayerClub.id.startDate; fin en club → PlayerClub.endDate.

### E. Si hay valores nulos, listas vacías o booleanos

| Enunciado | Ejemplo | Parámetros |
|---|---|---|
| Sin fecha de fin | `List<PlayerClub> findByEndDateIsNull();` | Ninguno. |
| Con fecha de fin | `List<PlayerClub> findByEndDateIsNotNull();` | Ninguno. |
| Países sin jugadores registrados | `List<Country> findByPlayersIsEmpty();` | Ninguno. |
| Países con jugadores registrados | `List<Country> findByPlayersIsNotEmpty();` | Ninguno. |
| Activo = true (si existiera un campo boolean active) | `findByActiveTrue()` | Ninguno. Es un ejemplo de otro modelo. |
| Activo = false (mismo supuesto) | `findByActiveFalse()` | Ninguno. |
| Booleano recibido por parámetro | `findByActive(boolean active)` | Uno. |

**No confundas null con vacío:** texto vacío es `""`; ausencia de valor es null; una colección sin elementos se consulta con IsEmpty. Una comparación Not no incluye automáticamente los valores null; si también los quieres, añade Or...IsNull explícitamente.

### F. Si el valor debe estar dentro o fuera de un conjunto

```java
// PlayerRepository: delanteros O mediocampistas, por ejemplo.
List<Player> findByPositionIn(Collection<String> positions);
List<Player> findByPositionNotIn(Collection<String> positions);

// Desde controlador/servicio:
playerRepository.findByPositionIn(List.of("FW", "MF"));
```

Import: `java.util.Collection`. In recibe **una colección de valores**, no un String con comas. Evita listas vacías o con null si no has definido qué resultado esperas.

### G. Si combinas condiciones: And, Or y paréntesis

```java
// Debe cumplir ambas; argumentos en el mismo orden que las condiciones.
List<Player> findByPositionAndFifaScoreGreaterThan(String position, Integer score);

// Basta que cumpla una.
List<Player> findByPositionOrFifaScoreGreaterThan(String position, Integer score);
```

**And se agrupa antes que Or:** AOrBAndC representa `A OR (B AND C)`. No puedes escribir paréntesis dentro del nombre.

Si necesitas `(A OR B) AND C`, tradúcelo como `(A AND C) OR (B AND C)` y repite C con su parámetro:

```java
// (posición dada O nombre contiene texto) Y score > mínimo.
List<Player> findByPositionAndFifaScoreGreaterThanOrNameContainingIgnoreCaseAndFifaScoreGreaterThan(
    String position, Integer min1, String texto, Integer min2);
// Al llamar, entrega el mismo mínimo en min1 y min2.
```

**Atajo:** si A y B comparan el mismo atributo, suele ser más claro In. Ejemplo:

```java
List<Player> findByPositionInAndFifaScoreGreaterThan(
    Collection<String> positions, Integer score);
```

### H. Si el filtro está en otra entidad: rutas del parcial

**Partes desde la entidad del repositorio.** Copia los nombres de atributos Java; no nombres de tablas, columnas ni clases de forma arbitraria.

| Repositorio | Dato que buscas | Ruta para el método |
|---|---|---|
| Player | Nombre del país | `Country_Name` |
| Player | Confederación | `Country_Confederation` |
| Player | Nombre de un club de su historial | `PlayerClubs_Club_Name` |
| Club | Nombre de un jugador de sus registros | `PlayerClubs_Player_Name` |
| Club | Fecha de fin en esos registros | `PlayerClubs_EndDate` |
| Club | Fecha de inicio en esos registros | `PlayerClubs_Id_StartDate` |
| Match | Nombre del país local / visitante | `HomeCountry_Name` / `AwayCountry_Name` |
| Match | Puntaje de jugadores del país local | `HomeCountry_Players_FifaScore` |
| Match | Posición de jugadores visitantes | `AwayCountry_Players_Position` |
| Match | Club de un jugador del país local | `HomeCountry_Players_PlayerClubs_Club_Name` |
| PlayerClub | Jugador / club dentro de la clave | `Id_PlayerId` / `Id_ClubId` |
| PlayerClub | Fecha de inicio dentro de la clave | `Id_StartDate` |

Si no encuentras una ruta, abre la clase y sigue sus atributos paso a paso. Un getter delegado como PlayerClub.getStartDate() no cambia el mapeo por campos: la fecha persistente está en `id.startDate`.

**La FK no obliga a usar solo números:** puedes buscar por el nombre o código del objeto relacionado, navegando sus atributos.

### I. Si navegas por colecciones: alguno, duplicados, ninguno

- **«Al menos un jugador con score > X»** → recorrer la colección `Players_FifaScoreGreaterThan`. GreaterThan compara puntaje, no cantidad de jugadores.
- Si varios elementos de la colección pueden coincidir, usa **Distinct** cuando necesites cada entidad principal una sola vez.
- `findDistinctBy...` elimina repeticiones de la entidad devuelta; no significa «valores únicos de cualquier columna».
- **«Alguno NO es delantero» no equivale a «ninguno es delantero».** `Players_PositionNot` puede encontrar un país que tenga un portero y también delanteros. No lo uses para expresar ausencia de delanteros.
- Si todas las condiciones deben corresponder al mismo jugador/registro, mantén la misma ruta de colección y comprueba un caso con varios registros; una consulta que arranca no garantiza por sí sola la lógica del enunciado.
- «Participan jugadores» se infiere aquí por sus países: el modelo no guarda alineaciones. Para ambos equipos del partido, considera la rama HomeCountry y la rama AwayCountry.

**Receta del parcial — clubes actuales de un jugador:** según estos datos, EndDate null representa una etapa que sigue abierta. En ClubRepository:

```java
List<Club> findDistinctByPlayerClubs_Player_NameIgnoreCaseAndPlayerClubs_EndDateIsNull(String name);
```

**Receta — partidos en estadio con algún jugador local por encima de un puntaje:** en MatchRepository:

```java
List<Match> findDistinctByStadiumAndHomeCountry_Players_FifaScoreGreaterThan(
    String stadium, Integer score);
```

No agregues filtros por actualidad, localía ni límite si el enunciado no los pide. «Jugó alguna vez en un club» incluye el historial: no añadas EndDateIsNull.

### J. Si debes filtrar una clave compuesta

```java
// PlayerClubRepository: una pieza, o un intervalo sobre una pieza de la clave.
List<PlayerClub> findById_PlayerId(Integer playerId);
List<PlayerClub> findById_StartDateBetween(LocalDate desde, LocalDate hasta);

// Buscar por TODA la clave: findById ya está heredado.
PlayerClubId clave = new PlayerClubId(7, 4, LocalDate.of(2020, 1, 1));
Optional<PlayerClub> etapa = playerClubRepository.findById(clave);
```

### K. Si debes ordenar o limitar

| Necesidad | Ejemplo en PlayerRepository |
|---|---|
| Orden descendente | `List<Player> findByPositionOrderByFifaScoreDesc(String position);` |
| Orden ascendente | `List<Player> findByPositionOrderByNameAsc(String position);` |
| Dos criterios | `List<Player> findByPositionOrderByFifaScoreDescNameAsc(String position);` |
| Ordenar todos, sin filtro | `List<Player> findAllByOrderByFifaScoreDesc();` |
| Mejores cinco de una posición | `List<Player> findTop5ByPositionOrderByFifaScoreDesc(String position);` |
| Mejor de una posición | `Optional<Player> findFirstByPositionOrderByFifaScoreDesc(String position);` |

First y Top son equivalentes. Sin número limitan a uno. **Top sin OrderBy no garantiza que sean «los mejores».** Añade un criterio como IdAsc si necesitas desempatar de forma estable. El orden va al final y no necesita un argumento adicional.

### L. Si el orden, límite o página llegan como parámetros

Orden dinámico:

```java
// En PlayerRepository:
List<Player> findByPosition(String position, Sort sort);
// En controlador/servicio:
playerRepository.findByPosition("FW", Sort.by("fifaScore").descending());
```

Paginación:

```java
// En PlayerRepository:
Page<Player> findByPosition(String position, Pageable pageable);
// Primera página: hasta 5 jugadores; mayor score primero, ID para desempatar.
Pageable pagina = PageRequest.of(0, 5,
    Sort.by(Sort.Order.desc("fifaScore"), Sort.Order.asc("id")));
Page<Player> resultado = playerRepository.findByPosition("FW", pagina);
List<Player> jugadores = resultado.getContent();
```

- Importa `Page`, `Pageable`, `PageRequest`, `Sort` desde `org.springframework.data.domain`.
- La primera página es **0**. El tamaño debe ser mayor que cero.
- Page incluye contenido y totales; normalmente requiere contar resultados. Slice indica si hay otra porción sin calcular el total. Un método con `List<Player>` y Pageable devuelve solo la lista limitada.
- Pageable y Sort son parámetros especiales: no se escriben como condiciones en el nombre. Si usas Pageable, incluye el orden dentro de él; no añadas un parámetro Sort aparte.
- No declares dos métodos con idénticos parámetros y distinto retorno. Escoge Page, Slice o List para esa firma.
- El nombre correcto de la interfaz de orden/paginación es **PagingAndSortingRepository**. En esta versión no hereda CrudRepository. JpaRepository reúne CRUD y paginación, pero no necesitas cambiar el repositorio solo por agregar Pageable a una consulta derivada.

### M. Si «actualmente» significa vigente en una fecha concreta

No confundir **fin desconocido** con **vigente en cualquier fecha**. Para una fecha de corte t, la regla habitual es:

```text
inicio <= t AND (fin IS NULL OR fin >= t)
```

Si el enunciado usa esa regla, en PlayerClubRepository puedes distribuir el AND:

```java
List<PlayerClub> findById_StartDateLessThanEqualAndEndDateIsNullOrId_StartDateLessThanEqualAndEndDateGreaterThanEqual(
    LocalDate corte1, LocalDate corte2, LocalDate corte3);
// Los tres argumentos reciben la misma fecha de corte.
```

Para el ejercicio de clubes actuales del parcial trabajaremos con el significado de sus datos: EndDate null. Si se pide «jugó entre dos fechas», distingue si habla de partidos, etapas que empiezan en ese intervalo o etapas que se solapan con él; son condiciones diferentes.

### N. Si piden solo algunos datos, contar, o eliminar

**Contar no es sumar/promediar:** `countBy...` devuelve cuántas entidades coinciden. `countDistinctByCountry_Confederation(...)` cuenta jugadores distintos de esa confederación, no países distintos. No inventes `sumBy` o `avgBy` como si fueran equivalentes.

**Solo nombre y puntaje:** puedes usar una proyección de interfaz, si el ejercicio lo permite. Define la interfaz en su archivo y úsala como retorno:

```java
public interface PlayerResumen {
    String getName();
    Integer getFifaScore();
}
// Método en PlayerRepository:
List<PlayerResumen> findResumenByPosition(String position);
```

Escribir `findNameBy...` no hace que el nombre intermedio seleccione mágicamente esa columna. El tipo de retorno/proyección define qué datos obtienes.

**Eliminar por condición (fuera de las consultas de lectura de este parcial):** requiere una transacción. Ejemplo en PlayerClubRepository:

```java
@Transactional
long deleteByEndDateBefore(LocalDate fecha);
```

Import: `org.springframework.transaction.annotation.Transactional`. Devuelve cuántos elimina. Un delete derivado no necesita Modifying; no lo ejecutes para probar búsquedas.

### O. Errores rápidos que debes evitar

1. **Nombre SQL en el método:** `Fifa_score` está mal; usa `FifaScore`.
2. **Valor dentro del nombre:** la posición `"FW"` va como argumento; el método usa `Position`.
3. **Repositorio equivocado:** revisa `extends ...<Entidad, TipoId>`: ESA entidad es el origen de las rutas. En PlayerClubRepository, Country_Confederation buscaría country dentro de PlayerClub, donde no existe. Escribir List<Player> como retorno no cambia el origen. Al mover un método al repositorio correcto, elimina la copia del incorrecto.
4. **Falta un parámetro:** cuenta condiciones; Between usa dos; IsNull, True, False e IsEmpty usan cero.
5. **Parámetros en otro orden:** deben seguir las condiciones; sus nombres locales no cambian ese orden.
6. **Retorno único sin garantizar unicidad:** Optional no limita automáticamente a uno; usa First/Top o un criterio único.
7. **Or mal agrupado:** no hay paréntesis en el nombre; distribuye las condiciones o usa In si corresponde.
8. **Olvidar visitantes:** una ruta HomeCountry solo contempla locales.
9. **Confundir fechas:** matchDate, birthDate, id.startDate y endDate significan cosas distintas.
10. **Confiar solo en la compilación:** guarda, arranca y prueba valores que incluyan, excluyan y no encuentren resultados.
11. **Llave extra al pegar un método:** al final del controlador normalmente cierras el último método y luego la clase; no agregues una tercera llave suelta. Un error en la última llave puede impedir compilar toda la clase.

**Límites del mecanismo:** no todo se resuelve alargando el nombre. Agrupaciones, sumas/promedios, «todos cumplen», ciertas ausencias y subconsultas pueden requerir otra estrategia. Regex/Near/Within no son operadores de consulta derivada JPA de este proyecto. Si el parcial exige únicamente Query Methods, no sustituyas las consultas por Query/SQL nativo; revisa la ruta y las alternativas permitidas.

Fuentes para consultar casos adicionales: [operadores y modificadores](https://docs.spring.io/spring-data/jpa/reference/3.5/repositories/query-keywords-reference.html), [construcción, límites y paginación](https://docs.spring.io/spring-data/jpa/reference/3.5/repositories/query-methods-details.html), [tipos de retorno](https://docs.spring.io/spring-data/jpa/reference/3.5/repositories/query-return-types-reference.html), [proyecciones](https://docs.spring.io/spring-data/jpa/reference/3.5/repositories/projections.html).

Referencias del resto de la guía: [arranque con Maven](https://docs.spring.io/spring-boot/maven-plugin/run.html), [Query Methods JPA](https://docs.spring.io/spring-data/jpa/reference/3.5/jpa/query-methods.html), [EmbeddedId](https://jakarta.ee/specifications/webprofile/10/apidocs/jakarta/persistence/embeddedid).

[player]: <src/main/java/edu/co/icesi/introspringboot/entity/Player.java>
[match]: <src/main/java/edu/co/icesi/introspringboot/entity/Match.java>
[country]: <src/main/java/edu/co/icesi/introspringboot/entity/Country.java>
[club]: <src/main/java/edu/co/icesi/introspringboot/entity/Club.java>
[playerclub]: <src/main/java/edu/co/icesi/introspringboot/entity/PlayerClub.java>
[playerclubid]: <src/main/java/edu/co/icesi/introspringboot/entity/PlayerClubId.java>
[playerrepo]: <src/main/java/edu/co/icesi/introspringboot/repository/PlayerRepository.java>
[controller]: <src/main/java/edu/co/icesi/introspringboot/controller/WorldCupController.java>

## Verificación final con los datos originales

Las cinco rutas respondieron HTTP 200 al ejecutar el WAR con data.sql. Con los parámetros de los ejemplos de esta guía:

| Consulta | Resultado comprobado |
|---|---|
| 1 | 18 jugadores de CONMEBOL, en orden descendente de puntaje |
| 2 | Solo Inter Miami para lionel messi |
| 3 | Partido 19 en Kansas City con puntaje local mayor a 90 |
| 4 | 6 clubes: Barcelona, Manchester City, PSG, Atlético Madrid, River Plate, Inter Miami |
| 5 | 9 partidos para delanteros asociados históricamente a Real Madrid |

También pasaron las 8 pruebas de integración con datos aislados y se generó el WAR ejecutable. La comprobación HTTP se hizo temporalmente en 8082 porque había una ejecución anterior en 8081. La configuración del proyecto conserva 8081: detén la ejecución anterior y vuelve a arrancar para cargar el código nuevo.
