package es.bsc.tools.undef2prv;

import java.util.ArrayList;
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

    public Daemon(RecordNEvent ner) {
//        this.recordId = ner.Process;
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
}
