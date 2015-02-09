package es.bsc.tools.undef2prv;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author smendoza
 */
public class DataOnMemory {

    public static FileNetDumping fnd = new FileNetDumping();
    public static FileParaver fprv = new FileParaver();
    public static Cluster hcluster = new Cluster();
    public static HashMap<String, String> pid_ntask = new HashMap<>();
    public static HashMap<String, String> ntask_pid = new HashMap<>();
    public static HashMap<String, String> pidThread_type = new HashMap<>();
    public static HashMap<String, String> NERProcess_NTask = new HashMap<>();
//    public static Set<CPort> portDebugging = new HashSet<>();
    public static HashMap<String, CPort> portDebugging = new HashMap<>();
//    public static HashMap<String, CPort> puertosConflictivosCounter = new HashMap<>();
//    public static HashMap<String, CPort> puertosConflictivosSize = new HashMap<>();
    public static String FILE_JC = "/tmp/smendoza/jc.pid";
    public static LinkedHashMap<String, ArrayList<Sysstat>> sysstats = new LinkedHashMap<>();

    public DataOnMemory() {
    }

    public static void loadOnMemoryDumpfile(String filePath) throws FileNotFoundException {

        fnd.loadOnMemoryDumpfile(filePath);
    }

    public static String getPid(String ip, String port) {

        String strIp = null;

        //Ordering the ip bytes
        Integer i = Integer.parseInt(ip);
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(i);
        byte[] result = b.array();

        try {
            InetAddress addr = InetAddress.getByAddress(result);
            strIp = addr.getHostAddress();
            byte[] raw = addr.getAddress();
        } catch (Exception e) {
            // Exception getting the Address
            Undef2prv.logger.error("Exception getting the pid for ip:port=" + ip + ":" + port);
            e.printStackTrace();
        }


//        strIp = "172.20.0.16"; TRUCO QUE ME ACABO DE INVENTAR PARA SALTARME LOOPBACKS, hay que corregir
        strIp = CommonFuncs.ipToStrIp(strIp); // parse to decimal ip

        //consultar la tabla mapped el pid de esa ip+puerto
        String key = FileNetDumping.HMKeyGen(strIp, port);
        String pid = fnd.ipport_pid.get(key);
        String NTask = null;

        //NTask = DataOnMemory.pid_ntask.get(pid);
        NTask = DataOnMemory.getNTaskByPid(pid);

        //Si el pid no me da ntask valida, miro a ver si los otros pids asociados a ese mismo puerto tienen ntask valida
        if (NTask == null) { // Busco en ipports

            Set<String> pids = fnd.ipport_pids.get(key);

            if (pids != null) {
                for (String p : pids) {
                    //NTask = DataOnMemory.pid_ntask.get(p);
                    NTask = DataOnMemory.getNTaskByPid(p);
                    if (NTask != null) {
                        Undef2prv.logger.debug("ALTERNATIVE OK_ipport_pids[" + strIp + ":" + port + "]->NTask[" + NTask + "]");
                        pid = p;
                    }
                }
                Undef2prv.logger.debug("ipports_pids[" + strIp + ":" + port + "] WITHOUT dando PID sin ntask asociada!");
            } else {
                Undef2prv.logger.debug("ipports_pids[" + strIp + ":" + port + "] IS NULL!");
            }
        } else {
            Undef2prv.logger.debug("OK_ipport_pids[" + strIp + ":" + port + "]->NTask[" + NTask + "]");
        }

        return pid;
    }

    public static Object getPort(String pid) {

        //consultar la tabla mapped el pid de esa ip+puerto
        return fnd.pid_port.get(pid);
    }

    public static Object getIp(String pid) {

        //consultar la tabla mapped el pid de esa ip+puerto
        return fnd.pid_ip.get(pid);
    }

