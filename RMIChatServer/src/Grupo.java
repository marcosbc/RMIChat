import java.util.LinkedList;
import java.util.List;

public class Grupo {
    String nombreGrupo;
    List<Cliente> miembrosGrupo;

    Grupo(){
        nombreGrupo = new String();
        miembrosGrupo = new LinkedList<Cliente>();
    }

    Grupo (String nombre, List<Cliente> miembros){
        this.nombreGrupo = nombre;
        this.miembrosGrupo = miembros;
    }

    void add (Cliente c){
        miembrosGrupo.add(c);
    }

    void remove (Cliente c){
        miembrosGrupo.remove(c);
    }
}
