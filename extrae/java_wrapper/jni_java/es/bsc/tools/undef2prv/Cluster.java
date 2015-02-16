package es.bsc.tools.undef2prv;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author smendoza
 */
class Cluster {

    public HashMap<String, Node> nodes = new HashMap<>();

    public static final Comparator<String> IP_COMPARATOR = new Comparator<String>() {
        public int compare(String d1, String d2){
            // Change ip endianness to allow integer comparison
            int ip1 = ByteBuffer.wrap(BigInteger.valueOf(Long.parseLong(d1)).toByteArray()).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
            int ip2 = ByteBuffer.wrap(BigInteger.valueOf(Long.parseLong(d2)).toByteArray()).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

            // Order by ip
            return Integer.valueOf(ip1).compareTo(Integer.valueOf(ip2));
        }
    };

    public void addDaemon(RecordNEvent ner) {

//        //Ordering the ip bytes
//        String ip = ner.getNodeIp();
//        Integer i = Integer.parseInt(ip);
//        ByteBuffer b = ByteBuffer.allocate(4);
//        b.order(ByteOrder.LITTLE_ENDIAN);
//        b.putInt(i);
//        byte[] result = b.array();
//
//        String strIp = null;
//        try {
//            InetAddress addr = InetAddress.getByAddress(result);
//            strIp = addr.getHostAddress();
//            byte[] raw = addr.getAddress();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        if (!nodes.containsKey(ner.getNodeIp())) {
//            nodes.put(ner.getNodeIp(), new Node(ner.getNodeIp()));
//        }

        String strIp = ner.getNodeIp();

        Node n = nodes.get(strIp);
        if (n == null) {
            //if the node does not exists, create one
            nodes.put(strIp, n = new Node(strIp));
            Undef2prv.logger.debug("HADOOP_CLUSTER-Adding node with ip " + strIp);
        } else {
            //if the node does exists, do nothing
        }

        //        Daemon d = new Daemon(ner.RecordEventsHM.get(RecordNEvent.KEY_NODE_DAEMON_PID), ner.getNodeType(), ner.getNodeIp());
        //        String pid = ner.RecordEventsHM.get(RecordNEvent.KEY_NODE_DAEMON_PID);
        Daemon d = new Daemon(ner);
        n.daemons.put(d.pid, d);
        Daemon g = n.daemons.get(d.pid);
        String msg = "DAEMON_ADDED_AT_HCLUSTER(IP=" + strIp + ", type=" + g.type + ", pid=" + g.pid + ", recordID=" + g.recordId + ")";
        Undef2prv.logger.debug(msg);
        System.out.println(msg);
        //TODO: verificar que ese daemon ya este en el nodo, antes de volverlo a anadir

    }

    public int getClusterSize() {
        return nodes.size();
    }

    public String getPidOfType(String rtype) {
        String retval = null;
        for (Node n : this.nodes.values()) {
            for (Daemon d : n.daemons.values()) {
                if (d.type.equals(rtype)) {
                    return d.pid;
                }
            }
        }
        return retval;
    }
    
    public ArrayList<String> getAllNodeIps() {
        ArrayList<String> ips = new ArrayList<>();

        for (Node n : this.nodes.values()) {
            ips.add(n.ip);
        }

        // Order IPs
        Collections.sort(ips, IP_COMPARATOR);

        return ips;
    }
    

    public ArrayList<String> getAllPids() {
        ArrayList<String> pids = new ArrayList<>();

        for (Daemon d : this.getAllDaemons()) {
            pids.add(d.pid);
        }

        return pids;
    }

    public Daemon getDaemonWithPid(String pidToFind) {
        ArrayList<String> pids = new ArrayList<>();

        for (Daemon d : this.getAllDaemons()) {
            if (d.pid.equals(pidToFind)) {
                Undef2prv.logger.debug("d.pid=" + d.pid + ", pidToFind=" + pidToFind);
                return d;
            }
        }

        return null;
    }

    //getAllDaemons() devuelve lista de daemons siguiendo este orden:
    // [1]->1)Nodo1
    // [2]->1.1)TaskTracker
    // [3]->1.2)DataNode
    // [4]->1.3)Task1
    // [5]->1.4)Task2
    // [6]->1.5)...
    // [7]->1.6)TaskN
    // [8]->2)Nodo2
    // [9]->2.1)TaskTracker
    // [10]->2.2)DataNode
    // [11]v2.3)Task1
    // [12]->2.4)Task2
    // [13]->2.5)...
    // [14]->2.6)TaskN
    //...
    public ArrayList<Daemon> getAllDaemons() {

        ArrayList<Daemon> ds = new ArrayList<>();

        for (Node n : this.nodes.values()) {
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_JOBTRACKER));
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_NAMENODE));
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_SECONDARY_NAMENODE));
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_TASKTRACKER));
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_DATANODE));
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_TASK));
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_JCLIENT));
        }

        // Order them (it's visually better)
        Collections.sort(ds, Daemon.VISUAL_COMPARATOR);

        return ds;
    }

    public ArrayList<Daemon> getAllDaemonsWithParaverType(String paraver_type) {
        // Get all daemons
        ArrayList<Daemon> all_ds = this.getAllDaemons();

        // Filter all the daemons and select only the ones that share the same "paraver type"
        ArrayList<Daemon> ds = new ArrayList<Daemon>();
        for (Daemon d : all_ds) {
            if (d.getParaverType().equals(paraver_type)) {
                ds.add(d);
            }
        }

        // Order the remaining daemons
        Collections.sort(ds, Daemon.VISUAL_COMPARATOR);

        return ds;
    }

    public ArrayList<Daemon> getAllTaskTrackers() {

        ArrayList<Daemon> ds = new ArrayList<>();

        for (Node n : this.nodes.values()) {
            ds.addAll(n.getDaemonsWithType(Daemon.NODE_ID_TASKTRACKER));
        }

        return ds;
    }

    public String getNTaskFromApp(String app) {

        String ntask = null;
        ArrayList<Daemon> ds = new ArrayList<>();

        for (Daemon d : this.getAllDaemons()) {
            if (app.equals(d.app)) {
                ntask = d.extraeNtask;
                Undef2prv.logger.debug("FOUND-BRO: " + app + "==" + d.recordId + ", d.extraeNtask=" + d.extraeNtask);
                break;
            } else {
                //System.out.println("NOT-FOUND: app!=d.recordId");
            }

        }

        return ntask;

    }

    public String getIpFromNTask(String ntask) {
        if (ntask == null) return null;
        for (Daemon d : this.getAllDaemons()) {
            if (ntask.equals(d.extraeNtask)) return d.ip;
        }
        return null;
    }

    public HashMap<String, ArrayList<String>> getPortsGroupedByPid() {
        HashMap<String, ArrayList<String>> hm = new HashMap<>();

        for (Daemon d : this.getAllDaemons()) {
            hm.put(d.pid, d.ports);
        }

        return hm;
    }

    public Set<String> getAllPidsByNode() {
        Set<String> retval = new HashSet();

        for (Node n : this.nodes.values()) {
            String strSize = Integer.toString(n.daemons.size());
            retval.add(strSize);
        }

        return retval;
    }

    public String getParaverCpu(Daemon daemon) {
        ArrayList<String> node_ips = this.getAllNodeIps();
        return Integer.toString(node_ips.indexOf(daemon.ip) + 1);
    }

    public String getParaverTask(Daemon daemon) {
        ArrayList<Daemon> ds = this.getAllDaemonsWithParaverType(daemon.getParaverType());
        return Integer.toString(ds.indexOf(daemon) + 1);
    }

}
