
import java.rmi.*;

interface Sesion extends Remote {
    String getUsername() throws RemoteException;
    String getPassword() throws RemoteException;
    void notify(String apodo, String m) throws RemoteException;
    void echo(String msg) throws RemoteException;
}
