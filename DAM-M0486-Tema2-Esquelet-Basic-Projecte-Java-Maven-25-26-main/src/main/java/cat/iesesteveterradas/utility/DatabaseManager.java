package cat.iesesteveterradas.utility;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe d'utilitat per gestionar la connexió i les operacions
 * amb una base de dades SQLite.
 */
public class DatabaseManager {

    // Instància del logger de SLF4J per a aquesta classe.
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    /**
     * Estableix una connexió amb la base de dades SQLite.
     *
     * @param filePath La ruta a l'arxiu de la base de dades (p.ex. "data/nor.db").
     * @return L'objecte Connection establert.
     * @throws SQLException Si hi ha un error en la connexió.
     */
    public static Connection connect(String filePath) throws SQLException {
        String url = "jdbc:sqlite:" + filePath;
        Connection conn = DriverManager.getConnection(url);
        
        logger.info("BBDD SQLite connectada a {}", filePath);
        DatabaseMetaData meta = conn.getMetaData();
        logger.info("BBDD driver: {}", meta.getDriverName());
        
        return conn;
    }

    /**
     * Tanca la connexió a la base de dades.
     * <p>
     * <b>Nota:</b> És millor pràctica usar try-with-resources en lloc de cridar aquest mètode.
     *
     * @param conn La connexió a tancar.
     */
    public static void disconnect(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.info("DDBB SQLite desconnectada");
            } catch (SQLException e) {
                logger.error("Error en tancar la connexió: {}", e.getMessage());
            }
        }
    }

    /**
     * Llista totes les taules de la base de dades.
     *
     * @param conn La connexió activa.
     * @return Una llista amb els noms de les taules.
     * @throws SQLException Si hi ha un error de base de dades.
     */
    public static List<String> listTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        // Utilitza try-with-resources per tancar el ResultSet automàticament
        try (ResultSet rs = conn.getMetaData().getTables(null, null, null, null)) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        logger.debug("S'han trobat {} taules.", tables.size());
        return tables;
    }

    // =================================================================
    // MÈTODES AMB STATEMENT (Només per a consultes estàtiques)
    // =================================================================

    /**
     * Executa una consulta d'actualització (CREATE TABLE, DROP TABLE) sense paràmetres.
     * <p>
     * <b>ALERTA:</b> NO FER SERVIR per a INSERT, UPDATE, DELETE amb dades variables
     * (risc d'injecció SQL).
     *
     * @param conn La connexió a la BBDD.
     * @param sql La consulta SQL completa i estàtica.
     * @return El nombre de files afectades.
     * @throws SQLException Si hi ha un error de base de dades.
     */
    public static int queryUpdate(Connection conn, String sql) throws SQLException {
        // Utilitza try-with-resources per tancar el Statement automàticament
        try (Statement stmt = conn.createStatement()) {
            int affectedRows = stmt.executeUpdate(sql);
            logger.debug("Executada queryUpdate. Files afectades: {}. SQL: {}", affectedRows, sql);
            return affectedRows;
        }
    }

    // =================================================================
    // MÈTODES AMB PREPAREDSTATEMENT (Segurs i Recomanats)
    // =================================================================
    
    /**
     * Executa una consulta d'actualització (INSERT, UPDATE, DELETE) de forma segura
     * usant PreparedStatement.
     *
     * @param conn La connexió a la BBDD.
     * @param sql La consulta SQL amb marcadors de posició '?'.
     * @param params Els paràmetres per a la consulta (en ordre).
     * @return El nombre de files afectades.
     * @throws SQLException Si hi ha un error de base de dades.
     */
    public static int queryUpdatePS(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]); 
            }
            int affectedRows = pstmt.executeUpdate();
            logger.debug("Executada queryUpdatePS. Files afectades: {}. SQL: {}", affectedRows, sql);
            return affectedRows;
        }
    }

    /**
     * Executa una consulta de selecció (SELECT) de forma segura usant PreparedStatement.
     * <p>
     * <b>IMPORTANT:</b> Qui cridi aquest mètode ÉS RESPONSABLE DE TANCAR
     * el {@link ResultSet} retornat!
     * Es recomana fer-ho sempre amb un bloc <b>try-with-resources</b>.
     *
     * @param conn La connexió a la BBDD.
     * @param sql La consulta SQL amb marcadors de posició '?'.
     * @param params Els paràmetres per a la consulta (en ordre).
     * @return Un objecte {@link ResultSet} amb els resultats.
     * @throws SQLException Si hi ha un error de base de dades.
     */
    public static ResultSet querySelectPS(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            logger.debug("Executant querySelectPS. SQL: {}", sql);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            if (pstmt != null) {
                pstmt.close();
            }
            logger.error("Error executant querySelectPS: {}", e.getMessage(), e);
            throw e; 
        }
    }
}