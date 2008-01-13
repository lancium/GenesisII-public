#include <tchar.h>
#include <stdio.h>
#include <windows.h>

#include <MakeGenesisIICalls.h>
#include "nulmrx.h"
#include <winioctl.h>

//-----------------------------------------------------------------------------
// Local structures

#define DEBUG			 0x00000000
#define NUMBER_OF_THREADS			 5
                                                
HANDLE sem_handle;
HANDLE GenesisControlHandle;		//Device handle	

int loggedin = 0;

typedef struct _WORKER{
	HANDLE thread;
	struct _WORKER *nextWorker;
}*PWORKER, WORKER;


typedef struct _CommRequest {

    GENII_CONTROL_REQUEST    ControlRequest;
    char                     ControlRequestBuffer[65536 + (3 * sizeof(long))];

} COMMREQUEST, *PCOMMREQUEST;

typedef struct _CommResponse {

    GENII_CONTROL_RESPONSE  ControlResponse;
	
	//Max 128 entries (65536 / 512) in QD
    char                    ControlResponseBuffer[65536 + (3 * sizeof(long))];

} COMMRESPONSE, *PCOMMRESPONSE;


void printListing(char * listing, int size){
	int i;	
	ULONG length;
	ULONG fileid;

	if(size == JNI_ERR){
		printf("Error occurred while processing request\n");
	}

	for(i =0; i< size; i++){		
		memcpy(&fileid, listing, sizeof(ULONG));
		printf("File ID: %d ", fileid);
		listing += sizeof(ULONG);

		printf("File Type: %s ", listing);		
		listing += strlen(listing) + 1;

		memcpy(&length, listing, sizeof(ULONG));
		printf("Size: %d ", length);
		listing += sizeof(ULONG);

		printf("Name: %s\n", listing);
		listing += strlen(listing) + 1;				
	}
}

void printListing2(char * listing, int bytes){

	if(bytes == JNI_ERR){
		printf("Error occurred while processing request\n");
	}
	else{
		listing[bytes] = '\0';
		printf_s("%s\n", listing);
	}
}

//Copy listing and return total bytes saved
int copyListing(char * buffer, char ** listing, int size){

	char * pointer = buffer;
	
	//Max 64 bit length
	char lengthBuffer[9];
	long length;

	int i;
	int bytesCopied = 0;

	__try{

		//Copy as F\0taco.txt\0
		for(i =0; i < 4*size; i+=4){
			//Copy file id
			memcpy(lengthBuffer, listing[i], strlen(listing[i]));
			lengthBuffer[strlen(listing[i])] = '\0';
			length = atol(lengthBuffer);
			memcpy(pointer, &length, sizeof(long));
			
			bytesCopied += sizeof(long);
			pointer += sizeof(long);

			//Copy type of File F | D
			memcpy(pointer, listing[i+1], strlen(listing[i+1]));
			pointer[strlen(listing[i+1])] = '\0';		

			bytesCopied += (int)strlen(listing[i+1]) + 1;
			pointer += strlen(listing[i+1]) + 1;		

			//Copy file length
			memcpy(lengthBuffer, listing[i+2], strlen(listing[i+2]));
			lengthBuffer[strlen(listing[i+2])] = '\0';
			length = atol(lengthBuffer);
			memcpy(pointer, &length, sizeof(long));

			bytesCopied += sizeof(long);
			pointer += sizeof(long);
			
			//Copy file name
			memcpy(pointer, listing[i+3], strlen(listing[i+3]));
			pointer[strlen(listing[i+3])] = '\0';		
			
			bytesCopied += (int)strlen(listing[i+3]) + 1;
			pointer += strlen(listing[i+3]) + 1;
		}
	}
	__finally{
		if((bytesCopied == 0) && (size > 0)){
			printf("Some error occurred on copy (no bytes copied)\n");
		}
	}
	return bytesCopied;
}

BOOL GetRequest(PGENII_CONTROL_REQUEST PRequest,LPOVERLAPPED POverlapped)
{
    BOOL    status;
    DWORD   bytesReturned;    

	WaitForSingleObject(sem_handle, INFINITE);	
	status = DeviceIoControl(GenesisControlHandle,GENII_CONTROL_GET_REQUEST,
                             PRequest,sizeof(GENII_CONTROL_REQUEST),
                             PRequest,sizeof(GENII_CONTROL_REQUEST),
                             &bytesReturned,
                             POverlapped);
	ReleaseSemaphore(sem_handle, 1, NULL);

    if(!status) {
        DWORD error = GetLastError();
        if(error != ERROR_IO_PENDING) {
			printf("Something went wrong:  GetRequest - Status %x",GetLastError());
            return FALSE;
        }
    }    

    return TRUE;                             
}

