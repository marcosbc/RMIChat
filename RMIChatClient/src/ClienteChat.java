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
            Cliente s = login(srv, entrada);

            if (!SALIDA && s != null) {
                printHelp();
                while (!SALIDA && entrada.hasNextLine()) {
                    String line = entrada.nextLine();
                    String words[] = line.split(" ");
                    String command = words[0];
                    if (line.equalsIgnoreCase("/exit")) {
                        SALIDA = true;
                    }
                    else if (words.length > 1 && command.equalsIgnoreCase("/m")) {
                        String msg[] = Arrays.copyOfRange(words, 1, words.length);

                        // Nuevo mensaje
                        srv.send(String.join(" ", msg), s);
                    }
                    else {
                        printHelp();
                    }
                }
            }

            srv.logout(s);
            System.exit(0);
        }
        catch (RemoteException e) {
            System.err.println("Error de comunicacion: " + e.toString());
        }
        catch (Exception e) {
            System.err.println("Excepcion en ClienteChat:");
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        // Comando invalido
        System.out.println(
            "Lista de comandos validos:\n\n" +
            "    /j GRUPO\n" +
            "    Union a un grupo de chat\n\n" +
            "    /l GRUPO\n" +
            "    Salida de un grupo de chat\n\n" +
            "    /m MENSAJE\n" +
            "    Envio de un nuevo mensaje\n\n" +
            "    /exit\n" +
            "    Salir de la aplicacion\n"
        );
    }

    private static Cliente login(ServicioChat srv, Scanner entrada) {
        boolean finLogin = false;
        String input;
        List<String> words = new ArrayList<String>();
        String username;
        String password;
        int intentos = 0;
        Cliente c = null;

        System.out.println(
            "\n*** Bienvenido a ClienteChat para el proyecto de SDySW ***\n" +
            "\nPor favor, indique su nombre de usuario o escriba /nuevo para crear uno" +
            "\nEn cualquier momento, escriba /exit para salir del programa\n"
        );

        System.out.print("Comando: ");
        while (entrada.hasNextLine() && !finLogin){
            input = entrada.nextLine();
            words = Arrays.asList(input.split(" "));
            //CASO /EXIT
            if (input == null || input.equals("")) {
                System.out.println("\nDebe introducir un texto para el funcionamiento del programa");
            }
            else if (input.equalsIgnoreCase("/exit")){
                finLogin = true;
                SALIDA = true;
            }
            //CASO DE MAS DE UNA PALABRA
            else if (words.size() != 1) {
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
                username = new String(input);
                System.out.print("Contraseña: ");
                password = entrada.nextLine();
                try {
                    c = new ClienteImpl(username, password);
                    if(srv.login(c)){
                        System.out.println("\nLas credenciales son correctas. Ha iniciado sesion correctamente.\n");
                        return c;
                    } else{
                        if (intentos < 3) {
                            System.out.println(
                                "\nLas credenciales no son correctas. " +
                                "Lleva " + (++intentos) + " de tres intentos."
                            );
                        } else {
                            finLogin = true;
                            SALIDA = true;
                            System.out.println("\n*** Demasiados intentos, el programa se cerrarrá ***");
                        }
                    }
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
            System.out.print("\nComando: ");
        }

        return null;
    }

    private static Cliente crearUsuario(ServicioChat srv, Scanner entrada) {
        boolean finCrear = false;
        String input;
        String username;
        String password;
        List<String> words = new ArrayList<String>();
        Cliente c = null;

        System.out.println("\nSe está procediendo a la creación de un nuevo usuario");
        System.out.print("Nombre de usuario: ");
        while (entrada.hasNextLine() && !finCrear){
            input = entrada.nextLine();
            words = Arrays.asList(input.split(" "));

            if (input.equalsIgnoreCase("/exit")){
                finCrear = true;
                SALIDA = true;
            } else if (words.size() != 1) {
                System.out.println("\nEl nombre de usuario ha de estar formado por una sola palabra");
            } else if (input.substring(0, 1).equals("/")){
                System.out.println("\nEl nombre de usuario no puede empezar por /, es un carácter reservado para comandos");
            } else{
                username = new String (input);
                System.out.print("Contraseña: ");
                password = entrada.nextLine();
                try {
                    c = new ClienteImpl (username, password);
                    // srv.echo("Creacion de cliente", c);
                    if(srv.addUsuario(c)){
                        // srv.echo("Cliente creado", c);
                        System.out.println("\n*** Usuario registrado correctamente ***\n");
                        return c;
                    } else{
                        // srv.echo("Cliente duplicado", c);
                        System.out.println("El usuario ya existe. Por favor inténtelo de nuevo o introduzca /exit para salir");
                        finCrear=true;
                    }
                    // srv.echo("Cliente no creado", c);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;

    }
}
