package es.bsc.tools.extrae;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Events {

    public static class Types {

        public static final int JobTracker = 11111;
        public static final int TaskTracker = 11112;
        public static final int NameNode = 11113;
        public static final int SecondaryNameNode = 11114;
        public static final int DataNode = 11115;
        public static final int MapTask = 11116;
        public static final int ReduceTask = 11117;
        public static final int MapOutputBuffer = 33333;
        public static final int MapTaskOutputSize = 44444;
    }

    public static class Values {

        public static final int Start = 1;
        public static final int End = 0;
        public static final int Flush = 1;
        public static final int SortAndSpill = 2;
        public static final int Sort = 3;
        public static final int Combine = 4;
        public static final int CreateSpillIndexFile = 5;
        public static final int TotalIndexCacheMemory = 6;
        public static final int SpillRecordDumped = 7;
        public static final int RunMapper = 8;
        public static final int RunReducer = 9;
        public static final int ReducerCopyPhase = 10;
        public static final int ReducerSortPhase = 11;
        public static final int ReducerReducePhase = 12;
    }

    public static class Tags {

        public static final int Packet = 1;
        public static final int HeartBeat = 2;
    }

    public static void GenerateEvent(int event, long value) {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println("Events.GenerateEvent > event=" + event + ", value=" + value + ", pid=" + pid);
        //<smendoza>
        //Wrapper.Event(event, value);
        //</smendoza>
        Events.GenerateNEvents(new int[]{event}, new long[]{value}); // como nevent puedo anyadir el pid
    }

    public static void GenerateNEvents(int types[], long values[]) {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        //Anyado el pid a estos events para poder post-procesarlos luego!
        //<smendoza>
        //long threadId = Thread.currentThread().getId();
        //System.out.println("Events.GenerateNEvents > pid=" + pid + ", thread=" + threadId);
        int[] t = new int[types.length + 1];
        long[] v = new long[values.length + 1];
        for (int i = 0; i < types.length; ++i) {
            t[i] = types[i];
            v[i] = values[i];
        }
        t[t.length - 1] = 99199;
        v[v.length - 1] = Long.parseLong(pid);
        //System.out.println("Events.GenerateNEvents>t=" + Arrays.toString(t) + ", v=" + Arrays.toString(v));
        //</smendoza>
        //Wrapper.nEvent(types, values);
        Wrapper.nEvent(t, v); //Genero el nevent con el pid en el campo 99199
    }

    public static void GenerateSendEvent(int tag, int size, int partner, long id) {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println("Events.GenerateSendEvent > pid=" + pid);
        Wrapper.Comm(true, tag, size, partner, id);

    }

    public static void GenerateReceiveEvent(int tag, int size, int partner, long id) {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println("Events.GenerateSendEvent > pid=" + pid);
        Wrapper.Comm(false, tag, size, partner, id);

    }
}
