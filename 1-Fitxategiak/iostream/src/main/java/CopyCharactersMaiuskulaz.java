import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CopyCharactersMaiuskulaz {

    public static void main(String[] args) throws IOException {

        FileReader inputStream = null;
        FileWriter outputStream = null;

        try {
            inputStream = new FileReader("xanadu.txt");
            outputStream = new FileWriter("resultado.txt");

            int c;

            while ((c = inputStream.read()) != -1) {
                outputStream.write(Character.toUpperCase(c));
            }

            System.out.println("Fichero convertido a mayúsculas.");

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}