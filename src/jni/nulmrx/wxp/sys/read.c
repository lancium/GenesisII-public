/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    read.c

Abstract:

    This module implements the mini redirector call down routines pertaining to read
    of file system objects.

--*/

#include "precomp.h"
#pragma hdrstop

NTSTATUS GenesisReadCompletionRoutine(PRX_CONTEXT RxContext){
	RxCaptureFcb;
	GenesisGetFcbExtension(capFcb, giiFCB);
	NTSTATUS Status = STATUS_SUCCESS;
	PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(RxContext);
	PVOID buffer;	

	buffer = RxLowIoGetBufferAddress(RxContext);
	Status = pIoCompContext->Status;

	if(NT_SUCCESS(Status) && buffer != NULL){
		__try { 
			RtlCopyMemory(buffer, pIoCompContext->Context, pIoCompContext->Information);										
			Status = STATUS_SUCCESS;		
		} __except (EXCEPTION_EXECUTE_HANDLER) {
			Status = GetExceptionCode();							
		}	
	}	
	else if(NT_SUCCESS(Status)){		
		//Staus is ok, buffer should not be null but is
		Status = STATUS_INSUFFICIENT_RESOURCES;
	}

	RxFreePool(pIoCompContext->Context);
	pIoCompContext->Context = NULL;

	ExReleaseFastMutex(&giiFCB->ExclusiveLock);
	
	RxSetIoStatusStatus(RxContext, Status);
	RxSetIoStatusInfo(RxContext, (NT_SUCCESS(Status) ? pIoCompContext->Information : 0));

	return Status;
}

NTSTATUS
NulMRxRead(
    IN PRX_CONTEXT RxContext
    )
/*++

Routine Description:

   This routine handles network read requests.

Arguments:

    RxContext - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;
    RxCaptureFcb;
	RxCaptureFobx;
	GenesisGetFcbExtension(capFcb, giiFcb);
	GenesisGetCcbExtension(capFobx, giiCcb);


    BOOLEAN SynchronousIo = !BooleanFlagOn(RxContext->Flags,RX_CONTEXT_FLAG_ASYNC_OPERATION);        	
	BOOLEAN PagingIo = BooleanFlagOn(RxContext->Flags, IRP_PAGING_IO);

	/* Genesis Specific Read info */
	LARGE_INTEGER ByteOffset;
	ULONG ByteCount;
	
	PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(RxContext);
	PFILE_OBJECT FileObject = RxContext->CurrentIrpSp->FileObject;

	BOOLEAN CompleteIrp = FALSE;
	BOOLEAN PostRequest = FALSE;    

	PAGED_CODE();	

	if(giiFcb->isDirectory){
		Status = STATUS_FILE_IS_A_DIRECTORY;		
		try_return(Status);
	}

	/* Get offset param */	
	ByteOffset.QuadPart = RxContext->LowIoContext.ParamsFor.ReadWrite.ByteOffset;
	ByteCount = RxContext->LowIoContext.ParamsFor.ReadWrite.ByteCount;    
		
	// If the read starts beyond End of File, return EOF.
	if (RtlLargeIntegerGreaterThan(ByteOffset, capFcb->Header.FileSize)
			|| (giiFcb->State == GENII_STATE_NOT_INITIALIZED)){        
        Status = STATUS_END_OF_FILE;
        try_return(Status);
    }    
    
    //Initialize the completion context in the RxContext
    ASSERT( sizeof(*pIoCompContext) == MRX_CONTEXT_SIZE );
    RtlZeroMemory( pIoCompContext, sizeof(*pIoCompContext) );
    
    if( SynchronousIo ) {
		GIIPrint(("GenesisDrive:  Read is sync\n"));
        pIoCompContext->IoType = IO_TYPE_SYNCHRONOUS;
    } else {        
        pIoCompContext->IoType = IO_TYPE_ASYNC;
    }

	//// If this happens to be a MDL read complete request, then
	//// there is not much processing that the FSD has to do.
	if (RxContext->CurrentIrpSp->MinorFunction & IRP_MN_COMPLETE) {
	//	// Caller wants to tell the Cache Manager that a previously
	//	// allocated MDL can be freed.
	//	GenesisMdlComplete(RxContext);
	//	// The IRP has been completed.
	//	CompleteIrp = FALSE;
		//GIIPrint(("NulMrxRead:  IRP_MN_COMPLETE Received"));
	//	try_return(Status = STATUS_SUCCESS);
	}

	//// If this is a request at IRQL DISPATCH_LEVEL, then post
	//// the request (your FSD may choose to process it synchronously
	//// if you implement the support correctly; obviously you will be
	//// quite constrained in what you can do at such IRQL).
	if (RxContext->CurrentIrpSp->MinorFunction & IRP_MN_DPC) {
	//	CompleteIrp = FALSE;
	//	PostRequest = TRUE;
		//GIIPrint(("NulMrxRead:  IRP_MN_DPC Received"));
	//	try_return(Status = STATUS_PENDING);
	}

	RxContext->LowIoContext.CompletionRoutine = GenesisReadCompletionRoutine;	

	ExAcquireFastMutex(&giiFcb->ExclusiveLock);

	//Store read data here
	pIoCompContext->Context = RxAllocatePoolWithTag(PagedPool, ByteCount, 'abcd');

	//Sends Genii read request
	Status = GenesisSendInvertedCall(RxContext, GENII_READ, !SynchronousIo);

	//Something could go wrong (only wait if something will actually come back to free you)
	if(NT_SUCCESS(Status)){

		if(SynchronousIo){
			PVOID requestBuffer = NULL;

			KeWaitForSingleObject(&(giiFcb->InvertedCallSemaphore), Executive, KernelMode, FALSE, NULL);			
			Status = STATUS_SUCCESS;

			if(!PagingIo){
				FileObject->CurrentByteOffset.QuadPart = 
					ByteOffset.QuadPart + pIoCompContext->Information;
			}
			else{
				// Paging IO Recv
			}
		}
	}

