import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * OPEN DATA PROIEKTUA
 *
 * Iturria:
 * OpenGolfAPI
 *
 * Azalpena:
 * Programak Internetetik golf zelaien datuak dituen CSV fitxategia
 * irakurtzen du. Ondoren, erabiltzaileak menu baten bidez datuak
 * kontsultatu ditzake.
 */
public class GolfAnalisis {

    // Golf zelai baten informazioa gordetzeko klasea
    static class GolfZelaia {

        String izena;
        String herrialdea;
        String estatua;
        String hiria;
        int zuloak;
        String par;

        public GolfZelaia(
                String izena,
                String herrialdea,
                String estatua,
                String hiria,
                int zuloak,
                String par) {

            this.izena = izena;
            this.herrialdea = herrialdea;
            this.estatua = estatua;
            this.hiria = hiria;
            this.zuloak = zuloak;
            this.par = par;
        }
    }

    public static void main(String[] args) {

        Scanner teklatua = new Scanner(System.in);

        // CSV fitxategiko golf zelaiak gordetzeko zerrenda
        ArrayList<GolfZelaia> golfZelaiak = datuakIrakurri();

        if (golfZelaiak.isEmpty()) {
            System.out.println("Ezin izan dira golf zelaien datuak irakurri.");
            teklatua.close();
            return;
        }

        int aukera;

        do {
            menuaErakutsi();

            System.out.print("Aukeratu aukera bat: ");

            while (!teklatua.hasNextInt()) {
                System.out.println("Errorea: zenbaki bat idatzi behar duzu.");
                teklatua.nextLine();
                System.out.print("Aukeratu aukera bat: ");
            }

            aukera = teklatua.nextInt();
            teklatua.nextLine();

            switch (aukera) {

                case 1:
                    golfZelaiGuztiakErakutsi(golfZelaiak);
                    break;

                case 2:
                    estatuarenAraberaBilatu(golfZelaiak, teklatua);
                    break;

                case 3:
                    zuloGehienDutenakErakutsi(golfZelaiak);
                    break;

                case 4:
                    System.out.println("Programa amaitu da.");
                    break;

                default:
                    System.out.println("Aukera ez da zuzena.");
                    break;
            }

        } while (aukera != 4);

        teklatua.close();
    }

    /*
     * CSV fitxategia Internetetik irakurtzen du eta golf zelaiak
     * ArrayList batean gordetzen ditu.
     */
    public static ArrayList<GolfZelaia> datuakIrakurri() {

        ArrayList<GolfZelaia> golfZelaiak = new ArrayList<>();

        String csvHelbidea =
                "https://raw.githubusercontent.com/opengolfapi/data/main/opengolfapi-us.csv";

        try {

            // URI erabiltzen da URL(String) deprecated eraikitzailea saihesteko
            URL url = URI.create(csvHelbidea).toURL();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openStream())
            );

            String lerroa;

            // CSV fitxategiaren goiburua saltatu
            br.readLine();

            while ((lerroa = br.readLine()) != null) {

                /*
                 * -1 erabilita, lerroaren amaieran hutsik dauden
                 * zutabeak ere mantentzen dira.
                 */
                String[] datuak = lerroa.split(",", -1);

                // Behar diren zutabeak existitzen direla egiaztatu
                if (datuak.length > 9) {

                    String izena = testuaGarbitu(datuak[1]);
                    String herrialdea = testuaGarbitu(datuak[4]);
                    String estatua = testuaGarbitu(datuak[5]);
                    String hiria = testuaGarbitu(datuak[6]);
                    int zuloak = zenbakiaBihurtu(datuak[8]);
                    String par = testuaGarbitu(datuak[9]);

                    GolfZelaia golfZelaia = new GolfZelaia(
                            izena,
                            herrialdea,
                            estatua,
                            hiria,
                            zuloak,
                            par
                    );

                    golfZelaiak.add(golfZelaia);
                }
            }

            br.close();

