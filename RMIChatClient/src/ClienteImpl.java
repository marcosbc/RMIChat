
import java.rmi.*;
import java.rmi.server.*;

class ClienteImpl extends UnicastRemoteObject implements Cliente {
    static final int CHAT_VOID = 0;
    static final int CHAT_DIRECTO = 1;
    static final int CHAT_GRUPO = 2;
    private String username;
    private String password;
    private String destination; //destino será un nombre de usuario o de grupo según el tipo de chat en el que estemos
    int status; //estado toma los valores de las constantes de arriba para saber si está en un chat directo o de grupo

    ClienteImpl(String username, String password) throws RemoteException {
        this.username = username;
        this.password = password;
        this.status = CHAT_VOID;
    }

    public void notify(String apodo, String m) throws RemoteException {
        System.out.println("\n" + apodo + "> " + m);
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    public String getPassword() throws RemoteException {
        return password;
    }

    public String getDestination() throws RemoteException {
        return destination;
    }

    public void setDestination(String destination) throws RemoteException {
        this.destination = destination;
    }
}
