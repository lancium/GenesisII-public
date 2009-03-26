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

NTSTATUS NulMRxQueryVolumeInformation(IN OUT PRX_CONTEXT RxContext)
/*++

Routine Description:

   This routine queries the volume information

Arguments:

    pRxContext         - the RDBSS context

    FsInformationClass - the kind of Fs information desired.

    pBuffer            - the buffer for copying the information

    pBufferLength      - the buffer length ( set to buffer length on input and set
                         to the remaining length on output)

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
	NTSTATUS Status = STATUS_NOT_SUPPORTED;    

    ULONG    OriginalLength = RxContext->Info.LengthRemaining;
    FS_INFORMATION_CLASS FsInformationClass = RxContext->Info.FsInformationClass;
    PVOID OriginalBuffer = RxContext->Info.Buffer;
    UNICODE_STRING ustrVolume;
    ULONG BytesToCopy;

	RxCaptureFcb;    

    switch( FsInformationClass ) {
        case FileFsVolumeInformation:
		{
    		PFILE_FS_VOLUME_INFORMATION pVolInfo = (PFILE_FS_VOLUME_INFORMATION) OriginalBuffer;
            RtlZeroMemory( pVolInfo, sizeof(FILE_FS_VOLUME_INFORMATION));			
            pVolInfo->VolumeCreationTime.QuadPart = 0;
            pVolInfo->VolumeSerialNumber = 0x00000001;
            pVolInfo->VolumeLabelLength = wcslen(GENESIS_VOLUME_NAME) * sizeof(WCHAR);
            pVolInfo->SupportsObjects = FALSE;			
            RtlInitUnicodeString( &ustrVolume, GENESIS_VOLUME_NAME);			
            RxContext->Info.LengthRemaining -= FIELD_OFFSET(FILE_FS_VOLUME_INFORMATION, VolumeLabel[0]);

            if (RxContext->Info.LengthRemaining >= (LONG)pVolInfo->VolumeLabelLength) {
                BytesToCopy = pVolInfo->VolumeLabelLength;
            } else {
                BytesToCopy = RxContext->Info.LengthRemaining;
            }

            RtlCopyMemory( &pVolInfo->VolumeLabel[0], (PVOID)ustrVolume.Buffer, BytesToCopy );
            RxContext->Info.LengthRemaining -= BytesToCopy;
            pVolInfo->VolumeLabelLength = BytesToCopy;
			RxContext->Info.LengthRemaining -= sizeof(FILE_FS_VOLUME_INFORMATION);          
            Status = STATUS_SUCCESS;            
			RxSetIoStatusInfo(RxContext, sizeof(FILE_FS_VOLUME_INFORMATION));					
		}
		break;
        case FileFsLabelInformation:			
		{
			WCHAR *label = L"Genesis";
			PFILE_FS_LABEL_INFORMATION pLabelInfo = (PFILE_FS_LABEL_INFORMATION) OriginalBuffer;
			RtlZeroMemory(pLabelInfo, sizeof(FILE_FS_LABEL_INFORMATION) );				

			//No label
			RtlCopyMemory(pLabelInfo->VolumeLabel, label, wcslen(label) * sizeof(WCHAR));
			pLabelInfo->VolumeLabelLength = wcslen(label) * sizeof(WCHAR);
			RxContext->Info.LengthRemaining -= sizeof(FILE_FS_LABEL_INFORMATION);
			Status = STATUS_SUCCESS;
			RxSetIoStatusInfo(RxContext, sizeof(FILE_FS_LABEL_INFORMATION));                   
		}
		break;
        case FileFsSizeInformation:
		{
			PFILE_FS_SIZE_INFORMATION pSizeInfo = (PFILE_FS_SIZE_INFORMATION) OriginalBuffer;
			RtlZeroMemory(pSizeInfo, sizeof(FILE_FS_SIZE_INFORMATION));				

			//Faked info!
			//4KB block size.  64KB Allocation Size.  1GB used of 8GB for user.
			pSizeInfo->BytesPerSector = 4096;
			pSizeInfo->SectorsPerAllocationUnit = 16;
			pSizeInfo->AvailableAllocationUnits = RtlConvertLongToLargeInteger(112000);
			pSizeInfo->TotalAllocationUnits = RtlConvertLongToLargeInteger(128000);
			RxContext->Info.LengthRemaining -= sizeof(FILE_FS_SIZE_INFORMATION);
			Status = STATUS_SUCCESS;            
			RxSetIoStatusInfo(RxContext, sizeof(FILE_FS_SIZE_INFORMATION));
		}
		break;
        case FileFsDeviceInformation:            
		{
			PFILE_FS_DEVICE_INFORMATION pDevInfo = (PFILE_FS_DEVICE_INFORMATION) OriginalBuffer;
			RtlZeroMemory(pDevInfo, sizeof(FILE_FS_DEVICE_INFORMATION) );			
			pDevInfo->DeviceType = FILE_DEVICE_NETWORK_FILE_SYSTEM;
			pDevInfo->Characteristics = FILE_REMOTE_DEVICE;						
			
			RxContext->Info.LengthRemaining -= sizeof(FILE_FS_DEVICE_INFORMATION);
			Status = STATUS_SUCCESS;            
			RxSetIoStatusInfo(RxContext, sizeof(FILE_FS_DEVICE_INFORMATION));
		}
		break;
        case FileFsAttributeInformation:
		{
			PFILE_FS_ATTRIBUTE_INFORMATION pAttribInfo = (PFILE_FS_ATTRIBUTE_INFORMATION) OriginalBuffer;
			RtlZeroMemory(pAttribInfo, sizeof(FILE_FS_ATTRIBUTE_INFORMATION));

			pAttribInfo->FileSystemAttributes = FILE_CASE_PRESERVED_NAMES | FILE_CASE_SENSITIVE_SEARCH;			
			pAttribInfo->MaximumComponentNameLength = MAX_PATH_LENGTH;			

            RxContext->Info.LengthRemaining -= FIELD_OFFSET(FILE_FS_ATTRIBUTE_INFORMATION, FileSystemName[0]);
			pAttribInfo->FileSystemNameLength = wcslen(GENESIS_FILE_SYSTEM) * sizeof(WCHAR);;
            if (RxContext->Info.LengthRemaining >= (LONG)pAttribInfo->FileSystemNameLength) {
                BytesToCopy = pAttribInfo->FileSystemNameLength;
            } else {
                BytesToCopy = RxContext->Info.LengthRemaining;
            }
            RtlCopyMemory( pAttribInfo->FileSystemName, GENESIS_FILE_SYSTEM, BytesToCopy );
            RxContext->Info.LengthRemaining -= BytesToCopy;
			pAttribInfo->FileSystemNameLength = BytesToCopy;
           
            Status = STATUS_SUCCESS;            
			RxSetIoStatusInfo(RxContext, sizeof(FILE_FS_ATTRIBUTE_INFORMATION));                   
		}
        break;
    
        case FileFsControlInformation:            
			Status = STATUS_NOT_IMPLEMENTED;
			RxSetIoStatusInfo(RxContext, 0);   
            break;
    
		case FileFsFullSizeInformation:{
			PFILE_FS_FULL_SIZE_INFORMATION pFsInfo = (PFILE_FS_FULL_SIZE_INFORMATION) OriginalBuffer;
			RtlZeroMemory(pFsInfo, sizeof(FILE_FS_FULL_SIZE_INFORMATION));            
			
			//Faked info!
			//4KB block size.  64KB Allocation Size.  1GB used of 8GB for user.  Total space is 10GB
			pFsInfo->BytesPerSector = 4096;
			pFsInfo->SectorsPerAllocationUnit = 16;
			pFsInfo->ActualAvailableAllocationUnits = RtlConvertLongToLargeInteger(160000);
			pFsInfo->TotalAllocationUnits =  RtlConvertLongToLargeInteger(128000);	
			pFsInfo->CallerAvailableAllocationUnits = RtlConvertLongToLargeInteger(112000);
			RxContext->Info.LengthRemaining -= sizeof(FILE_FS_FULL_SIZE_INFORMATION);
			Status = STATUS_SUCCESS;          
			RxSetIoStatusInfo(RxContext, sizeof(FILE_FS_FULL_SIZE_INFORMATION));   
		}
        break;
        case FileFsObjectIdInformation:            
			RxSetIoStatusInfo(RxContext, 0);   
			Status = STATUS_NOT_IMPLEMENTED;
            break;
    
        case FileFsMaximumInformation:            
			RxSetIoStatusInfo(RxContext, 0);   
			Status = STATUS_NOT_IMPLEMENTED;			
            break;
    
        default:			
			RxSetIoStatusInfo(RxContext, 0);   
			Status = STATUS_NOT_IMPLEMENTED;
            break;
    }
    
    return(Status);
}

NTSTATUS NulMRxSetVolumeInformation(IN OUT PRX_CONTEXT pRxContext)
/*++

Routine Description:

   This routine sets the volume information

Arguments:

    pRxContext - the RDBSS context

    FsInformationClass - the kind of Fs information desired.

    pBuffer            - the buffer for copying the information

    BufferLength       - the buffer length

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_NOT_IMPLEMENTED;    
    return(Status);
}


NTSTATUS NulMRxQueryFileInformation(IN PRX_CONTEXT RxContext)
/*++

Routine Description:

   This routine does a query file info. Only the NT-->NT path is implemented.

   The NT-->NT path works by just remoting the call basically without further ado.

Arguments:

    RxContext - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation

--*/
{    
	NTSTATUS Status = STATUS_SUCCESS;    	
	
	RxCaptureFcb;	
	GenesisGetFcbExtension(capFcb, giiFCB);    

	PAGED_CODE();		

	ExAcquireFastMutex(&(giiFCB->ExclusiveLock));	 	

	//Something went wrong
	if(giiFCB == NULL){		
		ExReleaseFastMutex(&(giiFCB->ExclusiveLock));
		GIIPrint(("GenesisDrive:  Unable to obtain lock to read information for file"));
		RxContext->Info.LengthRemaining = 0;
		Status = STATUS_FILE_CLOSED;
	}
	else{				
		Status = GenesisCompleteQueryFileInformation(RxContext);	
		ExReleaseFastMutex(&(giiFCB->ExclusiveLock));
	}        
    return(Status);
}