BOOL SendResponse(PGENII_CONTROL_RESPONSE PResponse,LPOVERLAPPED POverlapped)
{
    BOOL    status;
    DWORD   bytesReturned;    


	WaitForSingleObject(sem_handle, INFINITE);	
	status = DeviceIoControl(GenesisControlHandle,GENII_CONTROL_SEND_RESPONSE,
                             PResponse,sizeof(GENII_CONTROL_RESPONSE),
                             NULL,0,
                             &bytesReturned,
                             POverlapped);
	ReleaseSemaphore(sem_handle, 1, NULL);

    if(!status) {
        DWORD error = GetLastError();
        if(error != ERROR_IO_PENDING) {
            printf("Something went wrong:  SendResponse - Status %x",GetLastError());
            return FALSE;
        }
    }        

    return TRUE;                             
}

//Prepares a response 
void prepareResponse(PGII_JNI_INFO pMyInfo, PGENII_CONTROL_REQUEST request, 
		PGENII_CONTROL_RESPONSE response){		

	response->RequestID = request->RequestID;
    response->ResponseType = request->RequestType;	

	switch(request->RequestType){
		case GENII_QUERYDIRECTORY:
		{			
			//Status code in Query Directory = # of entries
			char *directory;
			char *target;
			char *bufPtr = (char*) request->RequestBuffer;
			char ** directoryListing;

			//Get Directory
			if(request->RequestBufferLength && strlen(bufPtr) > 0){
				directory = (char*) malloc(strlen(bufPtr) + 1);
				memcpy(directory, bufPtr, strlen(bufPtr));
				directory[strlen(bufPtr)] = '\0';				
			}else{
				directory = "";
			}			

			bufPtr += (request->RequestBufferLength > 0) ? strlen(bufPtr) + 1 : 0;

			if(request->RequestBufferLength && strlen(bufPtr) > 0){
				target = (char*) malloc(strlen(bufPtr) + 1);
				memcpy(target, bufPtr, strlen(bufPtr));
				target[strlen(bufPtr)] = '\0';
			}else{
				target = "";
			}

			printf("Directory: %s Target: %s for Query Directory\n", directory, target);			
			response->StatusCode = genesisII_directory_listing(pMyInfo, &directoryListing, directory, target);
			printf("Got actual listing\n");
			//If an error, no copy is done
			response->ResponseBufferLength = response->StatusCode == -1 ? 0 : 
				copyListing(response->ResponseBuffer, directoryListing, response->StatusCode);

			printf("Listing Copied successful\n");					
			break;
		}	
		case GENII_QUERYFILEINFO:{
			//Status code in Query Directory = # of entries
			char * path;			
			char *bufPtr = (char*) request->RequestBuffer;
			char ** listing;

			//Get Directory
			if(request->RequestBufferLength && strlen(bufPtr) > 0){
				path = (char*) malloc(strlen(bufPtr) + 1);
				memcpy(path, bufPtr, strlen(bufPtr));
				path[strlen(bufPtr)] = '\0';				
			}else{
				path = "";
			}					

			printf("Path: %s for Query Info\n", path);			
			response->StatusCode = genesisII_get_information(pMyInfo, &listing, path);
			printf("Got Info!\n");
			//If an error, no copy is done
			response->ResponseBufferLength = response->StatusCode == -1 ? 0 : 
				copyListing(response->ResponseBuffer, listing, response->StatusCode);

			printf("Information Copied successful\n");								 
			break;						 
		}
		case GENII_CREATE:{
			//Status code in Query Directory = # of entries
			char * path;			
			char *bufPtr = (char*) request->RequestBuffer;
			char ** listing;
			BOOLEAN isMalformed = TRUE;

			//Create parameters
			int requestedDeposition;
			int desiredAccess;
			int isDirectory;

			//Get Directory
			if(request->RequestBufferLength && strlen(bufPtr) > 0){
				path = (char*) malloc(strlen(bufPtr) + 1);
				memcpy(path, bufPtr, strlen(bufPtr));
				path[strlen(bufPtr)] = '\0';				
			}else{
				path = "";
			}						
			
			bufPtr += (request->RequestBufferLength > 0) ? strlen(bufPtr) + 1 : 0;

			__try{			
				memcpy(&requestedDeposition, bufPtr, sizeof(int));
				bufPtr += sizeof(int);
				memcpy(&desiredAccess, bufPtr, sizeof(int));
				bufPtr += sizeof(int);
				memcpy(&isDirectory, bufPtr, sizeof(int));
				bufPtr += sizeof(int);

				isMalformed = FALSE;
						
				printf("Path: %s for Create\n", path);
				printf("Options: %d, %d, %d\n", requestedDeposition, desiredAccess, isDirectory);

				response->StatusCode = genesisII_open(pMyInfo, path, requestedDeposition, desiredAccess, 
					isDirectory, &listing);
				printf("Create finished on Genesis side!\n");
				
				//If an error, no copy is done
				response->ResponseBufferLength = (response->StatusCode == -1) ? 0 : 
					copyListing(response->ResponseBuffer, listing, response->StatusCode);

				printListing(response->ResponseBuffer, response->StatusCode);
			}__finally{
				if(isMalformed){
					response->StatusCode = -1;
				}
			}				
			break;
		}
		case GENII_CLOSE:{
			char *bufPtr = (char*) request->RequestBuffer;				
			long fileID;			
			int returnCode;

			memcpy(&fileID, bufPtr, sizeof(long));			
			response->ResponseBufferLength = sizeof(int);
			printf("Closing file with fileID: %d\n", fileID);
			returnCode = genesisII_close(pMyInfo, fileID);
			memcpy(response->ResponseBuffer, &returnCode, sizeof(int));	
			printf("File %d closed\n", fileID);

			break;
		}
		case GENII_READ:
		{			
			char *bufPtr = (char*) request->RequestBuffer;		
			long fileID, offset, length;			

			//Get all three parameters
			if(request->RequestBufferLength == sizeof(long) * 3){
				memcpy(&fileID, bufPtr, sizeof(long));
				bufPtr += sizeof(int);
				memcpy(&offset, bufPtr, sizeof(long));
				bufPtr += sizeof(int);
				memcpy(&length, bufPtr, sizeof(long));
			}
			printf("Read started for file with fileID: %d, offset: %d, length %d\n", fileID, offset, length);

			response->ResponseBufferLength = genesisII_read(pMyInfo, fileID, response->ResponseBuffer, offset, length);
			//printListing2(response->ResponseBuffer, response->ResponseBufferLength);
			printf("Read finished on Genesis side!\n");	

			break;
		}
		case GENII_WRITE:
		{
			char *bufPtr = (char*) request->RequestBuffer;		
			long fileID, offset, length;			

			response->ResponseBufferLength = 0;

			//Get all three parameters
			__try{
				memcpy(&fileID, bufPtr, sizeof(long));
				bufPtr += sizeof(long);
				memcpy(&offset, bufPtr, sizeof(long));
				bufPtr += sizeof(long);
				memcpy(&length, bufPtr, sizeof(long));
				bufPtr += sizeof(long);
				
				printf("Write started for file with fileID: %d, offset: %d, length %d\n", fileID, offset, length);

				if(DEBUG){
					printf("Data: %s\n", bufPtr);
				}

				response->ResponseBufferLength = genesisII_write(pMyInfo, fileID, bufPtr, offset, length);
				printf("Write finished on Genesis side!\n");	
			}			
			__finally{			
			}
			break;				
		}
		case GENII_TRUNCATEAPPEND:
		{
			char *bufPtr = (char*) request->RequestBuffer;		
			long fileID, offset, length;			

			response->ResponseBufferLength = 0;

			//Get all three parameters
			__try{
				memcpy(&fileID, bufPtr, sizeof(long));
				bufPtr += sizeof(long);
				memcpy(&offset, bufPtr, sizeof(long));
				bufPtr += sizeof(long);
				memcpy(&length, bufPtr, sizeof(long));
				bufPtr += sizeof(long);
				
				printf("TruncateAppend started for file with fileID: %d, offset: %d, length %d\n", fileID, offset, length);

				if(DEBUG){
					printf("Data: %s\n", bufPtr);
				}

				response->ResponseBufferLength = genesisII_truncate_append(pMyInfo, fileID, bufPtr, offset, length);
				printf("TruncateAppend finished on Genesis side!\n");	
			}			
			__finally{			
			}
			break;
		}
		default:
			//default action just copies request buff into response buff
            response->ResponseBufferLength = request->RequestBufferLength;

            memcpy(response->ResponseBuffer,
                   request->RequestBuffer,
                   request->RequestBufferLength);
		
			break;
	}
}

