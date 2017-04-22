
import java.rmi.*;

interface ServicioChat extends Remote {
    void joinGroup(String group, Cliente c) throws RemoteException;
    void leaveGroup(String group, Cliente c) throws RemoteException;
    void sendMessage(String dest, String msg, Cliente c) throws RemoteException;
    boolean addUsuario(Cliente c) throws RemoteException;
    boolean login(Cliente c) throws RemoteException;
    void logout(Cliente c) throws RemoteException;
    void echo(String msg, Cliente c) throws RemoteException;
}