    /**
     * Get the Ntask (if exists) from the sources ip:port of the RecordNEvent
     * specified
     *
     * @param ner
     * @return
     */
    public static String getNTaskSender(RecordNEvent ner) {
        String ip = ner.getSrcIp();
        String port = ner.getSrcPort();

        String dstPid = DataOnMemory.getPid(ip, port);
//      String NTask = DataOnMemory.pid_ntask.get(dstPid);
        String NTask = DataOnMemory.getNTaskByPid(dstPid);
        Undef2prv.logger.debug("ip:port[" + ip + ":" + port + "]->NTask[" + NTask + "]");

        return NTask;
    }

    /**
     * Get the Ntask (if exists) from the destination ip:port of the
     * RecordNEvent specified
     *
     * @param ner
     * @return
     */
    public static String getNTaskReceiver(RecordNEvent ner) {
        String ip = ner.getDstIp();
        String port = ner.getDstPort();

        String dstPid = DataOnMemory.getPid(ip, port);
//        String NTask = DataOnMemory.pid_ntask.get(dstPid);
        String NTask = DataOnMemory.getNTaskByPid(dstPid);
//
//        if (NTask == null) {
//            portDebugging.add(ip + ":" + port); // ip:puerto sin ntask asociado
//            Integer counter = puertosConflictivosCounter.get(port);
//            if (counter == null) {
//                counter = new Integer(1);
//                puertosConflictivosCounter.put(port, counter.intValue() + 1);
//            } else {
//                puertosConflictivosCounter.put(port, counter + 1);
//            }
//            Undef2prv.logger.debug("Puerto Conflictivo: ip:port");
//            //ESTO ES INCORRECTO!
////            NTask = DataOnMemory.NERProcess_NTask.get(ner.Process);
////            if (NTask == null) {
////                Undef2prv.logger.error("ip:port[" + ip + ":" + port + "]->NTask[" + NTask + "]");
////            }
//
//        } else {
//        }

        Undef2prv.logger.debug("ip:port[" + ip + ":" + port + "]->NTask[" + NTask + "]");

        return NTask;
    }

    public static String getNThreadByPid(String[] key) {
        //TODO:
        //1) Encontrar el daemon al que corresponde este thread (mirar su proceso
        String tPid = key[0];
        String threadId = key[1];
        Daemon d = DataOnMemory.hcluster.getDaemonWithPid(tPid);
        return d.jthread_nthread.get(threadId);

    }

    public static String getNTaskByPid(String keyPid) {
        return DataOnMemory.getNTaskByPid(new String[]{keyPid});
    }

    public static String getNTaskByPid(String[] keyPid) {
        String hmkey = keyPid[0];
        for (int i = 1; i < keyPid.length; ++i) {
            hmkey += ":" + keyPid[i];
        }
        //Consulta del HashMap
        return DataOnMemory.pid_ntask.get(hmkey); //usando solo el pid:thread
        //return DataOnMemory.pid_ntask.get(keyPid[0]); //usando solo el pid
    }

    public static String getNTaskByIpPort(String ip, String port) {
        if (ip == null || port == null) {
            return null;
        } else {
            String dstPid = DataOnMemory.getPid(ip, port);
//            String NTask = DataOnMemory.pid_ntask.get(dstPid);
            String NTask = DataOnMemory.getNTaskByPid(dstPid);

            if (NTask == null) {
//            Undef2prv.logger.error("ip:port[" + ip + ":" + port + "]->NTask[" + NTask + "]");
//            portDebugging.add(port);
                //En este caso tendria que buscar por el process
            } else {
                Undef2prv.logger.error("getNTaskByIpPort ip:port[" + ip + ":" + port + "]->pid[" + dstPid + "]->NTask[" + NTask + "]");
            }

            return NTask;
        }
    }

