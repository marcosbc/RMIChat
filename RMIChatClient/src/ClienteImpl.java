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

    public void notify(String user, String group, String m) throws RemoteException {
        Logger.notif(user, group, m);
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    public String getPassword() throws RemoteException {
        return password;
    }

    public void echo(String msg) {
        Logger.info("Recibido mensaje de eco: " + msg);
    }
}
