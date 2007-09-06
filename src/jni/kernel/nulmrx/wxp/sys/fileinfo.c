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
#include "LPC.h"

#pragma hdrstop
#pragma warning(error:4101)   // Unreferenced local variable

//Local references
WCHAR * GenesisGetDirectoryEntry(PGENESIS_FCB ccb, int index, LARGE_INTEGER *, int *);

//  The local debug trace level
#define Dbg                              (DEBUG_TRACE_FILEINFO)

NTSTATUS NulMRxQueryDirectory(IN OUT PRX_CONTEXT RxContext)
/*++

Routine Description:

   This routine does a directory query to Genesis. First calls are always
   called asynchronously (gets all data).

Arguments:

    RxContext - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_INVALID_PARAMETER;        	
	ULONG *FileIndex;
	ULONG *DirectorySize;		
	PGENESIS_CCB ccb;
	PGENESIS_FCB fcb;
	PSTRING target;

	RxCaptureFcb;
	RxCaptureFobx;		

	PAGED_CODE();			

	fcb = (PGENESIS_FCB)capFcb->Context2;	
	ccb = (PGENESIS_CCB)capFobx->Context2;	      

	KeWaitForSingleObject(&(fcb->FcbPhore), Executive, KernelMode, FALSE, NULL);

	//GenesisII-Specific File Index Information	
	FileIndex = &(ccb->FileIndex);
	DirectorySize = &(fcb->Size);

	//For some weird error this is null sometimes
	if(FileIndex == NULL){		
		KeReleaseSemaphore(&(fcb->FcbPhore), 0, 1, FALSE);
		RxContext->Info.LengthRemaining = 0;
		Status = STATUS_FILE_CLOSED;
		try_return(Status);
	}	
	if(!(fcb->isDirectory)){
		KeReleaseSemaphore(&(fcb->FcbPhore), 0, 1, FALSE);
		DbgPrint("QueryDirectory:  File is not a directory %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
		RxContext->Info.LengthRemaining = 0;
		RxContext->IoStatusBlock.Information = FILE_NON_DIRECTORY_FILE;
		Status = STATUS_NOT_A_DIRECTORY;
		try_return(Status);
	}

	RxDbgBreakPoint(85);

	//If initial query, reset counter
	if(RxContext->QueryDirectory.InitialQuery)
	{
		/* Print out directory and target */
		target = RxContext->CurrentIrpSp->Parameters.QueryDirectory.FileName;
		if(target != NULL){									
			int i;
			//Weird naming (this should work)
			for(i=0; i<target->Length; i+=2){
				ccb->Target.Buffer[i/2] = target->Buffer[i];
			}
			ccb->Target.Buffer[(target->Length)/2] = '\0';
			ccb->Target.Length = (target->Length)/2;

			//It's a lie, PSTRING is a PUNICODE string (who knows why) (offset by 1)
			DbgPrint("QueryDirectory: Started for file %wZ with target %s\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName,
				ccb->Target.Buffer);						
		}
		else{
			STRING temp;
			RtlInitString(&temp, "*\0");
			RtlCopyString(&(ccb->Target), &temp);
			DbgPrint("QueryDirectory:  Started for file %wZ with target *\n",RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);			
		}
		*FileIndex = 0;
		ccb->IsTargetSet = TRUE;
	}				
			
	//Check if we have the listing yet
	if(fcb->State != GENII_STATE_HAVE_LISTING){
		//Keep semaphore (releases after async call)

		//Calls Genesis to get Directory Listing
		Status = GenesisSendInvertedCall(RxContext, GENII_QUERYDIRECTORY, TRUE);
		try_return(Status);
	}						
	else{
		//Release Semaphore
		KeReleaseSemaphore(&(fcb->FcbPhore), 0, 1, FALSE);
		
		//Actually get listing
		Status = GenesisCompleteQueryDirectory(RxContext, NULL);			
		try_return(Status);
	}				

