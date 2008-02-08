/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    openclos.c

Abstract:

    This module implements the mini redirector call down routines pertaining to opening/
    closing of file/directories.

--*/

#include "precomp.h"
#pragma hdrstop

//
//  The debug trace level
//

#define Dbg                              (DEBUG_TRACE_CREATE)

//
//  forwards & pragmas
//

PCHAR ProbeAndLockName(PRX_CONTEXT RxContext, PUNICODE_STRING targetString, PMDL *mdl);

NTSTATUS
NulMRxCreateFileSuccessTail (
    PRX_CONTEXT     RxContext,
    PBOOLEAN        MustRegainExclusiveResource,
    RX_FILE_TYPE    StorageType,
    ULONG           CreateAction,
    FILE_BASIC_INFORMATION*     pFileBasicInfo,
    FILE_STANDARD_INFORMATION*  pFileStandardInfo
    );

VOID
NulMRxSetSrvOpenFlags (
    PRX_CONTEXT     RxContext,
    RX_FILE_TYPE    StorageType,
    PMRX_SRV_OPEN   SrvOpen
    );
  
void GenesisInitializeFCB(PGENESIS_FCB fcb){ 		
	RtlZeroMemory(fcb, sizeof(GenesisFCB));

	KeQuerySystemTime(&(fcb->OpenTime));
	fcb->CreateTime = fcb->AccessedTime = fcb->ModifiedTime = fcb->OpenTime;

	fcb->DirectorySize = 0;
	fcb->DirectoryListing = NULL;
	fcb->isDirectory = FALSE;			
	fcb->State = GENII_STATE_NOT_INITIALIZED;	
	fcb->GenesisTempFileID = GENII_FILE_INVALID;

	KeInitializeSemaphore(&(fcb->FcbPhore), 1, 1);
}

void GenesisInitializeCCB(PGENESIS_CCB ccb, ULONG tempFileID){
	char * buffer = ExAllocatePool(PagedPool, 256);	
	buffer[0] = '*';
	buffer[1] = '\0';

	RtlZeroMemory(ccb, sizeof(GenesisCCB));

	//Open, info, read and write
	ccb->GenesisFileID = tempFileID;
	ccb->CurrentByteOffset.QuadPart = 0;

	//Query Directory
	ccb->FileIndex = 0;	

	//Target Name for QD
	ccb->Target.Buffer = buffer;
	ccb->Target.Length = 1;
	ccb->Target.MaximumLength = 255;	
}

void GenesisInitializeSrvOpen(PGENESIS_SRV_OPEN srvOpen, PGENESIS_FCB relatedFcb){ 		
	RtlZeroMemory(srvOpen, sizeof(GenesisSrvOpen));

	srvOpen->ServerAccessedTime = srvOpen->ServerModifiedTime = srvOpen->ServerCreateTime = relatedFcb->OpenTime;	
}



#ifdef ALLOC_PRAGMA
#pragma alloc_text(PAGE, NulMRxCreate)
#pragma alloc_text(PAGE, NulMRxShouldTryToCollapseThisOpen)
#pragma alloc_text(PAGE, NulMRxCreateFileSuccessTail)
#pragma alloc_text(PAGE, NulMRxSetSrvOpenFlags)
#endif

NTSTATUS
NulMRxShouldTryToCollapseThisOpen (
    IN PRX_CONTEXT RxContext
    )
