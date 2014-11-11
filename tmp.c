#include <stdio.h>
#include <unistd.h>
#include <dirent.h>
#include <string.h>

main(){ 

unsigned short port = 22;
char line[1000];
    char port_hex[10]; /* two bytes of hex = 4 characters, plus NULL terminator */
    char *ptr;
#define datcp "/proc/net/tcp"
    FILE *fp = NULL;
    fp = fopen(datcp, "r");
    if (fp != NULL) {
        while (fgets(line, sizeof line, fp) != NULL) {
            sprintf(port_hex, "%04X", port);
            printf("line=%s, port_hex=%s\n", line, port_hex);
            if (strstr(line, port_hex) != NULL) {
                ptr = strtok(line, " ");
                int i = 0;
                char inode[15];
                printf("ptr=%s,", ptr);
                while ((ptr = strtok(NULL, " ")) != NULL) {
                    i++;
                    if (i == 9) {
                        sprintf(inode, "%s", ptr);
                        printf("inode=%s, ", inode);
                        //anyadir la busqueda en /proc/[pid]/fd/* del pid:
                        DIR *d;
                        DIR *d2;
                        struct dirent *dir;
                        d = opendir("/proc/");

                        if (d) {
                            while ((dir = readdir(d))) {
                                //printf("readdir=%s,", dir->d_name);
                                char x[100];
                                struct dirent *dir2;
                                sprintf(x, "/proc/%s/fd/", dir->d_name);
                                //printf("x=%s, ",x);
                                d2 = opendir(x);
                                if (d2) {
                                    while ((dir2 = readdir(d2))) {
                                        if (dir2->d_type & DT_LNK) {
                                            char buf[1024];
                                            ssize_t len;
                                            char mylink[100];
                                            sprintf(mylink, "/proc/%s/fd/%s", dir->d_name, dir2->d_name);
                                            if ((len = readlink(mylink, buf, sizeof (buf) - 1)) != -1) {
                                                buf[len] = '\0';
                                                if (strstr(buf, ptr) != NULL) {
                                                    printf("%s->%s, PID=%s", dir2->d_name, buf, dir->d_name);
                                                }
                                            }
                                        }
                                    }
                                    closedir(d2);
                                }
                            }
                            closedir(d);
                        }
                        printf("\n");
                    }
                }
            }
        }
    }
}
