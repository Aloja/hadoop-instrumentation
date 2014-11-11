package es.bsc.tools.undef2prv;

import java.math.BigInteger;

/**
 *
 * @author smendoza
 */
public class NEvent2Comm {

    public NEvent2Comm() {
    }

    //returns nEvents sort by [sendt,reception]
    public static RecordNEvent[] nEvSort(RecordNEvent nev1, RecordNEvent nev2) {

        RecordNEvent[] sorted = new RecordNEvent[2];

        BigInteger t1 = new BigInteger(nev1.getTime());
        BigInteger t2 = new BigInteger(nev2.getTime());
        if (t1.compareTo(t2) <= 0) {
            sorted[0] = nev1;
            sorted[1] = nev2;
            return sorted;
        } else if (t1.compareTo(t2) > 0) {
            sorted[0] = nev2;
            sorted[1] = nev1;
            return sorted;
        } else {
            //UNKNOWN
            return null;
        }
        
        /*
         *  long t1 = Long.parseLong(nev1.getTime());
        long t2 = Long.parseLong(nev2.getTime());
        if (t1 <= t2) {
            sorted[0] = nev1;
            sorted[1] = nev2;
            return sorted;
        } else if (t1 > t2) {
            sorted[0] = nev2;
            sorted[1] = nev1;
            return sorted;
        } else {
            //UNKNOWN
            return null;
        }
        * */
    }

    public static RecordComm nEventsToComm(RecordNEvent nev1, RecordNEvent nev2) {

        RecordNEvent[] a = nEvSort(nev1, nev2);
        RecordNEvent nEvSrc = a[0];
        RecordNEvent nEvDst = a[1];

//        long x = Long.parseLong(nEvDst.EventTime) - Long.parseLong(nEvSrc.EventTime);
//        Undef2prv.logger.debug(String.format("Time between = (%s - %s) = %s", nEvDst.EventTime, nEvSrc.EventTime, x));
//        Undef2prv.logger.debug(String.format("seqN1=%s - seqN2=%s", nEvDst.getTcpNSeq(), nEvSrc.getTcpNSeq()));

        RecordComm eRComm = new RecordComm();

//2:5:1:881:1:4290409779888766:77770:8181922547240009729:77771:8181922547508491913:77772:8181922547508495184:77773:8181922547240009796:77774:8181922547240009728

        eRComm.RecordType = RecordComm.REC_TYPE_COMM;

        //Source Information
        eRComm.CommSrcCpu = nEvSrc.Cpu; //Communication Source: CPU
        eRComm.CommSrcApplication = DataOnMemory.getNTaskSender(nEvSrc); //nEvSrc.Application; //Communication Source: APPLICATION
        eRComm.CommSrcProcess = nEvSrc.Process; //Communication Source: PROCESS
        eRComm.CommSrcThread = nEvSrc.Thread; //Communication Source: THREAD
        eRComm.CommSrcTimeLogical = nEvSrc.getTime(); //Communication Destination: tLogical
        eRComm.CommSrcTimePhysical = nEvSrc.getTime(); //Communication Destination: tPhysical

        //Destination information
        eRComm.CommDstCpu = nEvDst.Cpu; //Communication Source: CPU
        eRComm.CommDstApplication = DataOnMemory.getNTaskReceiver(nEvSrc); //nEvDst.Application; //Communication Destination: APPLICATION
        eRComm.CommDstProcess = nEvDst.Process;//Communication Destination: PROCESS
        eRComm.CommDstThread = nEvDst.Thread; //Communication Destination: THREAD
        eRComm.CommDstTimeLogical = nEvDst.getTime(); //Communication Destination: tLogical
        eRComm.CommDstTimePhysical = nEvDst.getTime(); //Communication Destination: tPhysical
        eRComm.CommSize = nEvSrc.getSizeApp(); //Communication sizeAcumulada
        eRComm.CommTag = RecordComm.DEFAULT_TAG;

        //<DEBUGGING....>
        //SRC_PORT ANALYSIS
        String sndPort = nEvSrc.getSrcPort();
        CPort srcCPort = DataOnMemory.portDebugging.get(nEvSrc.getSrcIp() + ":" + sndPort);
        if (srcCPort == null) {
            srcCPort = new CPort();
            srcCPort.port = sndPort;
            srcCPort.ip = nEvSrc.getSrcIp();
            srcCPort.portDst = nEvSrc.getDstPort();
            srcCPort.ip = nEvSrc.getDstIp();
            DataOnMemory.portDebugging.put(srcCPort.ip + ":" + sndPort, srcCPort);
        }
        if (eRComm.CommSrcProcess == null) {
            //Null TASK
            srcCPort.conflictivo = true;
            srcCPort.conflictos++;
        }
        srcCPort.sizeAcumulada += Integer.parseInt(nEvSrc.getSizeRawPcket());
        srcCPort.paquetesTransmitidos++;

        //DST_PORT ANALYSIS
        String dstPort = nEvSrc.getDstPort();
        String dstIP = nEvSrc.getDstIp();
        String dstSize = nEvDst.getSizeRawPcket();
        CPort dstCPort = DataOnMemory.portDebugging.get(dstIP + ":" + dstPort);
        if (dstCPort == null) {
            dstCPort = new CPort();
            dstCPort.port = dstPort;
            dstCPort.ip = dstIP;
            dstCPort.ipDst = nEvSrc.getSrcIp();
            dstCPort.portDst = nEvSrc.getSrcPort();
            DataOnMemory.portDebugging.put(dstCPort.ip + ":" + dstPort, dstCPort);
        }
        if (eRComm.CommDstProcess == null) {
            //Null TASK
            dstCPort.conflictivo = true;
            dstCPort.conflictos++;
        }
        dstCPort.sizeAcumulada += Integer.parseInt(dstSize);
        dstCPort.paquetesTransmitidos++;
        //</DEBUGGING....>

        return eRComm;
    }

