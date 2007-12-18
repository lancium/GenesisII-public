/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    DownLvlI.c

Abstract:

    This module implements downlevel fileinfo, volinfo, and dirctrl.

--*/

#include "precomp.h"
#pragma hdrstop

//
//  The local debug trace level
//

RXDT_DefineCategory(DOWNLVLI);
#define Dbg                 (DEBUG_TRACE_DOWNLVLI)

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

    RxTraceEnter("NulMRxTruncateFile");

	PAGED_CODE();

    RxDbgTrace(0,  Dbg, ("NewFileSize is %d\n", pNewFileSize->LowPart));

	DbgPrint("NulMRxTruncate:  Truncating file size to %d\n", pNewFileSize->LowPart);

	capFcb->Header.FileSize = *pNewFileSize;	
	capFcb->Header.AllocationSize = capFcb->Header.FileSize;

	if (RtlLargeIntegerGreaterThan(capFcb->Header.ValidDataLength, *pNewFileSize)) {
		// Decrease the valid data length value.
		capFcb->Header.ValidDataLength = *pNewFileSize;
	}	

	pNewAllocationSize = &capFcb->Header.FileSize;

    RxTraceLeave(Status);
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

    RxTraceEnter("NulMRxExtendFile");

	PAGED_CODE();

    RxDbgTrace(0,  Dbg, ("NewFileSize is %d\n", pNewFileSize->LowPart));
	    
	DbgPrint("NulMRxExtend:  Extending file size to %d\n", pNewFileSize->LowPart);

	capFcb->Header.FileSize = *pNewFileSize;	
	capFcb->Header.AllocationSize = *pNewFileSize;

	if (RtlLargeIntegerGreaterThan(capFcb->Header.ValidDataLength, *pNewFileSize)) {
		// Decrease the valid data length value.
		capFcb->Header.ValidDataLength = *pNewFileSize;
	}	

	pNewAllocationSize = &capFcb->Header.FileSize;

    RxTraceLeave(Status);
    return Status;
}


