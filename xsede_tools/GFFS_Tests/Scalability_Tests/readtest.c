 /* Usage:
 * Submit this program to any number of nodes.
 * On the command line, give the RNS path to a large file.
 * Each node will read the file and calculate the MD5 sum.
 * All nodes run at the same time.
 *
 * Assumptions:
 * 1. The "grid" script is in your PATH.
 * 2. Some server is running somewhere.  (XCG?  Desktop server?)
 * 3. The directory ~/.genesisII-2.0 contains your grid client state.
 * 4. In your grid client state, you are connected to the server,
 *    and you are logged in.
 *
 * Notes:
 * Each node copies ~/.genesisII-2.0 to local disk, so that it does not
 * depend on NFS during the timed part of the test.
 * After copying the directory, the nodes wait at an MPI barrier,
 * so they all start the timed part of the test (after the barrier)
 * at the same time.
 */

/*Author: Sal Valenete
* Author: Vanamala Venkataswamy
*/

#include "mpi.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char **argv)
{
    int id, numprocs, result;
    char *filename, userdir[256], cmd[256];
    char installdir[256];

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &id);
    MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
    filename = argv[1];
    printf("id %d of %d\n", id, numprocs);

    sprintf(userdir, "/tmp/readtest%d.%d", (int) getpid(), id);
    setenv("GENII_USER_DIR", userdir, 1);
    sprintf(cmd, "cp -rp %s/.genesisII-2.0 %s", getenv("TEST_TEMP"), userdir);
    result = system(cmd);
    if (result != 0) perror("cp");

    MPI_Barrier(MPI_COMM_WORLD);

    sprintf(installdir, "%s", getenv("GENII_INSTALL_DIR"));
    sprintf(cmd, "%s/grid cp %s file:%s", installdir, filename, userdir);
    result = system(cmd);
    if (result != 0) perror("grid");

    sprintf(cmd, "rm -rf %s", userdir);
    result = system(cmd);
    if (result != 0) perror("rm");
    MPI_Finalize();
    return 0;
}

