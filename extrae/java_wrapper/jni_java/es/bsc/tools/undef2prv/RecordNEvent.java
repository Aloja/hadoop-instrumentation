package es.bsc.tools.undef2prv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author smendoza
 */
public class RecordNEvent {

    public String RecordType; //Record Type: (STATE/EVENT/COMMUNICATION)
    public String Cpu; //Communication Source: CPU
    public String Application; //Communication Source: APPLICATION
    public String Process; //Communication Source: PROCESS
    public String Thread; //Communication Source: THREAD
    public String EventTime; //Event Time
    public String OriginalApplication;
    public HashMap<String, String> RecordEventsHM = new HashMap<>();
    public static final String KEY_LOCAL_IP_sin_addr = "77770";
    public static final String KEY_LOCAL_PORT = "77771";
    public static final String KEY_REMOTE_IP_sin_addr = "77772";
    public static final String KEY_REMOTE_PORT = "77773";
    public static final String KEY_RAW_PCKT_LEN = "77774";
    public static final String KEY_APP_PAYLOAD_LEN = "77775";
    public static final String KEY_NUM_SEQ = "77776";
    public static final String KEY_NUM_ACK = "77777";
    public static final String KEY_FLAGS = "77778";
    public static final String KEY_SEND = "77779";
    public static final String KEY_TIME_PCAP = "77780";
    public static final String KEY_TIME_EVENT = "77781";
    public static final String KEY_NODE_IP_sin_addr = "88880";
    public static final String KEY_NODE_TYPE = "88881";
    public static final String KEY_NODE_DAEMON_PID = "88882";
    public static final String KEY_NODE_SYNC = "88883";
    private static final int FLAG_SYN = 0x02;
    private static final int FLAG_FIN = 0x01;
    private static final int FLAG_ACK = 0x10;

    public RecordNEvent() {
    }

    public RecordNEvent(String line) {

//2:5:1:881:1:4290409779888766:77770:8181922547240009729:77771:8181922547508491913:77772:8181922547508495184:77773:8181922547240009796:77774:8181922547240009728
        String[] splitted = line.split("\\:");

        this.RecordType = splitted[0];
        this.Cpu = splitted[1];
        this.Application = splitted[2];
        this.OriginalApplication = splitted[2];
        this.Process = splitted[3];
        this.Thread = splitted[4];
        this.EventTime = splitted[5];

        for (int i = 6; i < splitted.length; i = i + 2) {
            this.RecordEventsHM.put(splitted[i], splitted[i + 1]);
        }
    }

    public void setTimeOffset(Long offset) {
        if (offset == null) return;

        this.EventTime = String.valueOf(Long.parseLong(this.EventTime) + offset);

        // State records change the end_time too
        if (this.RecordType.equals("1")) {
            // I have to do this shitty workaround because states are saved
            // like user events, so the end_time is saved as a key of an event
            String key = this.RecordEventsHM.keySet().iterator().next();
            String value = this.RecordEventsHM.remove(key);
            key = String.valueOf(Long.parseLong(key) + offset);
            this.RecordEventsHM.put(key, value);
        }
    }

    public boolean isSend() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_LOCAL_IP_sin_addr);
        Long send = (Long.parseLong(val) & 0xFFFF);
        if (send == 0) {
            return true;
        } else {
            return false;
        }
    }

    //NOT US
    public boolean isRcv() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_LOCAL_IP_sin_addr);
        Long send = (Long.parseLong(val) & 0xFFFF);
        if (send == 1) {
            return true;
        } else {
            return false;
        }
    }

    public int getSendRcv() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_LOCAL_IP_sin_addr);
        Long send = (Long.parseLong(val) & 0xFFFF);
        return send.intValue();
    }

    public String getTime() {
        //send/receive cannot be known, one nEvent for each
        return this.EventTime;
    }

    public String getSizeApp() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_APP_PAYLOAD_LEN);
//        return (int) Integer.parseInt(val);
        return val;
    }

    public String getSizeRawPcket() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_RAW_PCKT_LEN);
//        return (int) Integer.parseInt(val);
        return val;
    }

    public String getTcpNSeq() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_NUM_SEQ);
//        Long l = Long.parseLong(val);
//        Long nseq = (l >> 32) & 0xFFFF;
//        int retval = nseq.intValue();
        return val;
    }

