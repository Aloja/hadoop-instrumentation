#include <es_bsc_tools_extrae_Wrapper.h>
#include <commons_bsc.h>
#include <extrae_user_events.h>
#include <extrae_types.h>
#include <stdlib.h>
#include <unistd.h>
#include <snifferreceiver.h>
#include <netdb.h>

int main(){
	extrae_type_t typesArray2[4];
	extrae_value_t valuesArray2[4];
	int k = 0;
	for(k=0; k < 4; k++) {
		typesArray2[k] = 1;
		valuesArray2[k] = 1;
	}
	Extrae_nevent(4, &typesArray2, &valuesArray2);
}


