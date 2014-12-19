#include <es_bsc_tools_extrae_Wrapper.h>
#include <commons_bsc.h>
#include <extrae_user_events.h>
#include <extrae_types.h>
#include <stdlib.h>
#include <unistd.h>
#include <snifferreceiver.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>

/*
//3.5.1
int THREADID = 1; //original: 0
int NUMTHREADS = 1;
int TASKID = 0;
int NUMTASKS = 1;
static unsigned int get_thread_id(void)
{ return THREADID; }

static unsigned int get_num_threads(void)
{ return NUMTHREADS; }

static unsigned int get_task_id(void)
{ return TASKID; }

static unsigned int get_num_tasks(void)
{ return NUMTASKS; }
 */

//3.4
int TASKID = -1;
int NUMTHREADS = 1;
//int NUMTASKS = -1;
int NUMTASKS = 6000;
char clusterNames[MAX_NUM_TASKS][MAX_NAME_SIZE];
int clusterIDs[MAX_NUM_TASKS];

unsigned int get_thread_id(void);
unsigned int get_task_id(void);
unsigned int get_num_tasks(void);
void gen_task_id(void);

unsigned int get_num_threads(void) {
	return 1;
}

unsigned int get_thread_id(void) {
	printf("Calling get_thread_id callback\n");
	return 1;
}

unsigned int get_task_id(void) {
	printf("Calling get_task_id callback - returning %d\n", TASKID);
	return TASKID;
}