try_exit:	NOTHING;

	if(Status != STATUS_PENDING){
		DbgPrint("QueryDirectory: Ended for file %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
	}
	return Status;
}


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

    RxTraceEnter("NulMRxQueryVolumeInformation");

    switch( FsInformationClass ) {
        case FileFsVolumeInformation:
		{
    		PFILE_FS_VOLUME_INFORMATION pVolInfo = (PFILE_FS_VOLUME_INFORMATION) OriginalBuffer;
            RtlZeroMemory( pVolInfo, sizeof(FILE_FS_VOLUME_INFORMATION) );
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
           
            Status = STATUS_SUCCESS;            
			DbgPrint("QueryVolumeInformation:  FileFsVolumeInformation\n");			
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

			Status = STATUS_SUCCESS;
            DbgPrint("QueryVolumeInformation:  FileFsLabelInformation\n");         
		}
		break;
        case FileFsSizeInformation:
		{
			PFILE_FS_SIZE_INFORMATION pSizeInfo = (PFILE_FS_SIZE_INFORMATION) OriginalBuffer;
			RtlZeroMemory(pSizeInfo, sizeof(FILE_FS_SIZE_INFORMATION) );	

			//Faked info!
			//4KB block size.  64KB Allocation Size.  128 GB free, 256GB Total
			pSizeInfo->BytesPerSector = 4096;
			pSizeInfo->SectorsPerAllocationUnit = 16;
			pSizeInfo->AvailableAllocationUnits.QuadPart = 200;
			pSizeInfo->TotalAllocationUnits.QuadPart =  2000;			

			Status = STATUS_SUCCESS;
            DbgPrint("QueryVolumeInformation:  FileFsSizeInformation\n");            
		}
		break;
        case FileFsDeviceInformation:            
		{
			PFILE_FS_DEVICE_INFORMATION pDevInfo = (PFILE_FS_DEVICE_INFORMATION) OriginalBuffer;
			RtlZeroMemory(pDevInfo, sizeof(FILE_FS_DEVICE_INFORMATION) );			
			pDevInfo->DeviceType = FILE_DEVICE_NETWORK_FILE_SYSTEM;
			pDevInfo->Characteristics = FILE_REMOTE_DEVICE;			
			DbgPrint("QueryVolumeInformation:  FileFsDeviceInformation\n");
			
			Status = STATUS_SUCCESS;            
		}
		break;
        case FileFsAttributeInformation:
		{
			PFILE_FS_ATTRIBUTE_INFORMATION pAttribInfo =
				(PFILE_FS_ATTRIBUTE_INFORMATION) OriginalBuffer;

			pAttribInfo->FileSystemAttributes = 0;			
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
            
            DbgPrint("QueryVolumeInformation:  FileFsAttributeInformation\n");
		}
        break;
    
        case FileFsControlInformation:
            DbgPrint("QueryVolumeInformation:  FileFsControlInformation\n");
			Status = STATUS_NOT_IMPLEMENTED;
            break;
    
        case FileFsFullSizeInformation:
            DbgPrint("QueryVolumeInformation:  FileFsFullSizeInformation\n");
			Status = STATUS_NOT_IMPLEMENTED;
            break;
    
        case FileFsObjectIdInformation:
            DbgPrint("QueryVolumeInformation:  FileFsObjectIdInformation\n");
			Status = STATUS_NOT_IMPLEMENTED;
            break;
    
        case FileFsMaximumInformation:
            DbgPrint("QueryVolumeInformation:  FileFsMaximumInformation\n");
			Status = STATUS_NOT_IMPLEMENTED;
            break;
    
        default:
			Status = STATUS_NOT_IMPLEMENTED;
            break;
    }

    RxTraceLeave(Status);
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

    DbgPrint("NulMRxSetVolumeInformation \n");
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
	PGENESIS_FCB giiFCB;
	
	RxCaptureFcb;	

    RxTraceEnter("NulMRxQueryFileInformation");	

	PAGED_CODE();		

	giiFCB = capFcb->Context2;

	KeWaitForSingleObject(&(giiFCB->FcbPhore), Executive, KernelMode, FALSE, NULL);	

	DbgPrint("NulMRxQueryFileInformation: Started for file %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);

	//Something went wrong
	if(giiFCB == NULL){		
		KeReleaseSemaphore(&(giiFCB->FcbPhore), 0, 1, FALSE);
		DbgPrint("NulMRxQueryFileInformation: Failed!  GIIFCB == NULL\n");
		RxContext->Info.LengthRemaining = 0;
		Status = STATUS_FILE_CLOSED;
	}
	else{

		if(giiFCB->State == GENII_STATE_NOT_INITIALIZED){
			//Will release semaphore later (on return)
			Status = GenesisSendInvertedCall(RxContext, GENII_QUERYFILEINFO, TRUE);
		}	
		else{
			KeReleaseSemaphore(&(giiFCB->FcbPhore), 0, 1, FALSE);
			Status = GenesisCompleteQueryFileInformation(RxContext, NULL);
		}
	}
    
    RxTraceLeave(Status);
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
    RxCaptureFcb;
    FILE_INFORMATION_CLASS FunctionalityRequested = 
            RxContext->Info.FileInformationClass;
    PFILE_END_OF_FILE_INFORMATION pEndOfFileInfo = 
            (PFILE_END_OF_FILE_INFORMATION) RxContext->Info.Buffer;
    LARGE_INTEGER NewAllocationSize;

    RxTraceEnter("NulMRxSetFileInformation");

    switch( FunctionalityRequested ) {
        case FileBasicInformation:
            RxDbgTrace(0, Dbg, ("FileBasicInformation\n"));
            break;
    
        case FileDispositionInformation:
            RxDbgTrace(0, Dbg, ("FileDispositionInformation\n"));
            break;
    
        case FilePositionInformation:
            RxDbgTrace(0, Dbg, ("FilePositionInformation\n"));
            break;
    
        case FileAllocationInformation:
            RxDbgTrace(0, Dbg, ("FileAllocationInformation\n"));
            RxDbgTrace(0, Dbg, ("AllocSize is %d AllocSizeHigh is %d\n",
            pEndOfFileInfo->EndOfFile.LowPart,pEndOfFileInfo->EndOfFile.HighPart));
            break;
    
        case FileEndOfFileInformation:
            RxDbgTrace(0, Dbg, ("FileSize is %d FileSizeHigh is %d\n",
            capFcb->Header.AllocationSize.LowPart,capFcb->Header.AllocationSize.HighPart));

            if( pEndOfFileInfo->EndOfFile.QuadPart > 
                    capFcb->Header.AllocationSize.QuadPart ) {
            
                Status = NulMRxExtendFile(
                                RxContext,
                                &pEndOfFileInfo->EndOfFile,
                                &NewAllocationSize
                                );

                RxDbgTrace(0, Dbg, ("AllocSize is %d AllocSizeHigh is %d\n",
                            NewAllocationSize.LowPart,NewAllocationSize.HighPart));

                //
                //  Change the file allocation
                //
                capFcb->Header.AllocationSize.QuadPart = NewAllocationSize.QuadPart;
            } else {
                Status = NulMRxTruncateFile(
                                RxContext,
                                &pEndOfFileInfo->EndOfFile,
                                &NewAllocationSize
                                );
            }

            RxContext->Info.LengthRemaining -= sizeof(FILE_END_OF_FILE_INFORMATION);
            break;
    
        case FileRenameInformation:
            RxDbgTrace(0, Dbg, ("FileRenameInformation\n"));
            break;
    
        default:
            break;
    }
    
    RxTraceLeave(Status);
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

NTSTATUS GenesisCompleteQueryDirectory(PRX_CONTEXT RxContext, PVOID requestBuffer){
/*
	Completes a QueryDirectory method 
	Used after FCB is stored
*/

	NTSTATUS Status = STATUS_INVALID_PARAMETER;
    FILE_INFORMATION_CLASS FileInformationClass;
    PVOID   Buffer;
    PULONG  pLengthRemaining;
	ULONG	CopySize;
	ULONG	BytesLeft;
	ULONG   TotalCopySize = 0;
	ULONG *FileIndex;
	ULONG *DirectorySize;	
	
	PGENESIS_FCB fcb;
	PGENESIS_CCB ccb;
	
	RxCaptureFcb;
	RxCaptureFobx;

	PAGED_CODE();

    FileInformationClass = RxContext->Info.FileInformationClass;
	
	//Uses either the buffer given (if from async operation) or the buffer in RxContext
	Buffer = ((requestBuffer == NULL) ? RxContext->Info.Buffer : requestBuffer);
    pLengthRemaining = &RxContext->Info.LengthRemaining;
	BytesLeft = RxContext->CurrentIrpSp->Parameters.QueryDirectory.Length;

	fcb = (PGENESIS_FCB)capFcb->Context2;	
	ccb = (PGENESIS_CCB)capFobx->Context2;		

	//GenesisII-Specific File Index Information	
	FileIndex = &(ccb->FileIndex);	
	DirectorySize = &(fcb->Size);

	if(*FileIndex >= *DirectorySize){						
		if(RxContext->QueryDirectory.ReturnSingleEntry != 0){			
			return STATUS_NO_SUCH_FILE;		
		}
		else{			
			return STATUS_NO_MORE_FILES;
		}				
	}

	if(fcb->State == GENII_STATE_NOT_FOUND){
		DbgPrint("QueryDirectory:  File not found in Genesis but still created\n");
		return STATUS_FILE_INVALID;
	}

	//Check to see the information class (type of info requested)
    switch (FileInformationClass)
    {
		case FileDirectoryInformation:
		{			
			PFILE_DIRECTORY_INFORMATION pDirInfo = NULL;
			WCHAR * currentName;
			int nLen, entryType;	
			LARGE_INTEGER entryLength;
			
			DbgPrint("QueryDirectory:  File Directory Information Requested \n");	

			currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
			nLen = wcslen(currentName);			
			CopySize = sizeof( FILE_DIRECTORY_INFORMATION) + (nLen - 1) * sizeof(WCHAR);  //skip first character;							
			
			while(BytesLeft > CopySize){								
				pDirInfo = (PFILE_DIRECTORY_INFORMATION) Buffer;
				RtlZeroMemory(pDirInfo, sizeof( FILE_DIRECTORY_INFORMATION));				
				pDirInfo->NextEntryOffset = CopySize + CopySize % 8;
				
				pDirInfo->FileAttributes = FILE_ATTRIBUTE_NORMAL | FILE_ATTRIBUTE_READONLY;
				if(entryType != 0){
					pDirInfo->FileAttributes |= FILE_ATTRIBUTE_DIRECTORY;
				}
				
				pDirInfo->FileNameLength = nLen * sizeof(WCHAR);				
				RtlCopyMemory(pDirInfo->FileName, currentName, nLen * sizeof(WCHAR));									

				//Genesis has no notion of Allocation Size
				pDirInfo->AllocationSize = entryLength;
				pDirInfo->EndOfFile = entryLength;
					
				//Move to next Entry
				BytesLeft -= pDirInfo->NextEntryOffset;		
				TotalCopySize += pDirInfo->NextEntryOffset;							
				(*FileIndex)++;				

				//If only one entry or last index reached
				if(RxContext->QueryDirectory.ReturnSingleEntry != 0 ||
					*FileIndex == *DirectorySize){						
					break;
				}
		
				//Moves the pointer to the next entry to fill
				((char*)Buffer) += pDirInfo->NextEntryOffset; // move pointer
				
				currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
				nLen = wcslen(currentName);				
				CopySize = sizeof( FILE_DIRECTORY_INFORMATION) + (nLen - 1) * sizeof(WCHAR);  //skip first character;										
			}			
		
			//If nothing was copied at this point (BytesLeft < CopySize)
			if(TotalCopySize == 0){
				Status = STATUS_BUFFER_TOO_SMALL;
			}
			else{
				//If everything is ok, make sure the last entry is correct
				if(pDirInfo != NULL)
					pDirInfo->NextEntryOffset = 0;				
				Status = STATUS_SUCCESS;
			}
		}
        break;

	    case FileFullDirectoryInformation:
		{			
			PFILE_FULL_DIR_INFORMATION pDirInfo = NULL;
			WCHAR * currentName;
			int nLen, entryType;				
			LARGE_INTEGER entryLength;

			DbgPrint("QueryDirectory:  File Full Dir Information Requested \n");																	

			currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
			nLen = wcslen(currentName);
			CopySize = sizeof( FILE_FULL_DIR_INFORMATION) + (nLen - 1) * sizeof(WCHAR);  //skip first character;

			while(BytesLeft > CopySize){				
				pDirInfo = (PFILE_FULL_DIR_INFORMATION) Buffer;
				RtlZeroMemory(pDirInfo, sizeof( FILE_FULL_DIR_INFORMATION));				
				
				pDirInfo->NextEntryOffset = CopySize + CopySize % 8;
				if(entryType == 0)
					pDirInfo->FileAttributes = FILE_ATTRIBUTE_NORMAL | FILE_ATTRIBUTE_READONLY;
				else
					pDirInfo->FileAttributes = FILE_ATTRIBUTE_DIRECTORY;

				pDirInfo->FileNameLength = nLen * sizeof(WCHAR);
				RtlCopyMemory(pDirInfo->FileName, currentName, nLen * sizeof(WCHAR));		

				//Genesis has no notion of Allocation Size
				pDirInfo->AllocationSize = entryLength;
				pDirInfo->EndOfFile = entryLength;																	
					
				//Move to next Entry
				BytesLeft -= pDirInfo->NextEntryOffset;		
				TotalCopySize += pDirInfo->NextEntryOffset;							
				(*FileIndex)++;				

				//If only one entry or last index reached
				if(RxContext->QueryDirectory.ReturnSingleEntry != 0 ||
					*FileIndex == *DirectorySize){						
					break;
				}
		
				//Moves the pointer to the next entry to fill
				((char*)Buffer) += pDirInfo->NextEntryOffset; // move pointer
				
				currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
				nLen = wcslen(currentName);
				CopySize = sizeof( FILE_FULL_DIR_INFORMATION) + (nLen - 1) * sizeof(WCHAR);  //skip first character;												
			}			
		
			//If nothing was copied at this point (BytesLeft < CopySize)
			if(TotalCopySize == 0){
				Status = STATUS_BUFFER_TOO_SMALL;
			}
			else{
				//If everything is ok, make sure the last entry is correct
				if(pDirInfo != NULL)
					pDirInfo->NextEntryOffset = 0;				
				Status = STATUS_SUCCESS;
			}						
		}
        break;

		case FileBothDirectoryInformation:
		{			        			
			PFILE_BOTH_DIR_INFORMATION pDirInfo = NULL; 
			WCHAR * currentName;
			int nLen,	entryType;	
			LARGE_INTEGER entryLength;
			
			DbgPrint("QueryDirectory:  File Both Dir Information Requested \n");		

			currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
			nLen = wcslen(currentName);			
			CopySize = sizeof( FILE_BOTH_DIR_INFORMATION) + ((nLen - 1) * sizeof(WCHAR));  //skip first character;							
			
			while(BytesLeft > CopySize){				
				pDirInfo = (PFILE_BOTH_DIR_INFORMATION) Buffer;
				RtlZeroMemory(pDirInfo, sizeof( FILE_BOTH_DIR_INFORMATION));				
				pDirInfo->NextEntryOffset = CopySize + CopySize % 8;				
				pDirInfo->FileAttributes = FILE_ATTRIBUTE_NORMAL | FILE_ATTRIBUTE_READONLY;
				if(entryType != 0)
				{
					pDirInfo->FileAttributes |= FILE_ATTRIBUTE_DIRECTORY;
				}				

				pDirInfo->FileNameLength = nLen * sizeof(WCHAR);
				RtlCopyMemory(pDirInfo->FileName, currentName, nLen * sizeof(WCHAR));
					
				//Copies up to the first 12 letters of the name
				//pDirInfo->ShortNameLength = (nLen > 12 ? 12 : nLen) * sizeof( WCHAR );
				//RtlCopyMemory(pDirInfo->ShortName, currentName, pDirInfo->ShortNameLength);
				pDirInfo->ShortNameLength = 0;
				pDirInfo->EaSize = 0;

				//Genesis has no notion of Allocation Size
				pDirInfo->AllocationSize = entryLength;
				pDirInfo->EndOfFile = entryLength;		
					
				//Move to next Entry
				BytesLeft -= pDirInfo->NextEntryOffset;		
				TotalCopySize += pDirInfo->NextEntryOffset;							
				(*FileIndex)++;				

				//If only one entry or last index reached
				if(RxContext->QueryDirectory.ReturnSingleEntry != 0 ||
					*FileIndex == *DirectorySize){						
					break;
				}
		
				//Moves the pointer to the next entry to fill
				((char*)Buffer) += pDirInfo->NextEntryOffset; // move pointer
				
				currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
				nLen = wcslen(currentName);				
				CopySize = sizeof( FILE_BOTH_DIR_INFORMATION) + (nLen - 1) * sizeof(WCHAR);  //skip first character;										
			}			
		
			//If nothing was copied at this point (BytesLeft < CopySize)
			if(TotalCopySize == 0){
				Status = STATUS_BUFFER_TOO_SMALL;
			}
			else{
				//If everything is ok, make sure the last entry is correct
				if(pDirInfo != NULL)
					pDirInfo->NextEntryOffset = 0;							
				Status = STATUS_SUCCESS;
			}
		}
        break;

		case FileNamesInformation:
		{			
			PFILE_NAMES_INFORMATION pDirInfo = NULL;
			WCHAR * currentName;
			int nLen, entryType;				
			LARGE_INTEGER entryLength;

			DbgPrint("QueryDirectory:  File Names Information Requested \n");

			currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
			nLen = wcslen(currentName);			
			CopySize = sizeof(FILE_NAMES_INFORMATION) + (nLen - 1) * sizeof(WCHAR);  //skip first character;							
			
			while(BytesLeft > CopySize){						
				pDirInfo = (PFILE_NAMES_INFORMATION) Buffer;	
				RtlZeroMemory(pDirInfo, sizeof( FILE_NAMES_INFORMATION));				
				pDirInfo->NextEntryOffset = CopySize + CopySize % 8;				
				pDirInfo->FileNameLength = nLen * sizeof(WCHAR);
				RtlCopyMemory(pDirInfo->FileName, currentName, nLen * sizeof(WCHAR));				
					
				//Move to next Entry
				BytesLeft -= pDirInfo->NextEntryOffset;		
				TotalCopySize += pDirInfo->NextEntryOffset;							
				(*FileIndex)++;				

				//If only one entry or last index reached
				if(RxContext->QueryDirectory.ReturnSingleEntry != 0 ||
					*FileIndex == *DirectorySize){						
					break;
				}
		
				//Moves the pointer to the next entry to fill
				((char*)Buffer) += pDirInfo->NextEntryOffset; // move pointer
				
				currentName = GenesisGetDirectoryEntry(fcb, *FileIndex, &entryLength, &entryType);
				nLen = wcslen(currentName);				
				CopySize = sizeof( FILE_NAMES_INFORMATION) + (nLen - 1) * sizeof(WCHAR);  //skip first character;										
			}			
		
			//If nothing was copied at this point (BytesLeft < CopySize)
			if(TotalCopySize == 0){
				Status = STATUS_BUFFER_TOO_SMALL;
			}
			else{
				//If everything is ok, make sure the last entry is correct
				if(pDirInfo != NULL)
					pDirInfo->NextEntryOffset = 0;				
				Status = STATUS_SUCCESS;
			}
		}
        break;

		default:
			RxDbgTrace( 0, Dbg, ("QueryDirectory:  Invalid FS information class\n"));
			Status = STATUS_INVALID_PARAMETER;
			break;
	}	

	(*pLengthRemaining) -= TotalCopySize;
    return(Status);
}
NTSTATUS GenesisCompleteQueryFileInformation(PRX_CONTEXT RxContext, PVOID requestBuffer){
	NTSTATUS Status = STATUS_SUCCESS;	
    FILE_INFORMATION_CLASS FunctionalityRequested = 
            RxContext->Info.FileInformationClass;    

	PVOID Buffer;
	PGENESIS_FCB giiFCB;

	RxCaptureFcb;	

	PAGED_CODE();	

	giiFCB = (PGENESIS_FCB)capFcb->Context2;	

	//Uses either the buffer given (if from async operation) or the buffer in RxContext
	Buffer = ((requestBuffer == NULL) ? RxContext->Info.Buffer : requestBuffer);

	if(giiFCB->State == GENII_STATE_NOT_FOUND){
		DbgPrint("QueryFileInformation:  File not found in Genesis but still created\n");
		Status = STATUS_FILE_INVALID;
	}

	switch( FunctionalityRequested ) {
		case FileBasicInformation:{
			PFILE_BASIC_INFORMATION pFileStdInfo = 
				(PFILE_BASIC_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_BASIC_INFORMATION));

			pFileStdInfo->FileAttributes = ((giiFCB->isDirectory==TRUE) ? 
				FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_READONLY);			

			RxContext->Info.LengthRemaining -= sizeof(FILE_BASIC_INFORMATION);

			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FBI\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
		}
		break;


		/* NOT SUPPORTED */
		case FileInternalInformation:{
			PFILE_INTERNAL_INFORMATION pFileStdInfo = 
				(PFILE_INTERNAL_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_INTERNAL_INFORMATION));

			//UNIQUE FILE ID?
			//pFileStdInfo->IndexNumber;

			RxContext->Info.LengthRemaining -= sizeof(FILE_INTERNAL_INFORMATION);
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FII\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
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
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FNI\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
		}
		break;

		case FileNetworkOpenInformation:{
			PFILE_NETWORK_OPEN_INFORMATION pFileStdInfo = 
				(PFILE_NETWORK_OPEN_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_NETWORK_OPEN_INFORMATION));

			pFileStdInfo->AllocationSize.QuadPart = giiFCB->Size;
			pFileStdInfo->EndOfFile.QuadPart = giiFCB->Size;
			pFileStdInfo->FileAttributes = ((giiFCB->isDirectory==TRUE) ? 
				FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_READONLY);						

			RxContext->Info.LengthRemaining -= sizeof(FILE_NETWORK_OPEN_INFORMATION);
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FNOI\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
		}
		break;

		case FilePositionInformation:{
			PFILE_POSITION_INFORMATION pFileStdInfo = 
				(PFILE_POSITION_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_POSITION_INFORMATION));

			pFileStdInfo->CurrentByteOffset.QuadPart = 0; // HARD CODED (fix later)								

			RxContext->Info.LengthRemaining -= sizeof(FILE_POSITION_INFORMATION);
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FPI\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
		}
		break;

		case FileAllInformation:{
			PFILE_ALL_INFORMATION pFileStdInfo = 
				(PFILE_ALL_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_ALL_INFORMATION));

			
			RxContext->Info.LengthRemaining -= sizeof(FILE_ALL_INFORMATION);
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FAI\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
		}
		break;

		case FileStreamInformation:{
			PFILE_STREAM_INFORMATION pFileStdInfo = 
				(PFILE_STREAM_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_STREAM_INFORMATION));
			
			RxContext->Info.LengthRemaining -= sizeof(FILE_STREAM_INFORMATION);
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FSI\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
 	    }
 	    break;

        /* NOT SUPPORTED */
		case FileEaInformation:{
			PFILE_EA_INFORMATION pFileStdInfo = 
				(PFILE_EA_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_EA_INFORMATION));
			
			RxContext->Info.LengthRemaining -= sizeof(FILE_EA_INFORMATION);											   
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FEI\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
		}
		break;
            
		case FileStandardInformation:{
			PFILE_STANDARD_INFORMATION pFileStdInfo = 
				(PFILE_STANDARD_INFORMATION) RxContext->Info.Buffer;	
			RtlZeroMemory(pFileStdInfo, sizeof(FILE_STANDARD_INFORMATION));
			
			pFileStdInfo->EndOfFile.QuadPart = giiFCB->Size;
			pFileStdInfo->AllocationSize.QuadPart = giiFCB->Size;				
			pFileStdInfo->Directory = giiFCB->isDirectory;
			pFileStdInfo->NumberOfLinks = 0; //hard-coded			
            
			RxContext->Info.LengthRemaining -= sizeof(FILE_STANDARD_INFORMATION);				
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ of type FStdI %d\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName, giiFCB->isDirectory);
		}	
		break;	        
		default:
			DbgPrint("NulMRxQueryFileInformation: Ended for file %wZ not supported!\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
			Status = STATUS_NOT_SUPPORTED;
			break;
	}    

	return Status;
}


