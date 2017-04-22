import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ClienteChat {
    // Constantes del programa
    public static final int MAX_INTENTOS = 3;

    // Colores para simplificar la lectura de mensajes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    // Programa principal "main"
    static public void main (String args[]) {
        boolean salirPrograma = false;
        Cliente c = null;
        if (args.length!=2) {
            System.err.println("Uso: ClienteChat hostregistro numPuertoRegistro");
            return;
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            ServicioChat srv = (ServicioChat) Naming.lookup("//" + args[0] + ":" + args[1] + "/Chat");
            Scanner input = new Scanner(System.in);
            // Realizar el login del usuario, mediante parametros por linea de comandos
            if ((c = login(srv, input)) != null) {
                printHelp();
                while (!salirPrograma && input.hasNextLine()) {
                    String line = input.nextLine();
                    String words[] = line.split(" ");
                    String command = words[0];
                    // Caso de linea vacia: Mostrar mensaje de error
                    if (line == null || line.equals("")) {
                        System.out.println("Debe introducir un comando válido para el funcionamiento del programa");
                    }
                    // Caso "/exit"; salir del programa
                    else if (line.equalsIgnoreCase("/exit")) {
                        // Salida del programa
                        salirPrograma = true;
                    }
                    // Caso "/j"; unión a un grupo
                    else if (line.substring(0, 2).equalsIgnoreCase("/j") && words.length == 2) {
                        srv.joinGroup(words[1], c);
                    }
                    // Caso "/l"; unión a un grupo
                    else if (line.substring(0, 2).equalsIgnoreCase("/l") && words.length == 2) {
                        srv.leaveGroup(words[1], c);
                    }
                    // Caso de mensaje público (@usuario) o privado (#grupo)
                    else if ((line.substring(0, 1).equals("@") || line.substring(0, 1).equals("#")) && words.length >= 2) {
                        String msg[] = Arrays.copyOfRange(words, 1, words.length);
                        // Mensaje hacia el usuario/grupo en cuestión
                        srv.sendMessage(words[0], String.join(" ", msg), c);
                    }
                    else {
                        printHelp();
                    }
                }
                srv.logout(c);
            }
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
            "    /j #GRUPO\n" +
            "    Union a un grupo de chat.\n\n" +
            "    /l #GRUPO\n" +
            "    Salida de un grupo de chat.\n\n" +
            "    #GRUPO MENSAJE\n" +
            "    Envio de un nuevo mensaje público al grupo #GRUPO. Por ejemplo:\n" +
            "    #general Hola a todos!\n\n" +
            "    @USUARIO MENSAJE\n" +
            "    Envio de un nuevo mensaje privado a USUARIO. Por ejemplo:\n" +
            "    @fulanito Hola!\n\n" +
            "    /exit\n" +
            "    Salir de la aplicación.\n"
        );
    }

    private static Cliente login(ServicioChat srv, Scanner input) {
        boolean finLogin = false;
        int intentos = 0;
        String username = null;
        String password = null;
        Cliente c = null;

        System.out.println(
            "\n*** Bienvenido a ClienteChat para el proyecto de SDySW ***\n" +
            "\nPor favor, indique su nombre de usuario o escriba /nuevo para crear uno" +
            "\nEn cualquier momento, escriba /exit para salir del programa\n"
        );

        // Obtener nombre de usuario
        System.out.print("Comando: ");
        while (!finLogin && input.hasNextLine()) {
            String line = input.nextLine();
            String words[] = line.split(" ");
            // Caso de linea vacia: Mostrar mensaje de error
            if (line == null || line.equals("")) {
                System.out.println("Debe introducir un comando válido para el funcionamiento del programa");
            }
            // Caso de mas de una palabra: Mostrar mensaje de error
            else if (words.length != 1) {
                System.out.println("\nEl nombre de usuario ha de estar formado por una sola palabra");
            }
            // Caso "/exit"; salir del programa
            else if (line.equalsIgnoreCase("/exit")){
                finLogin = true;
            }
            // Caso "/nuevo"; registro de usuario con el servidor
            else if (line.equalsIgnoreCase("/nuevo")){
                // Si se ha creado el usuario correctamente, finalizar el login
                if ((c = crearUsuario(srv, input)) != null) {
                    finLogin = true;
                }
            }
            // Validacion; los nombres no pueden empezar por "/" (reservado para comandos)
            else if (line.substring(0, 1).equals("/")) {
                System.out.println("\nEl nombre de usuario no puede empezar por /, es un carácter reservado para comandos");
            }
            // Todo bien; pedimos contraseña
            else {
                username = line;
                System.out.print("Contraseña: ");
                password = input.nextLine();
                try {
                    // Instanciar el cliente y realizar login
                    c = new ClienteImpl(username, password);
                    if (srv.login(c)) {
                        System.out.println("\nLas credenciales son correctas. Ha iniciado sesion correctamente.\n");
                        finLogin = true;
                    } else {
                        c = null;
                        // Hay un limite de intentos de login
                        if (intentos < MAX_INTENTOS) {
                            System.out.println(
                                "\nLas credenciales no son correctas. " +
                                "Lleva " + (++intentos) + " de " + MAX_INTENTOS + " intentos."
                            );
                        } else {
                            System.out.println("\n*** Demasiados intentos, el programa se cerrarrá ***");
                            // Se producira una salida del programa
                            break;
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    // Evitar problemas posteriores y salir del programa
                    System.exit(1);
                }
            }
            if (!finLogin) {
                System.out.print("Comando: ");
            }
        }

        // Si es nulo, no se ha realizado el inicio de sesion correctamente
        return c;
    }

    private static Cliente crearUsuario(ServicioChat srv, Scanner input) {
        boolean finCrear = false;
        String username = null;
        String password = null;
        Cliente c = null;

        System.out.println("\nSe está procediendo a la creación de un nuevo usuario");

        // Obtener nombre de usuario
        System.out.print("Nombre de usuario: ");
        while (!finCrear && input.hasNextLine()) {
            String line = input.nextLine();
            String words[] = line.split(" ");
            // Caso de linea vacia: Mostrar mensaje de error
            if (line == null || line.equals("")) {
                System.out.println("Debe introducir un nombre de usuario no vacío");
            }
            // Caso "/exit"; salir del programa
            else if (line.equalsIgnoreCase("/exit")) {
                finCrear = true;
            }
            // Caso de mas de una palabra: Mostrar mensaje de error
            else if (words.length != 1) {
                System.out.println("\nEl nombre de usuario ha de estar formado por una sola palabra");
            }
            // Validacion; los nombres no pueden empezar por "/" (reservado para comandos)
            else if (line.substring(0, 1).equals("/")) {
                System.out.println("\nEl nombre de usuario no puede empezar por /, es un carácter reservado para comandos");
            }
            // Todo bien; pedimos contraseña
            else {
                username = line;
                System.out.print("Contraseña: ");
                password = input.nextLine();
                try {
                    // Instanciar el cliente y realizar login
                    c = new ClienteImpl(username, password);
                    if (srv.addUsuario(c)) {
                        System.out.println("\n*** Usuario registrado correctamente ***\n");
                        finCrear = true;
                    } else {
                        c = null;
                        System.out.println("El usuario ya existe. Por favor, inténtelo de nuevo o introduzca /exit para salir.");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    // Evitar problemas posteriores y salir del programa
                    System.exit(1);
                }
            }
            if (!finCrear) {
                System.out.print("Nombre de usuario: ");
            }
        }

        // Si es nulo, no se ha creado el usuario correctamente
        return c;
    }
}
