package es.bsc.tools.extrae;

import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.conf.Configuration;

public class IDManager {

    static Map<String, Integer> IDmap = new HashMap<String, Integer>();
    static Map<Integer, Integer> PIDmap = new HashMap<Integer, Integer>();
    static boolean registered = false;
    // mapred.tasktracker.map.tasks.maximum = 4 static assumption 
    static int MAX_MAPS_PER_TRACKER = 4;
    // mapred.tasktracker.reduce.tasks.maximum = 1 static assumption 
    static int MAX_REDUCES_PER_TRACKER = 1;
    static int MAX_TASKS_PER_TRACKER = MAX_MAPS_PER_TRACKER + MAX_REDUCES_PER_TRACKER;
    //static int MAX_IDS_PER_SLAVE = MAX_TASKS_PER_TRACKER + 2; //DataNode + TaskTracker
    static int MAX_IDS_PER_SLAVE = MAX_TASKS_PER_TRACKER + 1; //TaskTracker
    static int NUM_UNIQUE_NODES = 0;
    // STATIC
    // JobTracker 		-> ID 0
    // NameNode 		-> ID 1
    // SecondaryNameNode	-> ID 2
    static int JOBTRACKER_ID = 0;
    static int NAMENODE_ID = 1;
    static int SECONDARYNAMENODE_ID = 2;

    // DYNAMIC
    // DataNode		-> 3 + (slave line number*(MaxMapsPerNode+MaxReducesPerNode))
    // TaskTracker		-> 3 + (slave line number*(MaxMapsPerNode+MaxReducesPerNode)) + 1
    // Task X		-> 3 + (slave line number*(MaxMapsPerNode+MaxReducesPerNode)) + N