NTSTATUS NulMRxSetFileInformation(IN PRX_CONTEXT RxContext)
/*++

Routine Description:

   This routine does a set file info. Only the NT-->NT path is implemented.

   The NT-->NT path works by just remoting the call basically without further ado.

Arguments:

    RxContext - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;    
    FILE_INFORMATION_CLASS FunctionalityRequested = 
            RxContext->Info.FileInformationClass;	
    
    LARGE_INTEGER NewAllocationSize;

	RxCaptureFcb;
	RxCaptureFobx;		
	GenesisGetFcbExtension(capFcb, fcb);
	GenesisGetCcbExtension(capFobx, ccb);

	PAGED_CODE();

    switch( FunctionalityRequested ) {
		case FileBasicInformation:{
			PFILE_BASIC_INFORMATION pBasic = (PFILE_BASIC_INFORMATION) RxContext->Info.Buffer;            			
			fcb->AccessedTime = pBasic->LastAccessTime;
			fcb->CreateTime = pBasic->CreationTime;
			fcb->ModifiedTime = pBasic->ChangeTime;						
			
			RxSetIoStatusInfo(RxContext, sizeof(FILE_BASIC_INFORMATION));
			RxContext->Info.LengthRemaining -= sizeof(FILE_BASIC_INFORMATION);									
		}
        break;    
		case FileDispositionInformation:{
			PFILE_DISPOSITION_INFORMATION pDispo = (PFILE_DISPOSITION_INFORMATION) RxContext->Info.Buffer;
			fcb->DeleteOnCloseSpecified = pDispo->DeleteFile;	

			RxSetIoStatusInfo(RxContext, sizeof(FILE_DISPOSITION_INFORMATION));
			RxContext->Info.LengthRemaining -= sizeof(FILE_DISPOSITION_INFORMATION);									
		}
        break;
    
		case FilePositionInformation:{
			PFILE_POSITION_INFORMATION position_info = (PFILE_POSITION_INFORMATION) RxContext->Info.Buffer;
			GIIPrint(("GenesisDrive: File position changing to %x %x\n", position_info->CurrentByteOffset.HighPart, 
				position_info->CurrentByteOffset.LowPart));
			ccb->CurrentByteOffset = position_info->CurrentByteOffset;

			RxContext->Info.LengthRemaining -= sizeof(FILE_POSITION_INFORMATION);
			RxSetIoStatusInfo(RxContext, sizeof(FILE_POSITION_INFORMATION));
			}
        break;
    
		case FileAllocationInformation:{
			PFILE_ALLOCATION_INFORMATION pAllocationInfo = 
				(PFILE_ALLOCATION_INFORMATION) RxContext->Info.Buffer;

			// Case falls through to FileEndOfFileInfo since we don't have knowledge of allocation versus
			// FileSize information as we are dealing with an obscure backend
						
			// Are we extending or truncating the file
			if(RtlLargeIntegerGreaterThan(pAllocationInfo->AllocationSize, capFcb->Header.FileSize)) {
            
                Status = NulMRxExtendFile(
                                RxContext,
                                &pAllocationInfo->AllocationSize,
                                &NewAllocationSize
                                );                    
            } else {
                Status = NulMRxTruncateFile(
                                RxContext,
                                &pAllocationInfo->AllocationSize,
                                &NewAllocationSize
                                );				
            }									

			RxContext->Info.LengthRemaining -= sizeof(FILE_ALLOCATION_INFORMATION);
			RxSetIoStatusInfo(RxContext, sizeof(FILE_ALLOCATION_INFORMATION));
		}
		break;

		case FileEndOfFileInformation:{
			PFILE_END_OF_FILE_INFORMATION pEndOfFileInfo = 
				(PFILE_END_OF_FILE_INFORMATION) RxContext->Info.Buffer;
			
			// Are we extending or truncating the file
			if(RtlLargeIntegerGreaterThan(pEndOfFileInfo->EndOfFile, capFcb->Header.FileSize)) {
            
                Status = NulMRxExtendFile(
                                RxContext,
                                &pEndOfFileInfo->EndOfFile,
                                &NewAllocationSize
                                );                    
            } else {
                Status = NulMRxTruncateFile(
                                RxContext,
                                &pEndOfFileInfo->EndOfFile,
                                &NewAllocationSize
                                );				
            }									

            RxContext->Info.LengthRemaining -= sizeof(FILE_END_OF_FILE_INFORMATION);
			RxSetIoStatusInfo(RxContext, sizeof(FILE_END_OF_FILE_INFORMATION));
		}
        break;
    
		case FileRenameInformation:{
			PFILE_RENAME_INFORMATION pRenameInfo = (PFILE_RENAME_INFORMATION) RxContext->Info.Buffer;
			GIIPrint(("GenesisDrive:  File rename received\n"));
			
			//Close this file handle on the Genesis Side
			Status = GenesisSendInvertedCall(RxContext, GENII_RENAME, FALSE);

			// Something could go wrong (only wait if something will actually come back to free you)
			if(NT_SUCCESS(Status)){
				//Waits for caller
				KeWaitForSingleObject(&(fcb->InvertedCallSemaphore), Executive, KernelMode, FALSE, NULL);
			}
			// Error for rename = access denied - don't have better information
			if(ccb->lastStatus == -1){
				Status =  STATUS_ACCESS_DENIED;
			}

			RxContext->Info.LengthRemaining -= sizeof(FILE_RENAME_INFORMATION);
			RxSetIoStatusInfo(RxContext, sizeof(FILE_RENAME_INFORMATION));
            break;
		}
        default:
			GIIPrint(("GenesisDrive: Unknown set information requested\n"));
			Status = STATUS_NOT_SUPPORTED;
            break;									  
    }        
	return Status;
}

NTSTATUS NulMRxSetFileInformationAtCleanup(IN PRX_CONTEXT RxContext)
/*++

Routine Description:

   This routine sets the file information on cleanup. the old rdr just swallows this operation (i.e.
   it doesn't generate it). we are doing the same..........

Arguments:

    pRxContext           - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_NOT_IMPLEMENTED;
    return(Status);
}

NTSTATUS GenesisCompleteQueryFileInformation(PRX_CONTEXT RxContext){
	NTSTATUS Status = STATUS_SUCCESS;	
    FILE_INFORMATION_CLASS FunctionalityRequested = 
            RxContext->Info.FileInformationClass;    

	PVOID Buffer;
	PIRP PtrIrp;

	RxCaptureFcb;
	RxCaptureFobx;		
	GenesisGetFcbExtension(capFcb, giiFCB);
	GenesisGetCcbExtension(capFobx, giiCCB);

	PAGED_CODE();	

	PtrIrp = RxContext->CurrentIrp;

	// We must determine the buffer pointer to be used. Since this
	// routine could either be invoked directly in the context of the
	// calling thread, or in the context of a worker thread, here is
	// a general way of determining what we should use.
	if (PtrIrp->MdlAddress) {
		Buffer = (PCHAR)MmGetSystemAddressForMdl(PtrIrp->MdlAddress);
	} else {
		Buffer = (PCHAR)PtrIrp->UserBuffer;
	}

	if(giiFCB->State == GENII_STATE_NOT_FOUND){		
		Status = STATUS_FILE_INVALID;
	}

	switch( FunctionalityRequested ) {
		case FileBasicInformation:{
			PFILE_BASIC_INFORMATION pFileStdInfo = 
				(PFILE_BASIC_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_BASIC_INFORMATION));

			pFileStdInfo->FileAttributes = ((giiFCB->isDirectory==TRUE) ? 
				FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_NORMAL);			

			pFileStdInfo->ChangeTime = giiFCB->OpenTime;
			pFileStdInfo->CreationTime = giiFCB->OpenTime;
			pFileStdInfo->LastAccessTime = giiFCB->OpenTime;
			pFileStdInfo->LastWriteTime = giiFCB->OpenTime;						

			RxContext->Info.LengthRemaining -= sizeof(FILE_BASIC_INFORMATION);			
		}
		break;
		
		case FileInternalInformation:{
			PFILE_INTERNAL_INFORMATION pFileStdInfo = 
				(PFILE_INTERNAL_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_INTERNAL_INFORMATION));
			
			pFileStdInfo->IndexNumber.QuadPart = giiCCB->GenesisFileID;			

			RxContext->Info.LengthRemaining -= sizeof(FILE_INTERNAL_INFORMATION);			
		}
		break;			

		case FileNameInformation:{
			PFILE_NAME_INFORMATION pFileStdInfo = 
				(PFILE_NAME_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_NAME_INFORMATION));

			RtlCopyMemory(pFileStdInfo->FileName, RxContext->pRelevantSrvOpen->pAlreadyPrefixedName->Buffer,
				RxContext->pRelevantSrvOpen->pAlreadyPrefixedName->Length);
			pFileStdInfo->FileNameLength = RxContext->pRelevantSrvOpen->pAlreadyPrefixedName->Length;			

			RxContext->Info.LengthRemaining -= sizeof(FILE_NAME_INFORMATION);			
		}
		break;

		case FileNetworkOpenInformation:{
			PFILE_NETWORK_OPEN_INFORMATION pFileStdInfo = 
				(PFILE_NETWORK_OPEN_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_NETWORK_OPEN_INFORMATION));

			pFileStdInfo->AllocationSize = capFcb->Header.AllocationSize;
			pFileStdInfo->EndOfFile = capFcb->Header.FileSize;
			pFileStdInfo->FileAttributes = ((giiFCB->isDirectory==TRUE) ? 
				FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_NORMAL);			
			
			pFileStdInfo->ChangeTime = giiFCB->OpenTime;
			pFileStdInfo->CreationTime = giiFCB->OpenTime;
			pFileStdInfo->LastAccessTime = giiFCB->OpenTime;
			pFileStdInfo->LastWriteTime = giiFCB->OpenTime;

			RxContext->Info.LengthRemaining -= sizeof(FILE_NETWORK_OPEN_INFORMATION);			
		}
		break;

		case FilePositionInformation:{
			PFILE_POSITION_INFORMATION pFileStdInfo = 
				(PFILE_POSITION_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_POSITION_INFORMATION));

			pFileStdInfo->CurrentByteOffset = giiCCB->CurrentByteOffset; 				

			RxContext->Info.LengthRemaining -= sizeof(FILE_POSITION_INFORMATION);			
		}
		break;

		case FileAllInformation:{
			PFILE_ALL_INFORMATION pFileStdInfo = 
				(PFILE_ALL_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_ALL_INFORMATION));
			
			RxContext->Info.LengthRemaining -= sizeof(FILE_ALL_INFORMATION);			
		}
		break;

		/* NOT SUPPORTED */
		case FileStreamInformation:{
			PFILE_STREAM_INFORMATION pFileStdInfo = 
				(PFILE_STREAM_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_STREAM_INFORMATION));				
			
			RxContext->Info.LengthRemaining -= sizeof(FILE_STREAM_INFORMATION);			
 	    }
 	    break;

        /* Extended Attributes are not supported in this driver */
		case FileEaInformation:{
			PFILE_EA_INFORMATION pFileStdInfo = 
				(PFILE_EA_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_EA_INFORMATION));			
			
			RxContext->Info.LengthRemaining -= sizeof(FILE_EA_INFORMATION);											   			
		}
		break;
            
		case FileStandardInformation:{
			PFILE_STANDARD_INFORMATION pFileStdInfo = 
				(PFILE_STANDARD_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_STANDARD_INFORMATION));
			
			pFileStdInfo->AllocationSize = capFcb->Header.AllocationSize;
			pFileStdInfo->EndOfFile = capFcb->Header.FileSize;			
			pFileStdInfo->Directory = giiFCB->isDirectory;
			pFileStdInfo->NumberOfLinks = 0; //hard-coded	
			pFileStdInfo->DeletePending = FALSE;
            
			RxContext->Info.LengthRemaining -= sizeof(FILE_STANDARD_INFORMATION);							
		}	
		break;	        

		case FileAttributeTagInformation:{
			PFILE_ATTRIBUTE_TAG_INFORMATION pFileStdInfo = 
				(PFILE_ATTRIBUTE_TAG_INFORMATION) RxContext->Info.Buffer;
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_ATTRIBUTE_TAG_INFORMATION));

			pFileStdInfo->FileAttributes = ((giiFCB->isDirectory==TRUE) ? 
				FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_NORMAL);	
		 }
		 break;
		default:
			GIIPrint(("GenesisDrive:  Unsupported type for Query Information received for file %wZ with type: %d!\n",
				RxContext->pRelevantSrvOpen->pAlreadyPrefixedName, FunctionalityRequested));
			Status = STATUS_NOT_SUPPORTED;
			break;
	}    

	return Status;
}

