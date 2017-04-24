import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ClienteImpl extends UnicastRemoteObject implements Cliente {
    // Propiedades que identifican un cliente con un usuario en el servidor
    private String username;
    private String password;

    // Constantes utilizadas para notificaciones a clientes
    public static final int NOTIFICATION_GROUPMSG = 0;
    public static final int NOTIFICATION_PRIVATEMSG = 1;
    public static final int NOTIFICATION_USERJOIN = 2;
    public static final int NOTIFICATION_USERLEAVE = 3;
    public static final int NOTIFICATION_NOSUCHGROUP = 4;
    public static final int NOTIFICATION_ALREADYMEMBERSHIP= 5;
    public static final int NOTIFICATION_NOMEMBERSHIP = 6;
    public static final int NOTIFICATION_NOSUCHUSER = 7;
    public static final int NOTIFICATION_NOVALIDMSG = 8;
    public static final int NOTIFICATION_GROUPEMPTY = 9;
    public static final int NOTIFICATION_NOMSGTYPE = 10;
    public static final int NOTIFICATION_NOSELFMSG = 11;

    // Constantes de texto
    public static final String NOOP = "No se ha podido realizar la operaci\u00f3n anterior";

    // Constructor de la clase
    ClienteImpl(String username, String password) throws RemoteException {
        this.username = username;
        this.password = password;
    }

    // Mostrar notificación, de parte del servidor, a un cliente
    // Aquí se definirá el estilo de cada tipo de notificación, y de si se mostrará o no
    public void notify(int type, String user, String group, String m) throws RemoteException {
        switch (type) {
            case NOTIFICATION_GROUPMSG:
               Logger.text(Logger.RED_BOLD + group + " " + Logger.CYAN_BOLD + user + "> " + Logger.RESET + m);
               break;
            case NOTIFICATION_PRIVATEMSG:
               Logger.text(Logger.GRAY_BOLD + "(privado) " + Logger.CYAN_BOLD + user + "> " + Logger.RESET + m);
               break;
            case NOTIFICATION_USERJOIN:
               Logger.text(Logger.RED_BOLD + group + " " + Logger.GRAY_BOLD + user + " se ha unido" + Logger.RESET);
               break;
            case NOTIFICATION_USERLEAVE:
               Logger.text(Logger.RED_BOLD + group + " " + Logger.GRAY_BOLD + user + " ha salido" + Logger.RESET);
               break;
            case NOTIFICATION_NOSUCHGROUP:
               Logger.info(NOOP + ": No existe tal grupo.");
               break;
            case NOTIFICATION_ALREADYMEMBERSHIP:
               Logger.info(NOOP + ": Ya formas parte del grupo.");
               break;
            case NOTIFICATION_NOMEMBERSHIP:
               Logger.info(NOOP + ": No formas parte del grupo.");
               break;
            case NOTIFICATION_NOSUCHUSER:
               Logger.info(NOOP + ": El usuario no existe o no ha iniciado sesi\u00f3n.");
               break;
            case NOTIFICATION_NOVALIDMSG:
               Logger.info(NOOP + ": La validaci\u00f3n del mensaje fall\u00f3.");
               break;
            case NOTIFICATION_GROUPEMPTY:
               Logger.info(NOOP + ": No hay ning\u00fan usuario activo en el grupo para recibir el mensaje.");
               break;
            case NOTIFICATION_NOMSGTYPE:
               Logger.warn(NOOP + ": El servidor no ha reconocido el tipo de mensaje. Mensajes permitidos: # (grupales), @ (privados).");
               break;
            case NOTIFICATION_NOSELFMSG:
               Logger.info(NOOP + ": No puedes enviarte mensajes a t\u00ed mismo.");
               break;
            default:
               Logger.err("Una notificaci\u00f3n recibida del servidor no se ha reconocido.");
        }
    }

    // Obtener nombre de usuario
    public String getUsername() throws RemoteException {
        return username;
    }

    // Obtener contraseña
    public String getPassword() throws RemoteException {
        return password;
    }

    // Simple método para probar conectividad con el cliente, de parte del servidor
    public boolean ping() throws RemoteException {
        return true;
    }
}
