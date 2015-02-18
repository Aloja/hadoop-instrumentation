package es.bsc.tools.undef2prv;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Set;

/**
 *
 * @author smendoza
 */
public class RecordComm {

    public static final String REC_TYPE_COMM = "3";
    public String RecordType = null; //Record Type: (STATE/EVENT/COMMUNICATION)
    public String CommSrcCpu = null; //Communication Source: CPU
    public String CommSrcApplication = null; //Communication Source: APPLICATION
    public String CommSrcProcess = null; //Communication Source: PROCESS
    public String CommSrcThread = null; //Communication Source: PROCESS
    public String CommSrcTimeLogical = null; //Communication Source: THREAD
    public String CommSrcTimePhysical = null;
    public String CommDstCpu = null; //Communication Source: CPU
    public String CommDstApplication = null; //Communication Destination: APPLICATION
    public String CommDstProcess = null; //Communication Destination: PROCESS
    public String CommDstThread = null; //Communication Destination: THREAD
    public String CommDstTimeLogical = null; //Communication Destination: tLogical
    public String CommDstTimePhysical = null; //Communication Destination: tPhysical
    public String CommSize = null; //Communication size
    public String CommTag = null; //Communication Tag
    public static final String DEFAULT_TAG = "1";
    static final Comparator<RecordComm> TEMPORAL_SORT =
            new Comparator<RecordComm>() {
                @Override
                public int compare(RecordComm e1, RecordComm e2) {
//                    Long srcTime = Long.parseLong(e1.CommSrcTimeLogical);
//                    Long dstTime = Long.parseLong(e2.CommSrcTimeLogical);
                    
                    BigInteger srcTime = new BigInteger(e1.CommSrcTimeLogical);
                    BigInteger dstTime = new BigInteger(e2.CommSrcTimeLogical);
                    
                    return srcTime.compareTo(dstTime); //{-1,0,1} si {<,=,>} respecticamente
                }
            };

    //    et->record_type = "3"; //Record Type: (STATE/EVENT/COMMUNICATION)
    //    et->comm_src_cpu = "0"; //Communication Source: CPU
    //    et->comm_src_app = "1"; //Communication Source: APPLICATION
    //    et->comm_src_process = "1"; //Communication Source: PROCESS
    //    et->comm_src_thread = "1"; //Communication Source: THREAD
    //    et->comm_src_time_log_sendt = total_msec; //Communication Source: Time logical sendt
    //    et->comm_src_time_phy_sendt = total_msec; //Communication Source: Time physical sendt
    //    et->comm_dst_cpu = ""; //Communication Dest: CPU
    //    et->comm_dst_app = ""; //Communication Dest: APPLICATION
    //    et->comm_dst_process = ""; //Communication Dest: PROCESS
    //    et->comm_dst_thread = ""; //Communication Dest: THREAD
    //    et->comm_dst_time_log_received = ""; //Communication Dest: Time logical received
    //    et->comm_dst_time_phy_received = ""; //Communication Dest: Time physical received
    //    et->size = "";
    //    et->tag = "";
    public RecordComm() {
        this.CommTag = RecordComm.DEFAULT_TAG;
    }

    public void setTimeOffset(Long offset_src, Long offset_dst) {
        if (offset_src != null) {
            this.CommSrcTimeLogical = String.valueOf(Long.parseLong(this.CommSrcTimeLogical) + offset_src);
            this.CommSrcTimePhysical = String.valueOf(Long.parseLong(this.CommSrcTimePhysical) + offset_src);
        }
        if (offset_dst != null) {
            this.CommDstTimeLogical = String.valueOf(Long.parseLong(this.CommDstTimeLogical) + offset_dst);
            this.CommDstTimePhysical = String.valueOf(Long.parseLong(this.CommDstTimePhysical) + offset_dst);
        }
    }

    public RecordComm(String line) {
        String[] splitted = line.split("\\:");

        this.RecordType = splitted[0];
        this.CommSrcCpu = splitted[1]; //Communication Source: CPU
        this.CommSrcApplication = splitted[2]; //Communication Source: APPLICATION
        this.CommSrcProcess = splitted[3]; //Communication Source: PROCESS
        this.CommSrcThread = splitted[4]; //Communication Source: PROCESS
        this.CommSrcTimeLogical = splitted[5]; //Communication Source: THREAD
        this.CommSrcTimePhysical = splitted[6];
        this.CommDstCpu = splitted[7]; //Communication Source: CPU
        this.CommDstApplication = splitted[8]; //Communication Destination: APPLICATION
        this.CommDstProcess = splitted[9]; //Communication Destination: PROCESS
        this.CommDstThread = splitted[10]; //Communication Destination: THREAD
        this.CommDstTimeLogical = splitted[11]; //Communication Destination: tLogical
        this.CommDstTimePhysical = splitted[12]; //Communication Destination: tPhysical
        this.CommSize = splitted[13]; //Communication size
        this.CommTag = splitted[14];

    }

