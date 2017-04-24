import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

// Hilo que comprobará periódicamente si las sesiones de usuarios siguen activas
class CheckSessionsThread extends Thread {
    // Parámetros a usar para comprobar el estado de las sesiones
    Map<Cliente, Usuario> sessions = null;
    ServicioChat srv = null;
    // Constructor, para cargar atributos
    CheckSessionsThread(Map<Cliente, Usuario> sessions, ServicioChat srv) {
        super();
        this.sessions = sessions;
        this.srv = srv;
    }
    // Método a ejecutar por el hilo
    public void run() {
        while (true) {
            // Asegurarnos que hay clientes conectados
            if (srv != null && sessions != null && !sessions.isEmpty()) {
                // Recorrer clientes conectados
                for (Map.Entry<Cliente, Usuario> entry: sessions.entrySet()) {
                    // Si ocurre una excepción, hay un problema de conectividad
                    try {
                        if (!entry.getKey().ping()) {
                            // El usuario está saliendo del servidor
                            srv.logout(entry.getKey());
                        }
                    }
                    catch (RemoteException e) {
                        try {
                            // El usuario ya se ha desconectado del servidor
                            srv.logout(entry.getKey());
                        }
                        catch (Exception serverException) {
                            // Problema de conectividad con el servidor
                            serverException.printStackTrace();
                        }
                    }
                }
            }
            try {
                // Esperar un segundo hasta la siguiente comprobación
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // Ignorar fallo
            }
        }
    }
}