try_exit:	NOTHING;					

	if(Status != STATUS_PENDING){
		// Read completed
	}
    
    return(Status);
} 

ULONG GenesisPrepareReadParams(PRX_CONTEXT RxContext, PVOID buffer){
    RxCaptureFcb;
	RxCaptureFobx;    
	GenesisGetFcbExtension(capFcb, giiFcb);
	GenesisGetCcbExtension(capFobx, giiCcb);

	ULONG FileID, Length;		
	LARGE_INTEGER ByteOffset;
	LARGE_INTEGER EndOffset;

	char * myBuffer = (char *) buffer;	
	BOOLEAN SynchronousIo = !BooleanFlagOn(RxContext->Flags,RX_CONTEXT_FLAG_ASYNC_OPERATION);     

	/* Get parameters */
	FileID = giiCcb->GenesisFileID;			
	ByteOffset.QuadPart = RxContext->LowIoContext.ParamsFor.ReadWrite.ByteOffset;

	//Fix byte offset
	if(SynchronousIo && (ByteOffset.LowPart == FILE_USE_FILE_POINTER_POSITION 
		&& ByteOffset.HighPart == -1)){
			ByteOffset = RxContext->CurrentIrpSp->FileObject->CurrentByteOffset;
	}

	Length = RxContext->LowIoContext.ParamsFor.ReadWrite.ByteCount;	

	// Cap the size
	if (Length > USER_KERNEL_MAX_TRANSFER_SIZE) {
		Length = USER_KERNEL_MAX_TRANSFER_SIZE;
	}

    //  If the read extends beyond EOF, truncate the read (fixes length	
	EndOffset.QuadPart = ByteOffset.QuadPart + Length;	
	if (RtlLargeIntegerGreaterThan(EndOffset, capFcb->Header.FileSize)){
		RxContext->CurrentIrpSp->Parameters.Read.Length = (ULONG)(capFcb->Header.FileSize.QuadPart - ByteOffset.QuadPart);
		RxContext->LowIoContext.ParamsFor.ReadWrite.ByteCount = (ULONG)(capFcb->Header.FileSize.QuadPart - ByteOffset.QuadPart);
		Length = (ULONG)(capFcb->Header.FileSize.QuadPart - ByteOffset.QuadPart);
    }

	GIIPrint(("GenesisDrive:  Read for fileID %d, ByteOffset is %I64d, ByteLength is %llu \n", FileID, ByteOffset.QuadPart, Length));

	//Let's copy other params
	RtlCopyMemory(myBuffer, &FileID, sizeof(LONG));
	myBuffer += sizeof(LONG);
	RtlCopyMemory(myBuffer, &ByteOffset.QuadPart, sizeof(LONGLONG));
	myBuffer += sizeof(LONGLONG);
	RtlCopyMemory(myBuffer, &Length, sizeof(LONG));
	myBuffer += sizeof(ULONG);

	return ((sizeof(LONG) * 2) + sizeof(LONGLONG));
}