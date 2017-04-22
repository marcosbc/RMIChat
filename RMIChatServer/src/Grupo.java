import java.util.LinkedList;
import java.util.List;

public class Grupo {
    String nombreGrupo;
    List<Usuario> miembrosGrupo;

    Grupo(){
        nombreGrupo = new String();
        miembrosGrupo = new LinkedList<Usuario>();
    }

    Grupo (String nombre, List<Usuario> miembros){
        this.nombreGrupo = nombre;
        this.miembrosGrupo = miembros;
    }

    void add (Usuario u){
        miembrosGrupo.add(u);
    }

    void remove (Usuario u){
        miembrosGrupo.remove(u);
    }
}