/*++

Routine Description:

   This routine determines if the mini knows of a good reason not
   to try collapsing on this open. Presently, the only reason would
   be if this were a copychunk open.

Arguments:

    RxContext - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation
        SUCCESS --> okay to try collapse
        other (MORE_PROCESSING_REQUIRED) --> dont collapse

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;
    RxCaptureFcb;

    PAGED_CODE();

    return Status;
}

NTSTATUS
NulMRxCreate(
      IN OUT PRX_CONTEXT RxContext
      )
/*++

Routine Description:

   This routine opens a file across the network

Arguments:

    RxContext - the RDBSS context

Return Value:

    RXSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;    
    RxCaptureFcb;
    PMRX_SRV_OPEN SrvOpen = RxContext->pRelevantSrvOpen;
    PMRX_NET_ROOT NetRoot = capFcb->pNetRoot; 
    PUNICODE_STRING RemainingName = SrvOpen->pAlreadyPrefixedName;

	/* Genii Create stuff */
	PCHAR ProbedName;
	PMDL mdl;
	PNULMRX_DEVICE_EXTENSION dataExt = (PNULMRX_DEVICE_EXTENSION)
			((PBYTE)(NulMRxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT));		
	PIO_STACK_LOCATION PtrIoStackLocation;
	PFILE_OBJECT NewFileObject;	
	PFILE_OBJECT RelatedFileObject;
	PNULMRX_FCB_EXTENSION RelatedFileObjectFcb;	
	ULONG RequestedOptions;
	ULONG RequestedDisposition;
	UINT8 FileAttributes;
	USHORT ShareAccess;
	ULONG tempFileID = GENII_FILE_INVALID;
	PULONG ReturnedInformation = &RxContext->IoStatusBlock.Information;	
	ACCESS_MASK DesiredAccess;
	wchar_t *firstCol;

	//Possible user options
	BOOLEAN DirectoryOnlyRequested, FileOnlyRequested, NoBufferingSpecified, WriteThroughRequested,
		DeleteOnCloseSpecified, NoExtAttrKnowledge, OpenByFileId, PageFileManipulation,
		OpenTargetDirectory, IgnoreCaseWhenChecking, AcquiredVCB=FALSE, CreatedFCBX=FALSE;

	/* END */

	PGENESIS_FCB giiFCB;	
	PGENESIS_SRV_OPEN giiSrvOpen;
	FILE_BASIC_INFORMATION FileBasicInfo;
    FILE_STANDARD_INFORMATION FileStandardInfo;
	BOOLEAN fMustRegainExclusiveResource=FALSE;
    RX_FILE_TYPE StorageType = FileTypeFile;
    ULONG CreateAction = FILE_OPENED;    //Look INTO this more
    
    NulMRxGetNetRootExtension(NetRoot,pNetRootExtension);	

    RxTraceEnter("NulMRxCreate");       
    RxDbgTrace(0, Dbg, ("     Attempt to open %wZ Len is %d\n", RemainingName, RemainingName->Length ));
	
	PAGED_CODE();		

    if( NetRoot->Type == NET_ROOT_DISK) {

		DbgPrint("NulMrxCreate:  Attempt to open %wZ\n", RemainingName);			

		//Lock VCB?  might as well (stop concurrent opens)
		ExAcquireResourceExclusiveLite(&(dataExt->VCBResource), TRUE);
		AcquiredVCB = TRUE;		

        RxDbgTrace(0, Dbg, ("NulMRxCreate: Type supported \n"));            
        //  Squirrel away the scatter list in the FCB extension.
        //  This is done only for data files.

		PtrIoStackLocation = RxContext->CurrentIrpSp;

		//Now we can obtain the parameters specified by the user
		NewFileObject = PtrIoStackLocation->FileObject;

		RelatedFileObject = NewFileObject->RelatedFileObject;
		RelatedFileObjectFcb = ((RelatedFileObject == NULL) ? NULL :
				(PNULMRX_FCB_EXTENSION)((PMRX_FCB)RelatedFileObject->FsContext)->Context);

		/* ----------- Get all things out of the create parameters ---------- */

		//doesn't really matter yet (R, W, X)
		DesiredAccess = PtrIoStackLocation->Parameters.Create.SecurityContext->DesiredAccess;

		//user supplied options 
		RequestedOptions = (PtrIoStackLocation->Parameters.Create.Options & FILE_VALID_OPTION_FLAGS);

		//File disposition is packed with user options
		RequestedDisposition = ((PtrIoStackLocation->Parameters.Create.Options >> 24) && 0xFF);

		//File attributes ... they don't mattah
		FileAttributes = (UINT8)(PtrIoStackLocation->Parameters.Create.FileAttributes & FILE_ATTRIBUTE_VALID_FLAGS);

		//Who R / W / D
		ShareAccess = PtrIoStackLocation->Parameters.Create.ShareAccess;		

		//First colon in name
		firstCol = wcschr(NewFileObject->FileName.Buffer, L':');
		firstCol = firstCol != NULL ? firstCol + 1 : firstCol;
		
		//If two :'s in Name this is an EA file
		if(firstCol != NULL && (wcschr(firstCol, L':') != NULL)){
			DbgPrint("NulMrxCreate:  Failed! EA not in Genesis\n");
			Status = STATUS_NO_SUCH_FILE;		
			try_return(Status);
		}

		//We don't do that EA stuff
		if(PtrIoStackLocation->Parameters.Create.EaLength > 0)
			/* || DesiredAccess & FILE_READ_EA || DesiredAccess & FILE_WRITE_EA)*/		
		{
			//ABORT!!!			
			Status = STATUS_EAS_NOT_SUPPORTED;
			DbgPrint("NulMrxCreate:  Failed! EA not supported\n");
			try_return(Status);
		}

		DirectoryOnlyRequested = ((RequestedOptions & FILE_DIRECTORY_FILE) ? TRUE : FALSE);
		FileOnlyRequested = ((RequestedOptions & FILE_NON_DIRECTORY_FILE) ? TRUE : FALSE);
		NoBufferingSpecified = ((RequestedOptions & FILE_NO_INTERMEDIATE_BUFFERING) ? TRUE : FALSE);
		WriteThroughRequested = ((RequestedOptions & FILE_WRITE_THROUGH) ? TRUE : FALSE);
		DeleteOnCloseSpecified = ((RequestedOptions & FILE_DELETE_ON_CLOSE) ? TRUE : FALSE);
		NoExtAttrKnowledge = ((RequestedOptions & FILE_NO_EA_KNOWLEDGE) ? TRUE : FALSE);		
		OpenByFileId = ((RequestedOptions & FILE_OPEN_BY_FILE_ID) ? TRUE : FALSE); //Somehow deny?		
		PageFileManipulation = ((PtrIoStackLocation->Flags & SL_OPEN_PAGING_FILE) ? TRUE : FALSE); //Don't deal with?
		OpenTargetDirectory = ((PtrIoStackLocation->Flags & SL_OPEN_TARGET_DIRECTORY) ? TRUE : FALSE); //??? book? rename
		IgnoreCaseWhenChecking = ((PtrIoStackLocation->Flags & SL_CASE_SENSITIVE) ? TRUE : FALSE);

		//Do especially for a volume open		
		if((NewFileObject->FileName.Length == 0) && (RelatedFileObject == NULL ||
			RelatedFileObjectFcb->NodeTypeCode == RDBSS_NTC_VCB)){
		
				if(OpenTargetDirectory){
					Status = STATUS_INVALID_PARAMETER;
					DbgPrint("NulMrxCreate:  Failed! Volume root is not a directory\n");
					try_return(Status);
				}
				if(DirectoryOnlyRequested){
					Status = STATUS_NOT_A_DIRECTORY;
					DbgPrint("NulMrxCreate:  Failed! Volume root is not a directory\n");
					try_return(Status);
				}
				if(RequestedDisposition != FILE_OPEN && RequestedDisposition != FILE_OPEN_IF){
					Status = STATUS_ACCESS_DENIED;
					DbgPrint("NulMrxCreate:  Failed! Wrong type of Disposition\n");
					try_return(Status);
				}

				DbgPrint("NulMrxCreate:  Volume open completed successfully\n");

				//Do something special for open volume (or not)				
				try_return(Status);
		}

		if(OpenByFileId){
			DbgPrint("NulMrxCreate: Failed!  Open By file id not supported\n");
			Status = STATUS_NOT_SUPPORTED;
			try_return(Status);
		}

		//We don't check the names at all cause of how we use names in Redirector

		RxAcquireExclusiveFcbResourceInMRx(capFcb);		
		
		//Super check of DOOM to eliminate EAS and Desktop.ini files (let's use filename)
		if(RemainingName != NULL && RemainingName->Length > 0 && 
			RemainingName->Buffer != NULL){			

			DbgPrint("NulMrxCreate:  Processing filename %wZ\n", &(NewFileObject->FileName));

			if(wcsstr(NewFileObject->FileName.Buffer, L"Desktop.ini") != NULL 
				|| wcsstr(NewFileObject->FileName.Buffer, L"desktop.ini") != NULL){
				DbgPrint("NulMrxCreate:  Desktop.ini not supported\n");
				Status = STATUS_NO_SUCH_FILE;				
				RxReleaseFcbResourceInMRx(capFcb);
				try_return(Status);
			}
		}

		if(RemainingName->Length == 0){
			//Open of root directory
			if(FileOnlyRequested || (RequestedDisposition == FILE_SUPERSEDE) ||
					RequestedDisposition == FILE_OVERWRITE ||
					RequestedDisposition == FILE_OVERWRITE_IF){
	
				Status = STATUS_FILE_IS_A_DIRECTORY;
				DbgPrint("NulMrxCreate: Failed!  Wrong options specified for root dir\n");
				try_return(Status);
			}

			//(continue normally)			
		}		

		//See if Genesis II FCB has been created yet
		if(capFcb->Context2 == NULL){
			//FCB->Context used as FCB extension for NulMrx
			capFcb->Context2 = RxAllocatePoolWithTag(NonPagedPool, sizeof(GenesisFCB), MRXGEN_FCB_POOLTAG);
			GenesisInitializeFCB((PGENESIS_FCB)capFcb->Context2);

			SrvOpen->Context2 = RxAllocatePoolWithTag(NonPagedPool, sizeof(GenesisSrvOpen), MRXGEN_FCB_POOLTAG);
			GenesisInitializeSrvOpen((PGENESIS_SRV_OPEN)SrvOpen->Context2, (PGENESIS_FCB)capFcb->Context2);
			CreatedFCBX=TRUE;
		}				

		giiFCB = (PGENESIS_FCB)capFcb->Context2;
		giiSrvOpen = (PGENESIS_SRV_OPEN)SrvOpen->Context2;

		//On behalf of Sender
		KeWaitForSingleObject(&(giiFCB->FcbPhore), Executive, KernelMode, FALSE, NULL);

		/* You would enter code here to do path traversal (but we just open up directly :-D) */
		DbgPrint("NulMrxCreate:  Checking in with Genesis\n");		
		Status = GenesisSendInvertedCall(RxContext, GENII_CREATE, FALSE);
		//Completes on return		

		//Waits for caller
		KeWaitForSingleObject(&(giiFCB->FcbPhore), Executive, KernelMode, FALSE, NULL);

		KeReleaseSemaphore(&(giiFCB->FcbPhore), IO_NO_INCREMENT, 1, FALSE);

		tempFileID = giiFCB->GenesisTempFileID;

		RxReleaseFcbResourceInMRx(capFcb);

		//FCB should already be set now (if not found this is bad!!!)
		if(giiFCB->State == GENII_STATE_NOT_FOUND)
		{
			*ReturnedInformation = FILE_DOES_NOT_EXIST;

			//Don't have permission to create a file (only reason why this would fail with these dispos)
			if(RequestedDisposition == FILE_OPEN_IF || RequestedDisposition == FILE_OVERWRITE_IF ||
				RequestedDisposition == FILE_CREATE || RequestedDisposition == FILE_SUPERSEDE){
				Status = STATUS_ACCESS_DENIED;
			}
			else{
				//No file to open
				Status = STATUS_NO_SUCH_FILE;
			}
			DbgPrint("NulMrxCreate: Genesis did not find the file\n");
			try_return(Status);
		}	
		else{
			//SrvOpen is valid now
			if(giiSrvOpen != NULL){
				giiSrvOpen->ServerFileSize = capFcb->Header.FileSize;
			}
		}

		/*	Let's do some checks now */

		//Check to see if file - dir mismatch
		if(FileOnlyRequested && giiFCB->isDirectory){
			Status = STATUS_FILE_IS_A_DIRECTORY;
			DbgPrint("NulMrxCreate: Failed!  Path is a directory\n");
			try_return(Status);
		}
		
		//Check to see if dir - file mismatch
		if(DirectoryOnlyRequested && !giiFCB->isDirectory){
			Status = STATUS_NOT_A_DIRECTORY;
			DbgPrint("NulMrxCreate: Failed!  Path is not a directory\n");
			try_return(Status);
		}

		//Genesis does security check (no need to do here)

		//Genesis does local security check (see sample code from FSINTERNALS)

		//Yay
		*ReturnedInformation = FILE_OPENED;

		//Complete CreateFile contract        
		RxDbgTrace(0,Dbg,("EOF is %d AllocSize is %d\n",(ULONG)EndOfFile,(ULONG)AllocationSize));
		
		//Let's get these attributes
		FileBasicInfo.FileAttributes = ((giiFCB->isDirectory) ? FILE_ATTRIBUTE_DIRECTORY : 
			FILE_ATTRIBUTE_NORMAL);

		//Doesn't matter (yet)		
		FileBasicInfo.CreationTime = giiFCB->CreateTime;
		FileBasicInfo.LastAccessTime = giiFCB->AccessedTime;
		FileBasicInfo.LastWriteTime = giiFCB->ModifiedTime;
		FileBasicInfo.ChangeTime = giiFCB->ModifiedTime;
		
		FileStandardInfo.DeletePending = FALSE;
		FileStandardInfo.Directory = giiFCB->isDirectory;
		FileStandardInfo.AllocationSize = capFcb->Header.AllocationSize;
		FileStandardInfo.EndOfFile = capFcb->Header.FileSize;
		FileStandardInfo.NumberOfLinks = 0;		

		//Creates FOBX and CCB structures
		Status = NulMRxCreateFileSuccessTail (    
									RxContext,
									&fMustRegainExclusiveResource,
									StorageType,
									CreateAction,
									&FileBasicInfo,
									&FileStandardInfo									
									);

		if( Status != STATUS_SUCCESS ) {            
			//  alloc error..            
			RxDbgTrace(0, Dbg, ("Failed to allocate Fobx \n"));
			DbgPrint("NulMrxCreate: Failed!  Failed to allocate Fobx\n");
			try_return(Status);
		}     
		else{
			GenesisInitializeCCB((PGENESIS_CCB)RxContext->pFobx->Context2, tempFileID);
		}
	}
	else {
        RxDbgTrace(0, Dbg, ("NulMRxCreate: Type not supported or invalid open\n"));
		DbgPrint("NulMRxCreate: Type not supported or invalid open\n");
        Status = STATUS_NOT_IMPLEMENTED;
		try_return(Status);
    }

