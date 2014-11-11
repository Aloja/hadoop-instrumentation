#include <linux/kernel.h>
#include <linux/module.h>
#include <sys/syscall.h>
#include <sys/socket.h>
#include <unistd.h>

void **sys_call_table;

asmlinkage int (*original_sys_connect)(int , const struct sockaddr*, socklen_t);

asmlinkage int our_fake_connect_function(int sockfd, const struct sockaddr *addr, socklen_t addrlen)
{
        /*print message on console every time we are called*/
		pid_t p = getpid();
		pid_t pp = getppid();
        printk("PID CALLING=%d PARENT=%d\n",p,pp);

        /*call the original sys_connect*/
        return original_sys_connect(sockfd, addr, addrlen);
}

/*this function is called when the module is loaded (initialization)*/
int init_module()
{
		//init sys_call_table (from grep sys_call /boot/System.map)
		sys_call_table=(void *)0xffffffff81801320;
		
		//store reference to the original sys_connect
		original_sys_connect=sys_call_table[__NR_connect];


        /*manipulate sys_call_table to call our
         *fake connect function instead
         *of sys_connect*/
        sys_call_table[__NR_connect]=our_fake_connect_function;
        return 0;
}

/*this function is called when the module is unloaded*/
void cleanup_module()
{
        /*make __NR_connect point to the original
         *sys_connect when our module
         *is unloaded*/
        sys_call_table[__NR_connect]=original_sys_connect;
}
