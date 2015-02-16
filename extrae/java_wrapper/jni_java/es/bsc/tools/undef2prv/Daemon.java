package es.bsc.tools.undef2prv;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author smendoza
 */
class Daemon {

    public String recordId;
    public String pid;
    public String type;
    public String ip;
    public String app;
    public String extraeNtask = null;
    public ArrayList<String> ports = new ArrayList<>(); //Communication info from Dumfile
    public static HashMap<String, String> jthread_nthread = new HashMap<>(); //
    public static final String NODE_ID_JOBTRACKER = "1";
    public static final String NODE_ID_NAMENODE = "2";
    public static final String NODE_ID_SECONDARY_NAMENODE = "3";
    public static final String NODE_ID_TASKTRACKER = "4";
    public static final String NODE_ID_DATANODE = "5";
    public static final String NODE_ID_TASK = "6";
    public static final String NODE_ID_MAP = "7";
    public static final String NODE_ID_REDUCE = "8";
    public static final String NODE_ID_JCLIENT = "9";

    public static final Comparator<Daemon> APP_COMPARATOR = new Comparator<Daemon>() {
        public int compare(Daemon d1, Daemon d2){
            return Integer.valueOf(d1.app).compareTo(Integer.valueOf(d2.app));
        }
    };

    public static final Comparator<Daemon> VISUAL_COMPARATOR = new Comparator<Daemon>() {
        public int compare(Daemon d1, Daemon d2){
            // Change ip endianness to allow integer comparison
            int ip1 = ByteBuffer.wrap(BigInteger.valueOf(Long.parseLong(d1.ip)).toByteArray()).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
            int ip2 = ByteBuffer.wrap(BigInteger.valueOf(Long.parseLong(d2.ip)).toByteArray()).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

            // First group by paraver type
            int result = Integer.valueOf(d1.getParaverType()).compareTo(Integer.valueOf(d2.getParaverType()));
            if (result != 0) return result;

            // Then by ip
            result = Integer.valueOf(ip1).compareTo(Integer.valueOf(ip2));
            if (result != 0) return result;

            // Then by type (jobtracker, namenode, ...)
            result = Integer.valueOf(d1.type).compareTo(Integer.valueOf(d2.type));
            if (result != 0) return result;

            // Finally by app id (to maintain order of creation)
            result = Integer.valueOf(d1.app).compareTo(Integer.valueOf(d2.app));
            return result;
        }
    };

    public Daemon(RecordNEvent ner) {
//        this.recordId = ner.Process;
        this.app = ner.Application;
        Integer app = Integer.parseInt(ner.Application) + 1; //para que luego se les pueda identificar por el app
        this.recordId = app.toString();
        this.pid = ner.RecordEventsHM.get(RecordNEvent.KEY_NODE_DAEMON_PID);
        this.type = ner.getNodeType();
        this.ip = ner.getNodeIp();
    }

    public Daemon(String dpid, String dtype, String dip) {
        this.pid = dpid;
        this.type = dtype;
        this.ip = dip;
    }

    public static String getDaemonTypeAsStr(String type) {
        String nline = null;
        switch (type) {
            case Daemon.NODE_ID_DATANODE:
                nline = "DN";
                break;
            case Daemon.NODE_ID_NAMENODE:
                nline = "NN";
                break;
            case Daemon.NODE_ID_SECONDARY_NAMENODE:
                nline = "SNN";
                break;
            case Daemon.NODE_ID_TASKTRACKER:
                nline = "TT";
                break;
            case Daemon.NODE_ID_JOBTRACKER:
                nline = "JT";
                break;
            case Daemon.NODE_ID_TASK:
                nline = "TASK";
                break;
            case Daemon.NODE_ID_MAP:
                nline = "MAP";
                break;
            case Daemon.NODE_ID_REDUCE:
                nline = "RDC";
                break;
            case Daemon.NODE_ID_JCLIENT:
                nline = "JCL";
                break;
            default:
                nline = "UnknownType" + type;
                break;
        }
        return nline;
    }

    public String getParaverType() {
        String result = null;
        switch (this.type) {
            case Daemon.NODE_ID_JOBTRACKER:
                result = ParaverResource.TYPE_DAEMON;
                break;
            case Daemon.NODE_ID_NAMENODE:
                result = ParaverResource.TYPE_DAEMON;
                break;
            case Daemon.NODE_ID_SECONDARY_NAMENODE:
                result = ParaverResource.TYPE_DAEMON;
                break;
            case Daemon.NODE_ID_TASKTRACKER:
                result = ParaverResource.TYPE_DAEMON;
                break;
            case Daemon.NODE_ID_DATANODE:
                result = ParaverResource.TYPE_DAEMON;
                break;
            case Daemon.NODE_ID_TASK:
                result = ParaverResource.TYPE_TASK;
                break;
            case Daemon.NODE_ID_MAP:
                result = ParaverResource.TYPE_TASK;
                break;
            case Daemon.NODE_ID_REDUCE:
                result = ParaverResource.TYPE_TASK;
                break;
            case Daemon.NODE_ID_JCLIENT:
                result = ParaverResource.TYPE_DAEMON;
                break;
            default:
                result = ParaverResource.TYPE_STAT;
                break;
        }
        return result;
    }
}