DWORD WINAPI TestThread(LPVOID parameter){	
	char ** directoryListing;
	char buffer[8192];
	int StatusCode;
	GII_JNI_INFO myInfo;

	if(initializeJavaVMForThread(&myInfo) == -1){
		printf("Thread initialization failed\n");
		return -1;
	}

	//Log in!
	WaitForSingleObject(sem_handle, INFINITE);
	if(!loggedin){
		printf("Logging In to Genesis\n");
		genesisII_login(&myInfo, "deployments/default/security/keys.pfx", "keys", "skynet1");
		printf("Login successful\n");
		loggedin = 1;
	}
	ReleaseSemaphore(sem_handle, 1, NULL);

	StatusCode = genesisII_directory_listing(&myInfo, &directoryListing, "", "");
	copyListing(buffer, directoryListing, StatusCode);
	printListing(buffer, StatusCode);

	Sleep(300000);
	return 0 ;
}

/*	
	Server thread with inverted calls
*/
DWORD WINAPI ServerThread(LPVOID parameter){
	BOOL			connected = TRUE;
	BOOL			isRunning = TRUE;    
    OVERLAPPED      GeniiRequestOverlapped;
    COMMREQUEST     GeniiRequest;
    COMMRESPONSE    GeniiResponse;
    BOOL            ret;	
	GII_JNI_INFO myInfo;	

	if(initializeJavaVMForThread(&myInfo) == -1){
		printf("Thread initialization failed\n");
		return -1;
	}
	
	//For this, we'll assume driver stays connected
	while(isRunning && connected){		        
		DWORD bytesTransferred;

        //Tell device you are ready to process 
        memset(&GeniiRequestOverlapped,0,sizeof(GeniiRequestOverlapped));
        GeniiRequestOverlapped.hEvent = CreateEvent(NULL,TRUE,FALSE,NULL);
        
		if(GeniiRequestOverlapped.hEvent == INVALID_HANDLE_VALUE) {
			//try to acquire one next time around
            continue;
        }

        memset(&GeniiRequest,0,sizeof(GeniiRequest));

        GeniiRequest.ControlRequest.RequestBuffer = &(GeniiRequest.ControlRequestBuffer[0]);
        GeniiRequest.ControlRequest.RequestBufferLength = sizeof(GeniiRequest.ControlRequestBuffer);

		printf("Notication sent to Kernel Driver\n");

        ret = GetRequest(&(GeniiRequest.ControlRequest),&GeniiRequestOverlapped);
        if(!ret) {
            CloseHandle(GeniiRequestOverlapped.hEvent);
            continue;
        } 			

		//Wait to be alerted by the device that IO is waiting
        while(!HasOverlappedIoCompleted(&GeniiRequestOverlapped)) {
            if(!isRunning) {
                goto exitStageLeft;                    
            } 
            Sleep(100);
        }

		//Get the request				                                 
		ret = GetOverlappedResult(GenesisControlHandle,&GeniiRequestOverlapped,&bytesTransferred,FALSE);                        
        
        printf("Request from Kernel Driver Received : Id= %x, Type = %x, Length %x\n", 
                 GeniiRequest.ControlRequest.RequestID,
                 GeniiRequest.ControlRequest.RequestType,                 
                 GeniiRequest.ControlRequest.RequestBufferLength);

		//This must be done before preparing the Response
		GeniiResponse.ControlResponse.ResponseBuffer = &(GeniiResponse.ControlResponseBuffer[0]);
		      
		//PREPARE GENESIS RESPONSE depending on RequestID
		prepareResponse(&myInfo, &(GeniiRequest.ControlRequest), &(GeniiResponse.ControlResponse));

		//Send response (Closes are asynchronous)
		
		ResetEvent(GeniiRequestOverlapped.hEvent);

		printf("Sending Response to Kernel Driver: Id= %x, Type = %x, Length %x\n", 
				 GeniiResponse.ControlResponse.RequestID,
				 GeniiResponse.ControlResponse.ResponseType,                 
				 GeniiResponse.ControlResponse.ResponseBufferLength);
		ret = SendResponse(&(GeniiResponse.ControlResponse),&GeniiRequestOverlapped);
		if(!ret) {              
			CloseHandle(GeniiRequestOverlapped.hEvent);
			continue;
		} 
		while(!HasOverlappedIoCompleted(&GeniiRequestOverlapped)) {
			if(!isRunning) {
				goto exitStageLeft;                    
			} 
			Sleep(100);
		}     		
        CloseHandle(GeniiRequestOverlapped.hEvent);
	}

exitStageLeft:
	return 0;
}