//    public String getLocal() {
//        String val = RecordEventsHM.get(RecordNEvent.KEY_LOCAL_IP_sin_addr);
////        int retval = (int) Integer.parseInt(val);
//        return val;
//    }
//
//    public String getPartner() {
//        String val = RecordEventsHM.get(RecordNEvent.KEY_REMOTE_IP_sin_addr);
////        int retval = (int) Integer.parseInt(val);
//        return val;
//    }
    public String getSrcIp() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_LOCAL_IP_sin_addr);
//        Long local = (Long.parseLong(val) >> 32) & 0xFFFF;
//        return CommonFuncs.ipportParseIpStr(local.toString());
        return val;
    }

    public String getSrcPort() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_LOCAL_PORT);
//        Long longport = Long.parseLong(val) & 0xFFFF;
//        return CommonFuncs.ipportParsePortStr(longport.toString());
        return val;
    }

    public String getDstIp() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_REMOTE_IP_sin_addr);
//        Long local = (Long.parseLong(val) >> 32) & 0xFFFF;
//        return CommonFuncs.ipportParseIpStr(local.toString());
        return val;
    }

    public String getDstPort() {
        String val = RecordEventsHM.get(RecordNEvent.KEY_REMOTE_PORT);
//        Long longport = Long.parseLong(val) & 0xFFFF;
//        return CommonFuncs.ipportParsePortStr(longport.toString());
        return val;
    }

    public String getNAck() {
        return RecordEventsHM.get(RecordNEvent.KEY_NUM_ACK);
    }

    public String getFlags() {
        return RecordEventsHM.get(RecordNEvent.KEY_FLAGS);
    }

    public Boolean getSend() {
        return RecordEventsHM.get(RecordNEvent.KEY_SEND).equals("1");
    }

    public String getNodeIp() {
        return RecordEventsHM.get(RecordNEvent.KEY_NODE_IP_sin_addr);
    }

    public String getNodeType() {
        return RecordEventsHM.get(RecordNEvent.KEY_NODE_TYPE);
    }

    public String getTimestamp() {
        return RecordEventsHM.get(RecordNEvent.KEY_NODE_SYNC);
    }

    public Long getTimePcap() {
        return Long.parseLong(RecordEventsHM.get(RecordNEvent.KEY_TIME_PCAP));
    }

    public Long getTimeEvent() {
        return Long.parseLong(RecordEventsHM.get(RecordNEvent.KEY_TIME_EVENT));
    }

    /**
     * Returns the tsc time of this event taking into account the delay between
     * the packet capture and the extrae event of this packet.
     */
    public String getTimeNetworkSynced() {
        Long tsc = Long.parseLong(this.getTime());
        Long diff = (this.getTimeEvent() - this.getTimePcap()) * 1000L;
        return Long.toString(tsc-diff);
    }

    public boolean isSyn() {
        boolean retval = false;

        if ((getFlags() != null)) {
            int flags = Integer.parseInt(getFlags());
            if ((RecordNEvent.FLAG_SYN & flags) != 0) {
                retval = true;
            }
        }
        return retval;
    }

    public boolean isFin() {
        boolean retval = false;

        if ((getFlags() != null)) {
            int flags = Integer.parseInt(getFlags());
            if ((RecordNEvent.FLAG_FIN & flags) != 0) {
                retval = true;
            }
        }
        return retval;
    }

    public boolean isAck() {
        boolean retval = false;

        if ((getFlags() != null)) {
            int flags = Integer.parseInt(getFlags());
            if ((RecordNEvent.FLAG_ACK & flags) != 0) {
                retval = true;
            }
        }
        return retval;
    }

    public String HMKeyGen(String ip, String port) {
        return ip + ":" + port;
    }

    @Override
    public String toString() {

        String str = "";

        //LOG
        str += ("this.RecordType->" + this.RecordType);
        str += ("\nthis.Cpu->" + this.Cpu);
        str += ("\nthis.Application->" + this.Application);
        str += ("\nthis.Process->" + this.Process);
        str += ("\nthis.Thread->" + this.Thread);
        str += ("\nthis.EventTime->" + this.EventTime);
        Set<String> keys = this.RecordEventsHM.keySet();
        for (String k : keys) {
            str += ("\nkey_value[" + k + "]->" + RecordEventsHM.get(k));
        }
        return str;
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

        ArrayList<String> vars = new ArrayList<>();
        vars.add(this.RecordType);

        if (!convert) {
            vars.add(this.Cpu);
            vars.add(this.Application);
            vars.add(this.Process);
            vars.add(this.Thread);
        } else {
            ParaverResource prvres = DataOnMemory.app_to_paraver_resource.get(this.OriginalApplication);
            if (prvres == null) {
                Undef2prv.logger.debug("RecordNEvent.toStringParaverFormat FAILED TO CONVERT, ORIGINAL STRING: " + this.toStringParaverFormat());
                return "";
            }
            vars.add(prvres.cpu);
            vars.add(prvres.app);
            vars.add(prvres.task);
            vars.add(prvres.thread);
        }

        vars.add(this.EventTime);

        for (String k : RecordEventsHM.keySet()) {
            vars.add(k);
            vars.add(RecordEventsHM.get(k));
        }

        retval = CommonFuncs.join(vars.toArray(new String[vars.size()]), ":");

        return retval;
    }

    public boolean myEquals(RecordNEvent ner) {
        boolean retval = false;

        String a = this.getFlags();
        String b = ner.getFlags();
        boolean c1 = false;
        if (a != null && b != null) {
            c1 = a.equals(b);
        } else {
            c1 = true;
        }

        String c = this.getTcpNSeq();
        String d = ner.getTcpNSeq();
        boolean c2 = false;
        if (c != null && d != null) {
            c2 = c.equals(d);
        } else {
            c2 = true;
        }

        String e = this.getNAck();
        String f = ner.getNAck();
        boolean c3 = false;
        if (e != null && f != null) {
            c3 = e.equals(f);
        } else {
            c3 = true;
        }

        String g = this.getSizeRawPcket();
        String h = ner.getSizeRawPcket();
        boolean c4 = false;
        if (g != null && h != null) {
            c4 = g.equals(h);
        } else {
            c4 = true;
        }

        String i = this.getSizeApp();
        String j = ner.getSizeApp();
        boolean c5 = false;
        if (i != null && j != null) {
            c5 = i.equals(j);
        } else {
            c5 = true;
        }

        // Compare inverse src/dest ip&port
        String k = this.getSrcIp();
        String l = ner.getDstIp();
        boolean c6 = false;
        if (k != null && l != null) {
            c6 = k.equals(l);
        } else {
            c6 = true;
        }
        String m = this.getDstIp();
        String n = ner.getSrcIp();
        boolean c7 = false;
        if (m != null && n != null) {
            c7 = m.equals(n);
        } else {
            c7 = true;
        }

        String o = this.getSrcPort();
        String p = ner.getDstPort();
        boolean c8 = false;
        if (o != null && p != null) {
            c8 = o.equals(p);
        } else {
            c8 = true;
        }
        String q = this.getDstPort();
        String r = ner.getSrcPort();
        boolean c9 = false;
        if (q != null && r != null) {
            c9 = q.equals(r);
        } else {
            c9 = true;
        }

        Boolean s = this.getSend();
        Boolean t = ner.getSend();
        boolean c10 = false;
        if (s != null && t != null) {
            c10 = (s != t);
        } else {
            c10 = true;
        }

        if (c1 && c2 && c3 && c4 && c5 && c6 && c7 && c8 && c9 && c10) {
            retval = true;
        }

        return retval;
    }

    public boolean isLoopback() {

        String iploopback = "16777343"; //127.0.0.1
        boolean retval = false;

        if (this.getDstIp() != null && this.getSrcIp() != null) {
            if (this.getDstIp().equals(iploopback) || this.getSrcIp().equals(iploopback)) {
                retval = true;
//            Undef2prv.logger.debug("SI Loopback? this.getDstIp()=" + this.getDstIp() + ", this.getSrcIp()=" + this.getSrcIp());
            } else {
                retval = false;
//            Undef2prv.logger.debug("NO Loopback? this.getDstIp()=" + this.getDstIp() + ", this.getSrcIp()=" + this.getSrcIp());
            }
        } else {
            Undef2prv.logger.error("ERROR analizando si es Loopback? this.getDstIp()=" + this.getDstIp() + ", this.getSrcIp()=" + this.getSrcIp());
        }

        return retval;
    }
}
