package es.bsc.tools.undef2prv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author smendoza
 */
public class FileNetDumping {

//    public HashMap<String, String> ipport_pid = new HashMap<>();
    public HashMap<String, String> ipport_pid = new HashMap<>();
    public HashMap<String, Set<String>> ipport_pids = new HashMap<>();
    public HashMap<String, String> pid_ip = new HashMap<>();
    public HashMap<String, String> pid_port = new HashMap<>();
    public HashMap<String, Set<String>> ip_pids = new HashMap<>(); // pids for each node
//    PID:IP:PORT
    protected static final int position_pid = 0;
    protected static final int position_ip = 1;
    protected static final int position_port = 2;

    class FNDLine {

        String pid;
        String ip;
        String port;
    }

    public FileNetDumping() {
    }

    public void loadOnMemoryDumpfile(String filePath) throws FileNotFoundException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                loadLine(line);
            }
        } catch (IOException ex) {
            //LOG
            Undef2prv.logger.error("Exception reading the dumpFile" + filePath);
        }
    }

    //line example: pid:ip:port
    public void loadLine(String line) {
        String[] split_str = line.split("\\:");

        //TODO: if match re "number:number:number\n" then add, else don't add
        try {
            if (split_str.length != 3) {
                Undef2prv.logger.error("Hay una linea que no tiene 3 parametros!");
                return; //linea no procesable, paso a la siguiente
            }
            String ip = split_str[FileNetDumping.position_ip];
            String port = split_str[FileNetDumping.position_port];
            String pid = split_str[FileNetDumping.position_pid];

            //check valid port y pid
            try {
                Integer.parseInt(port);
                Integer.parseInt(pid);
            } catch (Exception e) {
                Undef2prv.logger.error("port[" + port + "] o pid[" + pid + "] no numeric al dumpfile");
                return;
            }
            //check si la ip es valida
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return; //linea no procesable, paso a la siguiente
            }

//            En distribuido, no se tricks con esto
//            ip = "172.20.0.16"; //TRICK 

            // line is in this format XXX.XXX.XXX.XXX
            // and want integer
            ip = CommonFuncs.ipToStrIp(ip);

            FNDLine fndl = new FNDLine();
            fndl.ip = ip;
            fndl.port = port;
            fndl.pid = pid;

            String keyipport = FileNetDumping.HMKeyGen(ip, port);

            if (this.ipport_pid.get(FileNetDumping.HMKeyGen(ip, port)) == null) {
                this.ipport_pid.put(FileNetDumping.HMKeyGen(ip, port), pid);
                Undef2prv.logger.debug("LOADING_FND.ipport_pid[" + ip + ":" + port + "]->PID[" + pid + "]");
            } else {
                Undef2prv.logger.debug(" != NULL LOADING_FND.ipport_pid[" + ip + ":" + port + "]->PID[" + pid + "]");
            }


            if (this.ipport_pids.get(keyipport) == null) {
                this.ipport_pids.put(keyipport, new HashSet<String>());
            }
            this.ipport_pids.get(keyipport).add(pid);

            this.pid_ip.put(pid, ip);
            this.pid_port.put(pid, port);
            if (this.ip_pids.get(ip) == null) {
                this.ip_pids.put(ip, new HashSet<String>());
            }
            this.ip_pids.get(ip).add(pid);

            //<MEJORA PARA TENER TODOS LOS PUERTOS USADOS POR UN PID/DAEMON!>
            try {
                Node n = DataOnMemory.hcluster.nodes.get(ip); //get node with this ip

                if (n == null) {
                    Undef2prv.logger.error("ANY NODE WITH ip=" + ip);
                } else if (n.daemons == null) {
                    Undef2prv.logger.error("ANY DAEMON AT NODE(ip=" + ip + ")");
                } else {
                    Daemon d = n.daemons.get(pid); //get daemon with this pid from node specified
                    if (d != null) {
                        d.ports.add(port);
                    } else {
                        Undef2prv.logger.error("ANY DAEMON AT NODE(ip=" + ip + ") with pid=" + pid);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Undef2prv.logger.error("ERROR parsing line (adding port do daemon): " + line);
                Undef2prv.logger.error("ERROR info: expected [Node] with ip=" + ip);
                Undef2prv.logger.error("ERROR info: expected [Daemon] pid=" + pid);
                System.out.println("ERROR parsing line: " + line);
            }
            // </MEJORA PARA TENER TODOS LOS PUERTOS USADOS POR UN PID/DAEMON!>

        } catch (Exception e) {
            //LOG
            Undef2prv.logger.error("ERROR Reading the line: " + line);
            e.printStackTrace();
        }
    }

    public Object getPid(String ip, String port) {

        //consultar la tabla mapped el pid de esa ip+puerto
        return ipport_pid.get(FileNetDumping.HMKeyGen(ip, port));
    }

    public Object getPort(String pid) {

        //consultar la tabla mapped el pid de esa ip+puerto
        return pid_port.get(pid);
    }

    public Object getIp(String pid) {

        //consultar la tabla mapped el pid de esa ip+puerto
        return pid_ip.get(pid);
    }

    public static String HMKeyGen(String ip, String port) {
        return ip + ":" + port;
    }

    public void printIpportPid() {
        Set<String> keys = this.ipport_pid.keySet();
        for (String k : keys) {
            //LOG
            Undef2prv.logger.debug("ipport_pid[" + k + "]->" + ipport_pid.get(k));
        }
    }

    public void printPidIp() {
        Set<String> keys = this.pid_ip.keySet();
        for (String k : keys) {
            Undef2prv.logger.debug("pid_ip[" + k + "]->" + pid_ip.get(k));
        }
    }

    public void printPidPort() {
        Set<String> keys = this.pid_port.keySet();
        for (String k : keys) {
            Undef2prv.logger.debug("pid_port[" + k + "]->" + pid_port.get(k));
        }
    }

    public void printAll() {
        this.printIpportPid();
        this.printPidIp();
        this.printPidPort();
    }
}