try_exit:  NOTHING;

	// Complete the request unless we are here as part of unwinding
	//	when an exception condition was encountered, OR
	//	if the request has been deferred (i.e. posted for later handling)
	if (Status != STATUS_PENDING) {
		// If we acquired any FCB resources, release them now ...

		// If any intermediate (directory) open operations were performed,
		//	implement the corresponding close (do *not* however close
		//	the target you have opened on behalf of the caller ...).

		if (NT_SUCCESS(Status)) {
			// Update the file object such that:
			//	(a) the FsContext field points to the NTRequiredFCB field
			//		 in the FCB
			//	(b) the FsContext2 field points to the CCB created as a
			//		 result of the open operation

			// If write-through was requested, then mark the file object
			//	appropriately
			if (WriteThroughRequested) {
				NewFileObject->Flags |= FO_WRITE_THROUGH;
			}
		} else {
			//gFCB and gFOB cleaned up I Think?
		}
	}

	if (AcquiredVCB) {
		ASSERT(dataExt);
		ExReleaseResource(&(dataExt->VCBResource));				
		AcquiredVCB = FALSE;
	}

	RxTraceLeave(Status);
	DbgPrint("NulMrxCreate:  Attempt to open %wZ finished\n", RemainingName);
	return Status;
}

VOID
NulMRxSetSrvOpenFlags (
    PRX_CONTEXT  RxContext,
    RX_FILE_TYPE StorageType,
    PMRX_SRV_OPEN SrvOpen
    )
{
    PMRX_SRV_CALL SrvCall = (PMRX_SRV_CALL)RxContext->Create.pSrvCall;

    //
    //  set this only if cache manager will be used for mini-rdr handles !
    //
    SrvOpen->BufferingFlags |= (FCB_STATE_WRITECACHEING_ENABLED  |
                                FCB_STATE_FILESIZECACHEING_ENABLED |
                                FCB_STATE_FILETIMECACHEING_ENABLED |
                                FCB_STATE_WRITEBUFFERING_ENABLED |
                                FCB_STATE_LOCK_BUFFERING_ENABLED |
                                FCB_STATE_READBUFFERING_ENABLED  |
                                FCB_STATE_READCACHEING_ENABLED);
}

