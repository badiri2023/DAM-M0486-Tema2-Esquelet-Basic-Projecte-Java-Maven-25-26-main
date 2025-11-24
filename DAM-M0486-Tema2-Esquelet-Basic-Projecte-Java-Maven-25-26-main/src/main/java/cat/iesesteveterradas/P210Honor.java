package cat.iesesteveterradas;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cat.iesesteveterradas.model.Faccion;
import cat.iesesteveterradas.model.Personaje;
import cat.iesesteveterradas.utility.DatabaseManager;

public class P210Honor {

    private static final Logger logger = LoggerFactory.getLogger(P210Honor.class);
    private static final String DB_PATH = "data/honor.db";
    
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        
        new File("data").mkdirs();

        try {
            initDatabase();
        } catch (SQLException e) {
            logger.error("Error al iniciar la base de datos.", e);
            return;
        }

        try (Connection conn = DatabaseManager.connect(DB_PATH)) {
            
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                String opcioStr = scanner.nextLine();

                try {
                    int opcion = Integer.parseInt(opcioStr); 

                    switch (opcion) {
                        case 1:
                            opcion1_MostrarTabla(conn);
                            pausaParaContinuar(); 
                            break;
                        case 2:
                            opcion2_PersonajesPorFaccion(conn);
                            pausaParaContinuar(); 
                            break;
                        case 3:
                            opcion3_MejorAtacantePorFaccion(conn);
                            pausaParaContinuar(); 
                            break;
                        case 4:
                            opcion4_MejorDefensorPorFaccion(conn);
                            pausaParaContinuar(); 
                            break;
                        case 5:
                            salir = true;
                            logger.info("Cerrando la aplicacion. Adios!");
                            break; 
                        default:
                            logger.warn("Opcion no valida. Por favor, elige un numero entre 1 y 5.");
                    }
                } catch (NumberFormatException e) {
                    logger.error("Error: Debes introducir un numero. '{}' no es valido.", opcioStr);
                } catch (SQLException e) {
                    logger.error("Se ha producido un error con la base de datos.", e);
                }
            }

        } catch (SQLException e) {
            logger.error("Error fatal de conexion a la base de datos.", e);
        } finally {
            scanner.close(); 
        }
    }

    /**
     * Muestra el menu de opciones por consola.
     */
    private static void mostrarMenu() {
        System.out.println("\n===== GESTION DE 'FOR HONOR' =====");
        System.out.println("1. Mostrar una tabla (Faccion o Personaje)");
        System.out.println("2. Mostrar personajes por faccion");
        System.out.println("3. Mostrar mejor atacante por faccion");
        System.out.println("4. Mostrar mejor defensor por faccion");
        System.out.println("5. Salir");
        System.out.print("Opcion: ");
    }

    // ========== METODOS POR OPCION DEL MENU ==========

    /**
     * Opcion 1: Muestra un submenu para elegir que tabla visualizar.
     */
    private static void opcion1_MostrarTabla(Connection conn) throws SQLException {
        logger.info("Has elegido: 1. Mostrar Tabla");
        
        System.out.println("\n--- Submenu: Mostrar Tabla ---");
        System.out.println("1. Mostrar Tabla Faccion");
        System.out.println("2. Mostrar Tabla Personaje");
        System.out.print("Opcion: "); 
        String subOpcion = scanner.nextLine();

        switch (subOpcion) {
            case "1":
                System.out.println("\n--- Contenido de la Tabla Faccion ---");
                // Cabecera de la tabla
                System.out.printf("%-5s | %-15s | %-50s\n", "ID", "Nombre", "Resumen (truncado)");
                System.out.println(new String(new char[74]).replace("\0", "-"));

                String sqlFaccion = "SELECT * FROM Faccion;";
                try (ResultSet rs = DatabaseManager.querySelectPS(conn, sqlFaccion)) {
                    boolean hasResults = false;
                    while (rs.next()) {
                        hasResults = true;
                        Faccion faccion = new Faccion(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("resum")
                        );
                        // Imprime la fila formateada
                        System.out.printf("%-5d | %-15s | %-50s\n",
                            faccion.getId(),
                            faccion.getNom(),
                            truncate(faccion.getResum(), 50)
                        );
                    }
                    if (!hasResults) {
                        System.out.println("La tabla 'Faccion' esta vacia.");
                    }
                }
                break; 

            case "2":
                System.out.println("\n--- Contenido de la Tabla Personaje ---");
                // Cabecera de la tabla
                System.out.printf("%-5s | %-15s | %-7s | %-7s | %-10s\n", "ID", "Nombre", "Atac", "Defensa", "ID Faccion");
                System.out.println(new String(new char[50]).replace("\0", "-"));
                
                String sqlPersonaje = "SELECT * FROM Personaje;";
                try (ResultSet rs = DatabaseManager.querySelectPS(conn, sqlPersonaje)) {
                    boolean hasResults = false;
                    while (rs.next()) {
                        hasResults = true;
                        Personaje p = new Personaje(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getDouble("atac"),
                            rs.getDouble("defensa"),
                            rs.getInt("idFaccion")
                        );
                        // Imprime la fila formateada
                        System.out.printf("%-5d | %-15s | %-7.1f | %-7.1f | %-10d\n",
                            p.getId(),
                            p.getNom(),
                            p.getAtac(),
                            p.getDefensa(),
                            p.getIdFaccion()
                        );
                    }
                    if (!hasResults) {
                        System.out.println("La tabla 'Personaje' esta vacia.");
                    }
                }
                break; 

            default:
                logger.warn("Opcion '{}' no valida. Debes elegir 1 o 2.", subOpcion);
        }
    }

    /**
     * Opcion 2: Pide una faccion y muestra sus personajes.
     */
    private static void opcion2_PersonajesPorFaccion(Connection conn) throws SQLException {
        logger.info("Has elegido: 2. Mostrar Personajes por Faccion");
        
        int idFaccion = pedirFaccion(conn);
        if (idFaccion == -1) {
            logger.warn("Seleccion de faccion invalida.");
            return;
        }
        System.out.println("\n--- Personajes de la Faccion ID: " + idFaccion + " ---");
        
        // Cabecera de la tabla
        System.out.printf("%-5s | %-15s | %-7s | %-7s | %-15s\n", "ID", "Nombre", "Atac", "Defensa", "Faccion");
        System.out.println(new String(new char[56]).replace("\0", "-"));
        
        String sql = "SELECT p.*, f.nom as nomFaccion " +
                     "FROM Personaje p " +
                     "JOIN Faccion f ON p.idFaccion = f.id " +
                     "WHERE p.idFaccion = ?;";

        try (ResultSet rs = DatabaseManager.querySelectPS(conn, sql, idFaccion)) {
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                Personaje p = new Personaje(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("atac"),
                    rs.getDouble("defensa"),
                    rs.getInt("idFaccion")
                );
                p.setNomFaccion(rs.getString("nomFaccion"));

                // Imprime la fila formateada
                System.out.printf("%-5d | %-15s | %-7.1f | %-7.1f | %-15s\n",
                    p.getId(),
                    p.getNom(),
                    p.getAtac(),
                    p.getDefensa(),
                    p.getNomFaccion()
                );
            }
            if (!hasResults) {
                System.out.println("No se han encontrado personajes para esta faccion.");
            }
        }
    }

    /**
     * Opcion 3: Pide una faccion y muestra su mejor atacante.
     */
    private static void opcion3_MejorAtacantePorFaccion(Connection conn) throws SQLException {
        logger.info("Has elegido: 3. Mejor Atacante por Faccion");

        int idFaccion = pedirFaccion(conn);
        if (idFaccion == -1) {
            logger.warn("Seleccion de faccion invalida.");
            return;
        }
        System.out.println("\n--- Mejor Atacante de la Faccion ID: " + idFaccion + " ---");
        
        // Cabecera de la tabla
        System.out.printf("%-5s | %-15s | %-7s | %-7s | %-15s\n", "ID", "Nombre", "Atac", "Defensa", "Faccion");
        System.out.println(new String(new char[56]).replace("\0", "-"));

        String sql = "SELECT p.*, f.nom as nomFaccion " +
                     "FROM Personaje p " +
                     "JOIN Faccion f ON p.idFaccion = f.id " +
                     "WHERE p.idFaccion = ? " +
                     "ORDER BY p.atac DESC " +
                     "LIMIT 1;";
        try (ResultSet rs = DatabaseManager.querySelectPS(conn, sql, idFaccion)) {
            if (rs.next()) {
                Personaje p = new Personaje(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("atac"),
                    rs.getDouble("defensa"),
                    rs.getInt("idFaccion")
                );
                p.setNomFaccion(rs.getString("nomFaccion"));
                
                // Imprime la fila formateada
                System.out.printf("%-5d | %-15s | %-7.1f | %-7.1f | %-15s\n",
                    p.getId(),
                    p.getNom(),
                    p.getAtac(),
                    p.getDefensa(),
                    p.getNomFaccion()
                );
            } else {
                System.out.println("No se han encontrado personajes para esta faccion.");
            }
        }
    }

    /**
     * Opcion 4: Pide una faccion y muestra su mejor defensor.
     */
    private static void opcion4_MejorDefensorPorFaccion(Connection conn) throws SQLException {
        logger.info("Has elegido: 4. Mejor Defensor por Faccion");

        int idFaccion = pedirFaccion(conn);
        if (idFaccion == -1) {
            logger.warn("Seleccion de faccion invalida.");
            return;
        }
        System.out.println("\n--- Mejor Defensor de la Faccion ID: " + idFaccion + " ---");
        
        // Cabecera de la tabla
        System.out.printf("%-5s | %-15s | %-7s | %-7s | %-15s\n", "ID", "Nombre", "Atac", "Defensa", "Faccion");
        System.out.println(new String(new char[56]).replace("\0", "-"));

        String sql = "SELECT p.*, f.nom as nomFaccion " +
                     "FROM Personaje p " +
                     "JOIN Faccion f ON p.idFaccion = f.id " +
                     "WHERE p.idFaccion = ? " +
                     "ORDER BY p.defensa DESC " +
                     "LIMIT 1;";
        try (ResultSet rs = DatabaseManager.querySelectPS(conn, sql, idFaccion)) {
            if (rs.next()) {
                Personaje p = new Personaje(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("atac"),
                    rs.getDouble("defensa"),
                    rs.getInt("idFaccion")
                );
                p.setNomFaccion(rs.getString("nomFaccion"));
                
                // Imprime la fila formateada
                System.out.printf("%-5d | %-15s | %-7.1f | %-7.1f | %-15s\n",
                    p.getId(),
                    p.getNom(),
                    p.getAtac(),
                    p.getDefensa(),
                    p.getNomFaccion()
                );
            } else {
                System.out.println("No se han encontrado personajes para esta faccion.");
            }
        }
    }


    /**
     * Pide al usuario que elija una faccion.
     */
    private static int pedirFaccion(Connection conn) throws SQLException {
        System.out.println("\nFacciones disponibles:");
        
        String sqlFaccions = "SELECT id, nom FROM Faccion;";
        try (ResultSet rs = DatabaseManager.querySelectPS(conn, sqlFaccions)) {
            while (rs.next()) {
                System.out.println("  " + rs.getInt("id") + ". " + rs.getString("nom"));
            }
        }
        
        System.out.print("Opcion (ID): "); 
        
        try {
            String input = scanner.nextLine();
            int idFaccion = Integer.parseInt(input);
            return idFaccion;
        } catch (NumberFormatException e) {
            logger.error("'{}' no es un numero valido.", e.getMessage());
            return -1;
        }
    }
    
    /**
     * Metodo para pausas.
     */
    private static void pausaParaContinuar() {
        System.out.println("\n(Enter to continue...)"); 
        scanner.nextLine(); 
    }

    /**
     * Metodo para truncar texto para tablas.
     */
    private static String truncate(String text, int length) {
        if (text == null) {
            return "";
        }
        if (text.length() <= length) {
            return text;
        }
        return text.substring(0, length - 3) + "...";
    }


    // ========== METODO DE INICIO TABLAS  ==========

    /**
     * Verifica si la base de datos existe. Si no, la crea y la puebla con datos.
     */
    private static void initDatabase() throws SQLException {
        File dbFile = new File(DB_PATH);

        if (dbFile.exists()) {
            logger.info("La base de datos {} ya existe. No es necesario inicializar.", DB_PATH);
            return;
        }

        logger.warn("La base de datos {} no existe. Creando y poblando...", DB_PATH);

        try (Connection conn = DatabaseManager.connect(DB_PATH)) {
            logger.info("Creando tablas...");
            
            String sqlFaccion = "CREATE TABLE Faccion (" +
                               " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               " nom VARCHAR(15) NOT NULL," +
                               " resum VARCHAR(500)" +
                               ");";
            DatabaseManager.queryUpdate(conn, sqlFaccion);

            String sqlPersonaje = "CREATE TABLE Personaje (" +
                                   " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                   " nom VARCHAR(15) NOT NULL," +
                                   " atac REAL," +
                                   " defensa REAL," +
                                   " idFaccion INTEGER," +
                                   " FOREIGN KEY (idFaccion) REFERENCES Faccion(id)" +
                                   ");";
            DatabaseManager.queryUpdate(conn, sqlPersonaje);
            
            logger.info("Tablas creadas correctamente.");
            logger.info("Insertando datos iniciales (Anexo)...");
            
            String insertFaccioSQL = "INSERT INTO Faccion (nom, resum) VALUES (?, ?);";
            DatabaseManager.queryUpdatePS(conn, insertFaccioSQL, "Cavallers", "Though seen as a single group, the Knights are hardly unified...");
            DatabaseManager.queryUpdatePS(conn, insertFaccioSQL, "Vikings", "The Vikings are a loose coalition of hundreds of clans and tribes...");
            DatabaseManager.queryUpdatePS(conn, insertFaccioSQL, "Samurais", "The Samurai are the most unified of the three factions...");

            String insertPersonajeSQL = "INSERT INTO Personaje (nom, atac, defensa, idFaccion) VALUES (?, ?, ?, ?);";
            
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Warden", 1.0, 3.0, 1);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Conqueror", 2.0, 2.0, 1);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Peacekeep", 2.0, 3.0, 1);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Raider", 3.0, 3.0, 2);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Warlord", 2.0, 2.0, 2);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Berserker", 1.0, 1.0, 2);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Kensei", 3.0, 2.0, 3);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Shugoki", 2.0, 1.0, 3);
            DatabaseManager.queryUpdatePS(conn, insertPersonajeSQL, "Orochi", 3.0, 2.0, 3);

            logger.info("Datos iniciales insertados correctamente.");

        } catch (SQLException e) {
            logger.error("Error durante la inicializacion de la BD: " + e.getMessage(), e);
            dbFile.delete(); 
            throw e;
        }
    }
}