package es.bsc.tools.undef2prv;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 *
 * @author smendoza
 */
public class CommonFuncs {

    public static int ipportParseIp(String str) {
        Long x = Long.parseLong(str);
        Long ip = x >> 32;

        return ip.intValue();
    }

    public static int ipportParsePort(String str) {
        Long x = Long.parseLong(str);
        Long port = 0xFFFF & x;

        return port.intValue();
    }

    public static String ipportParseIpStr(String str) {
        Long x = Long.parseLong(str);
        Long ip = x >> 32;

        return ip.toString();
    }

    public static String ipportParsePortStr(String str) {
        Long x = Long.parseLong(str);
        Long port = 0xFFFF & x;

        return port.toString();
    }

    public static int taskIdGenerator(int ip, int pid) {
        int taskid = 0;
// [1]  TASKID = (((ip & 0x0000FFFF) << 16) | (pid & 0x0000FFFF)) % 6000 ;  on ip es ((struct in_addr*)he->h_addr)->s_addr;

        taskid = ((((ip & 0x0000FFFF) << 16) | (pid & 0x0000FFFF)) % 6000) + 1;

        return taskid;
    }

    public static String join(String r[], String d) {
        if (r.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i < r.length - 1; i++) {
            sb.append(r[i] + d);
        }
        return sb.toString() + r[i];
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String ipToStrIp(String ip) {
        String str = null;
        try {
            InetAddress addr = InetAddress.getByName(ip);
            byte[] x = addr.getAddress();
//            byte[] y = {0, 0, 0, 0, x[0], x[1], x[2], x[3]};
            byte[] y = {0, 0, 0, 0, x[3], x[2], x[1], x[0]};
            ByteBuffer bb = ByteBuffer.wrap(y);
            Long lo = bb.getLong(0);
            str = lo.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * Converts the IP address from Integer to the human readable format 1.2.3.4
     */
    public static String ipIntToHuman(String ip) {
        byte[] bytes = BigInteger.valueOf(Long.parseLong(ip)).toByteArray();
        String result = null;
        try {
            result = InetAddress.getByAddress(bytes).getHostAddress();
        } catch (UnknownHostException e) {}
        return result;
    }

}
