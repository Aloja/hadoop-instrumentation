package es.bsc.tools.undef2prv;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author smendoza
 */
public class ParaverHeader {

    /**
     * The trace header is a line where the different fields define the object
     * structure (fields are separated by colons). The trace header format is -
     * #Paraver (dd/mm/yy at hhamn): defines the date and hour where trace has
     * been generated. Is is important to use the symbol # at the beginning of
     * the header incl because it inidicates that it is in ASCII trace format:
     * fume: total trace time in microseconds - nNodes(nCpus 1 [mCpus2,...,nC
     * pusN]) : defines the number of nodes and number pro-cess°. per node.
     * After the number of nodes (nNodes), the list ofthe number of processors
     * must be specified (nCpusl is the number of processors on node 1, nCpus2
     * is the number of processors on node 2,...). - nAppl: number of
     * applications in the trace file. - applicationList : The application list
     * defines the application object structure. Each ap-plication hos its
     * application list (applicationList) separared by a colon. The application
     * list format is: nTasks(nThreadsl:node nThreadsN:node)
     */
    class ParaverApplList {

        public String nTasks;
        public ArrayList<ParaverProcess> tasks;
    }

    class ParaverProcess {

        public String nThreads;
        public String node;
    }

    class ParaverNode {

        public String node;
        public String cpus;
    }

    public static String ParaverHeaderGenerator() {
        String retval = "";
        String p1 = genDate(); //dd/mm/yy
        String p2 = genTraceTime(); //ftime
        String p3 = genNNodes(); //nNodes(nCpus1[,nCpus2,...,nCpusN])
        //String p4 = genNAppl(); //manera clean
        String p4 = Integer.toString(DataOnMemory.ntask_pid.keySet().size()); //manera dirty
        //String p5 = genApplicationList(); //manera clean
        String p5 = dirtyGenApplicationList(); //manera dirty

        String[] allStr = {p1, p2, p3, p4, p5};
        retval = CommonFuncs.join(allStr, ":");

        // LOG
        Undef2prv.logger.debug(retval);

        return retval;
    }

    public static String genTraceTime() {
        //TODO: obtener el trace-record con el tiempo mínimo y el trace-record con tiempo máximo
        //Long totalTraceTime = 4323979284422710L;
        BigInteger totalTraceTime = new BigInteger("0");

        BigInteger first = new BigInteger(DataOnMemory.fprv.ERCConverted.get(0).CommSrcTimePhysical);
        BigInteger last = new BigInteger(DataOnMemory.fprv.ERCConverted.get(DataOnMemory.fprv.ERCConverted.size() - 1).CommSrcTimePhysical);



        totalTraceTime = last.subtract(first);

        // LOG
        Undef2prv.logger.debug("genTraceTime().first=" + first);
        Undef2prv.logger.debug("genTraceTime().last=" + last);
        Undef2prv.logger.debug("genTraceTime().totalTraceTime = (last - first) = " + last);

        return String.format("%d_ns", totalTraceTime); // total tracetime in ns
    }

    public static String genNAppl() {
        //int nAppl = 1;
        int nAppl = DataOnMemory.hcluster.getAllDaemons().size();
        return String.format("%d", nAppl); //number of applications in the trace file
    }

    public static String genNNodes() {
        String retval = null;
        int nNodes = DataOnMemory.hcluster.getClusterSize();
        Set<String> daemons = DataOnMemory.hcluster.getAllPidsByNode();
        String[] strArr = daemons.toArray(new String[daemons.size()]);

        retval = String.format("%d(%s)", nNodes, CommonFuncs.join(strArr, ","));
        return retval;
    }

    public static String dirtyGenApplicationList() {

        ArrayList<String> nthrStr = new ArrayList<>();
        String retval = null;
        
        for (String s : DataOnMemory.ntask_pid.keySet()) {
            Undef2prv.logger.debug("dirtyGenApplicationList->"+s);
            nthrStr.add(String.format("1(1:%s)", s)); // Default #threads is 1
        }

        String d = CommonFuncs.join(nthrStr.toArray(new String[nthrStr.size()]), ",");

        //retval = String.format("%d(%s)", nthrStr.size(), d); //nTasks(nThreadsl:node,...,nThreadsN:node) 
        retval = String.format("%s", d); //nTasks(nThreadsl:node,...,nThreadsN:node), 
        return retval;
    }

    public static String genApplicationList() {
        String retval = null;
        int node_i = 1;
        int daemon_i = 1;
        int totalTasks = 0;

        ArrayList<String> nthrStr = new ArrayList<>();
        Collection<Node> nodes = DataOnMemory.hcluster.nodes.values();
        for (Node n : nodes) {
            for (Daemon d : n.daemons.values()) {
                nthrStr.add(String.format("1(1:%d)", node_i)); // Default #threads is 1
                totalTasks++;
            }
            node_i++;
        }

        String d = CommonFuncs.join(nthrStr.toArray(new String[nthrStr.size()]), ",");

        //retval = String.format("%d(%s)", nthrStr.size(), d); //nTasks(nThreadsl:node,...,nThreadsN:node) 
        retval = String.format("%s", d); //nTasks(nThreadsl:node,...,nThreadsN:node), 
        return retval;
    }

    public static String genDate() {
        Date date = new Date();
        SimpleDateFormat day = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat hour = new SimpleDateFormat("HH:mm");

        //"#Paraver (dd/mm/yy at hhamn)"
        String dStr = day.format(date);
        String hStr = hour.format(date);

        String paraverDate = "#Paraver (" + dStr + " at " + hStr + ")";
        return paraverDate;
    }
}
