package es.bsc.tools.undef2prv;

import java.util.ArrayList;

class Sysstat {

    public static final String CPU_USER_EVENT = "2004";
    public static final String CPU_NICE_EVENT = "2005";
    public static final String CPU_SYSTEM_EVENT = "2006";
    public static final String CPU_IOWAIT_EVENT = "2007";
    public static final String CPU_STEAL_EVENT = "2008";
    public static final String CPU_IDLE_EVENT = "2009";

    public String ip;
    public String timestamp;
    public String cpu_user;
    public String cpu_nice;
    public String cpu_system;
    public String cpu_iowait;
    public String cpu_steal;
    public String cpu_idle;

    public Sysstat(String line) {
        String[] splitted = line.split(";");

        this.ip = splitted[0];
        this.timestamp = splitted[2];
        this.cpu_user = splitted[4];
        this.cpu_nice = splitted[5];
        this.cpu_system = splitted[6];
        this.cpu_iowait = splitted[7];
        this.cpu_steal = splitted[8];
        this.cpu_idle = splitted[9];
    }

    public String toStringParaverFormat(int num_app) {
        String retval = "";

        ArrayList<String> vars = new ArrayList<String>();
        vars.add("2");  // Recordtype
        vars.add("1");  // Cpu
        vars.add(Integer.toString(num_app));  // Application
        vars.add("1");  // Process
        vars.add("1");  // Thread
        vars.add(this.timestamp);  // EventTime

        vars.add(CPU_USER_EVENT);
        vars.add(this.cpu_user);

        vars.add(CPU_NICE_EVENT);
        vars.add(this.cpu_nice);

        vars.add(CPU_SYSTEM_EVENT);
        vars.add(this.cpu_system);

        vars.add(CPU_IOWAIT_EVENT);
        vars.add(this.cpu_iowait);

        vars.add(CPU_STEAL_EVENT);
        vars.add(this.cpu_steal);

        vars.add(CPU_IDLE_EVENT);
        vars.add(this.cpu_idle);

        retval = CommonFuncs.join(vars.toArray(new String[vars.size()]), ":");

        return retval;
    }

}
