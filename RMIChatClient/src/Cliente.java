
import java.rmi.*;

interface Cliente extends Remote {
    String getUsername() throws RemoteException;
    String getPassword() throws RemoteException;
    void notify(String apodo, String m) throws RemoteException;
    void echo(String msg) throws RemoteException;
    String[] listGroups() throws RemoteException;
    void joinGroup(String g) throws RemoteException;
    void leaveGroup(String g) throws RemoteException;
}
