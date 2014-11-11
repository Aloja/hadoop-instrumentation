#include <net_dumper.h>

//pthread_mutex_t *runcontrol_mutex;

void start() {

    //    pthread_mutex_init(runcontrol_mutex, NULL);
    //    pthread_mutex_lock(runcontrol_mutex);
    // start dumping
    run = TRUE;
    paused = FALSE;

    //    pthread_mutex_unlock(runcontrol_mutex);

}

void pause() {
    //    pthread_mutex_init(runcontrol_mutex, NULL);
    //    pthread_mutex_lock(runcontrol_mutex);
    // start dumping
    paused = TRUE;

    //    pthread_mutex_unlock(runcontrol_mutex);
}

void stop() {

    //    pthread_mutex_init(runcontrol_mutex, NULL);
    //    pthread_mutex_lock(runcontrol_mutex);
    /* aquí se accede a la estructura de datos */
    run = FALSE;
    //    pthread_mutex_unlock(runcontrol_mutex);
}