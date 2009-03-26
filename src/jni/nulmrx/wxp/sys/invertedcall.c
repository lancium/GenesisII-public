/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    fileinfo.c

Abstract:

    This module implements the mini redirector call down routines pertaining to retrieval/
    update of file/directory/volume information.

--*/

#include "precomp.h"
#include "stdlib.h"
#include "nulmrx.h"

#pragma hdrstop
#pragma warning(error:4101)   // Unreferenced local variable

//  The local debug trace level
#define Dbg                              (DEBUG_TRACE_FILEINFO)

ULONG GenesisPrepareDirectoryAndTarget(PRX_CONTEXT RxContext, PVOID buffer, BOOLEAN hasTarget){
/*
	Adds the target directory and regexp to the buffer to send the user mode
*/		
	RxCaptureFobx;
	GenesisGetCcbExtension(capFobx, giiCCB);	
	char * myBuffer = (char *) buffer;	

	RtlCopyMemory(myBuffer, &giiCCB->GenesisFileID, sizeof(long));	
	myBuffer += sizeof(long);

	if(hasTarget){

		//If it has a target				
		RtlCopyMemory(myBuffer,giiCCB->Target.Buffer, giiCCB->Target.Length);
		myBuffer[giiCCB->Target.Length] = '\0';
		
		GIIPrint(("GenesisDrive:  Sending to Genesis for Directory %s and Target %s\n", (char*)buffer, myBuffer));

		//Return lengths + 2 null characters
		return sizeof(long) + giiCCB->Target.Length + 2;	
	}else{
		GIIPrint(("GenesisDrive:  Sending get info to Genesis for File %s\n", (char*)buffer));
		myBuffer[0] = '\0';
		return sizeof(long) + 1;
	}
}

ULONG GenesisPrepareCreate(PRX_CONTEXT RxContext, PVOID buffer){
/*
	Adds the target directory and regexp to the buffer to send the user mode
*/
	char * myBuffer = (char *) buffer;	
	int i;
	ANSI_STRING temp;	
	UINT DesiredAccessNew = 0;
	UINT isDirectory = 0;
	
	//Unaltered
	UINT RequestedDisposition;	
	ULONG RequestedOptions;
	ACCESS_MASK DesiredAccess;	
	PIO_STACK_LOCATION PtrIoStackLocation = RxContext->CurrentIrpSp;	

	//Get unaltered deposition
	RequestedDisposition = ((PtrIoStackLocation->Parameters.Create.Options >> 24) & 0xFF);	

	GIIPrint(("RequestedDispostion %d\n", RequestedDisposition));

	//Set up the desired access new appropriately
	DesiredAccess = PtrIoStackLocation->Parameters.Create.SecurityContext->DesiredAccess;
	DesiredAccessNew |= ((DesiredAccess & FILE_READ_DATA) ? GENESIS_FILE_READ_DATA : 0);
	DesiredAccessNew |= ((DesiredAccess & FILE_WRITE_DATA) ? GENESIS_FILE_WRITE_DATA : 0);
	DesiredAccessNew |= ((DesiredAccess & FILE_APPEND_DATA) ? GENESIS_FILE_APPEND_DATA : 0);
	DesiredAccessNew |= ((DesiredAccess & FILE_EXECUTE) ? GENESIS_FILE_EXECUTE : 0);
	DesiredAccessNew |= ((DesiredAccess & DELETE) ? GENESIS_FILE_DELETE : 0);

	//Check whether directory or file
	RequestedOptions = (PtrIoStackLocation->Parameters.Create.Options & FILE_VALID_OPTION_FLAGS);
	isDirectory = ((RequestedOptions & FILE_DIRECTORY_FILE) ? TRUE : FALSE);
	
	RtlUnicodeStringToAnsiString(&temp, RxContext->pRelevantSrvOpen->pAlreadyPrefixedName,TRUE);
	RtlCopyMemory(myBuffer, temp.Buffer, temp.Length);			
	for(i=0; i<temp.Length; i++){
		if(myBuffer[i] =='\\'){
			myBuffer[i] = '/';
		}
	}
	myBuffer[temp.Length] = '\0';
	myBuffer += temp.Length + 1;	
	
	RtlFreeAnsiString(&temp);

	RtlCopyMemory(myBuffer, &RequestedDisposition, sizeof(UINT));
	myBuffer += sizeof(UINT);
	RtlCopyMemory(myBuffer, &DesiredAccessNew, sizeof(UINT));
	myBuffer += sizeof(UINT);
	RtlCopyMemory(myBuffer, &isDirectory, sizeof(UINT));
	myBuffer += sizeof(UINT);

	GIIPrint(("GenesisDrive:  Sending create to Genesis for File %s\n", (char*)buffer));
	myBuffer[0] = '\0';
	return temp.Length + 2 + (sizeof(UINT) * 3);	
}