    public String toString() {
	StringBuffer output = new StringBuffer();
	output.append("this.RecordType->" + this.RecordType);
        output.append("\nthis.CommSrcCpu->" + this.CommSrcCpu);
        output.append("\nthis.CommSrcApplication->" + this.CommSrcApplication);
        output.append("\nthis.CommSrcProcess->" + this.CommSrcProcess);
        output.append("\nthis.CommSrcThread->" + this.CommSrcThread);
        output.append("\nthis.CommSrcTimeLogical->" + this.CommSrcTimeLogical);
        output.append("\nthis.CommSrcTimePhysical->" + this.CommSrcTimePhysical);
        output.append("\nthis.CommDstCpu->" + this.CommDstCpu);
        output.append("\nthis.CommDstApplication->" + this.CommDstApplication);
        output.append("\nthis.CommDstProcess->" + this.CommDstProcess);
        output.append("\nthis.CommDstThread->" + this.CommDstThread);
        output.append("\nthis.CommDstTimeLogical->" + this.CommDstTimeLogical);
        output.append("\nthis.CommDstTimePhysical->" + this.CommDstTimePhysical);
        output.append("\nthis.CommSize->" + this.CommSize);
        output.append("\nthis.CommTag->" + this.CommTag);
        output.append("\n");
	return output.toString();
    }

    public void printAll() {

        System.out.println("this.RecordType->" + this.RecordType);
        System.out.println("this.CommSrcCpu->" + this.CommSrcCpu);
        System.out.println("this.CommSrcApplication->" + this.CommSrcApplication);
        System.out.println("this.CommSrcProcess->" + this.CommSrcProcess);
        System.out.println("this.CommSrcThread->" + this.CommSrcThread);
        System.out.println("this.CommSrcTimeLogical->" + this.CommSrcTimeLogical);
        System.out.println("this.CommSrcTimePhysical->" + this.CommSrcTimePhysical);
        System.out.println("this.CommDstCpu->" + this.CommDstCpu);
        System.out.println("this.CommDstApplication->" + this.CommDstApplication);
        System.out.println("this.CommDstProcess->" + this.CommDstProcess);
        System.out.println("this.CommDstThread->" + this.CommDstThread);
        System.out.println("this.CommDstTimeLogical->" + this.CommDstTimeLogical);
        System.out.println("this.CommDstTimePhysical->" + this.CommDstTimePhysical);
        System.out.println("this.CommSize->" + this.CommSize);
        System.out.println("this.CommTag->" + this.CommTag);

    }

    public String toStringParaverFormat() {
        return this.toStringParaverFormat(false);
    }

    /**
     * If convert is false, the original values of cpu, app, process and thread will be returned.
     * If convert is true, these values will be translated to our custom Paraver Resource model.
     */
    public String toStringParaverFormat(Boolean convert) {
        String retval = "";

        String[] vars = {
            this.RecordType,
            this.CommSrcCpu,
            this.CommSrcApplication,
            this.CommSrcProcess,
            this.CommSrcThread,
            this.CommSrcTimeLogical,
            this.CommSrcTimePhysical,
            this.CommDstCpu,
            this.CommDstApplication,
            this.CommDstProcess,
            this.CommDstThread,
            this.CommDstTimeLogical,
            this.CommDstTimePhysical,
            this.CommSize,
            this.CommTag};

        if (convert) {
            ParaverResource prvres_src = DataOnMemory.ntask_to_paraver_resource.get(this.CommSrcApplication);
            ParaverResource prvres_dst = DataOnMemory.ntask_to_paraver_resource.get(this.CommDstApplication);
            if (prvres_src == null || prvres_dst == null) {
                Undef2prv.logger.debug("RecordComm.toStringParaverFormat FAILED TO CONVERT, prvres_src=" + prvres_src + " prvres_dst=" + prvres_dst + " ORIGINAL STRING: " + this.toStringParaverFormat());
                return "";
            }
            vars[1] = prvres_src.cpu;
            vars[2] = prvres_src.app;
            vars[3] = prvres_src.task;
            vars[4] = prvres_src.thread;
            vars[7] = prvres_dst.cpu;
            vars[8] = prvres_dst.app;
            vars[9] = prvres_dst.task;
            vars[10] = prvres_dst.thread;
        }

        String obligatory = CommonFuncs.join(vars, ":");

        retval = String.format("%s", obligatory);

        return retval;
    }

    /**
     * Returns whether this communication is local (from/to the same node)
     */
    public Boolean isLocal() {
        ParaverResource prvres_src = DataOnMemory.ntask_to_paraver_resource.get(this.CommSrcApplication);
        ParaverResource prvres_dst = DataOnMemory.ntask_to_paraver_resource.get(this.CommDstApplication);
        if (prvres_src == null || prvres_dst == null) return null;
        return (
            prvres_src.cpu.equals(prvres_dst.cpu)
        );
    }
}
