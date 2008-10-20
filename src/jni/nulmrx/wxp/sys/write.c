/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    write.c

Abstract:

    This module implements the mini redirector call down routines pertaining
    to write of file system objects.

--*/

#include "precomp.h"
#pragma hdrstop


NTSTATUS GenesisWriteCompletionRoutine(PRX_CONTEXT RxContext){
	RxCaptureFcb;
	GenesisGetFcbExtension(capFcb, giiFCB);
	PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(RxContext);
	PGENESIS_SRV_OPEN giiSrvOpen = (PGENESIS_SRV_OPEN)RxContext->pRelevantSrvOpen->Context2;

	if(NT_SUCCESS(pIoCompContext->Status)){
		giiSrvOpen->ServerFileSize = capFcb->Header.FileSize;
	}

	ExReleaseFastMutex(&giiFCB->ExclusiveLock);

	RxContext->IoStatusBlock.Information = pIoCompContext->Information;
	RxContext->IoStatusBlock.Status = pIoCompContext->Status;
	return pIoCompContext->Status;
}

//  The local debug trace level
#define Dbg                              (DEBUG_TRACE_WRITE)

NTSTATUS
NulMRxWrite (
      IN PRX_CONTEXT RxContext)

/*++

Routine Description:

   This routine opens a file across the network.

Arguments:

    RxContext - the RDBSS context

Return Value:

    RXSTATUS - The return status for the operation

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

    RxTraceEnter("NulMRxWrite");

	PAGED_CODE();

	DbgPrint("NulMRxWrite:  Started for file: %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);	

	if(giiFcb->isDirectory){
		Status = STATUS_INVALID_DEVICE_REQUEST;
		DbgPrint("NulMRxWrite:  Write attempted on directory!\n");
		try_return(Status);
	}

	/* Get offset param */	
	ByteOffset.QuadPart = RxContext->LowIoContext.ParamsFor.ReadWrite.ByteOffset;
	ByteCount = RxContext->LowIoContext.ParamsFor.ReadWrite.ByteCount;  
    
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
		DbgPrint("NulMrxWrite:  IRP_MN_COMPLETE Received");
	//	try_return(Status = STATUS_SUCCESS);
	}

	//// If this is a request at IRQL DISPATCH_LEVEL, then post
	//// the request (your FSD may choose to process it synchronously
	//// if you implement the support correctly; obviously you will be
	//// quite constrained in what you can do at such IRQL).
	if (RxContext->CurrentIrpSp->MinorFunction & IRP_MN_DPC) {
	//	CompleteIrp = FALSE;
	//	PostRequest = TRUE;
		DbgPrint("NulMrxWrite:  IRP_MN_DPC Received");
	//	try_return(Status = STATUS_PENDING);
	}

	RxContext->LowIoContext.CompletionRoutine = GenesisWriteCompletionRoutine;	

	ExAcquireFastMutex(&giiFcb->ExclusiveLock);

	//Sends Genii read request
	Status = GenesisSendInvertedCall(RxContext, GENII_WRITE, !SynchronousIo);

	//Something could go wrong (only wait if something will actually come back to free you)
	if(NT_SUCCESS(Status)){
		if(SynchronousIo){
			PVOID requestBuffer = NULL;

			KeWaitForSingleObject(&(giiFcb->InvertedCallSemaphore), Executive, KernelMode, FALSE, NULL);			

			if(!PagingIo){
				FileObject->CurrentByteOffset.QuadPart = ByteOffset.QuadPart + pIoCompContext->Information;
			}
			else{
				DbgPrint("NulMrxWrite:  Paging IO Recv'd\n");
			}
		}
	}

    RxDbgTrace(0, Dbg, ("Status = %x Info = %x\n",RxContext->IoStatusBlock.Status,RxContext->IoStatusBlock.Information));

try_exit:	NOTHING;					

	if(Status != STATUS_PENDING){
		DbgPrint("NulMRxWrite:  Completed for file: %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
	}

    RxTraceLeave(Status);
    return(Status);
} 

ULONG GenesisPrepareWriteParams(PRX_CONTEXT RxContext, PVOID buffer, PBOOLEAN isTruncateAppend, PBOOLEAN isTruncateWrite){
    RxCaptureFcb;
	RxCaptureFobx;    
	GenesisGetFcbExtension(capFcb, giiFcb);
	GenesisGetCcbExtension(capFobx, giiCcb);
	GenesisGetSrvOpenExtension(RxContext->pRelevantSrvOpen, giiSrvOpen);

	LONG FileID;
	ULONG Length;
	LARGE_INTEGER ByteOffset;	
	LARGE_INTEGER EndOffset;
	PVOID writeData;

	char * myBuffer = (char *) buffer;	
	BOOLEAN SynchronousIo = !BooleanFlagOn(RxContext->Flags,RX_CONTEXT_FLAG_ASYNC_OPERATION);     
	
	*isTruncateAppend = FALSE;
	*isTruncateWrite = FALSE;

	/* Get parameters */
	FileID = giiCcb->GenesisFileID;			
	ByteOffset = RxContext->CurrentIrpSp->Parameters.Read.ByteOffset;
	writeData = RxLowIoGetBufferAddress(RxContext);

	//Fix byte offset
	if(SynchronousIo && (ByteOffset.LowPart == FILE_USE_FILE_POINTER_POSITION 
		&& ByteOffset.HighPart == -1)){
			//Continuing a previous write
			ByteOffset = RxContext->CurrentIrpSp->FileObject->CurrentByteOffset;
	}
	else if(ByteOffset.LowPart == FILE_WRITE_TO_END_OF_FILE && ByteOffset.HighPart == -1){
		//If append  
		ByteOffset = capFcb->Header.FileSize;
	}

	//Fix length (checks if length + offset is bigger than valid data length)
	Length = RxContext->CurrentIrpSp->Parameters.Write.Length;
	EndOffset.QuadPart = ByteOffset.QuadPart + Length;
	if(RtlLargeIntegerGreaterThan(EndOffset, capFcb->Header.ValidDataLength)){
		Length = (ULONG)(capFcb->Header.ValidDataLength.QuadPart - ByteOffset.QuadPart);
	}

	//Do we need to truncate before writing?
	if(RtlLargeIntegerLessThan(capFcb->Header.FileSize, giiSrvOpen->ServerFileSize)){		
		if((capFcb->Header.FileSize.QuadPart + ByteOffset.QuadPart) == capFcb->Header.FileSize.QuadPart){
			*isTruncateAppend = TRUE;
			DbgPrint("IsTruncateAppend!\n");
		}
		else{
			*isTruncateWrite = TRUE;
			DbgPrint("IsTruncateWrite!\n");
		}
	}

	DbgPrint("GenesisWrite:  FileID is %d, ByteOffset is %I64d, ByteLength is %d \n", FileID, ByteOffset.QuadPart, Length);

	//Let's copy other params
	RtlCopyMemory(myBuffer, &FileID, sizeof(LONG));
	myBuffer += sizeof(LONG);
	RtlCopyMemory(myBuffer, &ByteOffset.QuadPart, sizeof(LONGLONG));
	myBuffer += sizeof(LONGLONG);
	RtlCopyMemory(myBuffer, &Length, sizeof(LONG));
	myBuffer += sizeof(LONG);
	RtlCopyMemory(myBuffer, writeData, Length);	

	return ((sizeof(LONG) * 2) + sizeof(LONGLONG) + Length);
}



