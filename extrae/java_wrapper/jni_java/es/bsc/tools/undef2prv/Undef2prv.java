package es.bsc.tools.undef2prv;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author smendoza
 */
public class Undef2prv {

    static Logger logger = Logger.getLogger("Undef2prv");

    /**
     * @param args [dumping path] [prv path] [row path]
     */
    public static void main(String[] args) {

        //arguments reading
        String fileDumpfile = args[0]; // dumping-host-port-pid file
        String filePrv = args[1]; // *.prv file
        String fileRow = args[2]; // *.row file
        String filePcf = args[3]; // *.pcf file
        String fileSysstat = args[4]; // sysstat.txt file

        //log4j output log
        System.setProperty("TRACES_OUTPUT", System.getenv("TRACES_OUTPUT"));
        String confFile = System.getenv("LOG4J_CONFFILE");
        if (confFile != null) {
            PropertyConfigurator.configure(confFile);
            System.out.println("Log configured from " + confFile);
        } else {
            System.out.println("Log not configured, disabling...");
            logger.setLevel(Level.OFF);
        }

        try {

            DataOnMemory.fprv.loadOnMemoryPrv(filePrv);
            DataOnMemory.fprv.loadOnMemorySysstat(fileSysstat);
            DataOnMemory.fprv.syncAnalysis();
            DataOnMemory.fprv.syncSysstat();
            DataOnMemory.loadJClient();
            //LOG
            Undef2prv.logger.info("prv loaded...");

            DataOnMemory.loadOnMemoryDumpfile(fileDumpfile);
            //LOG
            Undef2prv.logger.debug("dumpfile loaded...");

//            DataOnMemory.genRecordIdNtask(); //USEFUL ? Mapping RecordId - Ntask (needed for non-communications events)
            DataOnMemory.assignNtaskToDaemons();
            DataOnMemory.genHTPidNtask(); // HashTable pid->ntask from the assignation done at assignNtaskToDaemons()
            //LOG
            Undef2prv.logger.info("Mapping pid-ntask generated...");

            //.prv conversion
            DataOnMemory.fprv.convertNEventsToComms();
            DataOnMemory.fprv.reidentifyNEventRecords();
            DataOnMemory.fprv.syncAnalysisNetworkHandshake();
            DataOnMemory.fprv.syncAllNodes();
            DataOnMemory.fprv.sortRecords();
            DataOnMemory.fprv.printPrvFile();

//            HashMap<String, ArrayList<String>> hmPidPorts = DataOnMemory.hcluster.getPortsGroupedByPid();

            // .row conversion
            FileParaverRow.Conversion(fileRow);

            // .pcf conversion (only copy for now)
            Files.copy(Paths.get(filePcf), Paths.get(filePcf.replace(".pcf", "-out.pcf")), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            //<debugging-ports>
            System.out.println(CPort.printheader);
            int stotal = 0;
            int ptotal = 0;
            int pconflict = 0;
            int pnoconflict = 0;
            int sizeConf = 0;
            for (CPort cp : DataOnMemory.portDebugging.values()) {
                if (cp.conflictivo) {
                    Undef2prv.logger.debug(cp);
                    System.out.println(cp);
                    pconflict++;
                    sizeConf += cp.sizeAcumulada;
                } else {
                    Undef2prv.logger.debug(cp);
                    pnoconflict++;
                }

                stotal += cp.sizeAcumulada;
                ptotal += cp.paquetesTransmitidos;
            }
            int media = stotal / DataOnMemory.portDebugging.size();

            System.out.println("sizeTotalConf=" + sizeConf + " - sizeTOTAL=" + stotal);
            Undef2prv.logger.debug("AVERAGE SIZE PER PORT = " + media);
            Undef2prv.logger.debug("#Puertos SI conflictivos = " + pconflict);
            Undef2prv.logger.debug("#Puertos NO conflictivos = " + pnoconflict);
            System.out.println("AVERAGE SIZE PER PORT = " + media);
            System.out.println("#Puertos SI conflictivos = " + pconflict);
            System.out.println("#Puertos NO conflictivos = " + pnoconflict);
            //</debugging-ports>

//            DataOnMemory.fprv.printPrvFileClean();
            DataOnMemory.printIpportPidNtask();
        } catch (Exception ex) {
            //Exception loading File
            Undef2prv.logger.error("Error executing the file...");
            ex.printStackTrace();
        }

        //2:5:1:881:1:4290409779888766:77770:8181922547240009729:77771:8181922547508491913:77772:8181922547508495184:77773:8181922547240009796:77774:8181922547240009728
//        NEventRecord nevtExample_1 = new NEventRecord("2:5:1:881:1:4290409779888766:77770:8181922547240009729:77771:8181922547508491913:77772:8181922547508495184:77773:8181922547240009796:77774:8181922547240009728");
//        NEventRecord nevtExample_2 = new NEventRecord("2:5:1:881:1:4290409779888766:77770:8181922547240009729:77771:8181922547508491913:77772:8181922547508495184:77773:8181922547240009796:77774:8181922547240009728");

        //convertir 2comm a 1comm
//        EventRecordComm etc3 = Conversions.nEventToEventComm(nevtExample_1, nevtExample_2);

    }
}
