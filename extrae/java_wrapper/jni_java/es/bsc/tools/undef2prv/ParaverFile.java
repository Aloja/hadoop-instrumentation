package es.bsc.tools. undef2prv;

/**
 *
 * @author smendoza
 */
public class ParaverFile {

    public void generateFile(String file) {
        ParaverHeader ph = new ParaverHeader();
        String h = ph.ParaverHeaderGenerator();
        String b = generateBody();
    }

    public String generateBody() {

        return null;
    }
}
