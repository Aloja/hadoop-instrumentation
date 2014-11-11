
#include "snifferreceiver.h"

#define MAX_FILTER_SIZE 100


char *readProperty(char *configFile, char *pname) {
    FILE * fp;
    char * line = NULL;
    size_t len = 0;
    ssize_t read;
    char *pvalue = calloc(MAX_FILTER_SIZE, sizeof (char));

    const char delimiters[] = "=\n";
    //    char *running;
    char *token;

    fp = fopen(configFile, "r");
    if (fp == NULL) {
        printf("Error: NO EXISTE EL FICHERO!");
        //        return "NO EXISTE EL FICHERO!";
    } else {
    }

    while ((read = getline(&line, &len, fp)) != -1) {
        //        printf("Retrieved line of length %zu :\n", read);
        //        printf("line: %s\n", line);

        token = strsep(&line, delimiters);
        //        printf("token: %s\n", token);
        if (strcmp(token, pname) == 0) {
            //if equal
            pvalue = strsep(&line, delimiters);
            //            pvalue = "a";
            printf("GOT pname|pvalue=>%s|%s\n", pname, pvalue);
            //            printf("pvalue: %s\n", pvalue);
            //            return;
            return pvalue;
        } else {

        }
    }
}