    //Debe mejorarse, identificar un proceso en un cluster requiere tb el nodo en el que se esta ejecutando
    public static void genHTPidNtask() {
//        Set<String> pids = new HashSet<>();
//
//        ArrayList<String> daemonPids = (DataOnMemory.hcluster.getAllPids()); //get all pids from daemons (obtained from prv 88880, 88881)
//        pids.addAll(daemonPids);
//
//        String pidJT = DataOnMemory.hcluster.getPidOfType(Daemon.NODE_ID_JOBTRACKER);
//        String pidNN = DataOnMemory.hcluster.getPidOfType(Daemon.NODE_ID_NAMENODE);
//        String pidSNN = DataOnMemory.hcluster.getPidOfType(Daemon.NODE_ID_SECONDARY_NAMENODE);
//
//        int ntask = 4; //1,2,3 occupied by JT, NN, SNN 
//        for (String pid : pids) {
//            if (CommonFuncs.isNumeric(pid) && !pid.equals(pidJT) && !pid.equals(pidNN) && !pid.equals(pidSNN)) {
//                DataOnMemory.pid_ntask.put(pid, Integer.toString(ntask));
//                ntask++;
//            } else {
//            }
//        }
//
//        DataOnMemory.pid_ntask.put(pidJT, Daemon.NODE_ID_JOBTRACKER);
//        DataOnMemory.pid_ntask.put(pidNN, Daemon.NODE_ID_NAMENODE);
//        DataOnMemory.pid_ntask.put(pidSNN, Daemon.NODE_ID_SECONDARY_NAMENODE);

        ArrayList<Daemon> pidsDaemons = DataOnMemory.hcluster.getAllDaemons(); //daemons from .prv file line (88880...)
        Undef2prv.logger.debug("DaemonsSize=" + pidsDaemons.size());
        for (Daemon d : pidsDaemons) {
            //DataOnMemory.pid_ntask.put(d.pid, d.extraeNtask);
            DataOnMemory.addPidNtask(new String[]{d.pid}, d.extraeNtask);
            String NTask = DataOnMemory.getNTaskByPid(d.pid);
            Undef2prv.logger.debug("pidntask[" + d.pid + "]=" + NTask);
        }
    }

    public static void addThreadNtask(String[] key) {

        //TODO:
        //1) Encontrar el daemon al que corresponde este thread (mirar su proceso
        String tPid = key[0];
        String threadId = key[1];
        String thType = key[2];
        Daemon d = DataOnMemory.hcluster.getDaemonWithPid(tPid);

        if (d != null && d.jthread_nthread.get(threadId) == null) {
            //2) Anyadir el thread al hashmap de threads del daemon con sus correspondientes ntasks
            Undef2prv.logger.debug("addThreadNtask(): 1pid registered as daemon threadID[" + tPid + ":" + threadId + "]->ntask[" + d.jthread_nthread.get(threadId) + "]");
            d.jthread_nthread.put(threadId, Integer.toString(d.jthread_nthread.size() + 1));
            Undef2prv.logger.debug("addThreadNtask(): 2pid registered as daemon threadID[" + tPid + ":" + threadId + "]->ntask[" + d.jthread_nthread.get(threadId) + "]");
            DataOnMemory.pidThread_type.put(tPid + ":" + threadId, thType);
            Undef2prv.logger.debug(DataOnMemory.pidThread_type.get(tPid + ":" + threadId));
        } else {
            Undef2prv.logger.debug("addThreadNtask(): pid not registered as daemon");
        }
    }

    public static boolean threadExists(String[] key) {

        //TODO:
        //1) Encontrar el daemon al que corresponde este thread (mirar su proceso
        String tPid = key[0];
        String threadId = key[1];
        Daemon d = DataOnMemory.hcluster.getDaemonWithPid(tPid);

        if (d != null && d.jthread_nthread.get(threadId) != null) {
            Undef2prv.logger.debug("SI");
            return true;
        } else {
            Undef2prv.logger.debug("NO");
        }
        return false;
    }