int runMultiThreaded(){
	
	PWORKER head, last=NULL, current;
    DWORD dwThreadId;	
	int i;	

	sem_handle = CreateSemaphore(NULL, 1, 1, NULL); 	

	//Open device that deals with inverted calls
	GenesisControlHandle = CreateFile(DD_GENII_CONTROL_DEVICE_USER_NAME,
									GENERIC_READ|GENERIC_WRITE,
                                    FILE_SHARE_READ | FILE_SHARE_WRITE,NULL,OPEN_EXISTING,
                                    FILE_ATTRIBUTE_NORMAL|FILE_FLAG_OVERLAPPED,NULL);

	// We cannot continue if we cannot find the device
	if(GenesisControlHandle == INVALID_HANDLE_VALUE) {
		DWORD error = GetLastError();
		printf("\n%x\n", error);
		_tprintf(_T("\nGenesis Control device not located.  Please reinstall the Genesis Driver\n"));
		return 0; 
	}

	head = (PWORKER) malloc(sizeof(WORKER));

	// Run both threads testing small LPC messages   
	head->thread = CreateThread(NULL, 0, ServerThread, NULL, 0, &dwThreadId);
	head->nextWorker = NULL;
	current = head;

	for(i=1; i < NUMBER_OF_THREADS; i++){
		last = current;
		current = (PWORKER) malloc(sizeof(WORKER));
		current->thread = CreateThread(NULL, 0, ServerThread, NULL, 0, &dwThreadId);
		current->nextWorker = NULL;
		last->nextWorker = current;
	}

	//Wait for last node
	WaitForSingleObject(current->thread, INFINITE);

	//Clean up
	current = head;
	while(current != NULL){
		last = current;
		current = current->nextWorker;		
		CloseHandle(last->thread);
		free(last);
	}	

	if(GenesisControlHandle != INVALID_HANDLE_VALUE) {

        CloseHandle(GenesisControlHandle);
        GenesisControlHandle = INVALID_HANDLE_VALUE;
    }

	//Delete semaphore
	CloseHandle(sem_handle);
	return 0;
}

