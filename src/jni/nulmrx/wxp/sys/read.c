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

//
//  The local debug trace level
//

#define Dbg                              (DEBUG_TRACE_READ)

NTSTATUS GenesisReadCompletionRoutine(PRX_CONTEXT RxContext){
	RxCaptureFcb;
	GenesisGetFcbExtension(capFcb, giiFCB);
	NTSTATUS Status = STATUS_SUCCESS;
	PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(RxContext);
	PVOID buffer;

	DbgPrint("Completing Call\n");

	buffer = RxLowIoGetBufferAddress(RxContext);
	Status = pIoCompContext->Status;

	if(NT_SUCCESS(Status) && buffer != NULL){
		__try { 
			RtlCopyMemory(buffer, pIoCompContext->Context, pIoCompContext->Information);							
			DbgPrint("NulMRxRead:  Copied successful: %d bytes\n", pIoCompContext->Information);
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
	LONG ByteOffset;
	LONG ByteCount;
	
	PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(RxContext);
	PFILE_OBJECT FileObject = RxContext->CurrentIrpSp->FileObject;

	BOOLEAN CompleteIrp = FALSE;
	BOOLEAN PostRequest = FALSE;

    RxTraceEnter("NulMRxRead");    

	PAGED_CODE();

	DbgPrint("NulMRxRead:  Started for file: %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);

	if(giiFcb->isDirectory){
		Status = STATUS_INVALID_DEVICE_REQUEST;
		DbgPrint("NulMRxRead:  Read attempted on directory!\n");
		try_return(Status);
	}

	/* Get offset param */	
	ByteOffset = ((LONG)RxContext->LowIoContext.ParamsFor.ReadWrite.ByteOffset & 0x000000007FFFFFFF);
	ByteCount = RxContext->LowIoContext.ParamsFor.ReadWrite.ByteCount;

    // If the read starts beyond End of File, return EOF.
	if (RtlLargeIntegerGreaterThan(RtlConvertLongToLargeInteger(ByteOffset + ByteCount), capFcb->Header.FileSize)
			|| (giiFcb->State == GENII_STATE_NOT_INITIALIZED)){
        RxDbgTrace( 0, Dbg, ("End of File\n", 0 ));
		DbgPrint("NulMRxRead:  End of file reached\n");

        Status = STATUS_END_OF_FILE;
        try_return(Status);
    }    
	if(ByteOffset < 0 || ByteCount < 0){
		DbgPrint("NulMrxRead:  Invalid Paramater (negative)\n");
		Status = STATUS_INVALID_PARAMETER;
		try_return(Status);
	}
    
    //Initialize the completion context in the RxContext
    ASSERT( sizeof(*pIoCompContext) == MRX_CONTEXT_SIZE );
    RtlZeroMemory( pIoCompContext, sizeof(*pIoCompContext) );
    
    if( SynchronousIo ) {
        DbgPrint("This I/O is sync\n");
        pIoCompContext->IoType = IO_TYPE_SYNCHRONOUS;
    } else {
        DbgPrint("This I/O is async\n");
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
		DbgPrint("NulMrxRead:  IRP_MN_COMPLETE Received");
	//	try_return(Status = STATUS_SUCCESS);
	}

	//// If this is a request at IRQL DISPATCH_LEVEL, then post
	//// the request (your FSD may choose to process it synchronously
	//// if you implement the support correctly; obviously you will be
	//// quite constrained in what you can do at such IRQL).
	if (RxContext->CurrentIrpSp->MinorFunction & IRP_MN_DPC) {
	//	CompleteIrp = FALSE;
	//	PostRequest = TRUE;
		DbgPrint("NulMrxRead:  IRP_MN_DPC Received");
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
				FileObject->CurrentByteOffset = 
					RtlConvertLongToLargeInteger(ByteOffset + pIoCompContext->Information);
			}
			else{
				DbgPrint("NulMrxRead:  Paging IO Recv'd\n");
			}
		}
	}

    RxDbgTrace(0, Dbg, ("Status = %x Info = %x\n",RxContext->IoStatusBlock.Status,RxContext->IoStatusBlock.Information));

try_exit:	NOTHING;					

	if(Status != STATUS_PENDING){
		DbgPrint("NulMRxRead:  Completed for file: %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
	}

    RxTraceLeave(Status);
    return(Status);
} 

ULONG GenesisPrepareReadParams(PRX_CONTEXT RxContext, PVOID buffer){
    RxCaptureFcb;
	RxCaptureFobx;    
	GenesisGetFcbExtension(capFcb, giiFcb);
	GenesisGetCcbExtension(capFobx, giiCcb);

	LONG FileID, Length, Offset;		
	LARGE_INTEGER ByteOffset;

	char * myBuffer = (char *) buffer;	
	BOOLEAN SynchronousIo = !BooleanFlagOn(RxContext->Flags,RX_CONTEXT_FLAG_ASYNC_OPERATION);     

	/* Get parameters */
	FileID = giiCcb->GenesisFileID;			
	ByteOffset = RxContext->CurrentIrpSp->Parameters.Read.ByteOffset;

	//Fix byte offset
	if(SynchronousIo && (ByteOffset.LowPart == FILE_USE_FILE_POINTER_POSITION 
		&& ByteOffset.HighPart == -1)){
			ByteOffset = RxContext->CurrentIrpSp->FileObject->CurrentByteOffset;
	}

	//Switch to ulong here
	Offset = (ByteOffset.LowPart & 0x7FFFFFFF);
	Length = (RxContext->CurrentIrpSp->Parameters.Read.Length & 0x7FFFFFF);
    
    //  If the read extends beyond EOF, truncate the read (fixes length
	if (RtlLargeIntegerGreaterThan(RtlConvertLongToLargeInteger(Length + Offset), capFcb->Header.FileSize)){
		RxContext->CurrentIrpSp->Parameters.Read.Length = (ULONG)(capFcb->Header.FileSize.LowPart - Offset);
		RxContext->LowIoContext.ParamsFor.ReadWrite.ByteCount = (ULONG)(capFcb->Header.FileSize.LowPart - Offset);
		Length = (ULONG)(capFcb->Header.FileSize.LowPart - Offset);
    }

	DbgPrint("FileID is %d, ByteOffset is %d, ByteLength is %d \n", FileID, Offset, Length);       

	//Let's copy other params
	RtlCopyMemory(myBuffer, &FileID, sizeof(LONG));
	myBuffer += sizeof(LONG);
	RtlCopyMemory(myBuffer, &Offset, sizeof(LONG));
	myBuffer += sizeof(LONG);
	RtlCopyMemory(myBuffer, &Length, sizeof(LONG));
	myBuffer += sizeof(LONG);

	return (sizeof(LONG) * 3);
}