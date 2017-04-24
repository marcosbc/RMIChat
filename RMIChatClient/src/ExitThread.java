import java.rmi.*;
import java.rmi.server.*;

// Hilo que gestionará la salida del programa
class ExitThread extends Thread {
    // Parámetros a usar en comunicación con el servidor
    Cliente c = null;
    ServicioChat srv = null;
    // Constructor, para cargar atributos
    ExitThread (Cliente c, ServicioChat srv) {
        super();
        this.c = c;
        this.srv = srv;
    }
    // Método a ejecutar por el hilo
    public void run() {
        if (c != null && srv != null) {
            try {
                Logger.info("Saliendo del programa...");
                srv.logout(c);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
