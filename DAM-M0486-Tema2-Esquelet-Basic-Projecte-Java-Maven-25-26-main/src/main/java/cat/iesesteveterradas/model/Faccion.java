package cat.iesesteveterradas.model;

public class Faccion { 
    private int id;
    private String nom;
    private String resum;

    // Constructor
    public Faccion(int id, String nom, String resum) { 
        this.id = id;
        this.nom = nom;
        this.resum = resum;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getResum() {
        return resum;
    }

    @Override
    public String toString() {
        return "Faccion [ID=" + id + ", Nom=" + nom + "]\nResumen: " + resum;
    }
}
