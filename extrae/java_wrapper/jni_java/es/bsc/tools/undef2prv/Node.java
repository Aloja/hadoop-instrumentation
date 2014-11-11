package es.bsc.tools.undef2prv;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author smendoza
 */
class Node {

    public String ip;
    public HashMap<String, Daemon> daemons; //pid->Daemon jobtracker, namenode...

    public Node() {
        this.ip = null;
        this.daemons = new HashMap<>();
    }

    public Node(String nip) {
        this.ip = nip;
        this.daemons = new HashMap<>();
    }

    public ArrayList<String> getAllDaemonPids() {
        ArrayList<String> pids = new ArrayList<>();
        for (Daemon d : this.daemons.values()) {
            pids.add(d.pid);
        }

        return pids;
    }

    public ArrayList<Daemon> getDaemonsWithType(String type) {
        ArrayList<Daemon> arrL = new ArrayList<>();
        for (Daemon d : this.daemons.values()) {
            if (d.type.equals(type)) {
                arrL.add(d);
            }
        }
        return arrL;
    }

    public int getSizeDaemons() {

        return daemons.size();
    }
}
