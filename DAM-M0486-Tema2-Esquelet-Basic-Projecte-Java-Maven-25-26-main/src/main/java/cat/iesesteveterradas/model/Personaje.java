package cat.iesesteveterradas.model;

public class Personaje {
    private int id;
    private String nom;
    private double atac;
    private double defensa;
    private int idFaccion; 
    
    private String nomFaccion; 

    // Constructor
    public Personaje(int id, String nom, double atac, double defensa, int idFaccion) { // <-- CANVIAT
        this.id = id;
        this.nom = nom;
        this.atac = atac;
        this.defensa = defensa;
        this.idFaccion = idFaccion; 
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public double getAtac() {
        return atac;
    }

    public double getDefensa() {
        return defensa;
    }

    public int getIdFaccion() { 
        return idFaccion;
    }

    public String getNomFaccion() { 
        return nomFaccion;
    }

    public void setNomFaccion(String nomFaccion) { 
        this.nomFaccion = nomFaccion;
    }

    @Override
    public String toString() {
        if (nomFaccion != null && !nomFaccion.isEmpty()) {
            return "Personaje [ID=" + id + ", Nom=" + nom + ", Atac=" + atac + 
                   ", Defensa=" + defensa + ", Faccion=" + nomFaccion + "]"; 
        }
        return "Personaje [ID=" + id + ", Nom=" + nom + ", Atac=" + atac + 
               ", Defensa=" + defensa + ", ID Faccion=" + idFaccion + "]";
    }
}