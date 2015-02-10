package es.bsc.tools.undef2prv;

import java.util.ArrayList;

class Sysstat {

    public static final String CPU_USER_EVENT = "2004";
    public static final String CPU_NICE_EVENT = "2005";
    public static final String CPU_SYSTEM_EVENT = "2006";
    public static final String CPU_IOWAIT_EVENT = "2007";
    public static final String CPU_STEAL_EVENT = "2008";
    public static final String CPU_IDLE_EVENT = "2009";
    public static final String PAGE_FAULTS_EVENT = "2012";
    public static final String MAJOR_PAGE_FAULTS_EVENT = "2013";
    public static final String PAGES_FREED_EVENT = "2014";
    public static final String MEM_KB_FREE_EVENT = "2019";
    public static final String MEM_KB_USED_EVENT = "2020";
    public static final String MEM_USED_EVENT = "2021";
    public static final String SYSTEM_LOAD_1_EVENT = "2031";
    public static final String SYSTEM_LOAD_5_EVENT = "2032";
    public static final String SYSTEM_LOAD_15_EVENT = "2033";

    public String ip;
    public Long timestamp;
    public String cpu_user;
    public String cpu_nice;
    public String cpu_system;
    public String cpu_iowait;
    public String cpu_steal;
    public String cpu_idle;
    public String page_faults;
    public String major_page_faults;
    public String pages_freed;
    public String mem_kb_free;
    public String mem_kb_used;
    public String mem_used;
    public String system_load_1;
    public String system_load_5;
    public String system_load_15;

    public Sysstat(String line) {
        String[] splitted = line.split(";");

        this.ip = CommonFuncs.ipHumanToInt(splitted[0]);
        this.timestamp = Long.valueOf(splitted[2]) * 1000000L;  // Converted to microseconds to match SyncInfo format
        this.cpu_user = splitted[4];
        this.cpu_nice = splitted[5];
        this.cpu_system = splitted[6];
        this.cpu_iowait = splitted[7];
        this.cpu_steal = splitted[8];
        this.cpu_idle = splitted[9];
        this.page_faults = splitted[12];
        this.major_page_faults = splitted[13];
        this.pages_freed = splitted[14];
        this.mem_kb_free = splitted[19];
        this.mem_kb_used = splitted[20];
        this.mem_used = splitted[21];
        this.system_load_1 = splitted[31];
        this.system_load_5 = splitted[32];
        this.system_load_15 = splitted[33];
    }

    public String toStringParaverFormat(int num_app) {
        String retval = "";

        ArrayList<String> vars = new ArrayList<String>();
        vars.add("2");  // Recordtype
        vars.add("1");  // Cpu
        vars.add(Integer.toString(num_app));  // Application
        vars.add("1");  // Process
        vars.add("1");  // Thread
        vars.add(this.timestamp.toString());  // EventTime

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

        vars.add(PAGE_FAULTS_EVENT);
        vars.add(this.page_faults);

        vars.add(MAJOR_PAGE_FAULTS_EVENT);
        vars.add(this.major_page_faults);

        vars.add(PAGES_FREED_EVENT);
        vars.add(this.pages_freed);

        vars.add(MEM_KB_FREE_EVENT);
        vars.add(this.mem_kb_free);

        vars.add(MEM_KB_USED_EVENT);
        vars.add(this.mem_kb_used);

        vars.add(MEM_USED_EVENT);
        vars.add(this.mem_used);

        vars.add(SYSTEM_LOAD_1_EVENT);
        vars.add(this.system_load_1);

        vars.add(SYSTEM_LOAD_5_EVENT);
        vars.add(this.system_load_5);

        vars.add(SYSTEM_LOAD_15_EVENT);
        vars.add(this.system_load_15);

        retval = CommonFuncs.join(vars.toArray(new String[vars.size()]), ":");

        return retval;
    }

}
