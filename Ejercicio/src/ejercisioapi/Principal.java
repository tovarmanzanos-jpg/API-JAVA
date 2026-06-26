package ejercisioapi;

import java.util.Scanner;

public class Principal {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ApiModelo api = new ApiModelo("https://jsonplaceholder.typicode.com/posts");

        System.out.println("=== Sincronizador de Publicaciones ===");
        System.out.print("Id de la publicacion: ");
        int id = leerEntero(scanner);

        try {
            System.out.print("UserId: ");
            int userId = leerEntero(scanner);

            System.out.print("Titulo: ");
            String title = scanner.nextLine();

            System.out.print("Contenido: ");
            String body = scanner.nextLine();

            ModeloInfo infoLocal = new ModeloInfo(id, userId, title, body);
            api.gestionar_sincronizacion(infoLocal);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error de validacion: " + ex.getMessage());
        }
    }

    private static int leerEntero(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Ingresa un numero valido: ");
            scanner.next();
        }
        int valor = scanner.nextInt();
        scanner.nextLine();
        return valor;
    }
}