ULONG GenesisPrepareClose(PRX_CONTEXT RxContext, PVOID buffer){	
	char* bufptr = (char*)buffer;
	RxCaptureFcb;
	RxCaptureFobx;
	GenesisGetFcbExtension(capFcb, fcb);
	GenesisGetCcbExtension(capFobx, ccb);
	
	PAGED_CODE();

	RtlCopyMemory(bufptr, &ccb->GenesisFileID, sizeof(long));	
	bufptr += sizeof(long);
	RtlCopyMemory(bufptr, &fcb->DeleteOnCloseSpecified, sizeof(char));
	return sizeof(long) + sizeof(char);
}

ULONG GenesisPrepareRename(PRX_CONTEXT RxContext, PVOID buffer){	
	char* bufptr = (char*)buffer;
	int i;
	UNICODE_STRING uni;
	ANSI_STRING temp;
	RxCaptureFcb;
	RxCaptureFobx;
	GenesisGetFcbExtension(capFcb, fcb);
	GenesisGetCcbExtension(capFobx, ccb);
	
	PFILE_RENAME_INFORMATION pRenameInfo = (PFILE_RENAME_INFORMATION) RxContext->Info.Buffer;
	
	PAGED_CODE();	

	RtlCopyMemory(bufptr, &ccb->GenesisFileID, sizeof(long));	
	bufptr += sizeof(long);

	uni.Buffer = pRenameInfo->FileName;
	uni.Length = (USHORT)pRenameInfo->FileNameLength;
	uni.MaximumLength = (USHORT)pRenameInfo->FileNameLength;

	RtlUnicodeStringToAnsiString(&temp, (PUNICODE_STRING)&uni,TRUE);
	RtlCopyMemory(bufptr, temp.Buffer, temp.Length);

	for(i=0; i<temp.Length; i++){
		if(bufptr[i] =='\\'){
			bufptr[i] = '/';
		}
	}
	bufptr[temp.Length] = '\0';

	return sizeof(long) + temp.Length + 1;
}

void GenesisSaveDirectoryListing(PGENESIS_FCB fcb, PVOID directoryListing, int size){
/*
	Stores the directory listing into the FCB slot of size size
*/		
	char * pointer = (char *)directoryListing;	
	WCHAR buffer[MAX_PATH_LENGTH];
	WCHAR * w_pointer;
	int i;

	//If an error occurred (return empty directory)
	if(size == -1){	
		fcb->State = GENII_STATE_NOT_FOUND;		
		return;
	}

	//If no error occurred we have a listing
	fcb->State = GENII_STATE_HAVE_LISTING;
	fcb->isDirectory = TRUE;	
	fcb->DirectorySize = size;	

	//Allocate Max Buffer size * Number of Directory Entries
	fcb->DirectoryListing = RxAllocatePoolWithTag(PagedPool, 
		(size) * MAX_PATH_LENGTH * sizeof(WCHAR), MRXGEN_CCB_POOLTAG);	

	w_pointer = fcb->DirectoryListing;

	for(i =0; i < size; i++){	
		//Skip file id		
		pointer += sizeof(long);

		//Grab F | D
		mbstowcs(buffer, pointer, MAX_PATH_LENGTH);
		RtlCopyMemory(w_pointer, buffer, sizeof(WCHAR) * wcslen(buffer));
		w_pointer[wcslen(buffer)] = L'\0';
		w_pointer += wcslen(buffer) + 1;
		pointer += strlen(pointer) + 1;

		//Grab file length
		RtlCopyMemory(w_pointer, pointer, sizeof(LONGLONG));
		w_pointer += sizeof(LONGLONG);
		pointer += sizeof(LONGLONG);

		//Grab file name
		mbstowcs(buffer, pointer, MAX_PATH_LENGTH);
		RtlCopyMemory(w_pointer, buffer, sizeof(WCHAR) * wcslen(buffer));		
		w_pointer[wcslen(buffer)] = L'\0';
		w_pointer += wcslen(buffer) + 1;
		pointer += strlen(pointer) + 1;
	}
}

