package es.bsc.tools.undef2prv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author smendoza
 */
public class FileParaver {

    protected HashMap<String, ArrayList<Object>> nseq_NERToConvert = new HashMap<>(); //records to convert
    protected ArrayList<RecordNEvent> nseq_NERDemonInfo = new ArrayList<>(); //records to convert
    protected Set<RecordNEvent> recordsToReidentify = new HashSet<>(); //records to reidentify, not convert
    protected ArrayList<RecordComm> ERCConverted = new ArrayList<>();
    protected ArrayList<RecordNEvent> NERConverted = new ArrayList<>();
    public String filePath = null;

    public FileParaver() {
    }

    public void loadOnMemoryPrv(String filePath) throws FileNotFoundException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        this.filePath = filePath;
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                loadLine(line);
            }
        } catch (IOException ex) {
            //ERROR LEYENDO EL FICHERO...
        }
    }

    public void loadLine(String line) {

        //TODO: if match re "number:number:...:7770:..:7771:...:7772:number\merged" then add, else don't add
        try {
            if (line.contains("#")) { // discard the header of the prv file
                return;
            } else {
            }
            RecordNEvent ner = new RecordNEvent(line);

//            if (ner.isSyn() || ner.isFin() || ner.isAck() || ner.getSizeApp().equals("0")) {
            if (ner.isSyn() || ner.isFin()) { // || !ner.isAck()) {
                //aquest "record trace" no s'incloura al definitiu
                return;
            }

            if (line.contains(":" + RecordNEvent.KEY_NODE_IP_sin_addr + ":") || line.contains(":" + RecordNEvent.KEY_NODE_TYPE + ":")) {
                //NEvent de Hadoop-Demons (88880, 88881, ...)
                Undef2prv.logger.info("CLASSIFY-AS-DAEMON[" + line + "]");
                DataOnMemory.hcluster.addDaemon(ner);
                this.nseq_NERDemonInfo.add(ner);
            } else if (line.contains(":" + RecordNEvent.KEY_LOCAL_IP_sin_addr + ":")) {
                //NEvent comunicaciones (77770,77771, ...)
                //agrupados por numero de secuencia del paquete

                //Si es Loopback, paso de el
                if (!ner.isLoopback()) {
                    Undef2prv.logger.info("CLASSIFY AS NEVENT TO CONVERT[" + line + "]");
                    String a = ner.getTcpNSeq();
                    ArrayList<Object> vner = this.nseq_NERToConvert.get(ner.getTcpNSeq());
                    if (vner == null) {
                        vner = new ArrayList<>();
                    } else {
                    }
                    vner.add(ner);
                    this.nseq_NERToConvert.put(ner.getTcpNSeq(), vner);

                } else {
                    Undef2prv.logger.debug("CLASSIFY AS IGNORED LOOPBACK PACKET[" + line + "]");
                }
            } else {
                Undef2prv.logger.debug("CLASSIFY AS TO REIDENTIFY[" + line + "]");
                Set<String> eventTypes = ner.RecordEventsHM.keySet();

                /*            
                 definidos en es.bsc.tools.extrae.Events.Types.MapOutputBuffer
                 public static final int JobTracker = 11111;
                 public static final int TaskTracker = 11112;
                 public static final int NameNode = 11113;
                 public static final int SecondaryNameNode = 11114;
                 public static final int DataNode = 11115;
                 public static final int MapTask = 11116;
                 public static final int ReduceTask = 11117;
                 public static final int MapOutputBuffer = 33333;
                 public static final int MapTaskOutputSize = 44444;
                 */

                //if (eventTypes.contains("33333")) {
                //En el post-procesado descarto nevents con 33333
                //} else {
                this.recordsToReidentify.add(ner);
                //}

            }


        } catch (Exception e) {
            Undef2prv.logger.error("ERROR READING line" + line);
            e.printStackTrace();
        }
    }

    //Convert 2 RecordNEvent to a EventReconrdComm
    public void convertNEventsToComms() {

        ArrayList<String> singleRecords = new ArrayList<>();

        Set<String> keys = this.nseq_NERToConvert.keySet();
        System.out.println("nseq_NERToConvert.keySet().size=" + nseq_NERToConvert.keySet().size());
        for (String k : keys) {
            ArrayList<RecordNEvent> nEvGroupedByNSEQ = new ArrayList<>();
            ArrayList<Object> actuales = this.nseq_NERToConvert.get(k);
            RecordComm mergedComm;

            //Cojo todos los nEvents con igual nseq
            for (Iterator<Object> it = actuales.iterator(); it.hasNext();) {
                RecordNEvent ner = (RecordNEvent) it.next();
                nEvGroupedByNSEQ.add(ner);
            }

            Undef2prv.logger.debug(String.format("nEvGroupedByNSEQ.size = %s", nEvGroupedByNSEQ.size()));

            if (nEvGroupedByNSEQ.size() == 1) {
                //This packets just have src/rcv record
                //If no rcv cannot do a communication trace record
                singleRecords.add(nEvGroupedByNSEQ.get(0).getTcpNSeq());
            } else {

                //Busqueda para emparejar nEvents
                for (int i = 0; i < nEvGroupedByNSEQ.size(); ++i) {
                    for (int j = i + 1; j < nEvGroupedByNSEQ.size(); ++j) {
                        if (i != j) {
                            RecordNEvent iNEv = nEvGroupedByNSEQ.get(i);
                            RecordNEvent jNEv = nEvGroupedByNSEQ.get(j);
                            if (iNEv.myEquals(jNEv)) {
                                //emparejar
                                mergedComm = NEvent2Comm.nEventsToComm(iNEv, jNEv);
                                this.ERCConverted.add(mergedComm);
                            }
                        }
                    }
                }
            }
        }
        String errors = CommonFuncs.join(singleRecords.toArray(new String[singleRecords.size()]), ",");
        //LOG
        Undef2prv.logger.info(singleRecords.size() + " packets solteros (solo envio o recepcion): {" + errors + "}");
    }

    /* 
     * Reidentifies the prv records that were originally at the prv-file
     * and were not nevents converted to comms.
     */
    public void reidentifyNEventRecords() {

        // Communication records?
        for (RecordNEvent ner2 : this.nseq_NERDemonInfo) {
            //String ntask = DataOnMemory.hcluster.getNTaskFromApp(ner2.Application);

            String pid = ner2.RecordEventsHM.get(RecordNEvent.KEY_NODE_DAEMON_PID);
            String ntask = DataOnMemory.getNTaskByPid(pid);
            Undef2prv.logger.debug("reidentifyNEventRecords()->DataOnMemory.getNTaskByPid(" + pid + ")=ntask[" + ntask + "]");

            if (ntask == null) {
                Undef2prv.logger.debug("ERROR-REIDENTIFYING(nseq_NERDemonInfo) ner2.Application[" + ner2.Application + "]->ntask[" + ntask + "]");
            } else {
                ner2.Application = ntask;
                Undef2prv.logger.debug("OK-REIDENTIFYING(nseq_NERDemonInfo) ner2.Application[" + ner2.Application + "]->ntask[" + ntask + "]");
            }

        }
        Undef2prv.logger.debug("ERROR REIDENTIFIED [" + this.nseq_NERDemonInfo.size() + "] OLD COMM. RECORDS");

        // Non-communication records
        for (RecordNEvent ner2 : this.recordsToReidentify) {
            //HashMap<String, String> a = DataOnMemory.NERProcess_NTask;
            //String recordId = ner2.Process;
            //Integer pid = Integer.parseInt(recordId) - 1;
            //ner2.Process = DataOnMemory.NERProcess_NTask.get(recordId);
            //ner2.Process = DataOnMemory.pid_ntask.get(pid.toString());
            String ip, port, ntask, threadId = null, pid = null;
            Set<String> eventTypes = ner2.RecordEventsHM.keySet();

            /*            
             public static final int JobTracker = 11111;
             public static final int TaskTracker = 11112;
             public static final int NameNode = 11113;
             public static final int SecondaryNameNode = 11114;
             public static final int DataNode = 11115;
             public static final int MapTask = 11116;
             public static final int ReduceTask = 11117;
             public static final int MapOutputBuffer = 33333;
             public static final int MapTaskOutputSize = 44444;
             */

            if (eventTypes.contains("33333")) {
                //Eventos 33333: son MapReduce Tasks es.bsc.tools.extrae.Events.Types.MapOutputBuffer
                pid = ner2.RecordEventsHM.get("99199");
                ntask = DataOnMemory.getNTaskByPid(pid);
                Undef2prv.logger.debug("eventTypes=33333 - pid[" + pid + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("5051")) {
                //Eventos 5051: generados en la libpcap antes/despues de la comunicación
                ip = ner2.RecordEventsHM.get("5051");
                port = ner2.RecordEventsHM.get("5052");
                ntask = DataOnMemory.getNTaskByIpPort(ip, port);
                Undef2prv.logger.debug("FROM-IP-PORT->ip:port[" + ip + ":" + port + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("1019911")) {
                //Eventos 1019911: lectura de paquete en BlockReceiver.readToBuf()
                pid = ner2.RecordEventsHM.get("1019911");
                ntask = DataOnMemory.getNTaskByPid(pid);

                //desde los ip-puerto
                ip = ner2.RecordEventsHM.get("1019912");
                port = ner2.RecordEventsHM.get("1019913");
                ntask = DataOnMemory.getNTaskByIpPort(ip, port);
                Undef2prv.logger.debug("DN-STATE-EVENT(eventTypes=1019912)->ip:port[" + ip + ":" + port + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("1029912")) {
                //Eventos 1029912: lectura de bloque en BlockReceiver.receiveBlock()
                //String pid = ner2.RecordEventsHM.get("1029912");
                //ntask = DataOnMemory.getNTaskByPid(pid);s

                //desde los ip-puerto
                ip = ner2.RecordEventsHM.get("1029914");
                port = ner2.RecordEventsHM.get("1029915");
                ntask = DataOnMemory.getNTaskByIpPort(ip, port);
                Undef2prv.logger.debug("DN-STATE-EVENT(eventTypes=1029912)->ip:port[" + ip + ":" + port + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("1200001")) {
                //Eventos 1200001: despues lectura buffer en el Reader del RPC-Server que usan NN, JT, TT
                /*
                 ip = ner2.RecordEventsHM.get("1200004");
                 port = ner2.RecordEventsHM.get("1200005");
                 ntask = DataOnMemory.getNTaskByIpPort(ip, port);
                 Undef2prv.logger.debug("RPC-SERVER-RCV(1200001)->ip:port[" + ip + ":" + port + "]->ntask[" + ntask + "]");
                 */
                pid = ner2.RecordEventsHM.get("1200001");
                ntask = DataOnMemory.getNTaskByPid(pid);
                Undef2prv.logger.debug("RPC-SERVER-RCV(1200001)->pid[" + pid + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("1100001")) {
                //Eventos 1100001: antes lectura buffer en el Reader del RPC-Server que usan NN, JT, TT
                /*
                 ip = ner2.RecordEventsHM.get("1100004");
                 port = ner2.RecordEventsHM.get("1100005");
                 ntask = DataOnMemory.getNTaskByIpPort(ip, port);
                 * Undef2prv.logger.debug("RPC-SERVER-RCV(1100001)->ip:port[" + ip + ":" + port + "]->ntask[" + ntask + "]");
                 * */
                pid = ner2.RecordEventsHM.get("1100001");
                ntask = DataOnMemory.getNTaskByPid(pid);
                Undef2prv.logger.debug("RPC-SERVER-RCV(1100001)->pid[" + pid + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("1300001")) {
                //Eventos 1300001: antes del call en el Handler del RPC-Server que usan NN, JT, TT
                pid = ner2.RecordEventsHM.get("1300001");
                threadId = ner2.RecordEventsHM.get("1300002");
                ntask = DataOnMemory.getNTaskByPid(new String[]{pid, threadId});
                boolean thExists = DataOnMemory.threadExists(new String[]{pid, threadId});
                if (!thExists) { // si thread no registrado en el daemon
                    //TODO: asociar una nueva ntask a ese pid:thread
                    int n = DataOnMemory.pid_ntask.size();
                    ntask = Integer.toString(n);
                    DataOnMemory.addPidNtask(new String[]{pid, threadId, "HANDLER"}, ntask);
                    //System.out.println("{pid,threadId}->ntask={"+pid+","+threadId+"}->"+ntask);
                }
                Undef2prv.logger.debug("RPC-SERVER-RCV(1300001)->pid:thread[" + pid + ":" + threadId + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("1400001")) {
                //Eventos 1400001: antes del call en el Responder del RPC-Server que usan NN, JT, TT
                pid = ner2.RecordEventsHM.get("1400001");
                threadId = ner2.RecordEventsHM.get("1400002");
                ntask = DataOnMemory.getNTaskByPid(new String[]{pid, threadId});
                boolean thExists = DataOnMemory.threadExists(new String[]{pid, threadId});
                if (!thExists) { // si thread no registrado en el daemon
                    //TODO: asociar una nueva ntask a ese pid:thread
                    int n = DataOnMemory.pid_ntask.size();
                    ntask = Integer.toString(n);
                    DataOnMemory.addPidNtask(new String[]{pid, threadId, "RESPONDER"}, ntask);
                    //System.out.println("{pid,threadId}->ntask={"+pid+","+threadId+"}->"+ntask);
                }
                Undef2prv.logger.debug("RPC-SERVER-RCV(1400001)->pid:thread[" + pid + ":" + threadId + "]->ntask[" + ntask + "]");
            } else if (eventTypes.contains("1500001")) {
                //Eventos 1500001: antes del call en el Listener del RPC-Server que usan NN, JT, TT
                pid = ner2.RecordEventsHM.get("1500001");
                threadId = ner2.RecordEventsHM.get("1500002");
                ntask = DataOnMemory.getNTaskByPid(new String[]{pid, threadId});
                boolean thExists = DataOnMemory.threadExists(new String[]{pid, threadId});
                if (!thExists) { // si thread no registrado en el daemon
                    //TODO: registrar el thread en el daemon
                    int n = DataOnMemory.pid_ntask.size();
                    ntask = Integer.toString(n);
                    DataOnMemory.addPidNtask(new String[]{pid, threadId, "LISTENER"}, ntask);
                    //System.out.println("{pid,threadId}->ntask={"+pid+","+threadId+"}->"+ntask);
                }
                Undef2prv.logger.debug("RPC-SERVER-RCV(1500001)->pid:thread[" + pid + ":" + threadId + "]->ntask[" + ntask + "]");
            } else {
                //PARA ANYADIR FUTURAS REIDENTIFICACIONES...
                ntask = null;
            }

            //String ntask = DataOnMemory.hcluster.getNTaskFromApp(ner2.Application);
            if (ntask == null) {
                Undef2prv.logger.error("ERROR-REIDENTIFYING(recordsToReidentify) ner2.Application[" + ner2.Application + "]->ntask[" + ntask + "]");
            } else {

                ner2.Application = ntask;
                Undef2prv.logger.debug("OK-REIDENTIFYING(recordsToReidentify) ner2.Application[" + ner2.Application + "]->ntask[" + ntask + "]");
                if (threadId != null) {
                    //TODO: poner threadId como paramentro del extrae record
                    //ner2.Thread = DataOnMemory.getNThreadByPid(new String[]{pid, threadId});
                    Undef2prv.logger.debug("OK-REIDENTIFYING(recordsToReidentify) ner2.Thread[" + ner2.Thread + "]->threadId[" + threadId + "]");
                }

            }
        }
        Undef2prv.logger.debug("REIDENTIFIED [" + this.recordsToReidentify.size() + "] OLD NON-COMM. RECORDS");
    }

    public void sortRecords() {
        Collections.sort(ERCConverted, RecordComm.TEMPORAL_SORT);
    }

    public void printNseqNevents() {
        Set<String> keys = this.nseq_NERToConvert.keySet();
        for (String k : keys) {
            Undef2prv.logger.debug("nseq_nevents[" + k + "]->");
            nseq_NERToConvert.get(k).toString();
        }
    }

    public void printNseqNVevents() {
        Set<String> keys = this.nseq_NERToConvert.keySet();
        for (String k : keys) {
            //LOG
            Undef2prv.logger.debug("nseq_nvevents[" + k + "]->");

            for (Iterator<Object> it = nseq_NERToConvert.get(k).iterator(); it.hasNext();) {
                Object obj = it.next();
                String xx = obj.getClass().getName();
                if (xx.contains("NEventRecord")) {
                    RecordNEvent ner = (RecordNEvent) obj;
                    ner.toString();
                } else if (xx.contains("EventRecordComm")) {
                    RecordComm erc = (RecordComm) obj;
                    erc.printAll();
                }

            }
        }
    }

    public void printAll() {
        Undef2prv.logger.debug("File records: " + this.nseq_NERToConvert.values().size());
        this.printNseqNevents();
        this.printNseqNVevents();

    }

    public void printPrvFile() {
        boolean clean = false; //true: sin nulls, false con nulls
        this.printPrvFile(clean);
    }

    public void printPrvFileClean() {
        boolean clean = true; //true: sin nulls
        this.printPrvFile(clean);
    }

    public void printPrvFile(boolean clean) {

        try {
            int reidcounter = 0;
            int commcounter = 0, commok = 0, commwnull = 0;
            int ercounter = 0;
            int nullcounter = 0;
            String outputFile = this.filePath.replace(".prv", "-out.prv");
            PrintWriter filepw = new PrintWriter(outputFile);

            //First the prv header
            filepw.println(ParaverHeader.ParaverHeaderGenerator());

            //los 888888
            for (RecordNEvent ner : this.nseq_NERDemonInfo) {
                String recordStr = ner.toStringParaverFormat();
                //Writing to the file
                filepw.println(recordStr);
                filepw.flush();
            }

            //Records merged
            for (Object obj : this.ERCConverted) {
                String xx = obj.getClass().getName();
                if (xx.contains("RecordNEvent")) {
                    RecordNEvent ner = (RecordNEvent) obj;
                    String recordStr = ner.toStringParaverFormat();

                    if (clean && recordStr.contains("null")) {
                        nullcounter++;
                    } else {
                        //OUTPUT file
                        filepw.println(recordStr);
                        ercounter++;
                    }
                } else if (xx.contains("RecordComm")) {
                    RecordComm erc = (RecordComm) obj;
                    String recordStr = erc.toStringParaverFormat();

                    if (clean && recordStr.contains("null")) {
                        commwnull++;
                    } else {
                        //OUTPUT file
                        if (recordStr.contains("null")) {
                            commwnull++;
                        } else {
                            commok++;
                        }
                        filepw.println(recordStr);

                    }
                    commcounter++;
                }

                //OUTPUT file
                filepw.flush();
            }

            //LOG
            Undef2prv.logger.debug("OUTPUT prv => " + outputFile);
            Undef2prv.logger.debug("NEventRecord (WRITTEN TO *.prv)=" + ercounter);
            Undef2prv.logger.debug("EventRecordComm=" + commcounter);
            Undef2prv.logger.debug("Communication unidentified src/dst=" + nullcounter);

            //Records not merged
            reidcounter = 0;
            int nevents = 0, nstates = 0;
            for (RecordNEvent ner : this.recordsToReidentify) {
                String recordStr = ner.toStringParaverFormat();

                if (clean && !recordStr.contains("null")) {
                    //OUTPUT file
                    filepw.println(recordStr);//Writing to the file
                    filepw.flush();
                } else {
                    //OUTPUT file
                    filepw.println(recordStr);//Writing to the file
                    filepw.flush();
                    reidcounter++;
                    if (ner.RecordType.equals("2")) {
                        nevents++;
                    } else if (ner.RecordType.equals("1")) {
                        nstates++;
                    }
                }
            }
            System.out.println("#events=" + nevents);
            System.out.println("#nstates=" + nstates);
            System.out.println("#ncomms_ok=" + commok);
            System.out.println("#ncomms_error" + commwnull);
            System.out.println("#ncomms_total" + commcounter);

            //LOG
            Undef2prv.logger.debug("NEventRecord reidentified=" + reidcounter);

            //OUTPUT file
            filepw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public static Integer[] posiblePids(String process) {
//        //process is [IP|IP|PID|PID]
//
//
//        /**
//         * void gen_task_id(void) { unsigned int id = -1; char name[256];
//         *
//         * gethostname(name, 256); struct hostent *he = gethostbyname(name);
//         * if(he == NULL) return;
//         *
//         * unsigned long ip = ((struct in_addr*)he->h_addr)->s_addr; unsigned
//         * int recordId = getpid();
//         *
//         * TASKID = (((ip & 0x0000FFFF) << 16) | (recordId & 0x0000FFFF)) % 6000 ;
//         *
//         * printf("Task ID: %d - PID: %x - IP: %s - IPbin: %x\merged", TASKID, recordId,
//         * inet_ntoa( *((struct in_addr*)he->h_addr) ), ip);
//         *
//         * }
//         */
//        int p = Integer.parseInt(process);
//
//        Set<Integer> pids = new HashSet<>();
//        int i = 0;
//        int possiblePid = 0;
//        for (i = 0; possiblePid < (Math.pow(2, 16) - 1); i++) {
//            possiblePid = p + 6000 * i;
//            //TODO: verificar si el recordId existe antes de anyadirlo
//            pids.add(possiblePid);
//            
//            String x = DataOnMemory.fnd.pid_ip.get(possiblePid);
//            if (x!=null){
//                System.out.println("UOOOOOOOOOOOOOOOOOOOOOOOOOO!");
//            }
//            
//        }
//
//        return pids.toArray(new Integer[pids.size()]);
//
//    }
}
