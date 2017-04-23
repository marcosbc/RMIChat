
import java.rmi.*;

interface ServicioChat extends Remote {
    boolean joinGroup(String group, Cliente c) throws RemoteException;
    boolean leaveGroup(String group, Cliente c) throws RemoteException;
    String[] listGroups() throws RemoteException;
    void sendMessage(String dest, String msg, Cliente c) throws RemoteException;
    boolean addUsuario(Cliente c) throws RemoteException;
    boolean login(Cliente c) throws RemoteException;
    void logout(Cliente c) throws RemoteException;
    void echo(String msg, Cliente c) throws RemoteException;
}