NTSTATUS
NulMRxCreateFileSuccessTail (
    PRX_CONTEXT  RxContext,
    PBOOLEAN MustRegainExclusiveResource,
    RX_FILE_TYPE StorageType,
    ULONG CreateAction,
    FILE_BASIC_INFORMATION* pFileBasicInfo,
    FILE_STANDARD_INFORMATION* pFileStandardInfo	
    )
/*++

Routine Description:

    This routine finishes the initialization of the fcb and srvopen for a 
successful open.

Arguments:


Return Value:

    RXSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;
    RxCaptureFcb;
    PMRX_SRV_OPEN SrvOpen = RxContext->pRelevantSrvOpen;	
    FCB_INIT_PACKET InitPacket;

    RxDbgTrace(0, Dbg, ("MRxExCreateFileSuccessTail\n"));
    PAGED_CODE();

    ASSERT( NodeType(SrvOpen) == RDBSS_NTC_SRVOPEN );
    ASSERT( NodeType(RxContext) == RDBSS_NTC_RX_CONTEXT );

    if (*MustRegainExclusiveResource) {        //this is required because of oplock breaks
        RxAcquireExclusiveFcb( RxContext, capFcb );
        *MustRegainExclusiveResource = FALSE;
    }

    // This Fobx should be cleaned up by the wrapper
    RxContext->pFobx = RxCreateNetFobx( RxContext, SrvOpen);
    if( RxContext->pFobx == NULL ) {
        return STATUS_INSUFFICIENT_RESOURCES;
    }
    
    ASSERT  ( RxIsFcbAcquiredExclusive ( capFcb )  );
    RxDbgTrace(0, Dbg, ("Storagetype %08lx/Action %08lx\n", StorageType, CreateAction ));

    RxContext->Create.ReturnedCreateInformation = CreateAction;

    RxFormInitPacket(
        InitPacket,
        &pFileBasicInfo->FileAttributes,
        &pFileStandardInfo->NumberOfLinks,
        &pFileBasicInfo->CreationTime,
        &pFileBasicInfo->LastAccessTime,
        &pFileBasicInfo->LastWriteTime,
        &pFileBasicInfo->ChangeTime,
        &pFileStandardInfo->AllocationSize,
        &pFileStandardInfo->EndOfFile,
        &pFileStandardInfo->EndOfFile);

    if (capFcb->OpenCount == 0) {
        RxFinishFcbInitialization( capFcb,
                                   RDBSS_STORAGE_NTC(StorageType),
                                   &InitPacket
                                 );
    } else {
        ASSERT( StorageType == 0 || NodeType(capFcb) ==  RDBSS_STORAGE_NTC(StorageType));
    }

    NulMRxSetSrvOpenFlags(RxContext,StorageType,SrvOpen);

    RxContext->pFobx->OffsetOfNextEaToReturn = 0;    

	//Genesis CCB initialization (happens every time)	
	RxContext->pFobx->Context2 = RxAllocatePoolWithTag(NonPagedPool, sizeof(GenesisCCB), MRXGEN_CCB_POOLTAG);		

    return Status;
}

NTSTATUS
NulMRxCollapseOpen(
      IN OUT PRX_CONTEXT RxContext
      )
/*++

Routine Description:

   This routine collapses a open locally

Arguments:

    RxContext - the RDBSS context

Return Value:

    RXSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status;        

    RxTraceEnter("NulMRxCollapseOpen");
	DbgPrint("NulMrxCollapseOpen is being called\n");    

	//Create handles this (baby)
	Status = NulMRxCreate(RxContext);
    
	DbgPrint("NulMrxCollapseOpen is finished\n");
    RxTraceLeave(Status);
    return Status;
}

NTSTATUS
NulMRxComputeNewBufferingState(
   IN OUT PMRX_SRV_OPEN   pMRxSrvOpen,
   IN     PVOID           pMRxContext,
      OUT PULONG          pNewBufferingState)
/*++

Routine Description:

   This routine maps specific oplock levels into the appropriate RDBSS
   buffering state flags

Arguments:

   pMRxSrvOpen - the MRX SRV_OPEN extension

   pMRxContext - the context passed to RDBSS at Oplock indication time

   pNewBufferingState - the place holder for the new buffering state

Return Value:


Notes:

--*/
{
    NTSTATUS Status = STATUS_NOT_IMPLEMENTED;

    DbgPrint("NulMRxComputeNewBufferingState \n");
    return(Status);
}

