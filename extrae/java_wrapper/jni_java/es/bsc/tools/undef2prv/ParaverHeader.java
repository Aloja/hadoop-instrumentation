package es.bsc.tools.undef2prv;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
     * pusN]) : defines the number of nodes and number process. per node.
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
        String p4 = "3"; // Always 3 apps: Stats, Daemons and Tasks
        //String p5 = genApplicationList(); //manera clean
        String p5 = dirtyGenApplicationList(); //manera dirty

        String[] allStr = {p1, p2, p3, p4, p5};
        retval = CommonFuncs.join(allStr, ":");

        // LOG
        Undef2prv.logger.debug(retval);

        return retval;
    }

    public static String genTraceTime() {
        // Search the last timestamp
        BigInteger last = new BigInteger("0");

        for (RecordNEvent ner : DataOnMemory.fprv.nseq_NERDemonInfo) {
            BigInteger current = new BigInteger(ner.EventTime);
            last = last.max(current);
        }
        for (Map.Entry<String, ArrayList<Sysstat>> entry : DataOnMemory.sysstats.entrySet()) {
            String ip = entry.getKey();
            ArrayList<Sysstat> sysstats = entry.getValue();

            ArrayList<String> cluster_ips = DataOnMemory.hcluster.getAllNodeIps();

            Integer num_cpu = cluster_ips.indexOf(ip);
            if (num_cpu < 0) {
                continue;
            }

            for (Sysstat sysstat : sysstats) {
                BigInteger current = new BigInteger(sysstat.timestamp.toString());
                last = last.max(current);
            }
        }

        // LOG
        Undef2prv.logger.debug("genTraceTime().last=" + last);

        return String.format("%d_ns", last); // total tracetime in ns
    }

    public static String genNAppl() {
        //int nAppl = 1;
        int nAppl = DataOnMemory.hcluster.getAllDaemons().size();
        return String.format("%d", nAppl); //number of applications in the trace file
    }

    public static String genNNodes() {
        String retval = null;
        int nNodes = DataOnMemory.hcluster.getClusterSize();
        String[] cpusArr = new String[nNodes];
        Arrays.fill(cpusArr, "1");

        retval = String.format("%d(%s)", nNodes, CommonFuncs.join(cpusArr, ","));
        return retval;
    }

    public static String dirtyGenApplicationList() {

        ArrayList<String> fields = new ArrayList<>();

        // One sysstat per node
        int num_nodes = DataOnMemory.hcluster.getAllNodeIps().size();
        String[] sysstatsArr = new String[num_nodes];
        for (int i = 0; i < num_nodes; i++) {
            // Every sysstat has one thread and is assigned to a consecutive cpu
            sysstatsArr[i] = String.format("1:%s", Integer.toString(i+1));
        }
        fields.add(String.format("%d(%s)", sysstatsArr.length, CommonFuncs.join(sysstatsArr, ",")));

        // All daemons
        ArrayList<Daemon> daemons = DataOnMemory.hcluster.getAllDaemonsWithParaverType(ParaverResource.TYPE_DAEMON);
        String[] daemonsArr = new String[daemons.size()];
        for (int i = 0; i < daemons.size(); i++) {
            // Every daemon has one thread and is assigned to its cpu
            daemonsArr[i] = String.format("1:%s", DataOnMemory.hcluster.getParaverCpu(daemons.get(i)));
        }
        fields.add(String.format("%d(%s)", daemonsArr.length, CommonFuncs.join(daemonsArr, ",")));

        // All tasks (maps & reduces)
        ArrayList<Daemon> tasks = DataOnMemory.hcluster.getAllDaemonsWithParaverType(ParaverResource.TYPE_TASK);
        String[] tasksArr = new String[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            // Every task has one thread and is assigned to its cpu
            tasksArr[i] = String.format("1:%s", DataOnMemory.hcluster.getParaverCpu(tasks.get(i)));
        }
        fields.add(String.format("%d(%s)", tasksArr.length, CommonFuncs.join(tasksArr, ",")));


        return CommonFuncs.join(fields.toArray(new String[fields.size()]), ",");
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