            System.out.println(
                    golfZelaiak.size()
                    + " golf zelairen datuak irakurri dira."
            );

        } catch (Exception e) {

            System.out.println("Errorea CSV fitxategia irakurtzean.");
            System.out.println("Errorearen mezua: " + e.getMessage());
        }

        return golfZelaiak;
    }

    /*
     * Programaren menua pantailan erakusten du.
     */
    public static void menuaErakutsi() {

        System.out.println();
        System.out.println("========================================");
        System.out.println("          GOLF ZELAIEN MENUA");
        System.out.println("========================================");
        System.out.println("1. Golf zelai guztiak ikusi");
        System.out.println("2. Estatuaren arabera bilatu");
        System.out.println("3. Zulo gehien dituzten zelaiak ikusi");
        System.out.println("4. Irten");
        System.out.println("========================================");
    }

    /*
     * Zerrendan dauden golf zelai guztiak erakusten ditu.
     */
    public static void golfZelaiGuztiakErakutsi(
            ArrayList<GolfZelaia> golfZelaiak) {

        taularenGoiburuaErakutsi();

        int kontagailua = 0;

        for (GolfZelaia golfZelaia : golfZelaiak) {

            golfZelaiaErakutsi(golfZelaia);
            kontagailua++;
        }

        taularenAmaieraErakutsi();

        System.out.println(
                "Erakutsitako golf zelaien kopurua: "
                + kontagailua
        );
    }

    /*
     * Erabiltzaileari estatu baten izena edo kodea eskatzen dio.
     * Ondoren, estatu horretako golf zelaiak erakusten ditu.
     */
    public static void estatuarenAraberaBilatu(
            ArrayList<GolfZelaia> golfZelaiak,
            Scanner teklatua) {

        System.out.print(
                "Idatzi estatuaren izena edo kodea, adibidez CA: "
        );

        String bilatutakoEstatua = teklatua.nextLine().trim();

        taularenGoiburuaErakutsi();

        int kontagailua = 0;

        for (GolfZelaia golfZelaia : golfZelaiak) {

            if (golfZelaia.estatua.equalsIgnoreCase(
                    bilatutakoEstatua)) {

                golfZelaiaErakutsi(golfZelaia);
                kontagailua++;
            }
        }

        taularenAmaieraErakutsi();

        if (kontagailua == 0) {

            System.out.println(
                    "Ez da golf zelairik aurkitu "
                    + bilatutakoEstatua
                    + " estatuan."
            );

        } else {

            System.out.println(
                    "Aurkitutako golf zelaien kopurua: "
                    + kontagailua
            );
        }
    }

    /*
     * Lehenengo zulo kopururik handiena bilatzen du.
     * Ondoren, zulo kopuru hori duten zelai guztiak erakusten ditu.
     */
    public static void zuloGehienDutenakErakutsi(
            ArrayList<GolfZelaia> golfZelaiak) {

        int zuloGehien = 0;

        // Zulo kopururik handiena bilatu
        for (GolfZelaia golfZelaia : golfZelaiak) {

            if (golfZelaia.zuloak > zuloGehien) {
                zuloGehien = golfZelaia.zuloak;
            }
        }

        taularenGoiburuaErakutsi();

        int kontagailua = 0;

        // Zulo kopururik handiena duten zelaiak erakutsi
        for (GolfZelaia golfZelaia : golfZelaiak) {

            if (golfZelaia.zuloak == zuloGehien) {
                golfZelaiaErakutsi(golfZelaia);
                kontagailua++;
            }
        }

        taularenAmaieraErakutsi();

        System.out.println("Zulo kopururik handiena: " + zuloGehien);

        System.out.println(
                "Zulo kopuru hori duten golf zelaiak: "
                + kontagailua
        );
    }

    /*
     * Taularen goiburua erakusten du.
     */
    public static void taularenGoiburuaErakutsi() {

        System.out.println(
                "=============================================================================================================="
        );

        System.out.printf(
                "%-35s %-18s %-12s %-15s %-8s %-5s%n",
                "GOLF ZELAIA",
                "HIRIA",
                "ESTATUA",
                "HERRIALDEA",
                "ZULOAK",
                "PAR"
        );

        System.out.println(
                "=============================================================================================================="
        );
    }

    /*
     * Golf zelai baten informazioa taularen lerro batean erakusten du.
     */
    public static void golfZelaiaErakutsi(GolfZelaia golfZelaia) {

        String izena = golfZelaia.izena;
        String hiria = golfZelaia.hiria;

        // Testu luzeek taula ez apurtzeko
        if (izena.length() > 33) {
            izena = izena.substring(0, 33);
        }

        if (hiria.length() > 16) {
            hiria = hiria.substring(0, 16);
        }

        System.out.printf(
                "%-35s %-18s %-12s %-15s %-8d %-5s%n",
                izena,
                hiria,
                golfZelaia.estatua,
                golfZelaia.herrialdea,
                golfZelaia.zuloak,
                golfZelaia.par
        );
    }

    /*
     * Taularen amaierako lerroa erakusten du.
     */
    public static void taularenAmaieraErakutsi() {

        System.out.println(
                "=============================================================================================================="
        );
    }

    /*
     * CSV fitxategitik jasotako komatxoak kentzen ditu.
     */
    public static String testuaGarbitu(String testua) {

        return testua
                .replace("\"", "")
                .trim();
    }

    /*
     * Testua zenbaki oso bihurtzen saiatzen da.
     * Ezin bada bihurtu, zero itzultzen du.
     */
    public static int zenbakiaBihurtu(String testua) {

        try {

            String testuGarbia = testuaGarbitu(testua);

            if (testuGarbia.isEmpty()) {
                return 0;
            }

            return Integer.parseInt(testuGarbia);

        } catch (NumberFormatException e) {

            return 0;
        }
    }
}