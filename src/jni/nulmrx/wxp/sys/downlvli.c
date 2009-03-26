/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    DownLvlI.c

Abstract:

    This module implements truncate and extend

--*/

#include "precomp.h"
#pragma hdrstop

NTSTATUS
NulMRxTruncateFile(
    IN OUT struct _RX_CONTEXT * RxContext,
    IN OUT PLARGE_INTEGER   pNewFileSize,
    OUT PLARGE_INTEGER   pNewAllocationSize
    )
/*++

Routine Description:

   This routine handles requests to truncate the file

Arguments:

    RxContext - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;
    RxCaptureFcb;	
	GenesisGetFcbExtension(capFcb,giiFCB);    	    

	PAGED_CODE();    

	GIIPrint(("GenesisDrive:  Truncating file size to %I64d\n", pNewFileSize->QuadPart));

	capFcb->Header.FileSize = *pNewFileSize;	
	capFcb->Header.AllocationSize = capFcb->Header.FileSize;

	if (RtlLargeIntegerGreaterThan(capFcb->Header.ValidDataLength, *pNewFileSize)) {
		// Decrease the valid data length value.
		capFcb->Header.ValidDataLength = *pNewFileSize;
	}	

	pNewAllocationSize = &capFcb->Header.FileSize;
    return Status;
}

NTSTATUS
NulMRxExtendFile(
    IN OUT struct _RX_CONTEXT * RxContext,
    IN OUT PLARGE_INTEGER   pNewFileSize,
    OUT PLARGE_INTEGER   pNewAllocationSize
    )
/*++

Routine Description:

   This routine handles requests to extend the file for cached IO.

Arguments:

    RxContext - the RDBSS context

Return Value:

    NTSTATUS - The return status for the operation

--*/
{
    NTSTATUS Status = STATUS_SUCCESS;
    RxCaptureFcb;
    GenesisGetFcbExtension(capFcb,giiFCB);          

	PAGED_CODE();    
	    
	GIIPrint(("GenesisDrive:  Extending file size to %I64d\n", pNewFileSize->QuadPart));

	capFcb->Header.FileSize = *pNewFileSize;	
	capFcb->Header.AllocationSize = *pNewFileSize;

	//Not valid
	/*if (RtlLargeIntegerGreaterThan(capFcb->Header.ValidDataLength, *pNewFileSize)) {
		// Decrease the valid data length value.
		capFcb->Header.ValidDataLength = *pNewFileSize;
	}*/	

	pNewAllocationSize = &capFcb->Header.FileSize;
    
    return Status;
}


