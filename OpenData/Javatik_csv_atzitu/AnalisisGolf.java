import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalisisGolf {

    // Dirección del archivo CSV
    private static final String URL_CSV =
            "https://raw.githubusercontent.com/opengolfapi/data/main/opengolfapi-us.csv";

    // Nombre del archivo guardado
    private static final String FICHERO_LOCAL = "golf.csv";

    /*
     * Descarga el archivo CSV desde Internet.
     */
    private static void descargarCsv(
            String direccion,
            String ficheroDestino) throws IOException {

        System.out.println("Descargando datos...");

        URL url = new URL(direccion);
        HttpURLConnection conexion =
                (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("GET");
        conexion.setConnectTimeout(15000);
        conexion.setReadTimeout(30000);

        conexion.setRequestProperty(
                "User-Agent",
                "Java OpenData Golf"
        );

        int codigoRespuesta = conexion.getResponseCode();

        if (codigoRespuesta != HttpURLConnection.HTTP_OK) {
            throw new IOException(
                    "Error HTTP: " + codigoRespuesta
            );
        }

        Path destino = Paths.get(ficheroDestino);

        try (InputStream entrada = conexion.getInputStream()) {

            Files.copy(
                    entrada,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {
            conexion.disconnect();
        }

        System.out.println("CSV descargado correctamente.");
        System.out.println(
                "Guardado en: " + destino.toAbsolutePath()
        );
    }

    /*
     * Separa correctamente una línea CSV.
     */
    private static String[] parseCsvLine(String linea) {

        List<String> campos = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();

        boolean dentroDeComillas = false;

        for (int i = 0; i < linea.length(); i++) {

            char caracter = linea.charAt(i);

            if (caracter == '"') {

                if (dentroDeComillas
                        && i + 1 < linea.length()
                        && linea.charAt(i + 1) == '"') {

                    campoActual.append('"');
                    i++;

                } else {
                    dentroDeComillas = !dentroDeComillas;
                }

            } else if (caracter == ',' && !dentroDeComillas) {

                campos.add(campoActual.toString());
                campoActual.setLength(0);

            } else {
                campoActual.append(caracter);
            }
        }

        campos.add(campoActual.toString());

        return campos.toArray(new String[0]);
    }

    /*
     * Busca la posición de una columna utilizando su nombre.
     */
    private static int buscarColumna(
            String[] cabecera,
            String nombreColumna) {

        for (int i = 0; i < cabecera.length; i++) {

            String nombre = cabecera[i]
                    .trim()
                    .replace("\"", "");

            if (nombre.equalsIgnoreCase(nombreColumna)) {
                return i;
            }
        }

        return -1;
    }

    /*
     * Convierte un texto a número entero.
     * Si no es válido, devuelve null.
     */
    private static Integer convertirEntero(String texto) {

        try {

            if (texto == null || texto.trim().isEmpty()) {
                return null;
            }

            return Integer.parseInt(texto.trim());

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /*
     * Lee y analiza el archivo CSV.
     */
    private static void analizarCsv(String fichero) {

        int totalCampos = 0;
        int sumaHoyos = 0;
        int camposConHoyos = 0;

        int anioMasAntiguo = Integer.MAX_VALUE;
        String campoMasAntiguo = "";
        String ciudadCampoMasAntiguo = "";
        String estadoCampoMasAntiguo = "";

        Map<String, Integer> camposPorEstado = new HashMap<>();

        try (
                FileInputStream fis =
                        new FileInputStream(fichero);

                InputStreamReader isr =
                        new InputStreamReader(
                                fis,
                                StandardCharsets.UTF_8
                        );

                BufferedReader br =
                        new BufferedReader(isr)
        ) {

            String primeraLinea = br.readLine();

            if (primeraLinea == null) {
                System.out.println("El archivo está vacío.");
                return;
            }

            String[] cabecera = parseCsvLine(primeraLinea);

            int columnaNombre =
                    buscarColumna(cabecera, "name");

            int columnaEstado =
                    buscarColumna(cabecera, "state");

            int columnaCiudad =
                    buscarColumna(cabecera, "city");

            int columnaHoyos =
                    buscarColumna(cabecera, "holes");

            int columnaAnio =
                    buscarColumna(cabecera, "year_built");

            /*
             * Comprobamos que las columnas principales existen.
             */
            if (columnaNombre == -1 || columnaEstado == -1) {

                System.out.println(
                        "No se encuentran las columnas necesarias."
                );

                System.out.println("Columnas encontradas:");

                for (String columna : cabecera) {
                    System.out.println("- " + columna);
                }

                return;
            }

            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] datos = parseCsvLine(linea);

                if (columnaNombre >= datos.length
                        || columnaEstado >= datos.length) {
                    continue;
                }

                totalCampos++;

                String nombre = datos[columnaNombre].trim();
                String estado = datos[columnaEstado].trim();

                String ciudad = "";

                if (columnaCiudad >= 0
                        && columnaCiudad < datos.length) {

                    ciudad = datos[columnaCiudad].trim();
                }

                if (estado.isEmpty()) {
                    estado = "Estado desconocido";
                }

                /*
                 * Se cuenta un campo más en ese estado.
                 */
                camposPorEstado.put(
                        estado,
                        camposPorEstado.getOrDefault(estado, 0) + 1
                );

                /*
                 * Se procesa el número de hoyos.
                 */
                if (columnaHoyos >= 0
                        && columnaHoyos < datos.length) {

                    Integer hoyos =
                            convertirEntero(datos[columnaHoyos]);

                    if (hoyos != null && hoyos > 0) {
                        sumaHoyos += hoyos;
                        camposConHoyos++;
                    }
                }

                /*
                 * Se busca el campo más antiguo.
                 */
                if (columnaAnio >= 0
                        && columnaAnio < datos.length) {

                    Integer anio =
                            convertirEntero(datos[columnaAnio]);

                    if (anio != null
                            && anio > 0
                            && anio < anioMasAntiguo) {

                        anioMasAntiguo = anio;
                        campoMasAntiguo = nombre;
                        ciudadCampoMasAntiguo = ciudad;
                        estadoCampoMasAntiguo = estado;
                    }
                }
            }

            mostrarResultados(
                    totalCampos,
                    sumaHoyos,
                    camposConHoyos,
                    campoMasAntiguo,
                    ciudadCampoMasAntiguo,
                    estadoCampoMasAntiguo,
                    anioMasAntiguo,
                    camposPorEstado
            );

        } catch (IOException e) {

            System.out.println(
                    "Error al leer el archivo: " + e.getMessage()
            );
        }
    }

    /*
     * Muestra las estadísticas calculadas.
     */
    private static void mostrarResultados(
            int totalCampos,
            int sumaHoyos,
            int camposConHoyos,
            String campoMasAntiguo,
            String ciudadCampoMasAntiguo,
            String estadoCampoMasAntiguo,
            int anioMasAntiguo,
            Map<String, Integer> camposPorEstado) {

        System.out.println();
        System.out.println("===== RESULTADOS =====");

        System.out.println(
                "Número total de campos: " + totalCampos
        );

        if (camposConHoyos > 0) {

            double mediaHoyos =
                    (double) sumaHoyos / camposConHoyos;

            System.out.printf(
                    "Media de hoyos: %.2f%n",
                    mediaHoyos
            );

        } else {

            System.out.println(
                    "No existen datos válidos sobre los hoyos."
            );
        }

        if (anioMasAntiguo != Integer.MAX_VALUE) {

            System.out.println();
            System.out.println("Campo más antiguo:");

            System.out.println(
                    "Nombre: " + campoMasAntiguo
            );

            System.out.println(
                    "Ciudad: " + ciudadCampoMasAntiguo
            );

            System.out.println(
                    "Estado: " + estadoCampoMasAntiguo
            );

            System.out.println(
                    "Año: " + anioMasAntiguo
            );

        } else {

            System.out.println(
                    "No existen datos válidos sobre el año."
            );
        }

        System.out.println();
        System.out.println("Campos por estado:");

        camposPorEstado.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Integer>comparingByValue()
                                .reversed()
                )
                .forEach(entrada ->
                        System.out.printf(
                                "%-25s %d%n",
                                entrada.getKey(),
                                entrada.getValue()
                        )
                );
    }

    /*
     * Método principal.
     */
    public static void main(String[] args) {

        try {

            descargarCsv(URL_CSV, FICHERO_LOCAL);
            analizarCsv(FICHERO_LOCAL);

        } catch (IOException e) {

            System.out.println(
                    "No se ha podido descargar el CSV."
            );

            System.out.println(
                    "Motivo: " + e.getMessage()
            );

            Path ficheroExistente =
                    Paths.get(FICHERO_LOCAL);

            /*
             * Si falla la descarga, utiliza una copia local.
             */
            if (Files.exists(ficheroExistente)) {

                System.out.println(
                        "Se utilizará el archivo golf.csv existente."
                );

                analizarCsv(FICHERO_LOCAL);

            } else {

                System.out.println(
                        "No existe una copia local del archivo."
                );
            }
        }
    }
}