    public static void addPidNtask(String[] key, String ntask) {

        String hmkey = key[0];

        if (key.length > 1) { // Es una app/proceso con threads, la anyado a su daemon
            hmkey += ":" + key[1];
            DataOnMemory.addThreadNtask(key);
            Undef2prv.logger.debug("addPidNtask(): IF! (key.length=" + key.length + ", key=" + Arrays.toString(key) + ")");
        } else {
            Undef2prv.logger.debug("addPidNtask(): ELSE! (key.length=" + key.length + ", key=" + Arrays.toString(key) + ")");
        }

        DataOnMemory.pid_ntask.put(hmkey, ntask);
        DataOnMemory.ntask_pid.put(ntask, hmkey);
    }

    //USEFUL??
    public static void genRecordIdNtask() {

        for (Daemon d : DataOnMemory.hcluster.getAllDaemons()) {
            String NTask = DataOnMemory.getNTaskByPid(d.pid);
            DataOnMemory.NERProcess_NTask.put(d.recordId, NTask);
        }

        HashMap<String, String> a = DataOnMemory.NERProcess_NTask;
    }

    public static void assignNtaskToDaemons() {
        ArrayList<Daemon> ds = new ArrayList<>();
        ArrayList<Daemon> pidsDaemons = DataOnMemory.hcluster.getAllDaemons(); //daemons from .prv file line (88880...)

        ds.addAll(pidsDaemons);

        //int ntask = 4; //1,2,3 occupied by JT, NN, SNN
        int ntask = 1;
        for (Daemon d : ds) {
            Undef2prv.logger.debug("Assigning Daemon[ip=" + d.ip + ", pid=" + d.pid + ", type=" + d.type + "]");
            if (CommonFuncs.isNumeric(d.pid)) {
                d.extraeNtask = Integer.toString(ntask);
                Undef2prv.logger.debug("Cluster NTASK[" + d.extraeNtask + "] ASSIGNED to DAEMON[ip:pid=" + d.ip + ":" + d.pid + "]");
                ntask++;
                /*
                 if (d.type.equals(Daemon.NODE_ID_JOBTRACKER)) {
                 d.extraeNtask = Daemon.NODE_ID_JOBTRACKER;
                 Undef2prv.logger.debug("Cluster NTASK[" + d.extraeNtask + "] ASSIGNED to DAEMON[ip:pid=" + d.ip + ":" + d.pid + "]");
                 } else if (d.type.equals(Daemon.NODE_ID_NAMENODE)) {
                 d.extraeNtask = Daemon.NODE_ID_NAMENODE;
                 Undef2prv.logger.debug("Cluster NTASK[" + d.extraeNtask + "] ASSIGNED to DAEMON[ip:pid=" + d.ip + ":" + d.pid + "]");
                 } else if (d.type.equals(Daemon.NODE_ID_SECONDARY_NAMENODE)) {
                 d.extraeNtask = Daemon.NODE_ID_SECONDARY_NAMENODE;
                 Undef2prv.logger.debug("Cluster NTASK[" + d.extraeNtask + "] ASSIGNED to DAEMON[ip:pid=" + d.ip + ":" + d.pid + "]");
                 } else {
                 d.extraeNtask = Integer.toString(ntask);
                 Undef2prv.logger.debug("Cluster NTASK[" + d.extraeNtask + "] ASSIGNED to DAEMON[ip:pid=" + d.ip + ":" + d.pid + "]");
                 ntask++;
                 }
                 */
            }
        }
    }

