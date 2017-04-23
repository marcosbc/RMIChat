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

    public boolean add (Usuario u) {
        boolean found = false;
        if (hasMember(u)) {
            return false;
        }
        members.add(u);
        return true;
    }

    public boolean remove (Usuario u) {
        if (!hasMember(u)) {
            return false;
        }
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equals(u)) {
                members.remove(i);
            }
        }
        return true;
    }

    public boolean hasMember (Usuario u) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equals(u)) {
                return true;
            }
        }
        return false;
    }
}
