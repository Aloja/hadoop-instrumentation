package es.bsc.tools.extrae;


public class Test
{

    	public static void main(String[] args){
		
		(new Test()).doStuff();

	}

	public void doStuff() {
		

		System.out.println("Hi!");

		int types[] = {1,2,3,4,5};
		long values[] = {11,22,33,44,55};

	//	es.bsc.tools.extrae.IDManager.registerJobTracker();
		//es.bsc.tools.extrae.IDManager.registerTaskTracker();
		//es.bsc.tools.extrae.IDManager.registerTask();
		//String dump = es.bsc.tools.extrae.IDManager.dump();
		//System.out.println(dump);
		//es.bsc.tools.extrae.Events.GenerateEvent(272727,727272727);
		es.bsc.tools.extrae.Events.GenerateNEvents(types, values);
		//es.bsc.tools.extrae.Events.GenerateEvent(es.bsc.tools.extrae.Events.Types.TaskTracker, es.bsc.tools.extrae.Events.Values.Start);
		//es.bsc.tools.extrae.Events.GenerateSendEvent(10, 100, es.bsc.tools.extrae.IDManager.getJobTrackerID(), 12);
		//es.bsc.tools.extrae.Events.GenerateReceiveEvent(10, 100, es.bsc.tools.extrae.IDManager.getJobTrackerID(), 12);
		//es.bsc.tools.extrae.Wrapper.Fini();

		System.out.println("Bye!");

	}

}

