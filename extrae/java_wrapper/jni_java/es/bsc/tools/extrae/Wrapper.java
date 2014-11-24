package es.bsc.tools.extrae;


public class Wrapper
{

    static {

        //System.loadLibrary("pttrace");
        System.loadLibrary("seqtrace");
        System.loadLibrary("jextrae");
        //System.load("/home/smendoza/lightness/hadoop-apps/libpcap-1.4.0-dist/lib/libpcap.so.1.4.0");
        //System.load("/home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist/lib/libseqtrace-2.5.1.so");
        //System.load("/home/smendoza/lightness/lib/libjextrae.so");        
        //System.out.println("getEnv().keys="+System.getenv().keySet()+"\ngetenv().values="+System.getenv().values());
	//System.out.println("Wrapper static");
	//Init();
    }


    public static native void Init();
    public static native void Fini();
    public static native int GetTaskId();
    public static native int GetPID();
    public static native void Event(int id, long val);
    public static native void nEvent(int types[], long values[]);
    public static native void Comm(boolean send, int tag, int size, int partner, long id);
    public static native void SetOptions(int options);
    public static native void Pause();
    public static native void Resume();
    public static native void SetID(int id);
    public static native void SetNumTasks(int num);
    public static native int GetID();
    public static native int GetNumTasks(); 
    public static native void PushIDsDown(String name, int id);
    public static native void StartSnifferLowLevel(boolean inbound, int ports[]);
    public static native void StartPortMapperLowLevel(final int ports[]);// adding ports, S
    public static void StartSniffer(final int ports[]) {
	Thread t_in = new Thread() {
    		public void run() {
        		StartSnifferLowLevel(true, ports);// adding ports, S
    		}
	};
	/*Thread t_out = new Thread() {
    		public void run() {
        		StartSnifferLowLevel(false);
    		}
	};*/
	t_in.start();	
	//t_out.start();	
    }
    public static void StartPortMapper(final int ports[]) {
	Thread t = new Thread() {
    		public void run() {
        		StartPortMapperLowLevel(ports);
    		}
	};
	t.start();	
    }



}