NTSTATUS
NulMRxDeallocateForFcb (
    IN OUT PMRX_FCB pFcb
    )
{
    NTSTATUS Status = STATUS_SUCCESS;
    NulMRxGetFcbExtension(pFcb,pFcbExtension);
    PMRX_NET_ROOT         pNetRoot = pFcb->pNetRoot;
    NulMRxGetNetRootExtension(pNetRoot,pNetRootExtension);

    RxTraceEnter("NulMRxDeallocateForFcb\n");	
	DbgPrint("NulMrxDeallocateForFcb\n");

    RxTraceLeave(Status);
    return(Status);
}

NTSTATUS
NulMRxTruncate(
      IN PRX_CONTEXT pRxContext)
/*++

Routine Description:

   This routine truncates the contents of a file system object

Arguments:

    pRxContext - the RDBSS context

Return Value:

    RXSTATUS - The return status for the operation

--*/
{
   ASSERT(!"Found a truncate");
   return STATUS_NOT_IMPLEMENTED;
}

NTSTATUS
NulMRxCleanupFobx(
      IN PRX_CONTEXT RxContext)
/*++

Routine Description:

   This routine cleansup a file system object...normally a noop. unless it's a pipe in which case
   we do the close at cleanup time and mark the file as being not open.

Arguments:

    pRxContext - the RDBSS context

Return Value:

    RXSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;
    PUNICODE_STRING RemainingName;
    RxCaptureFcb; RxCaptureFobx;
	GenesisGetFcbExtension(capFcb, giiFCB);
	GenesisGetCcbExtension(capFobx, geniiCCB);

    NODE_TYPE_CODE TypeOfOpen = NodeType(capFcb);

    PMRX_SRV_OPEN SrvOpen = capFobx->pSrvOpen;

    BOOLEAN SearchHandleOpen = FALSE;

    PAGED_CODE();

    ASSERT( NodeType(SrvOpen) == RDBSS_NTC_SRVOPEN );
    ASSERT ( NodeTypeIsFcb(capFcb) );

    RxDbgTrace( 0, Dbg, ("NulMRxCleanupFobx\n"));
	DbgPrint("NulMRxCleanupFobx for %d\n", geniiCCB->GenesisFileID);	

    if (FlagOn(capFcb->FcbState,FCB_STATE_ORPHANED)) {
       RxDbgTrace( 0, Dbg, ("File orphaned\n"));
       return (STATUS_SUCCESS);
    }

    if ((capFcb->pNetRoot->Type != NET_ROOT_PIPE) && !SearchHandleOpen) {
       RxDbgTrace( 0, Dbg, ("File not for closing at cleanup\n"));
       return (STATUS_SUCCESS);
    }

	DbgPrint("NulMRxCleanupFobx for %d finished\n", geniiCCB->GenesisFileID);	
    RxDbgTrace( 0, Dbg, ("NulMRxCleanup  exit with status=%08lx\n", Status ));	

    return(Status);
}

NTSTATUS
NulMRxForcedClose(
      IN PMRX_SRV_OPEN pSrvOpen)
/*++

Routine Description:

   This routine closes a file system object

Arguments:

    pSrvOpen - the instance to be closed

Return Value:

    RXSTATUS - The return status for the operation

Notes:



--*/
{
	PGENESIS_FCB giiFCB;

    RxDbgTrace( 0, Dbg, ("NulMRxForcedClose\n"));

	giiFCB = (PGENESIS_FCB)pSrvOpen->pFcb->Context2;
	DbgPrint("NulMRxForcedClose for %wZ\n", pSrvOpen->pAlreadyPrefixedName);

	if(giiFCB != NULL){

		//Make sure no one else is trying to edit this
		KeWaitForSingleObject(&(giiFCB->FcbPhore), Executive, KernelMode, FALSE, NULL);
		KeReleaseSemaphore(&(giiFCB->FcbPhore), 0, 1, FALSE);
	}

    return STATUS_SUCCESS;
}

