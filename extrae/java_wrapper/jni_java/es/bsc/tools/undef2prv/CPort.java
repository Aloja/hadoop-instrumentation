/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.bsc.tools.undef2prv;

/**
 *
 * @author smendoza
 */
public class CPort {

    String ip;
    String port;
    String ipDst;
    String portDst;
    int sizeAcumulada = 0;
    int paquetesTransmitidos = 0;
    boolean conflictivo = false;
    int conflictos = 0;
    public static String printheader = "ip:port(from/to ip:port)\tsizeAcumulada\tconflictivo\tconflictos";

    @Override
    public String toString() {
        return String.format("%s:%s(%s:%s)\t%s\t%s\t%s", this.ip, this.port, this.ipDst, this.portDst, this.sizeAcumulada, this.conflictivo, this.conflictos);
    }
}
