package es.bsc.tools.undef2prv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author smendoza
 */
public class FileParaverRow {

    public String path;

    public FileParaverRow(String inPath) {
        this.path = inPath;
    }

    public static void Conversion(String fPath) {

        try {
            String outputPath = fPath.replace(".row", "-out.row");
            BufferedReader reader = new BufferedReader(new FileReader(fPath));
            BufferedWriter buffwrttr = new BufferedWriter(new FileWriter(outputPath));

            String line, newline;
            int threadCounter = 0;

            //LEVEL CPU SIZE X
            String[] cpuNames = convertCpuNames();
            //System.out.println("convertCpuNames()->" + Arrays.toString(cpuNames));
            String p1 = String.format("LEVEL CPU SIZE %d\n", cpuNames.length);
            buffwrttr.write(p1);
            for (String r : cpuNames) {
                buffwrttr.write(r + "\n");
            }
            buffwrttr.write("\n"); //Linea vacia
            buffwrttr.flush();

            //LEVEL NODE SIZE Y
            String[] nodeNames = convertNodeNames();
            System.out.println("convertNodeNames()->" + Arrays.toString(nodeNames));
            String p2 = String.format("LEVEL NODE SIZE %d\n", nodeNames.length);
            buffwrttr.write(p2);
            for (String r : nodeNames) {
                buffwrttr.write(r + "\n");
            }
            buffwrttr.write("\n"); //Linea vacia
            buffwrttr.flush();

            //LEVEL THREAD SIZE X
            String[] replacements = FileParaverRow.ConvertThreadNames();
            //TODO: anyadir rename de las pid_ntask que hacen referencia a RPC-Server Listener, Reader, Handler y Responder
            // String[] replacements2 = FileParaverRow.ConvertThreadNamesRpcServers();
            // int threadsNumber = replacements.length + replacements2.length;
            int threadsNumber = replacements.length;
            String p3 = String.format("LEVEL THREAD SIZE %d\n", threadsNumber);
            buffwrttr.write(p3);
            for (String r : replacements) {
                buffwrttr.write(r + "\n");
            }
            // for (String r : replacements2) {
            //     buffwrttr.write(r + "\n");
            // }
            buffwrttr.flush();

            /*
             while ((line = reader.readLine()) != null) {
             Undef2prv.logger.debug("row-file-debugging: line[" + line + "] , threadCounter=" + threadCounter + ", replacements.length=" + replacements.length);
             // Modify names changing lines: THREAD X.1.1
             //Writes all threads and breaks the loop
             if (line.contains(".1.1")) {
             //writes threads from daemons and 
             int i = 0;
             for (String r : replacements) {
             newline = r + "\n";
             buffwrttr.write(newline);
             ++i;
             }
             System.out.println("Conversion()>replacements.length=" + replacements.length + ", i=" + i);
             //writes threads non associated with daemons (RPC-Server)
             for (String r : replacements2) {
             newline = r + "\n";
             buffwrttr.write(newline);
             ++i;
             }
             System.out.println("Conversion()>replacements2.length=" + replacements2.length + ", i=" + i);

             buffwrttr.flush(); //flush por si acaso no llega al close
             //breaks the loop
             break;
             } else {
             // other lines are copied equal
             buffwrttr.write(line + "\n");
             }
             }
             */

            /*
             while ((line = reader.readLine()) != null) {
             Undef2prv.logger.debug("row-file-debugging: line[" + line + "] , threadCounter=" + threadCounter);
             if (line.contains("THREAD 1.") && (threadCounter < arrS.size())) {
             // Thread names replaced with new ordered, done when
             // calling DataOnMemory.hcluster.getAllDaemons()
             newline = FileParaverRow.ConvertThreadNames(line);
             String dtype = arrS.get(threadCounter).type;
             buffwrttr.write(Daemon.getDaemonTypeAsStr(dtype) + "\n");
             if (!newline.contains("notask")) {
             threadCounter++;
             }
             buffwrttr.flush(); //flush por si acaso no llega al close
             } else {
             // other lines are copied equal
             buffwrttr.write(line + "\n");
             }
             }
             */

            reader.close();
            buffwrttr.close();
//            Files.copy(Paths.get(tmpPath), Paths.get(fPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            //ERROR LEYENDO EL FICHERO...
            e.printStackTrace();
        }
    }

