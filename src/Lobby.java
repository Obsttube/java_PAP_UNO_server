import java.io.Serializable;

public class Lobby implements Serializable {
    private static final long serialVersionUID = 2L;
    private static long next_available_id = 0;
    public String id;
    public String name;
    /*public Lobby(String id, String name) {
        this.id = id;
        this.name = name;
    }*/
    public Lobby(String name) {
        this.id = "" + next_available_id;
        next_available_id++;
        this.name = name;
    }
}
