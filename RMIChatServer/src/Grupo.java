import java.util.LinkedList;
import java.util.List;

public class Grupo {
    String name;
    List<Usuario> members;

    Grupo() {
        name = new String();
        members = new LinkedList<Usuario>();
    }

    Grupo (String name, List<Usuario> members) {
        this.name = name;
        this.members = members;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getName () {
        return name;
    }

    public void setMembers (List<Usuario> members) {
        this.members = members;
    }

    public List<Usuario> getMembers () {
        return members;
    }

    public void add (Usuario u) {
        members.add(u);
    }

    public void remove (Usuario u) {
        members.remove(u);
    }
}