int main(int argc, char* argv[])
{
	int status = 0;
	
	int desiredAccess = GENESIS_FILE_READ_DATA | GENESIS_FILE_WRITE_DATA;						
	char requestedDeposition = GENESIS_FILE_CREATE;
	int isDirectory = FALSE;

	GII_JNI_INFO rootInfo;		

	int StatusCode, bytes, fileid;	
	char ** directoryListing;
	char buffer[8192];	

	//Initialize in root thread
	if(initializeJavaVM(NULL, &rootInfo) == -1){
		printf("Initialization Failed!\n ");
		return 0;
	}

	if(!loggedin){
		int isSuccessful;
		printf("Logging In to Genesis\n");
		isSuccessful = genesisII_login(&rootInfo, NULL, "keys", "sky");
		if(isSuccessful == JNI_ERR){
			printf("Login unsuccessful\n");
			return 0;
		}
		else{
			printf("Login successful\n");
			loggedin = 1;
		}
	}
	
	status = runMultiThreaded();		

	//fileid=0;

	//printf("Creating directory SOME_DIRECTORY\n");

	//genesisII_remove(&rootInfo, "/taco.txt", 0, 0);

	//StatusCode = genesisII_open(&rootInfo, "/taco.txt", requestedDeposition, desiredAccess, isDirectory, &directoryListing);
	//printf("Got Create Info for taco.txt\n");
	//bytes = copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);

	//StatusCode = genesisII_write(&rootInfo,fileid, "IlikeTacos", 0, 10);
	//printf("Wrote data %d\n", StatusCode);
	//
	//StatusCode = genesisII_read(&rootInfo,fileid, buffer, 0, 10);
	//printf("Got Data\n");
	//printListing2(buffer, StatusCode);

	//StatusCode = genesisII_close(&rootInfo,fileid);

	//memcpy(&fileid, buffer, sizeof(ULONG));

	//printf("Reading 3468 from offset 4000 bytes from %d \n", fileid);

	//StatusCode = genesisII_read(&rootInfo,fileid, buffer, 0, 4096);
	//printf("Got Data\n");
	//printListing2(buffer, StatusCode);

	//StatusCode = genesisII_truncate_append(&rootInfo,fileid, "IlikeTacos", 100, 10);
	//printf("Wrote data %d\n", StatusCode);

	//*StatusCode = genesisII_write(&rootInfo,fileid, "IlikeTacos", 8000, 10);
	//printf("Wrote data %d\n", StatusCode);

	genesisII_logout(&rootInfo);
	
	return status;
}