    public static RecordComm TwoECommToOneComm(RecordComm eComm1, RecordComm eComm2) {
        RecordComm result = new RecordComm();

        return result;
    }

    /*
     public static RecordComm FourEventsToOneComm(EventRecord eSend, EventRecord eLocal, EventRecord ePartner, EventRecord eSize) {
     RecordComm etc = new RecordComm();
     int remoteMachineIp;
     int remoteMachinePort;
     int localMachineIp;
     int localMachinePort;
     int pid;

     Long send = Long.parseLong(eSend.EventValue); // 0receive, 1Send, 2Ambiguous?
     String local = eLocal.EventValue; //IP|IP|PORT|PORT
     String partner = ePartner.EventValue; //IP|IP|PORT|PORT
     String sizeAcumulada = eSize.EventValue; //CORREGIR, NO COINCIDEN

     //              eRComm.RecordType = "3";
     //        this.CommSrcCpu = splitted[1]; //Communication Source: CPU
     //        this.CommSrcApplication = splitted[2]; //Communication Source: APPLICATION
     //        this.CommSrcProcess = splitted[3]; //Communication Source: PROCESS
     //        this.CommSrcThread = splitted[4]; //Communication Source: PROCESS
     //        this.CommSrcTimeLogical = splitted[5]; //Communication Source: THREAD
     //        this.CommSrcTimePhysical = splitted[6];
     //        this.CommDstCpu = splitted[7]; //Communication Source: CPU
     //        this.CommDstApplication = splitted[8]; //Communication Destination: APPLICATION
     //        this.CommDstProcess = splitted[9]; //Communication Destination: PROCESS
     //        this.CommDstThread = splitted[10]; //Communication Destination: THREAD
     //        this.CommDstTimeLogical = splitted[11]; //Communication Destination: tLogical
     //        this.CommDstTimePhysical = splitted[12]; //Communication Destination: tPhysical
     //        this.CommSize = splitted[13]; //Communication sizeAcumulada
     //        this.CommTag = splitted[14];

     //2:5:1:881:1:4290409779888766:77770:8181922547240009729:77771:8181922547508491913:77772:8181922547508495184:77773:8181922547240009796:77774:8181922547240009728

     etc.RecordType = REC_TYPE_COMM;
     etc.CommSize = sizeAcumulada; //Communication sizeAcumulada
     etc.CommTag = null;
     if ((send & 0xFFFF) == 0) {
     // Receive
     remoteMachineIp = CommonFuncs.ipportParseIp(local);
     remoteMachinePort = CommonFuncs.ipportParsePort(local);
     localMachineIp = CommonFuncs.ipportParseIp(partner);
     localMachinePort = CommonFuncs.ipportParsePort(partner);

     pid = (int) DataOnMemory.getPid(partner);

     etc.CommDstProcess = local;
     etc.CommDstCpu = null; //Communication Source: CPU
     etc.CommDstApplication = null; //Communication Destination: APPLICATION
     etc.CommDstProcess = null; //Communication Destination: PROCESS
     etc.CommDstThread = null; //Communication Destination: THREAD
     etc.CommDstTimeLogical = null; //Communication Destination: tLogical
     etc.CommDstTimePhysical = null; //Communication Destination: tPhysical

     } else if ((send & 0xFFFF) == 1) {
     // Send
     remoteMachineIp = CommonFuncs.ipportParseIp(partner);
     remoteMachinePort = CommonFuncs.ipportParsePort(partner);
     localMachineIp = CommonFuncs.ipportParseIp(local);
     localMachinePort = CommonFuncs.ipportParsePort(local);
     etc.CommDstProcess = partner;
     etc.CommSrcProcess = local;
     //        this.CommSrcCpu = splitted[1]; //Communication Source: CPU
     //        this.CommSrcApplication = splitted[2]; //Communication Source: APPLICATION
     //        this.CommSrcProcess = splitted[3]; //Communication Source: PROCESS
     //        this.CommSrcThread = splitted[4]; //Communication Source: PROCESS
     //        this.CommSrcTimeLogical = splitted[5]; //Communication Source: THREAD
     //        this.CommSrcTimePhysical = splitted[6];
     pid = (int) DataOnMemory.getPid(local);
     }
     //        if (send.equals("2")) {
     //            // Ambiguous
     //            remoteMachineIp = CommonFuncs.ipportParseIp(partner);
     //            remoteMachinePort = CommonFuncs.ipportParsePort(partner);
     //            localMachineIp = CommonFuncs.ipportParseIp(local);
     //            localMachinePort = CommonFuncs.ipportParsePort(local);
     //            pid = (int) DataOnMemory.getPid(local);
     //        } else 

     return etc;
     }
     */
}
