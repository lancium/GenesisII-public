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
	LONG *FileIndex;
	LONG *DirectorySize;			
	PUNICODE_STRING target;

	RxCaptureFcb;
	RxCaptureFobx;		
	GenesisGetFcbExtension(capFcb, fcb);
	GenesisGetCcbExtension(capFobx, ccb);

	PAGED_CODE();			      

	KeWaitForSingleObject(&(fcb->FcbPhore), Executive, KernelMode, FALSE, NULL);

	if(IoIsOperationSynchronous(RxContext->CurrentIrp)){
		DbgPrint("QueryDirectory: Io Is Synchronous\n");
	}

	//GenesisII-Specific File Index Information	
	FileIndex = &(ccb->FileIndex);
	DirectorySize = &(fcb->DirectorySize);

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

	//If initial query, reset counter
	if(RxContext->QueryDirectory.InitialQuery)
	{		
		/* Print out directory and target */				
		ANSI_STRING tempString;
		target = (PUNICODE_STRING) RxContext->CurrentIrpSp->Parameters.QueryDirectory.FileName;		
		//Start on second char		
		if(target != NULL){			
			RtlUnicodeStringToAnsiString(&tempString, target, TRUE);
			tempString.Buffer[tempString.Length] = '\0';
		
			DbgPrint("QueryDirectory: Started for file %wZ with target %s\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName,
				tempString.Buffer);								
			DbgPrint("QueryDirectory: Target before conversion %wZ\n", target);		

			if(RtlCompareString(&tempString, &ccb->Target, FALSE) != 0){
				DbgPrint("QueryDirectory:  Targets do not match.  Copying new Target and calling Genesis\n");
				fcb->State = GENII_STATE_HAVE_INFO; //revert state 								
				RtlCopyString(&(ccb->Target), &tempString);				
			}		
		}
		else{		
			//Only initialize if not already gotten contents
			if(fcb->State != GENII_STATE_HAVE_LISTING){
				RtlInitAnsiString(&tempString, "*\0");
				RtlCopyString(&(ccb->Target), &tempString);
				DbgPrint("QueryDirectory:  Started for file %wZ with target * \n",
					RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);			
			}
		}
		ccb->Target.Buffer[ccb->Target.Length] = '\0';

		*FileIndex = 0;		
	}				
			
	//Check if we have the listing yet
	if(fcb->State != GENII_STATE_HAVE_LISTING){
		//Keep semaphore (releases after async call)

		//Calls Genesis to get Directory Listing
		Status = GenesisSendInvertedCall(RxContext, GENII_QUERYDIRECTORY, 
			(!IoIsOperationSynchronous(RxContext->CurrentIrp)));

		//If this operation is synchronous we don't block
		if(IoIsOperationSynchronous(RxContext->CurrentIrp)){
			KeWaitForSingleObject(&(fcb->FcbPhore), Executive, KernelMode, FALSE, NULL);			
			
			//Actually get listing
			Status = GenesisCompleteQueryDirectory(RxContext);	
			KeReleaseSemaphore(&(fcb->FcbPhore), 0, 1, FALSE);
		}

		try_return(Status);
	}						
	else{				
		//Actually get listing
		Status = GenesisCompleteQueryDirectory(RxContext);					
		
		//Release Semaphore
		KeReleaseSemaphore(&(fcb->FcbPhore), 0, 1, FALSE);

		try_return(Status);
	}				

try_exit:	NOTHING;

	if(Status != STATUS_PENDING){
		DbgPrint("QueryDirectory: Ended for file %wZ\n", RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
	}
	return Status;
}



NTSTATUS GenesisCompleteQueryDirectory(PRX_CONTEXT RxContext){
/*
	Completes a QueryDirectory method 
	Used after FCB is stored
*/

	NTSTATUS Status = STATUS_INVALID_PARAMETER;
    FILE_INFORMATION_CLASS FileInformationClass;
    PCHAR   Buffer;
    PULONG  pLengthRemaining;
	ULONG	CopySize;
	ULONG	BytesLeft;
	ULONG   TotalCopySize = 0;
	LONG *FileIndex;
	LONG *DirectorySize;	
	PIRP PtrIrp = RxContext->CurrentIrp;
	
	RxCaptureFcb;
	RxCaptureFobx;		
	GenesisGetFcbExtension(capFcb, fcb);
	GenesisGetCcbExtension(capFobx, ccb);

	PAGED_CODE();

    FileInformationClass = RxContext->Info.FileInformationClass;

	// We must determine the buffer pointer to be used. Since this
	// routine could either be invoked directly in the context of the
	// calling thread, or in the context of a worker thread, here is
	// a general way of determining what we should use.
	if (PtrIrp->MdlAddress) {
		Buffer = (PCHAR)MmGetSystemAddressForMdl(PtrIrp->MdlAddress);
	} else {
		Buffer = (PCHAR)PtrIrp->UserBuffer;
	}

	BytesLeft = RxContext->CurrentIrpSp->Parameters.QueryDirectory.Length;

	//Not used
	pLengthRemaining = &RxContext->Info.LengthRemaining;	

	//GenesisII-Specific File Index Information	
	FileIndex = &(ccb->FileIndex);	
	DirectorySize = &(fcb->DirectorySize);

	DbgPrint("QueryDirectory:  FileIndex %d, DirectorySize: %d\n", *FileIndex, *DirectorySize);

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
		PtrIrp->IoStatus.Information = TotalCopySize;
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
				
				pDirInfo->FileAttributes = FILE_ATTRIBUTE_NORMAL;
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
					pDirInfo->FileAttributes = FILE_ATTRIBUTE_NORMAL;
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
				pDirInfo->FileAttributes = FILE_ATTRIBUTE_NORMAL;
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
				if(RxContext->QueryDirectory.ReturnSingleEntry ||
					*FileIndex >= *DirectorySize){						
					break;
				}
		
				//Moves the pointer to the next entry to fill
				Buffer += pDirInfo->NextEntryOffset; // move pointer
				
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

	PtrIrp->IoStatus.Information = TotalCopySize;
	RxContext->Info.LengthRemaining -= TotalCopySize;	
	
    return(Status);
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