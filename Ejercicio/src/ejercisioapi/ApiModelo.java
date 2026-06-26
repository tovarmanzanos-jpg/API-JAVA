package ejercisioapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiModelo {

    private final String apiUrl;
    public HttpResponse<String> respuesta_api;

    public ApiModelo(String dato_url) {
        this.apiUrl = dato_url == null || dato_url.trim().isEmpty()
                ? "https://jsonplaceholder.typicode.com/posts"
                : dato_url.trim();
        this.respuesta_api = null;
    }

    public HttpResponse<String> hacer_peticion_get(int idPublicacion) {
        validarId(idPublicacion);
        try {
            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(construirUrlGet(idPublicacion)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            this.respuesta_api = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            return this.respuesta_api;
        } catch (IOException ex) {
            throw new RuntimeException("No fue posible hacer la peticion GET: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("La peticion GET fue interrumpida: " + ex.getMessage(), ex);
        }
    }

    public ModeloInfo convertir_info_respuesta(String jsonCrudo) {
        if (jsonCrudo == null || jsonCrudo.trim().isEmpty()) {
            return null;
        }

        String contenido = jsonCrudo.trim();
        if (contenido.startsWith("[")) {
            int inicio = contenido.indexOf('{');
            int fin = contenido.lastIndexOf('}');
            if (inicio < 0 || fin < 0 || fin <= inicio) {
                return null;
            }
            contenido = contenido.substring(inicio, fin + 1);
        }

        Integer id = extraerEntero(contenido, "\"id\"\\s*:\\s*(\\d+)");
        Integer userId = extraerEntero(contenido, "\"userId\"\\s*:\\s*(\\d+)");
        String title = extraerTexto(contenido, "\"title\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        String body = extraerTexto(contenido, "\"body\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

        if (id == null || userId == null || title == null || body == null) {
            return null;
        }

        return new ModeloInfo(id, userId, title, body);
    }

    public HttpResponse<String> hacer_peticion_post(ModeloInfo infoNueva) {
        if (infoNueva == null) {
            throw new IllegalArgumentException("La informacion nueva no puede ser nula.");
        }

        String json = construirJson(infoNueva);
        try {
            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(this.apiUrl))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            this.respuesta_api = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            return this.respuesta_api;
        } catch (IOException ex) {
            throw new RuntimeException("No fue posible hacer la peticion POST: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("La peticion POST fue interrumpida: " + ex.getMessage(), ex);
        }
    }

    public void imprimir_info(ModeloInfo info) {
        if (info == null) {
            System.out.println("No hay informacion para imprimir.");
            return;
        }

        System.out.println("================================");
        System.out.println("Id: " + info.getId());
        System.out.println("UserId: " + info.getUserId());
        System.out.println("Titulo: " + info.getTitle());
        System.out.println("Body: " + info.getBody());
        System.out.println("================================");
    }

    public void gestionar_sincronizacion(ModeloInfo infoLocal) {
        if (infoLocal == null) {
            throw new IllegalArgumentException("La informacion local no puede ser nula.");
        }

        ModeloInfo infoServidor = null;
        try {
            HttpResponse<String> respuestaGet = hacer_peticion_get(infoLocal.getId());
            if (respuestaGet.statusCode() >= 200 && respuestaGet.statusCode() < 300) {
                infoServidor = convertir_info_respuesta(respuestaGet.body());
            }
        } catch (RuntimeException ex) {
            System.out.println("No se pudo consultar el servidor, se intentara guardar directamente.");
        }

        if (infoServidor != null && infoServidor.coincideCon(infoLocal)) {
            System.out.println("La publicacion ya existe en el servidor. Se descarta la copia duplicada.");
            imprimir_info(infoServidor);
            return;
        }

        HttpResponse<String> respuestaPost = hacer_peticion_post(infoLocal);
        System.out.println("Publicacion enviada con exito. Estado: " + respuestaPost.statusCode());
        ModeloInfo respuestaCreada = convertir_info_respuesta(respuestaPost.body());
        if (respuestaCreada != null) {
            imprimir_info(respuestaCreada);
        } else {
            imprimir_info(infoLocal);
        }
    }

    private String construirUrlGet(int idPublicacion) {
        if (this.apiUrl.endsWith("/")) {
            return this.apiUrl + idPublicacion;
        }
        return this.apiUrl + "/" + idPublicacion;
    }

    private void validarId(int idPublicacion) {
        if (idPublicacion <= 0) {
            throw new IllegalArgumentException("El id de publicacion debe ser mayor que cero.");
        }
    }

    private Integer extraerEntero(String texto, String patron) {
        Matcher matcher = Pattern.compile(patron).matcher(texto);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String extraerTexto(String texto, String patron) {
        Matcher matcher = Pattern.compile(patron, Pattern.DOTALL).matcher(texto);
        if (!matcher.find()) {
            return null;
        }
        return desescaparJson(matcher.group(1));
    }

    private String construirJson(ModeloInfo info) {
        return "{"
                + "\"id\":" + info.getId() + ","
                + "\"userId\":" + info.getUserId() + ","
                + "\"title\":\"" + escaparJson(info.getTitle()) + "\","
                + "\"body\":\"" + escaparJson(info.getBody()) + "\""
                + "}";
    }

    private String escaparJson(String valor) {
        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String desescaparJson(String valor) {
        StringBuilder resultado = new StringBuilder();
        boolean escapado = false;
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (escapado) {
                switch (c) {
                    case '"':
                        resultado.append('"');
                        break;
                    case '\\':
                        resultado.append('\\');
                        break;
                    case '/':
                        resultado.append('/');
                        break;
                    case 'b':
                        resultado.append('\b');
                        break;
                    case 'f':
                        resultado.append('\f');
                        break;
                    case 'n':
                        resultado.append('\n');
                        break;
                    case 'r':
                        resultado.append('\r');
                        break;
                    case 't':
                        resultado.append('\t');
                        break;
                    default:
                        resultado.append(c);
                        break;
                }
                escapado = false;
            } else if (c == '\\') {
                escapado = true;
            } else {
                resultado.append(c);
            }
        }
        return resultado.toString();
    }
}