    /*
     static {
     Configuration.addDefaultResource("mapred-default.xml");
     Configuration.addDefaultResource("mapred-site.xml");
     Configuration.addDefaultResource("hdfs-default.xml");
     Configuration.addDefaultResource("hdfs-site.xml");

     String home = System.getenv("HADOOP_PREFIX");
     String line;

     String slaves = home+"/conf/slaves";
     String masters = home+"/conf/masters";

     try {
     BufferedReader mastersReader = new BufferedReader(new FileReader(masters));
     while ((line = mastersReader.readLine()) != null) {
     Wrapper.PushIDsDown(line, JOBTRACKER_ID);
     for(InetAddress addr : InetAddress.getAllByName(line)) {
     Wrapper.PushIDsDown(addr.getHostAddress(), JOBTRACKER_ID);
     }
     }
     mastersReader.close();

     BufferedReader slavesReader = new BufferedReader(new FileReader(slaves));
     while ((line = slavesReader.readLine()) != null) {
     int currentItem = NUM_UNIQUE_NODES;
     NUM_UNIQUE_NODES++;
     IDmap.put(line, currentItem);
     Wrapper.PushIDsDown(line, currentItem);
     System.out.println(line+": "+currentItem);
     for(InetAddress addr : InetAddress.getAllByName(line)) {
     IDmap.put(addr.getHostAddress(), currentItem);
     Wrapper.PushIDsDown(addr.getHostAddress(), currentItem);
     System.out.println(addr.getHostAddress()+": "+currentItem);
     }
     }
     slavesReader.close();

     } catch (IOException e) {

     e.printStackTrace();
     System.exit(-1);
     }

     setNumTasks();

     }

     */
    public static long inetAddressToLong() {
        try {
            byte[] addr = InetAddress.getLocalHost().getAddress();

            /*    long b1 = (addr[0] << 24);
             long b2 = (addr[1] << 16);
             long b3 = (addr[2] << 8);
             long b4 = (addr[3]);
             */
            long b1 = (addr[0]);
            long b2 = (addr[1] << 8);
            long b3 = (addr[2] << 16);
            long b4 = (addr[3] << 24);

            /*long a1 = b1 & 0x00000000ff000000L;
             long a2 = b2 & 0x0000000000ff0000L;
             long a3 = b3 & 0x000000000000ff00L;
             long a4 = b4 & 0x00000000000000ffL;*/
            long a1 = b1 & 0x00000000000000ffL;
            long a2 = b2 & 0x000000000000ff00L;
            long a3 = b3 & 0x0000000000ff0000L;
            long a4 = b4 & 0x00000000ff000000L;

            long ret = a1 | a2 | a3 | a4;

            // long ret = (addr[0] << 24) | (addr[1] << 16) | (addr[2] << 8) |
            // addr[3];
            // long ip = (ret < 0) ? (ret + (1L<<32)) : ret;

            // IP address arithmetic is a royal pain
            assert ret > 0;


            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return 0;
    }

    public static void registerJobTracker(JobConf conf) {
        if (registered) {
            return;
        }

        int ports[] = digestConfiguration(conf);
        int types[] = {88880, 88881, 88882};
        long values[] = {inetAddressToLong(), 1, Wrapper.GetPID()};


        Wrapper.SetID(JOBTRACKER_ID);
        registered = true;
        System.out.println("DCARRERA: JT -> (" + Wrapper.GetPID() + ", " + Wrapper.GetID() + ")");
        Wrapper.StartPortMapper(ports);
        System.out.println("Wrapper.nEvent(types="+Arrays.toString(types)+", values=" + Arrays.toString(values)+");");
        Wrapper.nEvent(types, values);
        dumpStack();
    }

    public static void registerDatanode(Configuration conf) {
        if (registered) {
            return;
        }

        digestConfiguration(conf);
        int types[] = {88880, 88881, 88882};
        long values[] = {inetAddressToLong(), 5, Wrapper.GetPID()};

        Wrapper.SetID(getDataNodeID());
        registered = true;
        System.out.println("DCARRERA: DN -> (" + Wrapper.GetPID() + ", " + Wrapper.GetID() + ")");
        System.out.println("Wrapper.nEvent(types="+Arrays.toString(types)+", values=" + Arrays.toString(values)+");");
        Wrapper.nEvent(types, values);
        System.out.println("Wrapper.nEvent.is.back");
        dumpStack();
    }

    public static void registerNamenode(Configuration conf) {
        if (registered) {
            return;
        }

        digestConfiguration(conf);
        int types[] = {88880, 88881, 88882};
        long values[] = {inetAddressToLong(), 2, Wrapper.GetPID()};

        Wrapper.SetID(NAMENODE_ID);
        registered = true;
        System.out.println("DCARRERA: NN -> (" + Wrapper.GetPID() + ", " + Wrapper.GetID() + ")");
        System.out.println("Wrapper.nEvent(types="+Arrays.toString(types)+", values=" + Arrays.toString(values)+");");
        Wrapper.nEvent(types, values);
        System.out.println("Wrapper.nEvent.is.back");
        dumpStack();
    }

    public static void registerSecondaryNamenode(Configuration conf) {
        if (registered) {
            return;
        }

        digestConfiguration(conf);
        int types[] = {88880, 88881, 88882};
        long values[] = {inetAddressToLong(), 3, Wrapper.GetPID()};

        Wrapper.SetID(SECONDARYNAMENODE_ID);
        registered = true;
        System.out.println("DCARRERA: SNN -> (" + Wrapper.GetPID() + ", " + Wrapper.GetID() + ")");
        System.out.println("Wrapper.nEvent(types="+Arrays.toString(types)+", values=" + Arrays.toString(values)+");");
        Wrapper.nEvent(types, values);
        System.out.println("Wrapper.nEvent.is.back");
        dumpStack();
    }

    public static void registerTaskTracker(JobConf conf) {
        if (registered) {
            return;
        }
        
        int ports[] = digestConfiguration(conf);
        int types[] = {88880, 88881, 88882};
        long values[] = {inetAddressToLong(), 4, Wrapper.GetPID()};

        Wrapper.SetID(getTaskTrackerID());
        registered = true;
        System.out.println("DCARRERA: TT --> (" + Wrapper.GetPID() + ", " + Wrapper.GetID() + ")");
        Wrapper.StartPortMapper(ports);
        System.out.println("StartPortMapper finish");
        Wrapper.nEvent(types, values);
        System.out.println("Ports to sniff: " + Arrays.toString(ports));
        Wrapper.StartSniffer(ports);

        dumpStack();
    }

    public static void registerTask(JobConf conf, String tracker) {
        if (registered) {
            return;
        }

        conf.set("mapred.task.tracker.report.address", tracker);
        digestConfiguration(conf);
        int types[] = {88880, 88881, 88882};
        long values[] = {inetAddressToLong(), 6, Wrapper.GetPID()};

        try {

            RandomAccessFile raf = new RandomAccessFile("/tmp/smfile", "rw");
            FileChannel chan = raf.getChannel();
            MappedByteBuffer buf = chan.map(MapMode.READ_WRITE, 0, 1024);
            buf.position(0);
            byte value;
            int pos;
            do {
                pos = buf.position();
                value = buf.get();
                //System.out.println("pos: "+ pos + " - value: "+value);
            } while (buf.position() < buf.capacity() && value == 'x');

            if (pos < buf.capacity()) {
                buf.put(pos, (byte) 'x');
            } else {
                System.exit(-1);
            }

            /*	int pid = Wrapper.GetPID();
             if(!PIDmap.containsKey(pid)) {
             PIDmap.put(pid, PIDmap.size());
             System.out.println("NewTask: ("+pid+", localTaskID: "+PIDmap.get(pid)+", globalTaskID: "+getTaskID(PIDmap.get(pid))+")");
             }
             Wrapper.SetID(getTaskID(PIDmap.get(pid)));*/

            Wrapper.SetID(getTaskID(pos));
            registered = true;
            System.out.println("DCARRERA: Task -> (" + Wrapper.GetPID() + ", " + Wrapper.GetID() + ")");
            System.out.println("Wrapper.nEvent(types="+Arrays.toString(types)+", values=" + Arrays.toString(values)+");");
            Wrapper.nEvent(types, values);
            dumpStack();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static int getJobTrackerID() {
        return 0;
    }

    public static int getNameNodeID() {
        return 1;
    }

    public static int getSecondaryNameNodeID() {
        return 2;
    }

    public static int getTaskTrackerID(String hostname) {
        System.out.println("Searching for " + hostname);
        int base = getSecondaryNameNodeID() + 1 + IDmap.get(hostname) * MAX_IDS_PER_SLAVE;
        return base;
    }

    public static int getTaskTrackerID() {
        int res = 0;
        try {
            res = getTaskTrackerID(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return res;
    }

    public static int getDataNodeID(String hostname) {
        return getTaskTrackerID(hostname) + 1;
    }

    public static int getDataNodeID() {
        return getTaskTrackerID() + 1;
    }

    public static int getTaskID(String hostname, int task) {
        int base = getSecondaryNameNodeID() + 1 + (IDmap.get(hostname) * MAX_IDS_PER_SLAVE + 2);
        return base + task;
    }

    public static int getTaskID(int task) {
        int res = 0;
        try {
            res = getTaskID(InetAddress.getLocalHost().getHostName(), task);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return res;
    }

    public static void setNumTasks() {
        System.out.println("setNumTasks()="+getNumTasks());
        Wrapper.SetNumTasks(getNumTasks());
    }

    public static int getNumTasks() {
        //return 3+(NUM_UNIQUE_NODES*MAX_IDS_PER_SLAVE);
        int tasks= 1 + (NUM_UNIQUE_NODES * MAX_IDS_PER_SLAVE);
        System.out.println("getNumTasks()=1 + (NUM_UNIQUE_NODES * MAX_IDS_PER_SLAVE)=1+("+NUM_UNIQUE_NODES+"*"+MAX_IDS_PER_SLAVE+")="+tasks);
        return tasks;
    }

    public static int[] digestConfiguration(Configuration conf) {

        ArrayList<Integer> ports = new ArrayList<Integer>();

        Configuration.addDefaultResource("mapred-default.xml");
        Configuration.addDefaultResource("mapred-site.xml");
        Configuration.addDefaultResource("hdfs-default.xml");
        Configuration.addDefaultResource("hdfs-site.xml");


        System.out.println("Value read for mapred.tasktracker.map.tasks.maximum: " + conf.get("mapred.tasktracker.map.tasks.maximum"));
        String[] addressPort;
        // JobTracker
        addressPort = conf.get("mapred.job.tracker").split(":", 2);
        System.out.println("JobTracker report: " + addressPort[0] + ":" + addressPort[1]);

        ports.add(Integer.parseInt(addressPort[1]));

        addressPort = conf.get("mapred.job.tracker.http.address").split(":", 2);
        System.out.println("JobTracker HTTP UI: " + addressPort[0] + ":" + addressPort[1]);

        ports.add(Integer.parseInt(addressPort[1]));


        // NameNode
        addressPort = conf.get("fs.default.name").split("//", 2)[1].split(":", 2);  // FIXME
        System.out.println("Namenode service: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        addressPort = conf.get("dfs.http.address").split(":", 2);
        System.out.println("Namenode UI HTTP: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        addressPort = conf.get("dfs.https.address").split(":", 2);
        System.out.println("Namenode UI HTTPS: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        // SecondaryNameNode
        addressPort = conf.get("dfs.secondary.http.address").split(":", 2);
        System.out.println("SecondaryNamenode HTTP: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));


        // TaskTracker
        addressPort = conf.get("mapred.task.tracker.report.address").split(":", 2);
        System.out.println("TaskTracker report: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        addressPort = conf.get("mapred.task.tracker.http.address").split(":", 2);
        System.out.println("TaskTracker UI HTTP: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));


        // DataNode
        addressPort = conf.get("dfs.datanode.address").split(":", 2);
        System.out.println("DataNode service: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        addressPort = conf.get("dfs.datanode.ipc.address").split(":", 2);
        System.out.println("DataNode IPC: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        addressPort = conf.get("dfs.datanode.http.address").split(":", 2);
        System.out.println("DataNode UI HTTP: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        addressPort = conf.get("dfs.datanode.https.address").split(":", 2);
        System.out.println("DataNode UI HTTPS: " + addressPort[0] + ":" + addressPort[1]);
        ports.add(Integer.parseInt(addressPort[1]));

        String home = System.getenv("HADOOP_PREFIX");
        String line;

        String slaves = home + "/conf/slaves";
        String masters = home + "/conf/masters";


        try {

            //Pushing IP + port for packet level requests

            String name;
            BufferedReader mastersReader = new BufferedReader(new FileReader(masters));
            while ((line = mastersReader.readLine()) != null) {
                name = line + ":";
                Wrapper.PushIDsDown(name + conf.get("mapred.job.tracker").split(":", 2)[1], JOBTRACKER_ID);
                Wrapper.PushIDsDown(name + conf.get("mapred.job.tracker.http.address").split(":", 2)[1], JOBTRACKER_ID);

                Wrapper.PushIDsDown(name + conf.get("fs.default.name").split("//", 2)[1].split(":", 2)[1], NAMENODE_ID);
                Wrapper.PushIDsDown(name + conf.get("dfs.http.address").split(":", 2)[1], NAMENODE_ID);
                Wrapper.PushIDsDown(name + conf.get("dfs.https.address").split(":", 2)[1], NAMENODE_ID);

                Wrapper.PushIDsDown(name + conf.get("dfs.secondary.http.address").split(":", 2)[1], SECONDARYNAMENODE_ID);

                for (InetAddress addr : InetAddress.getAllByName(line)) {
                    name = addr.getHostAddress() + ":";
                    Wrapper.PushIDsDown(name + conf.get("mapred.job.tracker").split(":", 2)[1], JOBTRACKER_ID);
                    Wrapper.PushIDsDown(name + conf.get("mapred.job.tracker.http.address").split(":", 2)[1], JOBTRACKER_ID);

                    Wrapper.PushIDsDown(name + conf.get("fs.default.name").split("//", 2)[1].split(":", 2)[1], NAMENODE_ID);
                    Wrapper.PushIDsDown(name + conf.get("dfs.http.address").split(":", 2)[1], NAMENODE_ID);
                    Wrapper.PushIDsDown(name + conf.get("dfs.https.address").split(":", 2)[1], NAMENODE_ID);

                    Wrapper.PushIDsDown(name + conf.get("dfs.secondary.http.address").split(":", 2)[1], SECONDARYNAMENODE_ID);
                }
            }
            mastersReader.close();

            BufferedReader slavesReader = new BufferedReader(new FileReader(slaves));
            while ((line = slavesReader.readLine()) != null) {
                name = line;
                int currentItem = NUM_UNIQUE_NODES;
                NUM_UNIQUE_NODES++;
                IDmap.put(name, currentItem);
                Wrapper.PushIDsDown(name, currentItem);
                System.out.println(name + ": " + currentItem);
                for (InetAddress addr : InetAddress.getAllByName(line)) {
                    name = addr.getHostAddress();
                    IDmap.put(name, currentItem);
                    Wrapper.PushIDsDown(name, currentItem);
                    System.out.println(name + ": " + currentItem);
                }
            }
            slavesReader.close();

        } catch (IOException e) {

            e.printStackTrace();
            System.exit(-1);
        }

        setNumTasks();


        int out[] = new int[ports.size()];
        for (int i = 0; i < ports.size(); i++) {
            out[i] = ports.get(i).intValue();
        }


        return out;

    }

    public static String dump() {

        StringBuffer b = new StringBuffer();

        b.append("JobTracker: " + getJobTrackerID());
        //b.append("\nNameNode: "+getNameNodeID());
        //b.append("\nSecondaryNameNode: "+getSecondaryNameNodeID());

        for (String host : IDmap.keySet()) {
            b.append("\nHost: " + host);
            b.append("\n\tTaskTracker: " + getTaskTrackerID(host));
            //b.append("\n\tDataNode: "+getDataNodeID(host));
            for (int task = 0; task < MAX_TASKS_PER_TRACKER; task++) {
                b.append("\n\tTask_" + task + ": " + getTaskID(host, task));
            }
        }

        b.append("\nLocal TaskTracker: " + getTaskTrackerID());
        b.append("\nLocal DataNode: " + getDataNodeID());
        b.append("\nNumTasks: " + getNumTasks());

        return b.toString();
    }

    private static void dumpStack() {

        StackTraceElement[] stackTraceElementArray =
                Thread.currentThread().getStackTrace();

        for (int i = 2; i < stackTraceElementArray.length; i++) {
            String method = stackTraceElementArray[i].getClassName() + "." + stackTraceElementArray[i].getMethodName();
            String file = stackTraceElementArray[i].getFileName() + ":" + stackTraceElementArray[i].getLineNumber();
            System.out.println("\t" + (i - 2) + ":" + method + " -- " + file);
        }
    }
}
