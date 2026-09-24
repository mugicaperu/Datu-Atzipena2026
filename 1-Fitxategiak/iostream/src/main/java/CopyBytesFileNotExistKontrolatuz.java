import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyBytesFileNotExistKontrolatuz {
    public static void main(String[] args) throws IOException {

        File F = new File("xanadu.txt");
        FileInputStream in = null;
        FileOutputStream out = null;

        if (!F.exists()) {
            System.out.println("No se ha encontrado " + F.getName() + ".");
            return;
        }

        try {
            in = new FileInputStream(F);
            out = new FileOutputStream("outagain.txt");
            int c;

            while ((c = in.read()) != -1) {
                out.write(c);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No se ha encontrado xanadu.txt.");
        } catch (IOException e) {
            System.out.println("Error al copiar el archivo: " + e.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}