ULONG GenesisSendDirectoryAndTarget(PRX_CONTEXT RxContext, PVOID buffer){
/*
	Adds the target directory and regexp to the buffer to send the user mode
*/
	char * myBuffer = (char *) buffer;	
	int i;
	ANSI_STRING temp;		

	RtlUnicodeStringToAnsiString(&temp, RxContext->pRelevantSrvOpen->pAlreadyPrefixedName,TRUE);
	RtlCopyMemory(myBuffer, temp.Buffer, temp.Length);
		
	DbgPrint("NulMRX Genesis Copy: Length of Prefixed Name %d\n", temp.Length);
	for(i=0; i<temp.Length; i++){
		if(myBuffer[i] =='\\'){
			myBuffer[i] = '/';
		}
	}
	myBuffer[temp.Length] = '\0';
	myBuffer += temp.Length + 1;
	
	RtlFreeAnsiString(&temp);
		
	//Hardcode pattern as *.* for now
	myBuffer[0] = '\0';
	return temp.Length + 2;
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
	fcb->Size = size;	

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
		RtlCopyMemory(w_pointer, pointer, sizeof(long));
		w_pointer += sizeof(long);
		pointer += sizeof(long);

		//Grab file name
		mbstowcs(buffer, pointer, MAX_PATH_LENGTH);
		RtlCopyMemory(w_pointer, buffer, sizeof(WCHAR) * wcslen(buffer));		
		w_pointer[wcslen(buffer)] = L'\0';
		w_pointer += wcslen(buffer) + 1;
		pointer += strlen(pointer) + 1;
	}
}

