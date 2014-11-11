package es.bsc.tools.undef2prv;

/**
 *
 * @author smendoza
 */
public class RecordEvent {

    public String RecordType; //Record Type: (STATE/EVENT/COMMUNICATION)
    public String Cpu; //Communication Source: CPU
    public String Application; //Communication Source: APPLICATION
    public String Process; //Communication Source: PROCESS
    public String Thread; //Communication Source: PROCESS
    public String EventTime; //Communication Source: THREAD
    public String EventType;
    public String EventValue;

    public RecordEvent() {
    }

    public RecordEvent(String line) {

        String[] splitted = line.split("\\:");

        this.RecordType = splitted[0];
        this.Cpu = splitted[1];
        this.Application = splitted[2];
        this.Process = splitted[3];
        this.Thread = splitted[4];
        this.EventTime = splitted[5];
        this.EventType = splitted[6];
        this.EventValue = splitted[7];
    }
}