    public static String[] ConvertThreadNamesRpcServers() {
        ArrayList<String> str = new ArrayList<>();
        int ns = DataOnMemory.ntask_pid.keySet().size();
        int ds = DataOnMemory.hcluster.getAllDaemons().size();

        System.out.println("ConvertThreadNamesRpcServers()>ns=" + ns + ",ds=" + ds);

        for (String s : DataOnMemory.ntask_pid.keySet()) {
            //System.out.println("DataOnMemory.ntask_pid.get(" + s + ")=" + DataOnMemory.ntask_pid.get(s));
        }
        for (int i = ds; i <= ns; ++i) {
            String pidThread = DataOnMemory.ntask_pid.get(Integer.toString(i));
            String pidDotThread = pidThread.replace(':', '.');
            String thType = DataOnMemory.pidThread_type.get(pidThread);
            str.add(pidDotThread+"_"+thType);
            System.out.println("ConvertThreadNamesRpcServers[ntask(" + i + ") -> pid(" + str.get(str.size() - 1) + ") ; pidThreadDot=" + pidDotThread);
        }
        String[] strArray = new String[str.size()];
        strArray = str.toArray(strArray);
        System.out.println("ConvertThreadNamesRpcServers>strArray.length=" + strArray.length);
        return strArray;
    }

    public static String[] ConvertThreadNames() {
        //Convert Daemons Thread_Names
        ArrayList<Daemon> alld = DataOnMemory.hcluster.getAllDaemons();
        String[] str = new String[alld.size() + DataOnMemory.sysstats.keySet().size()];

        for (Daemon d : alld) {
            //String dapid = DataOnMemory.getPid(d.ip, d.ports.get(0));
            int extraeNtask = Integer.valueOf(d.extraeNtask);
            str[extraeNtask - 1] = CommonFuncs.ipIntToHuman(CommonFuncs.ipToStrIp(d.ip)) + "_" + Daemon.getDaemonTypeAsStr(d.type);
            //System.out.println("ConvertThreadNames_2 str[" + extraeNtask + "]=" + str[extraeNtask - 1] + ", pid=[" + d.extraeNtask + "]" + extraeNtask);
        }
        int num_sysstat = alld.size();
        for (String s : DataOnMemory.sysstats.keySet()) {
            str[num_sysstat] = CommonFuncs.ipIntToHuman(CommonFuncs.ipToStrIp(s)) + "_" + "STATS";
            num_sysstat++;
        }
        return str;
    }

    public static String[] convertCpuNames() {
        //Convert Daemons Thread_Names
        ArrayList<Daemon> alld = DataOnMemory.hcluster.getAllDaemons();
        String[] str = new String[DataOnMemory.ntask_pid.keySet().size()];

        //TODO - anyadir los nodos de los daemons
        int k = 1;
        for (Daemon d : alld) {
            //String dapid = DataOnMemory.getPid(d.ip, d.ports.get(0));
            int extraeNtask = Integer.valueOf(d.extraeNtask);
            str[extraeNtask - 1] = k + "." + d.ip;
            ++k;
            //System.out.println("ConvertThreadNames_2 str[" + extraeNtask + "]=" + str[extraeNtask - 1] + ", pid=[" + d.extraeNtask + "]" + extraeNtask);
        }

        //TODO - anyadir los nodos de los threads
        int ns = DataOnMemory.ntask_pid.keySet().size();
        int ds = DataOnMemory.hcluster.getAllDaemons().size();
        for (int i = ds; i <= ns; ++i) {
            String x = DataOnMemory.ntask_pid.get(Integer.toString(i));
            String dpid = x.split(":")[0];
            Daemon d = DataOnMemory.hcluster.getDaemonWithPid(dpid);
            str[i - 1] = i + "." + d.ip;
        }

        return str;
    }

    public static String[] convertNodeNames() {
        //Convert Daemons Thread_Names
        ArrayList<String> str = new ArrayList<>();

        int i = 1;
        for (String s : DataOnMemory.hcluster.getAllNodeIps()) {
            str.add(i + "." + s);
            ++i;
        }
        String[] ret = new String[str.size()];
        return str.toArray(ret);
    }
}