unsigned int get_num_tasks(void) {
	//printf("Calling get_num_tasks callback\n");
	return NUMTASKS;
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_Init(JNIEnv *env, jclass jc) {

	/*
	   char slaves[1000];
	   char order[1000];
	   char *name[4];
	   char buffer[1000];
	   FILE *fp;

	   sprintf(slaves,"wc -l %s/conf/slaves\n",getenv("HADOOP_PREFIX"));
	   sprintf(order, "grep -n `hostname` %s/conf/slaves  | cut -d \":\" -f1\n",getenv("HADOOP_PREFIX"));

	   fp = popen(slaves, "r");
	   if (fp == NULL) {
	   printf("Failed to run command\n" );
	   exit;
	   }

	   NUMTASKS=atoi(fgets(buffer, sizeof(buffer)-1, fp));

	   pclose(fp);
	   NUMTASKS += 3; // JT, NN1, NN2
	   printf("NUMTASKS: %d\n",NUMTASKS);	

	//name[2] = order;
	//TASKID = execvp("/bin/sh", name);
	fp = popen(order, "r");
	if (fp == NULL) {
	printf("Failed to run command\n" );
	exit;
	}

	TASKID=atoi(fgets(buffer, sizeof(buffer)-1, fp));
	pclose(fp);
	TASKID--;

	printf("TID: %d\n",TASKID);	
	 */

	printf("Wrapper.Init(): Empiezo un extrae!!\n");
	gen_task_id();

	Extrae_set_numthreads_function(get_num_threads);
	//Extrae_set_threadid_function(get_thread_id); //Problemas, porque?
	Extrae_set_taskid_function(get_task_id);
	Extrae_set_numtasks_function(get_num_tasks);
        if(!Extrae_is_initialized()){
		printf("Initializing Extrae by Wrapper.Init()...");
                Extrae_init();
        }else{
		printf("Extrae already Initialized when Wrapper.Init() called...");
	}
	fflush(stdout);
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_Fini(JNIEnv *env, jclass jc) {
	Extrae_fini();


}

JNIEXPORT jint JNICALL Java_es_bsc_tools_extrae_Wrapper_GetTaskId(JNIEnv *env, jclass jc) {
	return get_task_id();


}

JNIEXPORT jint JNICALL Java_es_bsc_tools_extrae_Wrapper_GetPID(JNIEnv *env, jclass jc) {
	return getpid();


}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_Event(JNIEnv *env, jclass jc, jint id, jlong val) {
	Extrae_event((extrae_type_t) id, (extrae_value_t) val);
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_Comm(JNIEnv *env, jclass jc, jboolean send, jint tag, jint size, jint partner, jlong id) {

	struct extrae_UserCommunication comm;
	struct extrae_CombinedEvents events;

	Extrae_init_UserCommunication(&comm);
	Extrae_init_CombinedEvents(&events);

	if (send)
		comm.type = EXTRAE_USER_SEND;
	else
		comm.type = EXTRAE_USER_RECV;

	comm.tag = tag;
	comm.size = size;
	comm.partner = partner;
	comm.id = id;

	events.nCommunications = 1;
	events.Communications = &comm;
	events.nEvents = 0;

	Extrae_emit_CombinedEvents(&events);

}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_SetOptions(JNIEnv *env, jclass jc, jint options) {
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_Pause(JNIEnv *env, jclass jc) {
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_Resume(JNIEnv *env, jclass jc) {
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_StartPortMapperLowLevel(JNIEnv *env, jclass jc, jintArray ports) {


	jint *portsArray = (*env)->GetIntArrayElements(env, ports, NULL);
	jint count = (*env)->GetArrayLength(env, ports);

	printf("Calling dumpingd\n");

	dumpingd(portsArray, count);

	(*env)->ReleaseIntArrayElements(env, ports, portsArray, JNI_ABORT);

}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_nEvent(JNIEnv *env, jclass jc, jintArray types, jlongArray values) {
	unsigned int i = 0;
	jint countTypes = 0;
	jint countValues = 0;

	/*
	   extrae_type_t typesArray2[4];
	   extrae_value_t valuesArray2[4];

	   int k = 0;
	   for(k=0; k < 4; k++) {
	   typesArray2[k] = 1;
	   valuesArray2[k] = 1;
	   }
	   Extrae_nevent(4, &typesArray2, &valuesArray2);
	 */

	//Iniciar Extrae si aun no se ha iniciado
	if(!Extrae_is_initialized()){
		printf("nevent() called with extrae not initialized");
		Extrae_init();
	}else{
                printf("Extrae already Initialized when Wrapper.nevent() called...");
	}

	jint *typesArray = (*env)->GetIntArrayElements(env, types, NULL);
	jlong *valuesArray = (*env)->GetLongArrayElements(env, values, NULL);
	if (typesArray == NULL || valuesArray == NULL) {
		return; // ERROR
	} else {
		countTypes = (*env)->GetArrayLength(env, types);
		countValues = (*env)->GetArrayLength(env, values);
		if (countTypes == countValues) {
			printf ("\nEL PROBLEMA.2 (types-length=%d, values-length=%d)\n",countTypes, countValues);
			Extrae_nevent(countTypes, (extrae_type_t *) typesArray, (extrae_value_t *) valuesArray);
			//printf("punteros? %x %x\n",&typesArray2, &valuesArray2);
			//Extrae_nevent(4, &typesArray2, &valuesArray2);
			printf ("EL PROBLEMA.3\n");

			for(i=0; i<countTypes; i++)
				printf("NEVENT[%d]=%d - %d ; ",  i, typesArray[i], valuesArray[i]);
			printf("\n");

		}
		printf ("EL PROBLEMA.4\n");
		// else ERROR
		(*env)->ReleaseIntArrayElements(env, types, typesArray, JNI_ABORT);
		(*env)->ReleaseLongArrayElements(env, values, valuesArray, JNI_ABORT);
		printf ("EL PROBLEMA.5\n");
	}
	fflush(stdout);
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_SetID(JNIEnv *env, jclass jc, jint id) {
	return;
}

JNIEXPORT jint JNICALL Java_es_bsc_tools_extrae_Wrapper_GetID(JNIEnv *env, jclass jc) {
	return TASKID;
}

JNIEXPORT jint JNICALL Java_es_bsc_tools_extrae_Wrapper_GetNumTasks(JNIEnv *env, jclass jc) {
	return NUMTASKS;
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_SetNumTasks(JNIEnv *env, jclass jc, jint numtasks) {
	NUMTASKS = numtasks;
}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_PushIDsDown(JNIEnv *env, jclass jc, jstring name, jint id) {
	/*const char *inCStr = (*env)->GetStringUTFChars(env, name, NULL);
	  if (NULL == inCStr)  { printf("Was NULL!!\n"); exit(-1); }
	  int len = (*env)->GetStringLength(env, name);
	  NUMTASKS++;
	  clusterIDs[NUMTASKS]=id;
	  strncpy(clusterNames[NUMTASKS], inCStr, len);
	  clusterNames[NUMTASKS][len]='\0';
	  printf("NUMTASKS: %d - clusterIDs[%d]: %d - clusterNames[%d]: %s\n",NUMTASKS, NUMTASKS,clusterIDs[NUMTASKS], NUMTASKS, clusterNames[NUMTASKS]);
	  (*env)->ReleaseStringUTFChars(env, name, inCStr);
	 */

}

JNIEXPORT void JNICALL Java_es_bsc_tools_extrae_Wrapper_StartSnifferLowLevel(JNIEnv *env, jclass jc, jboolean inbound, jintArray ports) {

	// adding ports, S
	jint *portsArray = (*env)->GetIntArrayElements(env, ports, NULL);
	//    jint count = (*env)->GetArrayLength(env, ports);
	jint count = (*env)->GetArrayLength(env, ports);

	char str_command[2000];
	char str_nports[128];
	char str_cmd[1000];
	int i=0;
	char port[6];
	/*
	   pid_t child_pid;
	   int child_status;
	//Start Extrae tracing at parent
	//printf("extrae_wrapper: Extrae_init()");
	//Extrae_init();
	child_pid = fork();
	if(child_pid == 0) {
	// This is done by the child process.
	if(inbound)
	sprintf(str_command,"/scratch/hdd/smendoza/bin/sniffer %d",1);
	else
	sprintf(str_command,"/scratch/hdd/smendoza/bin/sniffer %d",0);

	//strcpy(str_command, "/scratch/hdd/smendoza/bin/sniffer 1");
	for(;i<count;++i){
	sprintf(port, " %d", portsArray[i]);
	strncat(str_command,port,6);
	}
	sprintf(str_nports," %d",count);
	strncat(str_command,str_nports,3);
	printf("the-sniffer-command: %s",str_command);
	system(str_command);
	// If execv returns, it must have failed.
	printf("Unknown command\n");
	exit(0);
	//return;
	}
	else {
	pid_t tpid = NULL;
	// This is run by the parent.  Wait for the child to terminate.
	printf("parent que espera...\n");
	fflush(stdout);
	tpid = wait(&child_status);
	printf("parent que va a llamar al Extrae_fini()...\n");
	fflush(stdout);
	//Stop Extrae tracing at parent
	//Extrae_fini();
	//return child_status;
	}
	 */

	if (inbound){
		strcpy(str_command, getenv("SNIFFER_BIN"));
		strcat(str_command, " 1");
		for(;i<count;++i){
			sprintf(port, " %d", portsArray[i]);
			strncat(str_command,port,6);
		}
		sprintf(str_nports," %d",count);
		strncat(str_command,str_nports,3);
		printf("%s",str_command);
		//printf("before sniffer system() call");
		system(str_command);
		//printf("leave sniffer system() call");
		//sniffing(1, portsArray, count);
	}
	else {
		strcpy(str_command, getenv("SNIFFER_BIN"));
		strcat(str_command, " 0");
		for(;i<count;++i){
			sprintf(port, " %d", portsArray[i]);
			strncat(str_command,port,6);
		}
		sprintf(str_nports," %d",count);
		strncat(str_command,str_nports,3);
		printf("%s",str_command);

		system(str_command);
		//sniffing(0, portsArray, count);
	}

	(*env)->ReleaseIntArrayElements(env, ports, portsArray, JNI_ABORT);
	
	fflush(stdout);

	//	if(inbound)
	//		sniffing(1);
	//	else
	//		sniffing(0);
}

void gen_task_id(void) {
	unsigned int id = -1;
	char name[256];
	printf("entrado al gen_task_id()\n");

	gethostname(name, 256);
	struct hostent *he = gethostbyname(name);
	if (he == NULL)
		return;

	unsigned long ip = ((struct in_addr*) he->h_addr)->s_addr;
	unsigned int pid = getpid();

	//TASKID = (((ip & 0x0000FFFF) << 16) | (pid & 0x0000FFFF)) % 6000 ;
	TASKID = (pid & 0x0000FFFF);

	printf("Task ID: %d - PID: %x - IP: %s - IPbin: %ld\n", TASKID, pid, inet_ntoa(*((struct in_addr*) he->h_addr)), ip);

}
