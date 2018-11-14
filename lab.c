#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <signal.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <string.h>

int child_pid = 0;
char current_char = '*';

void send_sigusr1_to_parent(int a)
{
    kill(getppid(), SIGUSR1);
}

void process_child_things(int a0, int r, int N)
{
    struct sigaction act;
    memset(&act, '\0', sizeof(act));
    act.sa_handler = send_sigusr1_to_parent;
    sigaction(SIGALRM, &act , NULL);
    alarm(1);
    double4 a = (double)a0;
    for(int i=0;i<N;i++)
    {
        a += 1.0/r;
    }
    
    printf("[%f]", a);
    exit(13);
}

void child_he_dead(int a)
{
    int status;
    wait(&status);
    printf("\nCHILD PID: %d - exit: %d\n", child_pid, WEXITSTATUS(status));
    exit(0);
}

void child_screams(int a)
{
    current_char = '+';   
}

int main(int argc, char** argv)
{
    if(argc < 4)
    {
        printf("usage <%s> a0 r N", argv[0]);
        exit(-1);
    }
    
    struct sigaction act;
    memset(&act, '\0', sizeof(act));
    act.sa_handler = child_he_dead;
    sigaction(SIGCHLD, &act, NULL);
    
    struct sigaction act2;
    memset(&act2, '\0', sizeof(act2));
    act2.sa_handler = child_screams;
    sigaction(SIGUSR1, &act2, NULL);
    
    if((child_pid = fork()) < 0)
    {
        printf("error creating the child process");
        exit(-1);
    }
    if(child_pid == 0)
    {
        process_child_things(atoi(argv[1]),atoi(argv[2]),atoi(argv[3]) );
    } else {
       while(1) printf("%c", current_char);
    }
    
}
