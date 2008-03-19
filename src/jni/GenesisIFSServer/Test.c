#include <windows.h>
#include <MakeGenesisIICalls.h>
#include "server.h"
#include "nulmrx.h"

void TestThread(GII_JNI_INFO rootInfo){
	int StatusCode, bytes, fileid;	
	char ** directoryListing;
	char buffer[8192];	
	
	int desiredAccess = GENESIS_FILE_READ_DATA | GENESIS_FILE_WRITE_DATA;
	char requestedDeposition = GENESIS_FILE_OPEN;
	int isDirectory = FALSE;

	fileid=0;

	genesisII_open(&rootInfo, "/home/sosa/somefile.txt", requestedDeposition, desiredAccess, FALSE, &directoryListing);

	genesisII_close(&rootInfo, fileid, TRUE);

	//printf("Creating directory SOME_DIRECTORY\n");

	//genesisII_remove(&rootInfo, "/taco.txt", 0, 0);	

	//StatusCode = genesisII_open(&rootInfo, "/home/sosa/crime.txt", requestedDeposition, desiredAccess, isDirectory, &directoryListing);	
	//bytes = copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);

	//StatusCode = genesisII_write(&rootInfo,fileid, "IlikeTacos", 0, 10);
	//printf("Wrote data %d\n", StatusCode);
	//
	//StatusCode = genesisII_read(&rootInfo,fileid, buffer, 0, 10);
	//printf("Got Data\n");
	//printListing2(buffer, StatusCode);

	//StatusCode = genesisII_close(&rootInfo,fileid, FALSE);

	//memcpy(&fileid, buffer, sizeof(ULONG));

	//printf("Reading 3468 from offset 4000 bytes from %d \n", fileid);

	//StatusCode = genesisII_read(&rootInfo,fileid, buffer, 0, 4096);
	//printf("Got Data\n");
	//printListing2(buffer, StatusCode);

	//StatusCode = genesisII_truncate_append(&rootInfo,fileid, "IlikeTacos", 100, 10);
	//printf("Wrote data %d\n", StatusCode);

	//StatusCode = genesisII_write(&rootInfo,fileid, "IlikeTacos", 8000, 10);
	//printf("Wrote data %d\n", StatusCode);
}