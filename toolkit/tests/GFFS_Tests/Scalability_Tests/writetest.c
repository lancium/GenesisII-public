/**
 * Genesis II concurrent write test using MPI.
 *
 * Usage:
 * Submit this program to any number of nodes.
 * On the command line, give the RNS path to a directory.
 * Each node will create a 250 MB file in that directory.
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
 * Each node creates a 250 MB file in /tmp from files on the local disk,
 * so we do not have to read 250n MB from NFS.
 * Each node creates a one-byte file in the grid *before* the timed
 * part of the test.  The timed part tests byteIO operations, not
 * directory operations.
 * After setup, the nodes wait at an MPI barrier, so they all start the
 * timed part of the test (after the barrier) at the same time.
 */

/* Author: Sal Valente
Author: Vanamala Venkataswamy
*/

#include "mpi.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char **argv)
{
    int id, numprocs, result;
    char userdir[256], cmd[1024], *directory, filename[256];
    char installdir[256];

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &id);
    MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
    printf("id %d of %d\n", id, numprocs);

    sprintf(userdir, "/tmp/writetest%d.%d", (int) getpid(), id);
    setenv("GENII_USER_DIR", userdir, 1);
    sprintf(cmd, "cp -rp %s/.genesisII-2.0 %s", getenv("TEST_TEMP"), userdir);
    result = system(cmd);
    if (result != 0) perror("cp");

    // Create an empty file in the grid.
    directory = argv[1];
    sprintf(directory, "%s", getenv("RNSPATH"));
    sprintf(filename, "%s/wt%d", directory, id);
    //sprintf(cmd, "/af15/vv3xu/Release-2.3.3/grid echo > %s", filename);
    //result = system(cmd);
    //if (result != 0) perror("grid");

    // Create a 250 MB file in /tmp.
    sprintf(cmd, "cd /usr/lib; cat libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a libpython2.6.a > %s/250", userdir);
    result = system(cmd);
    if (result != 0) perror("cat");

    MPI_Barrier(MPI_COMM_WORLD);
    printf("id %d past barrier\n", id);

    sprintf(installdir, "%s", getenv("GENII_INSTALL_DIR"));
    sprintf(cmd, "%s/grid cp file:%s/250 %s", installdir,userdir, filename);
    result = system(cmd);
    if (result != 0) perror("grid");

    printf("id %d done\n", id);
    sprintf(cmd, "rm -rf %s", userdir);
    result = system(cmd);
    if (result != 0) perror("rm");
    MPI_Finalize();
    return 0;
}

