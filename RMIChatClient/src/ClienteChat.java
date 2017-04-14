import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ClienteChat {
    public static boolean SALIDA = false;
    
    static public void main (String args[]) {
        if (args.length!=2) {
            System.err.println("Uso: ClienteChat hostregistro numPuertoRegistro");
            return;
        }

       if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            
            ServicioChat srv = (ServicioChat) Naming.lookup("//" + args[0] + ":" + args[1] + "/Chat");
            Scanner entrada = new Scanner(System.in);
            
            //**********LOGIN********************
            Cliente c = login(srv, entrada);
            
            
            if (!SALIDA && c != null){
            //RESTO DEL PROGRAMA    
            }
            
            srv.baja(c);
            System.exit(0);
            
            
            /*String apodo = args[2];
            ClienteImpl c = new ClienteImpl(apodo);
            srv.alta(c);
            Scanner ent = new Scanner(System.in);
            System.out.print(apodo + "> ");
            while (ent.hasNextLine()) {
                srv.envio(c, apodo, ent.nextLine());
                System.out.print(apodo + "> ");
            }
            srv.baja(c);
            System.exit(0);*/
        }
        catch (RemoteException e) {
            System.err.println("Error de comunicacion: " + e.toString());
        }
        catch (Exception e) {
            System.err.println("Excepcion en ClienteChat:");
            e.printStackTrace();
        }
    }

    private static Cliente login(ServicioChat srv, Scanner entrada) {
        boolean finLogin = false;
        String input;
        List<String> palabras = new ArrayList<String>();
        String nombre;
        String contraseña;
        int intentos = 0;
        Cliente c = null;
        
        System.out.println("\n\n\n\n\n");
        System.out.println("Bienvenido al ClienteChat para el proyecto de SDySW");
        System.out.println("Por favor, indique su nombre de usuario o escriba /nuevo para crear uno");
        System.out.println("En cualquier momento, escriba /exit para salir del programa\n");
        
        while (entrada.hasNextLine() && !finLogin){
            input = entrada.nextLine();
            palabras = Arrays.asList(input.split(" "));
            //CASO /EXIT
            if (input.equalsIgnoreCase("/exit")){
                finLogin = true;
                SALIDA = true;
            }
            //CASO DE MAS DE UNA PALABRA
            else if (palabras.size()!=1) {
                System.out.println("\nEl nombre de usuario ha de estar formado por una sola palabra");
            } 
            //CASO DE NUEVO CLIENTE
            else if (input.equalsIgnoreCase("/nuevo")){
                c = crearUsuario(srv, entrada);
                if (c!=null){
                    return c;
                }else{
                    return null;
                }
            } 
            //NOMBRES NO PUEDEN EMPEZAR CON /
            else if (input.substring(0, 1).equals("/")){
                System.out.println("\nEl nombre de usuario no puede empezar por /, es un carácter reservado para comandos");
            } 
            //CASO DE LOGIN NORMAL, PIDIENDO CONTRASEÑA
            else {
                nombre = new String(input);
                System.out.println("\nPor favor, introduzca la contraseña");
                contraseña = entrada.nextLine();
                System.out.println("Comprobando credenciales, espere por favor");
                try {
                    c = new ClienteImpl(nombre, contraseña);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                if(srv.login(c)){
                    return c;
                } else{
                    System.out.println("\nLas credenciales no son correctas. Se permiten tres intentos.");
                    intentos++;
                    System.out.println("LLeva "+intentos+" intentos");
                    
                    if (intentos >= 3){
                        finLogin = true;
                        SALIDA = true;
                        System.out.println("\nDemasiados intentos, el programa se cerrarrá.");
                    }                    
                    if (!finLogin){
                        System.out.println("Por favor, vuelva a introducir su nombre de usuario o /nuevo para crear uno");
                    }
                }
            }
        }
        
        return null;
    }

    private static Cliente crearUsuario(ServicioChat srv, Scanner entrada) {
        boolean finCrear = false;
        String input;
        String nombre;
        String contraseña;
        List<String> palabras = new ArrayList<String>();
        Cliente c = null;
        
        System.out.println("\nSe está procediendo a la creación de un nuevo usuario");
        System.out.println("Por favor, indique su nombre de usuario: ");
        
        while (entrada.hasNextLine() && !finCrear){
            input = entrada.nextLine();
            palabras = Arrays.asList(input.split(" "));
            
            if (input.equalsIgnoreCase("/exit")){
                finCrear = true;
                SALIDA = true;
            } else if (palabras.size()!=1) {
                System.out.println("\nEl nombre de usuario ha de estar formado por una sola palabra");
            } else if (input.substring(0, 1).equals("/")){
                System.out.println("\nEl nombre de usuario no puede empezar por /, es un carácter reservado para comandos");
            } else{
                nombre = new String (input);
                System.out.println("\nPor favor, introduzca la contraseña");
                contraseña = entrada.nextLine();
                System.out.println("Se va a proceder a añadir el usuario, espere por favor");
                
                try {
                    c = new ClienteImpl (nombre, contraseña);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                if(srv.añadirUsuario(c)){
                    System.out.println("\nUsuario registrado correctamente");
                    return c;
                } else{
                    System.out.println("El usuario ya existe. Por favor inténtelo de nuevo o introduzca /exit para salir");
                    finCrear=true;
                }
            }
        }
        return null;
        
    }
}
