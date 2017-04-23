import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ClienteImpl extends UnicastRemoteObject implements Cliente {
    private String username;
    private String password;
    private List<String> currentGroups;

    public static final int NOTIFICATION_GROUPMSG = 0;
    public static final int NOTIFICATION_PRIVATEMSG = 1;
    public static final int NOTIFICATION_USERJOIN = 2;
    public static final int NOTIFICATION_USERLEAVE = 3;

    ClienteImpl(String username, String password) throws RemoteException {
        this.username = username;
        this.password = password;
        this.currentGroups = new ArrayList<String>();
    }

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
            default:
               Logger.err("Una notificación recibida del servidor no se ha reconocido");
        }
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