void GenesisSaveInfoIntoFCB(PGENESIS_FCB fcb, PVOID info, int size){
/* 
	Gets file info out of buffer and into FCB 
*/
	char * pointer = (char *)info;			
	int i;

	//If an error occurred (return empty directory)
	if(size == -1){	
		fcb->State = GENII_STATE_NOT_FOUND;
		return;
	}			

	//Have listing supercedes have info
	if(fcb->State != GENII_STATE_HAVE_LISTING){
		fcb->State = GENII_STATE_HAVE_INFO;
	}

	for(i =0; i < size; i++){			
		//Grab file id
		RtlCopyMemory(&(fcb->GenesisFileID), pointer, sizeof(long));
		pointer += sizeof(long);

		//Grab F | D		
		if(strcmp(pointer, "D") == 0) fcb->isDirectory = TRUE;				
		pointer += strlen(pointer) + 1;

		//Grab length
		RtlCopyMemory(&(fcb->Size), pointer, sizeof(long));		
		pointer += sizeof(long);

		//Skip Name
	}
}
WCHAR * GenesisGetDirectoryEntry(PGENESIS_FCB fcb, int index, LARGE_INTEGER *fileLength, int *ifDirectory){
/* 
	Gets the directory entry specified by the index from the FCB 
	Sets ifDirectory and fileLength variables (0 for directories)
*/		
	WCHAR * pointer = fcb->DirectoryListing;	
	int i;
	*ifDirectory = -1;	
	fileLength->QuadPart = 0;

	for(i =0; i < index; i++){	
		pointer += wcslen(pointer) + 1; //skip F | D
		pointer += sizeof(long);		//skin length
		pointer += wcslen(pointer) + 1; //skip name
	}
	__try{
		if(pointer != NULL){
			if(wcscmp(pointer, L"D") == 0){
				*ifDirectory = 1;		
			}
			else{
				*ifDirectory = 0;				
			}
		}
	}finally{
		if(*ifDirectory == -1){
			pointer = NULL;
		}
		else{
			pointer += wcslen(pointer) + 1; //skip F | D
			RtlCopyMemory(&(fileLength->LowPart), pointer, sizeof(long)); // copy length			
			pointer += sizeof(long);		//skip length
		}
	}	
	return pointer;
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
	PLIST_ENTRY listEntry;
	PIRP controlIrp;
	PGENII_CONTROL_REQUEST controlRequest;	
	PFAST_MUTEX queueLock;
	PLIST_ENTRY queue;	
	
	PMDL mdl;
	PVOID controlBuffer;
    
	queue = &dataExt->GeniiRequestQueue;
    queueLock = &dataExt->GeniiRequestQueueLock;

	switch (controlExt->DeviceState) {
		case GENII_CONTROL_ACTIVE:			
			// Data device read must be satisfied by queuing request
			// off to the service.
			dataRequest = (PGENII_REQUEST) ExAllocatePoolWithTag(PagedPool, sizeof(GENII_REQUEST), 'rdCO');
			if (!dataRequest) {
				// Complete the request, indicating that the operation failed						
				status = STATUS_INSUFFICIENT_RESOURCES;
				break;
			}
			RtlZeroMemory(dataRequest, sizeof(GENII_REQUEST));
			dataRequest->RequestID = (ULONG) InterlockedIncrement(&GeniiRequestID);
			dataRequest->Irp = Irp;			

			//Also want originating RxContext!
			dataRequest->RxContext = RxContext;

			// Since we are enqueuing the IRP, mark it pending
			if(MarkAsPending){
				RxMarkContextPending(RxContext);			
				status = STATUS_PENDING;
			}
			
			// Insert the request into the appropriate queue here
			ExAcquireFastMutex(queueLock);
			InsertTailList(queue, &dataRequest->ListEntry);
			ExReleaseFastMutex(queueLock);
			
			// Now, let's try to dispatch this to a service thread (really an IRP)
			// and if we cannot do so, we need to enqueue it for later processing
			// when a thread becomes available.
			ExAcquireFastMutex(&controlExt->ServiceQueueLock);
			if (IsListEmpty(&controlExt->ServiceQueue)) {

				// No waiting threads.  We need to insert this into the service request queue
				ExAcquireFastMutex(&controlExt->RequestQueueLock);
				InsertTailList(&controlExt->RequestQueue, &dataRequest->ServiceListEntry);
				ExReleaseFastMutex(&controlExt->RequestQueueLock);
				
				// Release the service queue lock
				ExReleaseFastMutex(&controlExt->ServiceQueueLock);
			}else {
				// A service thread is available right now.  Remove the service thread
				// from the queue.						
				listEntry = RemoveHeadList(&controlExt->ServiceQueue);
				controlIrp = CONTAINING_RECORD(listEntry, IRP, Tail.Overlay.ListEntry);
				
				// Now build the request packet here.
				controlRequest = (PGENII_CONTROL_REQUEST) controlIrp->AssociatedIrp.SystemBuffer;
				controlRequest->RequestID = dataRequest->RequestID;			
				controlRequest->RequestType = callType;				

				// Our problem here is that the control buffer is in a different
				// address space.  So, we need to reach over into that address space and
				// grab it.
				mdl = IoAllocateMdl(controlRequest->RequestBuffer,
                                  controlRequest->RequestBufferLength,
                                  FALSE, // should not be any other MDLs associated with control IRP
                                  FALSE, // no quota charged
                                  controlIrp); // track the MDL in the control IRP...
				if (NULL == mdl) {                
					// We failed to get an MDL.  What a pain.                
					InsertTailList(&controlExt->ServiceQueue, listEntry);
                
					// Complete the data request - this falls through and completes below.
					status = STATUS_INSUFFICIENT_RESOURCES;
                
					// Release the service queue lock
					ExReleaseFastMutex(&controlExt->ServiceQueueLock);					                    
					status = STATUS_PENDING;
	                break;
                }

				__try {
					// Probe and lock the pages
					MmProbeAndLockProcessPages(mdl,
						IoGetRequestorProcess(controlIrp),UserMode,IoWriteAccess);

				} __except(EXCEPTION_EXECUTE_HANDLER) {
					// Access probe failed
	                
					status = GetExceptionCode();

					// Cleanup what we were doing....
					IoFreeMdl(mdl);
					InsertTailList(&controlExt->ServiceQueue, listEntry);

					// Release the service queue lock
					ExReleaseFastMutex(&controlExt->ServiceQueueLock);					
					status = STATUS_PENDING;
					break;
				}

				//Actually get pointer (same as ControlRequest->RequestBuffer)
				controlBuffer = MmGetSystemAddressForMdlSafe(mdl, NormalPagePriority);

				switch(callType){
					case GENII_QUERYFILEINFO:
						//Copies Directory and Target info into buffer with length
						controlRequest->RequestBufferLength = 
							GenesisSendDirectoryAndTarget(RxContext, controlBuffer);			
						break;
					case GENII_QUERYDIRECTORY:
						//Copies Directory and Target info into buffer with length
						controlRequest->RequestBufferLength = 
							GenesisSendDirectoryAndTarget(RxContext, controlBuffer);
						break;
					case GENII_CREATE:
						//Copies Directory and Target info into buffer with length
						controlRequest->RequestBufferLength = 
							GenesisSendDirectoryAndTarget(RxContext, controlBuffer);
						break;
					default:
						DbgPrint("Unsupported function placed in async queue");
						status = STATUS_NOT_SUPPORTED;
						return status;
				}

				// And complete the control request
				controlIrp->IoStatus.Status = STATUS_SUCCESS;
				controlIrp->IoStatus.Information = sizeof(GENII_CONTROL_REQUEST);          
				IoCompleteRequest(controlIrp, IO_NO_INCREMENT);						
		
				// Release the service queue lock
                ExReleaseFastMutex(&controlExt->ServiceQueueLock);
			}
			break;
			//end switch case
		default:
			//Device is inactive			
			status = STATUS_INVALID_DEVICE_REQUEST;
			break;				
	}		  
	// Done.
	return status;	
}


									   