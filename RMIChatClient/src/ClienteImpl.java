
import java.rmi.*;
import java.rmi.server.*;

class ClienteImpl extends UnicastRemoteObject implements Cliente {
    static final int CHAT_VOID = 0;
    static final int CHAT_DIRECTO = 1;
    static final int CHAT_GRUPO = 2;
    String nombre;
    String contrase�a;
    String destino; //destino ser� un nombre de usuario o de grupo seg�n el tipo de chat en el que estemos
    int estado; //estado toma los valores de las constantes de arriba para saber si est� en un chat directo o de grupo
    
    
    ClienteImpl(String nombre, String contrase�a) throws RemoteException {
        this.nombre = nombre;
        this.contrase�a = contrase�a;
        this.estado = CHAT_VOID;
    }
    
    public void notificacion(String apodo, String m) throws RemoteException {
	System.out.println("\n" + apodo + "> " + m);
    }
}