//
//  The local debug trace level
//

#undef  Dbg
#define Dbg                              (DEBUG_TRACE_CLOSE)

NTSTATUS
NulMRxCloseSrvOpen(
      IN     PRX_CONTEXT   RxContext
      )
/*++

Routine Description:

   This routine closes a file across the network

Arguments:

    RxContext - the RDBSS context

Return Value:

    RXSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;	
    
    RxCaptureFcb;
    RxCaptureFobx;

	GenesisGetFcbExtension(capFcb, giiFCB);
	GenesisGetCcbExtension(capFobx, giiCCB);

    PMRX_SRV_OPEN   pSrvOpen = capFobx->pSrvOpen;
    PUNICODE_STRING RemainingName = pSrvOpen->pAlreadyPrefixedName;
    PMRX_SRV_OPEN   SrvOpen;
    NODE_TYPE_CODE  TypeOfOpen = NodeType(capFcb);
    PMRX_NET_ROOT   pNetRoot = capFcb->pNetRoot;
    NulMRxGetNetRootExtension(pNetRoot,pNetRootExtension);

	giiFCB = (PGENESIS_FCB)capFcb->Context2;

    RxDbgTrace( 0, Dbg, ("NulMRxCloseSrvOpen \n"));
	DbgPrint("NulMrxCloseSrvOpen for %wZ\n", pSrvOpen->pAlreadyPrefixedName);		

	//Only makes sense for files that were opened correctly
	if(giiFCB->State == GENII_STATE_HAVE_INFO && !giiFCB->isDirectory){

		//Close this file handle on the Genesis Side
		Status = GenesisSendInvertedCall(RxContext, GENII_CLOSE, FALSE);

		//Waits for caller
		KeWaitForSingleObject(&(giiFCB->FcbPhore), Executive, KernelMode, FALSE, NULL);		
	}	

    return(Status);
}

NTSTATUS
NulMRxDeallocateForFobx (
    IN OUT PMRX_FOBX pFobx
    )
{
	GenesisGetCcbExtension(pFobx,geniiCCB);	

    RxDbgTrace( 0, Dbg, ("NulMRxDeallocateForFobx\n"));		

	DbgPrint("NulMRxDeallocateForFobx for fileid %d\n", geniiCCB->GenesisFileID);

    return(STATUS_SUCCESS);
}