    public static void genNodeMappingPids() {
        Set<String> pids = new HashSet<>();
        pids.addAll(DataOnMemory.fnd.pid_ip.keySet()); //get all pids from dumping file
        pids.addAll(DataOnMemory.hcluster.getAllPids()); //get all pids from daemons (obtained from prv 88880, 88881)

        String pidJT = DataOnMemory.hcluster.getPidOfType(Daemon.NODE_ID_JOBTRACKER);
        String pidNN = DataOnMemory.hcluster.getPidOfType(Daemon.NODE_ID_NAMENODE);
        String pidSNN = DataOnMemory.hcluster.getPidOfType(Daemon.NODE_ID_SECONDARY_NAMENODE);
        int ntask = 4;

        pids.remove(pidJT);
        pids.remove(pidNN);
        pids.remove(pidSNN);

        for (String pid : pids) {
            if (!pid.equals(pidJT) && !pid.equals(pidNN) && !pid.equals(pidSNN)) {
//                DataOnMemory.pid_ntask.put(pid, Integer.toString(ntask));
                DataOnMemory.addPidNtask(new String[]{pid}, Integer.toString(ntask));
                ntask++;
            }
        }

//        DataOnMemory.pid_ntask.put(pidJT, Daemon.NODE_ID_JOBTRACKER);
//        DataOnMemory.pid_ntask.put(pidNN, Daemon.NODE_ID_NAMENODE);
//        DataOnMemory.pid_ntask.put(pidSNN, Daemon.NODE_ID_SECONDARY_NAMENODE);
        DataOnMemory.addPidNtask(new String[]{pidJT}, Daemon.NODE_ID_JOBTRACKER);
        DataOnMemory.addPidNtask(new String[]{pidNN}, Daemon.NODE_ID_NAMENODE);
        DataOnMemory.addPidNtask(new String[]{pidSNN}, Daemon.NODE_ID_SECONDARY_NAMENODE);
    }

    public static void printIpportPidNtask() {
        Set<String> keys = DataOnMemory.fnd.ipport_pid.keySet();
        for (String k : keys) {
            String pid = DataOnMemory.fnd.ipport_pid.get(k);
//            String ntask = DataOnMemory.pid_ntask.get(pid);
            String ntask = DataOnMemory.getNTaskByPid(pid);


            String[] ipport = k.split(":");
            Undef2prv.logger.debug("ipport[" + Long.toHexString(Long.valueOf(ipport[0])) + ":" + ipport[1] + "]->pid[" + pid + "]->ntask[" + ntask + "]");
            System.out.println("ipport[" + Long.toHexString(Long.valueOf(ipport[0])) + ":" + ipport[1] + "]->pid[" + pid + "]->ntask[" + ntask + "]");
        }

    }

    public static void loadJClient() {

        BufferedReader br = null;

        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(DataOnMemory.FILE_JC));
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);

                //if (line.contains(":" + RecordNEvent.KEY_NODE_IP_sin_addr + ":") || line.contains(":" + RecordNEvent.KEY_NODE_TYPE + ":")) {
                //NEvent de Hadoop-Demons (88880, 88881, ...)

                InetAddress thisIp = InetAddress.getLocalHost();
                String ip = CommonFuncs.ipToStrIp(thisIp.getHostAddress());
                String nodeType = Daemon.NODE_ID_JCLIENT;
                String pid = sCurrentLine.split("@")[0];
                int recordID = Integer.parseInt(pid) + 1;
                String RNELine = "2:1:1:" + recordID + ":1:6726209962059720:88880:" + ip + ":88881:" + nodeType + ":88882:" + pid;
                Undef2prv.logger.debug("JOB_CLIENT[RNELine:" + RNELine + "]");
                RecordNEvent ner = new RecordNEvent(RNELine);
                DataOnMemory.hcluster.addDaemon(ner);
                DataOnMemory.fprv.nseq_NERDemonInfo.add(ner);
            }

            ArrayList<Daemon> pidsDaemons = DataOnMemory.hcluster.getAllDaemons(); //daemons from .prv file line (88880...)
            Undef2prv.logger.debug("DaemonsSizeLJ=" + pidsDaemons.size());

        } catch (IOException e) {
            Undef2prv.logger.debug("JOB_CLIENT[Exception while Reading]");
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Undef2prv.logger.debug("JOB_CLIENT[Exception while closing]");
                ex.printStackTrace();
            }
        }


    }
}
