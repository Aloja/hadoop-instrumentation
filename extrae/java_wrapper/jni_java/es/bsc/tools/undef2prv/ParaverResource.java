package es.bsc.tools.undef2prv;

class ParaverResource {

    public static final String TYPE_STAT = "1";
    public static final String TYPE_DAEMON = "2";
    public static final String TYPE_TASK = "3";

    public String original_app;
    public String ntask;
    public String cpu;
    public String app;
    public String task;
    public String thread;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{original_app: " + original_app + ", ntask: " + ntask + ", cpu: " + cpu + ", app: " + app + ", task: " + task + ", thread: " + thread + "}";
    }

}
