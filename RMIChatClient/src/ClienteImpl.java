import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ClienteImpl extends UnicastRemoteObject implements Cliente {
    private String username;
    private String password;
    private List<String> currentGroups;

    ClienteImpl(String username, String password) throws RemoteException {
        this.username = username;
        this.password = password;
        this.currentGroups = new ArrayList<String>();
    }

    public void notify(String apodo, String m) throws RemoteException {
        System.out.println(apodo + "> " + m);
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    public String getPassword() throws RemoteException {
        return password;
    }

    public String[] listGroups() {
        return currentGroups.toArray(new String[currentGroups.size()]);
    }

    public void joinGroup(String g) {
        if (!currentGroups.contains(g)) {
            currentGroups.add(g);
        }
    }

    public void leaveGroup(String g) {
        for (int i = 0; i < currentGroups.size(); i++) {
            if (currentGroups.get(i).equals(g)) {
                currentGroups.remove(i);
            }
        }
    }

    public void echo(String msg) {
        System.out.println("Recibido mensaje de eco: " + msg);
    }
}