void GenesisSaveInfoIntoFCB(PMRX_FCB commonFcb, PVOID info, int StatusCode){
/* 
	Gets file info out of buffer and into FCB 
*/
	char * pointer = (char *)info;			
	GenesisGetFcbExtension(commonFcb, fcb);

	//If an error occurred (return empty directory)
	if(StatusCode == -1){	
		fcb->State = GENII_STATE_NOT_FOUND;
		return;
	}			
	
	//Grab file id (mostly a check to see if the file exists and an io handle for later
	RtlCopyMemory(&(fcb->GenesisTempFileID), pointer, sizeof(long));
	pointer += sizeof(long);

	//Use current fcb size on local file system if already retreived
	if(fcb->State != GENII_STATE_HAVE_INFO && fcb->State != GENII_STATE_HAVE_LISTING){

		//Grab F | D		
		if(strcmp(pointer, "D") == 0) fcb->isDirectory = TRUE;				
		pointer += strlen(pointer) + 1;

		//Grab length ( CHANGE FOR LONG BYTE)		
		RtlCopyMemory(&(commonFcb->Header.FileSize.QuadPart), pointer, sizeof(LONGLONG));
		commonFcb->Header.AllocationSize = commonFcb->Header.FileSize;
		pointer += sizeof(LONGLONG);		
	}	
	else{
		//Skips components
		pointer += strlen(pointer) + 1;
		pointer += sizeof(LONGLONG);		
	}

	//Skip Name

	//Have listing supercedes have info
	if(fcb->State != GENII_STATE_HAVE_LISTING){
		fcb->State = GENII_STATE_HAVE_INFO;
	}
}

