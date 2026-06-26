package ejercisioapi;

import java.util.Objects;

public class ModeloInfo {

    private int id;
    private int userId;
    private String title;
    private String body;

    public ModeloInfo() {
        this(0, 1, "Sin titulo", "Contenido minimo de ejemplo.");
    }

    public ModeloInfo(int id, int userId, String title, String body) {
        setId(id);
        setUserId(userId);
        setTitle(title);
        setBody(body);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id no puede ser negativo.");
        }
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("El userId debe ser mayor que cero.");
        }
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = limpiarTextoObligatorio(title, "title", 50);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        String limpio = limpiarTextoObligatorio(body, "body", Integer.MAX_VALUE);
        if (limpio.length() < 15) {
            throw new IllegalArgumentException("El body debe tener al menos 15 caracteres.");
        }
        this.body = limpio;
    }

    private String limpiarTextoObligatorio(String texto, String campo, int maximo) {
        if (texto == null) {
            throw new IllegalArgumentException("El campo " + campo + " no puede ser nulo.");
        }

        String limpio = texto.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacio.");
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar " + maximo + " caracteres.");
        }
        return limpio;
    }

    public boolean coincideCon(ModeloInfo otro) {
        if (otro == null) {
            return false;
        }
        return id == otro.id
                && userId == otro.userId
                && Objects.equals(title, otro.title)
                && Objects.equals(body, otro.body);
    }

    @Override
    public String toString() {
        return "ModeloInfo{"
                + "id=" + id
                + ", userId=" + userId
                + ", title='" + title + '\''
                + ", body='" + body + '\''
                + '}';
    }
}
