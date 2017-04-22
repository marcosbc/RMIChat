
import java.rmi.*;

interface ServicioChat extends Remote {
    /*
    void alta(Cliente c) throws RemoteException;
    */
    void send(String msg, Cliente c) throws RemoteException;
    boolean addUsuario(Cliente c) throws RemoteException;
    boolean login(Cliente c) throws RemoteException;
    void logout(Cliente c) throws RemoteException;
    void echo(String msg, Cliente c) throws RemoteException;
}