NTSTATUS GenesisSendInvertedCall(PRX_CONTEXT RxContext, ULONG callType, BOOLEAN MarkAsPending){
	PIRP Irp = RxContext->CurrentIrp;
	NTSTATUS status = STATUS_SUCCESS;
	ULONG information = 0;
	PGENII_REQUEST dataRequest;
	PNULMRX_DEVICE_EXTENSION dataExt = (PNULMRX_DEVICE_EXTENSION)
			((PBYTE)(NulMRxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT));	
	PGENESIS_CONTROL_EXTENSION controlExt =
		(PGENESIS_CONTROL_EXTENSION)GeniiControlDeviceObject->DeviceExtension;
	PLIST_ENTRY listEntry = NULL;
	PIRP controlIrp;
	PGENII_CONTROL_REQUEST controlRequest;	
	PFAST_MUTEX dataQueueLock;
	PLIST_ENTRY dataQueue;		
	PMDL mdl;
	PVOID controlBuffer;
	BOOLEAN isActive = TRUE;
    
	dataQueue = &dataExt->GeniiRequestQueue;
    dataQueueLock = &dataExt->GeniiRequestQueueLock;
	
	// Data device read must be satisfied by queuing request off to the service.
	dataRequest = (PGENII_REQUEST) ExAllocatePoolWithTag(PagedPool, sizeof(GENII_REQUEST), 'rdCO');
	if (!dataRequest) {
		// Complete the request, indicating that the operation failed						
		return STATUS_INSUFFICIENT_RESOURCES;				
	}
	RtlZeroMemory(dataRequest, sizeof(GENII_REQUEST));
	dataRequest->RequestID = (ULONG) InterlockedIncrement(&GeniiRequestID);
	dataRequest->RequestType = callType;

	GIIPrint(("Preparing InvCall For reqId %d with type %d\n", dataRequest->RequestID, dataRequest->RequestType));
	dataRequest->Irp = Irp;

	// Also want originating RxContext!
	dataRequest->RxContext = RxContext;
				
	// Acqiure lock here ***************************
	ExAcquireFastMutex(&controlExt->ServiceQueueLock);	

	// Fine grained lock control requires this
	if(controlExt->DeviceState != GENII_CONTROL_ACTIVE) {
		ExFreePoolWithTag(dataRequest, 'rdC0');
		ExReleaseFastMutex(&controlExt->ServiceQueueLock);
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	// Remove head of the queue if not empty
	if (!IsListEmpty(&controlExt->ServiceQueue)) {
		//UFS has a thread ready to handle the request		
		listEntry = RemoveHeadList(&controlExt->ServiceQueue);
	}

	// Release lock here *****************************
	ExReleaseFastMutex(&controlExt->ServiceQueueLock);

	//If no waiting threads.  We need to insert this into the request queue
	if(listEntry == NULL) {				
		ExAcquireFastMutex(dataQueueLock);		
		ExAcquireFastMutex(&controlExt->RequestQueueLock);		

		// Fine grained lock control requires this
		if(controlExt->DeviceState != GENII_CONTROL_ACTIVE) {
			ExFreePoolWithTag(dataRequest, 'rdC0');
			ExReleaseFastMutex(&controlExt->RequestQueueLock);
			ExReleaseFastMutex(dataQueueLock);
			return STATUS_DEVICE_NOT_CONNECTED;
		}

		//We are enqueuing the IRP, mark it as pending if allowed
		if(MarkAsPending){
			RxMarkContextPending(RxContext);					
			status = STATUS_PENDING;
		}
		
		//Put the user's actual request in the queue for safekeeping
		InsertTailList(dataQueue, &dataRequest->ListEntry);						

		//Second since we don't have a thread to deal with this, put in rq		
		InsertTailList(&controlExt->RequestQueue, &dataRequest->ServiceListEntry);

		ExReleaseFastMutex(&controlExt->RequestQueueLock);
		ExReleaseFastMutex(dataQueueLock);
	}
	else{		
		controlIrp = CONTAINING_RECORD(listEntry, IRP, Tail.Overlay.ListEntry);

		//This stuff locks control buffer (to write input commands into)
		controlRequest = (PGENII_CONTROL_REQUEST) controlIrp->AssociatedIrp.SystemBuffer;
		controlRequest->RequestID = dataRequest->RequestID;
		controlRequest->RequestType = callType;

		// Our problem here is that the control buffer is in a different address space.
		mdl = IoAllocateMdl(controlRequest->RequestBuffer,
						  controlRequest->RequestBufferLength,
						  FALSE, // should not be any other MDLs associated with control IRP
						  FALSE, // no quota charged
						  controlIrp); // track the MDL in the control IRP...
		
		if (NULL == mdl) {               
			// Complete the data request - this falls through and completes below.
			status = STATUS_INSUFFICIENT_RESOURCES;				                    									
		}				
		else{
			__try {
				// Probe and lock the pages
				MmProbeAndLockProcessPages(
					mdl, 
					IoGetRequestorProcess(controlIrp),
					UserMode,
					IoModifyAccess
					);
			} __except(EXCEPTION_EXECUTE_HANDLER) {
				// Access probe failed				
				status = GetExceptionCode();
				GIIPrint(("GenesisDrive: MDL probe and lock page exception in invCall: %d\n", status));
				IoFreeMdl(mdl);																		
			}			
		}
		//We are still succesful
		if(NT_SUCCESS(status)){			
			
			//Actually get pointer (same as ControlRequest->RequestBuffer)
			controlBuffer = MmGetSystemAddressForMdlSafe(mdl, NormalPagePriority);				

			switch(callType){
				case GENII_QUERYDIRECTORY:
					//Copies Directory and Target info into buffer with length
					controlRequest->RequestBufferLength = 
						GenesisPrepareDirectoryAndTarget(RxContext, controlBuffer, TRUE);
					break;
				case GENII_CREATE:
					//Copies Target info into buffer
					controlRequest->RequestBufferLength = 
						GenesisPrepareCreate(RxContext, controlBuffer);
					break;
				case GENII_CLOSE:
					//Copies Target info into buffer
					controlRequest->RequestBufferLength = 
						GenesisPrepareClose(RxContext, controlBuffer);
					break;						
				case GENII_READ:

					//NO IDEA WHY HERE
					//Let's now lock the user buffer (back to user space) (lock for write)
					//status = GenesisLockCallersBuffer(RxContext->CurrentIrp, FALSE, 
					//	RxContext->CurrentIrpSp->Parameters.Read.Length);
					//Copies target, offset and length info into buffer
					controlRequest->RequestBufferLength = 
						GenesisPrepareReadParams(RxContext, controlBuffer);					
					break;
				case GENII_WRITE:
				{
					BOOLEAN isTruncateAppend = FALSE;
					BOOLEAN isTruncateWrite = FALSE;

					//Copies target, offset and length info into buffer
					controlRequest->RequestBufferLength = 
						GenesisPrepareWriteParams(RxContext, controlBuffer, &isTruncateAppend, 
							&isTruncateWrite);

					if(isTruncateAppend || isTruncateWrite){
						controlRequest->RequestType = GENII_TRUNCATEAPPEND;
					}
					break;
				}
				case GENII_RENAME:				
					//Copies Target info into buffer
					controlRequest->RequestBufferLength = 
						GenesisPrepareRename(RxContext, controlBuffer);
					break;						
				default:
					GIIPrint(("GenesisDrive: Unsupported function trying to be sent to UFS"));
					status = STATUS_NOT_SUPPORTED;
			}
		}
		//Were we successful with getting the control request and moving the data request into it
		if(NT_SUCCESS(status)){

			//Insert into Data Q (ready to be responded to)
			ExAcquireFastMutex(dataQueueLock);

			// Fine grained lock control requires this
			if(controlExt->DeviceState != GENII_CONTROL_ACTIVE) {
				ExFreePoolWithTag(dataRequest, 'rdC0');
				IoFreeMdl(mdl);
				ExReleaseFastMutex(dataQueueLock);
				return STATUS_DEVICE_NOT_CONNECTED;
			}

			//We are enqueuing the IRP, mark it as pending if allowed
			if(MarkAsPending){
				RxMarkContextPending(RxContext);					
				status = STATUS_PENDING;
			}			
			
			InsertTailList(dataQueue, &dataRequest->ListEntry);
			ExReleaseFastMutex(dataQueueLock);

			//Second, sends back inverted call
			controlIrp->IoStatus.Status = STATUS_SUCCESS;
			controlIrp->IoStatus.Information = sizeof(GENII_CONTROL_REQUEST);          
			IoCompleteRequest(controlIrp, IO_NO_INCREMENT);
		}
		else{
			GIIPrint(("GenesisDrive: InvCall returned bad status %d !!!\n", status));

			//Free memory
			ExFreePoolWithTag(dataRequest,'rdCO');

			//Return entry to ServiceQueue (so we don't lose threads :-))
			ExAcquireFastMutex(&controlExt->ServiceQueueLock);
			
			// Fine grained lock control requires this
			if(controlExt->DeviceState != GENII_CONTROL_ACTIVE) {								
				ExReleaseFastMutex(&controlExt->ServiceQueueLock);
				return STATUS_DEVICE_NOT_CONNECTED;
			}

			InsertTailList(&controlExt->ServiceQueue, listEntry);
			ExReleaseFastMutex(&controlExt->ServiceQueueLock);
		}			
	}
	return status;	
}

/*++
***********************************************************************
*
* Function: GenesisLockCallersBuffer()
*
* Description:
*	Obtain a MDL that describes the buffer. Lock pages for I/O
*
* Expected Interrupt Level (for execution) :
*
*  IRQL_PASSIVE_LEVEL
*
* Return Value: STATUS_SUCCESS/Error
*
*************************************************************************
--*/
NTSTATUS GenesisLockCallersBuffer(PIRP PtrIrp, BOOLEAN IsReadOperation, ULONG Length)
{
	NTSTATUS			RC = STATUS_SUCCESS;
	PMDL				PtrMdl = NULL;

	ASSERT(PtrIrp);
	
	try {
		// Is a MDL already present in the IRP
		if (!(PtrIrp->MdlAddress)) {
			// Allocate a MDL
			if (!(PtrMdl = IoAllocateMdl(PtrIrp->UserBuffer, Length, FALSE, FALSE, PtrIrp))) {
				RC = STATUS_INSUFFICIENT_RESOURCES;
				try_return(RC);
			}

			// Probe and lock the pages described by the MDL
			// We could encounter an exception doing so, swallow the exception
			// NOTE: The exception could be due to an unexpected (from our
			// perspective), invalidation of the virtual addresses that comprise
			// the passed in buffer
			try {				
				MmProbeAndLockPages(PtrMdl, PtrIrp->RequestorMode, (IsReadOperation ? IoWriteAccess:IoReadAccess));
			} except(EXCEPTION_EXECUTE_HANDLER) {
				RC = STATUS_INVALID_USER_BUFFER;
			}
		}

		try_exit:	NOTHING;

	} finally {
		if (!NT_SUCCESS(RC) && PtrMdl) {
			IoFreeMdl(PtrMdl);
			// You MUST NULL out the MdlAddress field in the IRP after freeing
			// the MDL, else the I/O Manager will also attempt to free the MDL
			// pointed to by that field during I/O completion. Obviously, the
			// pointer becomes invalid once you free the allocated MDL and hence
			// you will encounter a system crash during IRP completion.
			PtrIrp->MdlAddress = NULL;
		}
		else{
			PtrIrp->MdlAddress = PtrMdl;
		}
	}

	return(RC);
}

