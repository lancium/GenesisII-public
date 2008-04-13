/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

Init.c

Abstract:

This module implements the DRIVER_INITIALIZATION routine for 
the null mini rdr.

--*/

#include "precomp.h"
#pragma  hdrstop

//  The local debug trace level

#define Dbg                              (DEBUG_TRACE_INIT)

#include "ntverp.h"
#include "nulmrx.h"

// Global data declarations.
NULMRX_STATE NulMRxState = NULMRX_STARTABLE;

//  Mini Redirector global variables.

//  LogRate
ULONG   LogRate = 0;

//  NULMRX version
ULONG   NulMRxVersion = VER_PRODUCTBUILD;

//  This is the minirdr dispatch table. It is initialized by 
//  NulMRxInitializeTables. This table will be used by the wrapper to call 
//  into this minirdr
struct _MINIRDR_DISPATCH  NulMRxDispatch;

// declare the shadow debugtrace controlpoints
RXDT_DefineCategory(CREATE);
RXDT_DefineCategory(CLEANUP);
RXDT_DefineCategory(CLOSE);
RXDT_DefineCategory(READ);
RXDT_DefineCategory(WRITE);
RXDT_DefineCategory(LOCKCTRL);
RXDT_DefineCategory(FLUSH);
RXDT_DefineCategory(PREFIX);
RXDT_DefineCategory(FCBSTRUCTS);
RXDT_DefineCategory(DISPATCH);
RXDT_DefineCategory(EA);
RXDT_DefineCategory(DEVFCB);
RXDT_DefineCategory(INIT);

// The following enumerated values signify the current state of the minirdr
// initialization. With the aid of this state information, it is possible
// to determine which resources to deallocate, whether deallocation comes
// as a result of a normal stop/unload, or as the result of an exception
typedef enum _NULMRX_INIT_STATES {
	NULMRXINIT_ALL_INITIALIZATION_COMPLETED,
	NULMRXINIT_GENII_CONTROLLER_CREATED,
	NULMRXINIT_MINIRDR_REGISTERED,
	NULMRXINIT_START
} NULMRX_INIT_STATES;

// function prototypes
NTSTATUS
NulMRxInitializeTables(
					   void
					   );

VOID
NulMRxUnload(
			 IN PDRIVER_OBJECT DriverObject
			 );

VOID
NulMRxInitUnwind(
				 IN PDRIVER_OBJECT DriverObject,
				 IN NULMRX_INIT_STATES NulMRxInitState
				 );


NTSTATUS
NulMRxFsdDispatch (
				   IN PDEVICE_OBJECT DeviceObject,
				   IN PIRP Irp
				   );

VOID
NulMRxReadRegistryParameters();

NTSTATUS VCGRGeniiControl(PDEVICE_OBJECT DeviceObject, PIRP Irp);
NTSTATUS VCGRGeniiControlCreate(PDEVICE_OBJECT DeviceObject, PIRP Irp);
NTSTATUS VCGRGeniiControlClose(PDEVICE_OBJECT DeviceObject, PIRP Irp);

NTSTATUS
DriverEntry(
			IN PDRIVER_OBJECT  DriverObject,
			IN PUNICODE_STRING RegistryPath
			)
			/*++

			Routine Description:

			This is the initialization routine for the mini redirector

			Arguments:

			DriverObject - Pointer to driver object created by the system.

			Return Value:

			RXSTATUS - The function value is the final status from the initialization
			operation.

			--*/
{
	NTSTATUS        	Status;
	PRX_CONTEXT     	RxContext = NULL;
	ULONG           	Controls = 0;
	NULMRX_INIT_STATES	NulMRxInitState = 0;

	UNICODE_STRING		NulMRxName;
	UNICODE_STRING		UserModeDeviceName;
	PNULMRX_DEVICE_EXTENSION dataExt;

	UNICODE_STRING		GeniiControlName;
	UNICODE_STRING		UserModeGCName;    
	PGENESIS_CONTROL_EXTENSION controlExt;

	ULONG i;

	DbgPrint("+++ NULMRX Driver %08lx Loaded +++\n", DriverObject);
	Status =  RxDriverEntry(DriverObject, RegistryPath);
	if (Status != STATUS_SUCCESS) {
		DbgPrint("Wrapper failed to initialize. Status = %08lx\n",Status);
		return(Status);
	}

	try {
		NulMRxInitState = NULMRXINIT_START;

		//  Register this minirdr with the connection engine. Registration makes the connection
		//  engine aware of the device name, driver object, and other characteristics.
		//  If registration is successful, a new device object is returned
		RtlInitUnicodeString(&NulMRxName, DD_NULMRX_FS_DEVICE_NAME_U);
		SetFlag(Controls,RX_REGISTERMINI_FLAG_DONT_PROVIDE_UNCS);
		SetFlag(Controls,RX_REGISTERMINI_FLAG_DONT_PROVIDE_MAILSLOTS);		

		Status = RxRegisterMinirdr(
			&NulMRxDeviceObject,				// where the new device object goes
			DriverObject,						// the Driver Object to register
			&NulMRxDispatch,					// the dispatch table for this driver
			Controls,							// dont register with unc and for mailslots
			&NulMRxName,						// the device name for this minirdr
			sizeof(NULMRX_DEVICE_EXTENSION),	// IN ULONG DeviceExtensionSize,
			FILE_DEVICE_NETWORK_FILE_SYSTEM,	// IN ULONG DeviceType - disk ?
			FILE_REMOTE_DEVICE					// IN  ULONG DeviceCharacteristics
			);

		if (Status!=STATUS_SUCCESS) {            
			try_return(Status);
		}

		//  Init the device extension data
		//  NOTE: the device extension actually points to fields
		//  in the RDBSS_DEVICE_OBJECT. Our space is past the end
		//  of this struct !!
		dataExt = (PNULMRX_DEVICE_EXTENSION)
			((PBYTE)(NulMRxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT));

		RxDefineNode(dataExt,NULMRX_DEVICE_EXTENSION);
		dataExt->DeviceObject = NulMRxDeviceObject;

		// initialize local connection list
		for (i = 0; i < 26; i++)
		{
			dataExt->LocalConnections[i] = FALSE;
		}
		// Mutex for synchronizining our connection list
		ExInitializeFastMutex( &dataExt->LCMutex );

		// The device object has been created. Need to setup a symbolic
		// link so that the device may be accessed from a Win32 user mode
		// application.
		RtlInitUnicodeString(&UserModeDeviceName, DD_NULMRX_USERMODE_SHADOW_DEV_NAME_U);
		Status = IoCreateSymbolicLink( &UserModeDeviceName, &NulMRxName);
		if (Status!=STATUS_SUCCESS) {            
			try_return(Status);
		}

		//initialize queue and mutex for main device
		InitializeListHead(&dataExt->GeniiRequestQueue);
		ExInitializeFastMutex(&dataExt->GeniiRequestQueueLock);

		ExInitializeResourceLite(&dataExt->VCBResource);

		NulMRxInitState = NULMRXINIT_MINIRDR_REGISTERED;

		/* ------------------ Genii Control Device code start ------------------------ */

		DbgPrint("Initializing the Genii Controller Device ... ");		
		RtlInitUnicodeString(&GeniiControlName, DD_GENII_CONTROL_DEVICE_NAME_U);						
		Status = IoCreateDevice(DriverObject,
			sizeof(GENESIS_CONTROL_EXTENSION),
			&GeniiControlName,
			GENII_CONTROL_TYPE,
			FILE_DEVICE_SECURE_OPEN,
			TRUE,
			&GeniiControlDeviceObject);	

		if(!NT_SUCCESS( Status )){
			try_return(Status);
		}

		//Initialize Extension for Device
		controlExt = (PGENESIS_CONTROL_EXTENSION) GeniiControlDeviceObject->DeviceExtension;		

		//method does allocation for me! :-)
		{
			CANSI_STRING dName;
			RtlInitAnsiString(&dName, "Genii");
			RtlAnsiStringToUnicodeString(&controlExt->DriverName, &dName, TRUE);				
		}
		//initialize queue and mutex for control
		InitializeListHead(&controlExt->ServiceQueue);
		ExInitializeFastMutex(&controlExt->ServiceQueueLock);
		InitializeListHead(&controlExt->RequestQueue);
		ExInitializeFastMutex(&controlExt->RequestQueueLock);		

		// Now we can create the symbolic link
		RtlInitUnicodeString(&controlExt->SymbolicLinkName, DD_GENII_CONTROL_DEVICE_USER_SHADOW_NAME);
		Status = IoCreateSymbolicLink(&controlExt->SymbolicLinkName, &GeniiControlName); 

		if(!NT_SUCCESS( Status )){
			try_return(Status);
		}

		DbgPrint("initialization complete!\n");

		NulMRxInitState = NULMRXINIT_GENII_CONTROLLER_CREATED;

		/* ------------------ Genii Control Device code done -------------------------- */        

		// Build the dispatch tables for the minirdr
		Status = NulMRxInitializeTables();

		if (!NT_SUCCESS( Status )) {
			try_return(Status);
		}

		// Get information from the registry
		NulMRxReadRegistryParameters();  
	} 	
	finally{} // do nothing for now

try_exit: NOTHING;

	if (Status != STATUS_SUCCESS) {
		NulMRxInitUnwind(DriverObject,NulMRxInitState);
		DbgPrint("NulMRx failed to start with %08lx %08lx\n",Status,NulMRxInitState);
		return(Status);
	}


	//  Setup Unload Routine
	DriverObject->DriverUnload = NulMRxUnload;

	//  setup the driver dispatch for people who come in here directly....like the browser
	for (i = 0; i < IRP_MJ_MAXIMUM_FUNCTION; i++)
	{
		DriverObject->MajorFunction[i] = (PDRIVER_DISPATCH)NulMRxFsdDispatch;
	}	

	//  Start the mini-rdr (used to be a START IOCTL)  
	RxContext = RxCreateRxContext(
		NULL,
		NulMRxDeviceObject,
		RX_CONTEXT_FLAG_IN_FSP);

	if (RxContext != NULL) {
		Status = RxStartMinirdr(
			RxContext,
			&RxContext->PostRequest);

		if (Status == STATUS_SUCCESS) {
			NULMRX_STATE State;

			State = (NULMRX_STATE)InterlockedCompareExchange(
				(LONG *)&NulMRxState,
				NULMRX_STARTED,
				NULMRX_STARTABLE);

			if (State != NULMRX_STARTABLE) {
				Status = STATUS_REDIRECTOR_STARTED;
				DbgPrint("Status is STATUS_REDIR_STARTED\n");
			}

			//
			//  Chance to get resources in context
			//  of system process.....!!!
			//

		} else if(Status == STATUS_PENDING ) {

		}

		RxDereferenceAndDeleteRxContext(RxContext);
	} else {
		Status = STATUS_INSUFFICIENT_RESOURCES;
	}

	return  STATUS_SUCCESS;
}

VOID
NulMRxInitUnwind(
				 IN PDRIVER_OBJECT DriverObject,
				 IN NULMRX_INIT_STATES NulMRxInitState
				 )
				 /*++

				 Routine Description:

				 This routine does the common uninit work for unwinding from a bad driver entry or for unloading.

				 Arguments:

				 NulMRxInitState - tells how far we got into the intialization

				 Return Value:

				 None

				 --*/

{
	PNULMRX_DEVICE_EXTENSION dataExt = (PNULMRX_DEVICE_EXTENSION)
		((PBYTE)(NulMRxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT));		

	PAGED_CODE();

	switch (NulMRxInitState) {
case NULMRXINIT_ALL_INITIALIZATION_COMPLETED:

	//Nothing extra to do...this is just so that the constant in RxUnload doesn't change.......
	//lack of break intentional

case NULMRXINIT_GENII_CONTROLLER_CREATED:
	IoDeleteDevice(GeniiControlDeviceObject);
	//lack of break intentional

case NULMRXINIT_MINIRDR_REGISTERED:
	ExDeleteResourceLite(&dataExt->VCBResource);
	RxUnregisterMinirdr(NulMRxDeviceObject);		

	//lack of break intentional

case NULMRXINIT_START:
	break;
	}

}

VOID
NulMRxUnload(
			 IN PDRIVER_OBJECT DriverObject
			 )
			 /*++

			 Routine Description:

			 This is the unload routine for the Exchange mini redirector.

			 Arguments:

			 DriverObject - pointer to the driver object for the NulMRx

			 Return Value:

			 None

			 --*/

{
	PRX_CONTEXT RxContext;
	NTSTATUS    Status;
	UNICODE_STRING  UserModeDeviceName;

	PAGED_CODE();

	NulMRxInitUnwind(DriverObject,NULMRXINIT_ALL_INITIALIZATION_COMPLETED);
	RxContext = RxCreateRxContext(
		NULL,
		NulMRxDeviceObject,
		RX_CONTEXT_FLAG_IN_FSP);

	if (RxContext != NULL) {
		Status = RxStopMinirdr(
			RxContext,
			&RxContext->PostRequest);


		if (Status == STATUS_SUCCESS) {
			NULMRX_STATE State;

			State = (NULMRX_STATE)InterlockedCompareExchange(
				(LONG *)&NulMRxState,
				NULMRX_STARTABLE,
				NULMRX_STARTED);

			if (State != NULMRX_STARTABLE) {
				Status = STATUS_REDIRECTOR_STARTED;
			}
		}

		RxDereferenceAndDeleteRxContext(RxContext);
	} else {
		Status = STATUS_INSUFFICIENT_RESOURCES;
	}

	RtlInitUnicodeString(&UserModeDeviceName, DD_NULMRX_USERMODE_SHADOW_DEV_NAME_U);
	Status = IoDeleteSymbolicLink( &UserModeDeviceName);
	if (Status!=STATUS_SUCCESS) {
		DbgPrint("NulMRx - main: Could not delete Symbolic Link\n");
	}

	/* Extra code for Genii Contorller unload */
	RtlInitUnicodeString(&UserModeDeviceName, DD_GENII_CONTROL_DEVICE_USER_NAME);
	Status = IoDeleteSymbolicLink( &UserModeDeviceName);
	if (Status!=STATUS_SUCCESS) {
		DbgPrint("NulMRx - Genii: Could not delete Symbolic Link\n");
	}		

	RxUnload(DriverObject);
	DbgPrint("+++ NULMRX Driver %08lx Unoaded +++\n", DriverObject);
}


NTSTATUS
NulMRxInitializeTables(
					   void
					   )
					   /*++

					   Routine Description:

					   This routine sets up the mini redirector dispatch vector and also calls
					   to initialize any other tables needed.

					   Return Value:

					   RXSTATUS - The return status for the operation

					   --*/
{


	// Ensure that the Exchange mini redirector context satisfies the size constraints    
	//ASSERT(sizeof(NULMRX_RX_CONTEXT) <= MRX_CONTEXT_SIZE);

	// Build the local minirdr dispatch table and initialize
	ZeroAndInitializeNodeType( &NulMRxDispatch, RDBSS_NTC_MINIRDR_DISPATCH, sizeof(MINIRDR_DISPATCH));

	// null mini redirector extension sizes and allocation policies.
	NulMRxDispatch.MRxFlags = (RDBSS_MANAGE_NET_ROOT_EXTENSION |
		RDBSS_MANAGE_FCB_EXTENSION);

	NulMRxDispatch.MRxSrvCallSize  = 0; // srvcall extension is not handled in rdbss
	NulMRxDispatch.MRxNetRootSize  = sizeof(NULMRX_NETROOT_EXTENSION);
	NulMRxDispatch.MRxVNetRootSize = 0;
	NulMRxDispatch.MRxFcbSize      = sizeof(NULMRX_FCB_EXTENSION);
	NulMRxDispatch.MRxSrvOpenSize  = 0;
	NulMRxDispatch.MRxFobxSize     = 0;

	// Mini redirector cancel routine ..    
	NulMRxDispatch.MRxCancel = NULL;

	// Mini redirector Start/Stop. Each mini-rdr can be started or stopped
	// while the others continue to operate.
	NulMRxDispatch.MRxStart                = NulMRxStart;
	NulMRxDispatch.MRxStop                 = NulMRxStop;
	NulMRxDispatch.MRxDevFcbXXXControlFile = NulMRxDevFcbXXXControlFile;

	// Mini redirector name resolution.
	NulMRxDispatch.MRxCreateSrvCall       = NulMRxCreateSrvCall;
	NulMRxDispatch.MRxSrvCallWinnerNotify = NulMRxSrvCallWinnerNotify;
	NulMRxDispatch.MRxCreateVNetRoot      = NulMRxCreateVNetRoot;
	NulMRxDispatch.MRxUpdateNetRootState  = NulMRxUpdateNetRootState;
	NulMRxDispatch.MRxExtractNetRootName  = NulMRxExtractNetRootName;
	NulMRxDispatch.MRxFinalizeSrvCall     = NulMRxFinalizeSrvCall;
	NulMRxDispatch.MRxFinalizeNetRoot     = NulMRxFinalizeNetRoot;
	NulMRxDispatch.MRxFinalizeVNetRoot    = NulMRxFinalizeVNetRoot;

	// File System Object Creation/Deletion.    
	NulMRxDispatch.MRxCreate            = NulMRxCreate;
	NulMRxDispatch.MRxCollapseOpen      = NulMRxCollapseOpen;
	NulMRxDispatch.MRxShouldTryToCollapseThisOpen = NulMRxShouldTryToCollapseThisOpen;
	NulMRxDispatch.MRxExtendForCache    = NulMRxExtendFile;
	NulMRxDispatch.MRxExtendForNonCache = NulMRxExtendFile;
	NulMRxDispatch.MRxTruncate          = NulMRxTruncate;
	NulMRxDispatch.MRxCleanupFobx       = NulMRxCleanupFobx;
	NulMRxDispatch.MRxCloseSrvOpen      = NulMRxCloseSrvOpen;
	NulMRxDispatch.MRxFlush             = NulMRxFlush;
	NulMRxDispatch.MRxForceClosed       = NulMRxForcedClose;
	NulMRxDispatch.MRxDeallocateForFcb  = NulMRxDeallocateForFcb;
	NulMRxDispatch.MRxDeallocateForFobx = NulMRxDeallocateForFobx;	

	// File System Objects query/Set
	NulMRxDispatch.MRxQueryDirectory       = NulMRxQueryDirectory;
	NulMRxDispatch.MRxQueryVolumeInfo      = NulMRxQueryVolumeInformation;
	NulMRxDispatch.MRxQueryEaInfo          = NulMRxQueryEaInformation;
	NulMRxDispatch.MRxSetEaInfo            = NulMRxSetEaInformation;
	NulMRxDispatch.MRxQuerySdInfo          = NulMRxQuerySecurityInformation;
	NulMRxDispatch.MRxSetSdInfo            = NulMRxSetSecurityInformation;
	NulMRxDispatch.MRxQueryFileInfo        = NulMRxQueryFileInformation;
	NulMRxDispatch.MRxSetFileInfo          = NulMRxSetFileInformation;
	NulMRxDispatch.MRxSetFileInfoAtCleanup = NulMRxSetFileInformationAtCleanup;	

	// Buffering state change
	NulMRxDispatch.MRxComputeNewBufferingState = NulMRxComputeNewBufferingState;

	// File System Object I/O
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_READ]            = NulMRxRead;
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_WRITE]           = NulMRxWrite;
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_SHAREDLOCK]      = NulMRxLocks;
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_EXCLUSIVELOCK]   = NulMRxLocks;
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_UNLOCK]          = NulMRxLocks;
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_UNLOCK_MULTIPLE] = NulMRxLocks;
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_FSCTL]           = NulMRxFsCtl;
	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_IOCTL]           = NulMRxIoCtl;

	NulMRxDispatch.MRxLowIOSubmit[LOWIO_OP_NOTIFY_CHANGE_DIRECTORY] = NulMRxNotifyChangeDirectory;

	// Miscellanous
	NulMRxDispatch.MRxCompleteBufferingStateChangeRequest = NulMRxCompleteBufferingStateChangeRequest;

	return(STATUS_SUCCESS);
}




NTSTATUS
NulMRxStart(
			PRX_CONTEXT RxContext,
			IN OUT PRDBSS_DEVICE_OBJECT RxDeviceObject
			)
			/*++

			Routine Description:

			This routine completes the initialization of the mini redirector fromn the
			RDBSS perspective. Note that this is different from the initialization done
			in DriverEntry. Any initialization that depends on RDBSS should be done as
			part of this routine while the initialization that is independent of RDBSS
			should be done in the DriverEntry routine.

			Arguments:

			RxContext - Supplies the Irp that was used to startup the rdbss

			Return Value:

			RXSTATUS - The return status for the operation

			--*/
{
	NTSTATUS Status = STATUS_SUCCESS;

	DbgPrint("Entering NulMRxStart \n");

	return Status;
}





NTSTATUS
NulMRxStop(
		   PRX_CONTEXT RxContext,
		   IN OUT PRDBSS_DEVICE_OBJECT RxDeviceObject
		   )
		   /*++

		   Routine Description:

		   This routine is used to activate the mini redirector from the RDBSS perspective

		   Arguments:

		   RxContext - the context that was used to start the mini redirector

		   pContext  - the null mini rdr context passed in at registration time.

		   Return Value:

		   RXSTATUS - The return status for the operation

		   --*/
{
	NTSTATUS Status;

	DbgPrint("Entering NulMRxStop \n");


	return(STATUS_SUCCESS);
}



NTSTATUS
NulMRxInitializeSecurity (VOID)
/*++

Routine Description:

This routine initializes the null miniredirector security .

Arguments:

None.

Return Value:

None.

Note:

This API can only be called from a FS process.

--*/
{
	NTSTATUS Status = STATUS_SUCCESS;

	ASSERT(IoGetCurrentProcess() == RxGetRDBSSProcess());
	return Status;
}


NTSTATUS
NulMRxUninitializeSecurity(VOID)
/*++

Routine Description:

Arguments:

None.

Return Value:

None.

Note:

This API can only be called from a FS process.

--*/
{
	NTSTATUS Status = STATUS_SUCCESS;

	PAGED_CODE();
	return Status;
}

NTSTATUS
NulMRxFsdDispatch (
				   IN PDEVICE_OBJECT DeviceObject,
				   IN PIRP Irp
				   )

				   /*++

				   Routine Description:

				   This routine implements the FSD dispatch for the mini DRIVER object. 

				   Arguments:

				   DeviceObject - Supplies the device object for the packet being processed.

				   Irp - Supplies the Irp being processed

				   Return Value:

				   RXSTATUS - The Fsd status for the Irp

				   --*/
{
	NTSTATUS Status = STATUS_SUCCESS;

	ASSERT(DeviceObject==(PDEVICE_OBJECT)NulMRxDeviceObject ||
		DeviceObject == GeniiControlDeviceObject);

	if (DeviceObject!=(PDEVICE_OBJECT)NulMRxDeviceObject &&
		DeviceObject != GeniiControlDeviceObject) {
			Irp->IoStatus.Status = STATUS_INVALID_DEVICE_REQUEST;
			Irp->IoStatus.Information = 0;
			IoCompleteRequest(Irp, IO_NO_INCREMENT );
			return (STATUS_INVALID_DEVICE_REQUEST);
	}

	if(DeviceObject==(PDEVICE_OBJECT)NulMRxDeviceObject){
		Status = RxFsdDispatch((PRDBSS_DEVICE_OBJECT)NulMRxDeviceObject,Irp);
	}
	else{
		//Target is Genesis Control Device
		PIO_STACK_LOCATION irpSp = IoGetCurrentIrpStackLocation(Irp);

		switch(irpSp->MajorFunction){

			case IRP_MJ_DEVICE_CONTROL:
				Status = VCGRGeniiControl(DeviceObject, Irp);
				break;
			case IRP_MJ_CREATE:
				Status = VCGRGeniiControlCreate(DeviceObject, Irp);
				break;
			case IRP_MJ_CLOSE:
				Status = VCGRGeniiControlClose(DeviceObject, Irp);
				break;		
			case IRP_MJ_FLUSH_BUFFERS:
			case IRP_MJ_CLEANUP:
				DbgPrint("G-ICING: Cleanup received\n");
				Irp->IoStatus.Status = STATUS_SUCCESS;
				Irp->IoStatus.Information = 0;
				IoCompleteRequest(Irp, IO_NO_INCREMENT);
				Status = STATUS_SUCCESS;
				break;
			default:		
				DbgPrint("NulMRxFsdDispatch:  Unknown control code for GC received %d.\n", irpSp->MajorFunction);
				Irp->IoStatus.Status = STATUS_INVALID_DEVICE_REQUEST;
				Irp->IoStatus.Information = 0;
				IoCompleteRequest(Irp, IO_NO_INCREMENT);
				Status = STATUS_INVALID_DEVICE_REQUEST;
		}
	}
	return Status;
}

NTSTATUS
NulMRxGetUlongRegistryParameter(
								HANDLE ParametersHandle,
								PWCHAR ParameterName,
								PULONG ParamUlong,
								BOOLEAN LogFailure
								)
								/*++

								Routine Description:

								This routine is called to read a ULONG param from t he registry.

								Arguments:

								ParametersHandle - the handle of the containing registry "folder"
								ParameterName    - name of the parameter to be read
								ParamUlong       - where to store the value, if successful
								LogFailure       - if TRUE and the registry stuff fails, log an error

								Return Value:

								RXSTATUS - STATUS_SUCCESS

								--*/
{
	ULONG Storage[16];
	PKEY_VALUE_PARTIAL_INFORMATION Value;
	ULONG ValueSize;
	UNICODE_STRING UnicodeString;
	NTSTATUS Status;
	ULONG BytesRead;

	PAGED_CODE(); //INIT

	Value = (PKEY_VALUE_PARTIAL_INFORMATION)Storage;
	ValueSize = sizeof(Storage);

	RtlInitUnicodeString(&UnicodeString, ParameterName);

	Status = ZwQueryValueKey(ParametersHandle,
		&UnicodeString,
		KeyValuePartialInformation,
		Value,
		ValueSize,
		&BytesRead);


	if (NT_SUCCESS(Status)) {
		if (Value->Type == REG_DWORD) {
			PULONG ConfigValue = (PULONG)&Value->Data[0];
			*ParamUlong = *((PULONG)ConfigValue);
			DbgPrint("readRegistryvalue %wZ = %08lx\n",&UnicodeString,*ParamUlong);
			return(STATUS_SUCCESS);
		} else {
			Status = STATUS_INVALID_PARAMETER;
		}
	}

	if (LogFailure)
	{
		// log the failure...
	}

	return Status;
}

VOID
NulMRxReadRegistryParameters()
{
	NTSTATUS Status;
	OBJECT_ATTRIBUTES ObjectAttributes;
	UNICODE_STRING ParametersRegistryKeyName;
	HANDLE ParametersHandle;
	ULONG Temp;

	RtlInitUnicodeString(&ParametersRegistryKeyName, NULL_MINIRDR_PARAMETERS);
	InitializeObjectAttributes(
		&ObjectAttributes,
		&ParametersRegistryKeyName,
		OBJ_CASE_INSENSITIVE,
		NULL,
		NULL
		);

	Status = ZwOpenKey (&ParametersHandle, KEY_READ, &ObjectAttributes);
	if (!NT_SUCCESS(Status)) {
		Status = NulMRxGetUlongRegistryParameter(ParametersHandle,
			L"LogRate",
			(PULONG)&Temp,
			FALSE
			);
	}
	if (NT_SUCCESS(Status)) LogRate = Temp;

	ZwClose(ParametersHandle);
}

// ProcessUFSClose
//  This routine is used to process a close from the UFS
// Inputs:
//  None.
// Outputs:
//  None.
// Returns:
//  STATUS_SUCCESS - the operation completed successfully
// Notes:
//  This is a helper function for the device control logic.  It does NOT
//  complete the control request - that is the job of the caller!  It DOES
//  complete the data request (if it finds a matching entry).
static NTSTATUS ProcessUFSClose()
{	
	PLIST_ENTRY dataQueue;
	PFAST_MUTEX dataQueueLock;
	PLIST_ENTRY listEntry;	
	PGENII_REQUEST dataRequest = NULL; 
	PIRP controlIrp;	
	NTSTATUS status = STATUS_SUCCESS;				

	PNULMRX_DEVICE_EXTENSION dataExt = (PNULMRX_DEVICE_EXTENSION)
		((PBYTE)(NulMRxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT));
	PGENESIS_CONTROL_EXTENSION controlExt =
		(PGENESIS_CONTROL_EXTENSION) GeniiControlDeviceObject->DeviceExtension;

	DbgPrint("ProcessUFSClose: Entered. \n");	

	//Get Data Queue locks and queue
	dataQueue = &dataExt->GeniiRequestQueue;
    dataQueueLock = &dataExt->GeniiRequestQueueLock;

	ExAcquireFastMutex(dataQueueLock);

	//Mark inactive
	controlExt->DeviceState = GENII_CONTROL_INACTIVE;

	//Get all other locks - time for clean up
	ExAcquireFastMutex(&controlExt->ServiceQueueLock);
	ExAcquireFastMutex(&controlExt->RequestQueueLock);		

	//Empty service Queue
	while(!IsListEmpty(dataQueue)){
		listEntry = RemoveHeadList(dataQueue);	
		dataRequest = CONTAINING_RECORD(listEntry, GENII_REQUEST, ListEntry);
		if (dataRequest != NULL) {

			BOOLEAN isSynchronous = IoIsOperationSynchronous(dataRequest->Irp);													
			GenesisGetFcbExtension(dataRequest->RxContext->pFcb, giiFcb);

			RemoveEntryList(listEntry);	

			switch(dataRequest->RequestType){
				case GENII_QUERYDIRECTORY:	
				{						
					if(isSynchronous){
						//Release semaphore
						KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);
					}
					else{
						status = STATUS_DEVICE_NOT_CONNECTED;
						//Release exclusive lock to FCB (information saved already)
						ExReleaseFastMutex(&giiFcb->ExclusiveLock);
						RxCompleteAsynchronousRequest(dataRequest->RxContext, status);						
					}					

					break;
				}

				//Always synchronous				
				case GENII_CREATE:
				case GENII_CLOSE:
				{										
					//Release semaphore.  This should make the create finish
					KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);																				
					break;
				}
				case GENII_READ:
				case GENII_TRUNCATEAPPEND:
				case GENII_WRITE:
				{				
					PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(dataRequest->RxContext);

					if(pIoCompContext->IoType == IO_TYPE_SYNCHRONOUS){
						//Release semaphore.  This should make the synch read finish
						KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);								
					}
					else{
						status = STATUS_DEVICE_NOT_CONNECTED;
						RxSetIoStatusInfo(dataRequest->RxContext, 0);
						RxSetIoStatusStatus(dataRequest->RxContext, status);
						RxLowIoCompletion(dataRequest->RxContext);
					}														
					break;
				}						
				default:							
					dataRequest->Irp->IoStatus.Status = STATUS_DEVICE_NOT_CONNECTED;
					dataRequest->Irp->IoStatus.Information = 0;
					IoCompleteRequest(dataRequest->Irp, IO_NO_INCREMENT);				
					break;
			}					
		}				
	}

	DbgPrint("ProcessUFSClose: Entries in DataQueue removed. \n");	

	//Empty request Queue
	while(!IsListEmpty(&controlExt->RequestQueue)){
		RemoveHeadList(&controlExt->RequestQueue);
	}	

	DbgPrint("ProcessUFSClose: Entries in RequestQueue removed. \n");

	//Empty service Queue
	while(!IsListEmpty(&controlExt->ServiceQueue)){
		listEntry = RemoveHeadList(&controlExt->ServiceQueue);
		controlIrp = CONTAINING_RECORD(listEntry, IRP, Tail.Overlay.ListEntry);
		if(controlIrp != NULL){					
			controlIrp->IoStatus.Status = STATUS_SUCCESS;      
			controlIrp->IoStatus.Information = sizeof(GENII_CONTROL_REQUEST);      
			IoCompleteRequest(controlIrp, IO_NO_INCREMENT);
		}
	}	

	DbgPrint("ProcessUFSClose: Entries in ServiceQueue removed. \n");	
	
	ExReleaseFastMutex(&controlExt->RequestQueueLock);
	ExReleaseFastMutex(&controlExt->ServiceQueueLock);	
	ExReleaseFastMutex(dataQueueLock);

	// Return the results of the operation.
	return STATUS_SUCCESS;
}



// ProcessResponse
//  This routine is used to process a response
// Inputs:
//  Irp - this is the IRP containing a (validated) response
// Outputs:
//  None.
// Returns:
//  STATUS_SUCCESS - the operation completed successfully
// Notes:
//  This is a helper function for the device control logic.  It does NOT
//  complete the control request - that is the job of the caller!  It DOES
//  complete the data request (if it finds a matching entry).
static NTSTATUS ProcessResponse(PIRP Irp)
{
	PGENII_CONTROL_RESPONSE response;
	PLIST_ENTRY dataQueue;
	PFAST_MUTEX dataQueueLock;
	PLIST_ENTRY listEntry;
	PLIST_ENTRY nextEntry;
	PGENII_REQUEST dataRequest = NULL; 
	NTSTATUS status = STATUS_SUCCESS;
	PVOID buffer = NULL;
	PIO_STACK_LOCATION irpSp;
	ULONG bytesToCopy;
	PMDL mdl;	
	BOOLEAN isActive;

	PNULMRX_DEVICE_EXTENSION dataExt = (PNULMRX_DEVICE_EXTENSION)
		((PBYTE)(NulMRxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT));
	PGENESIS_CONTROL_EXTENSION controlExt =
		(PGENESIS_CONTROL_EXTENSION) GeniiControlDeviceObject->DeviceExtension;

	DbgPrint("ProcessResponse: Entered. \n");

	// Get the response packet
	response = (PGENII_CONTROL_RESPONSE) Irp->AssociatedIrp.SystemBuffer;

	//Always assuming correct response type
	dataQueue = &dataExt->GeniiRequestQueue;
	dataQueueLock = &dataExt->GeniiRequestQueueLock;

	//Check to make sure if active first
	ExAcquireFastMutex(dataQueueLock);
	isActive = (controlExt->DeviceState == GENII_CONTROL_ACTIVE);	
	
	//Early abort if device is inactive
	if(!isActive){
		ExReleaseFastMutex(dataQueueLock);
		return status;
	}

	for (listEntry = dataQueue->Flink;
		listEntry != dataQueue;
		listEntry = listEntry->Flink) {

			// Check to see if this response matches up    
			dataRequest = CONTAINING_RECORD(listEntry, GENII_REQUEST, ListEntry);

			if (dataRequest != NULL && dataRequest->RequestID == response->RequestID) {

				//  This is our request, process it:
				//  - Remove it from the list
				//  - Transfer the data
				//  - Indicate the results
				//  - Complete the IRP
				BOOLEAN isSynchronous = IoIsOperationSynchronous(dataRequest->Irp);
				BOOLEAN hasReleasedRequest = FALSE;

				RemoveEntryList(listEntry);

				irpSp = IoGetCurrentIrpStackLocation(dataRequest->Irp);

				try{				
					PMRX_FCB commonFcb = dataRequest->RxContext->pFcb;
					GenesisGetFcbExtension(commonFcb, giiFcb);
					GenesisGetCcbExtension(commonFcb, giiCcb);

					switch(response->ResponseType){
						case GENII_QUERYDIRECTORY:	
							{
								//Stores result into File Control Block (should have exclusive access)
								GenesisSaveDirectoryListing(giiFcb, response->ResponseBuffer, response->StatusCode);								

								if(!isSynchronous){
									status = GenesisCompleteQueryDirectory(dataRequest->RxContext);
									//Release exclusive lock to FCB (information saved already)
									ExReleaseFastMutex(&giiFcb->ExclusiveLock);

									DbgPrint("NulMRxQueryDirectory: Ended for file %wZ\n", 
										dataRequest->RxContext->pRelevantSrvOpen->pAlreadyPrefixedName);
									
									RxCompleteAsynchronousRequest(dataRequest->RxContext, status);						
								}
								else{
									//Release semaphore
									KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);					
								}								

								//If it got here it is successful regardless
								status = STATUS_SUCCESS;
								break;
							}

						//Always synchronous
						case GENII_CREATE:
						{				
							//SAVE INTO FCB here
							GenesisSaveInfoIntoFCB(commonFcb, response->ResponseBuffer, response->StatusCode);										

							//Release semaphore.  This should make the create finish
							KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);

							//If it got here it is successful regardless
							status = STATUS_SUCCESS;					
							break;
						}
						case GENII_RENAME:
						{
							memcpy(&giiCcb->lastStatus, response->ResponseBuffer, sizeof(int));

							//Release semaphore.  This should make the create finish
							KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);

							//If it got here it is successful regardless
							status = STATUS_SUCCESS;					
							break;
						}
						case GENII_READ:			
						{				
							PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(dataRequest->RxContext);
							ULONG Information = 0;

							if (response->ResponseBufferLength < irpSp->Parameters.Read.Length) {
								bytesToCopy = response->ResponseBufferLength;
							} else {
								bytesToCopy = irpSp->Parameters.Read.Length;
							}

							DbgPrint("GeniiReadReturn:  Starting copy\n");

							//Check to see if some error happened
							if(response->ResponseBufferLength == -1){
								status = STATUS_FILE_INVALID;
								DbgPrint("GeniiReadReturn: Response buffer -1\n");
							}

							else{
								buffer = pIoCompContext->Context;						

								__try { 
									RtlCopyMemory(buffer, response->ResponseBuffer, bytesToCopy);							
									DbgPrint("GeniiReadReturn:  Copy successful to temp buffer %d\n", bytesToCopy);
									status = STATUS_SUCCESS;
									Information = bytesToCopy;
								} __except (EXCEPTION_EXECUTE_HANDLER) {
									status = GetExceptionCode();							
								}							
							}

							//Complete Request
							pIoCompContext->Status = status;
							pIoCompContext->Information = Information;

							if(pIoCompContext->IoType == IO_TYPE_SYNCHRONOUS){
								//Release semaphore.  This should make the synch read finish
								KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);
							}
							else{
								RxSetIoStatusInfo(dataRequest->RxContext, Information);
								RxSetIoStatusStatus(dataRequest->RxContext, status);
								status = RxLowIoCompletion(dataRequest->RxContext);
							}

							// The control operation was successful in any case
							status = STATUS_SUCCESS;

							// And break from the loop, no sense looking any farther
							break;
						}
						case GENII_TRUNCATEAPPEND:
						case GENII_WRITE:
						{
							PGENESIS_COMPLETION_CONTEXT pIoCompContext = GenesisGetMinirdrContext(dataRequest->RxContext);
							ULONG Information = 0;

							if(response->ResponseType == GENII_TRUNCATEAPPEND){
								DbgPrint("GeniiTruncateAppend returned!\n");
							}else{
								DbgPrint("GeniiWriteReturned\n");
							}

							//Check to see if some error happened
							if(response->ResponseBufferLength == -1){
								status = STATUS_ACCESS_DENIED;						
							}
							else{
								Information = response->ResponseBufferLength;
								status = STATUS_SUCCESS;
							}

							pIoCompContext->Information = Information;
							pIoCompContext->Status = status;

							if(pIoCompContext->IoType == IO_TYPE_SYNCHRONOUS){
								//Release semaphore.  This should make the synch read finish
								KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);								
							}
							else{
								RxSetIoStatusInfo(dataRequest->RxContext, Information);
								RxSetIoStatusStatus(dataRequest->RxContext, status);
								status = RxLowIoCompletion(dataRequest->RxContext);
							}

							status = STATUS_SUCCESS;
							break;

						}
						case GENII_CLOSE:
							//Release semaphore.  This should make the close finish
							KeReleaseSemaphore(&(giiFcb->InvertedCallSemaphore),0, 1, FALSE);		
							break;
						default:
							DbgPrint("ProcessResponse:  Invalid Response Type!\n");
							dataRequest->Irp->IoStatus.Status = STATUS_INVALID_PARAMETER;
							dataRequest->Irp->IoStatus.Information = 0;
							IoCompleteRequest(dataRequest->Irp, IO_NO_INCREMENT);				
							break;
					}
					hasReleasedRequest = TRUE;
				}
				finally{
					if(!hasReleasedRequest){
						DbgPrint("Response type was unknown %d  \n", response->ResponseType);	
						dataRequest->Irp->IoStatus.Status = STATUS_INVALID_PARAMETER;
						dataRequest->Irp->IoStatus.Information = 0;
						IoCompleteRequest(dataRequest->Irp, IO_NO_INCREMENT);	
					}
				}			
			}
	}
	ExReleaseFastMutex(dataQueueLock);

	// Return the results of the operation.
	return status;
}


// ProcessControlRequest
//  This routine is used to either satisfy the control request or enqueue it
// Inputs:
//  Irp - this is the IRP that we are processing
//  ControlRequest - this is the control request (from the IRP, actually)
// Outputs:
//  None.
// Returns:
//  SUCCESS - there's data going back up to the application
//  PENDING - the IRP will block and wait 'til it is time...
static NTSTATUS ProcessControlRequest(PIRP Irp)
{
	PLIST_ENTRY listEntry = NULL;
	NTSTATUS status = STATUS_SUCCESS;
	PGENII_CONTROL_REQUEST controlRequest;
	PIRP dataIrp;
	PGENII_REQUEST dataRequest;
	PIO_STACK_LOCATION irpSp;
	PVOID dataBuffer;
	ULONG bytesToCopy;	
	BOOLEAN isActive;

	PNULMRX_DEVICE_EXTENSION dataExt = (PNULMRX_DEVICE_EXTENSION)
		((PBYTE)(NulMRxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT));
	PGENESIS_CONTROL_EXTENSION controlExt =
		(PGENESIS_CONTROL_EXTENSION) GeniiControlDeviceObject->DeviceExtension;

	DbgPrint("ProcessControlRequest: Entered. \n");

	//Data Queue Lock
	ExAcquireFastMutex(&dataExt->GeniiRequestQueueLock);
	isActive = (controlExt->DeviceState == GENII_CONTROL_ACTIVE);
	ExReleaseFastMutex(&dataExt->GeniiRequestQueueLock);
	
	//Early abort if device is inactive
	if(!isActive){
		return status;
	}

	//First, we need to lock the control queue before we do anything else
	ExAcquireFastMutex(&controlExt->ServiceQueueLock);
	ExAcquireFastMutex(&controlExt->RequestQueueLock);

	// Check request queue
	if (!IsListEmpty(&controlExt->RequestQueue)) {
		listEntry = RemoveHeadList(&controlExt->RequestQueue);
		status = STATUS_SUCCESS;
	} else {
		controlRequest = (PGENII_CONTROL_REQUEST) Irp->AssociatedIrp.SystemBuffer;
		irpSp = IoGetCurrentIrpStackLocation(Irp);

		if(!controlRequest || irpSp->Parameters.DeviceIoControl.OutputBufferLength < sizeof(GENII_CONTROL_REQUEST)) {
			status = STATUS_INVALID_PARAMETER;
		} else {
			// We have to insert the control IRP into the queue
			IoMarkIrpPending(Irp);			
			InsertTailList(&controlExt->ServiceQueue, &Irp->Tail.Overlay.ListEntry);
			status = STATUS_PENDING;
		}
	}
	// OK.  At this point we can drop both locks
	ExReleaseFastMutex(&controlExt->RequestQueueLock);
	ExReleaseFastMutex(&controlExt->ServiceQueueLock);

	// If we found an entry to process, we need to return the information to
	// the caller here.
	if (listEntry) {

		// This is the request we removed from the queue.
		controlRequest = (PGENII_CONTROL_REQUEST) Irp->AssociatedIrp.SystemBuffer;

		dataRequest = CONTAINING_RECORD(listEntry, GENII_REQUEST, ServiceListEntry);
		irpSp = IoGetCurrentIrpStackLocation(dataRequest->Irp);

		// We are going to use the request ID to match the response
		controlRequest->RequestID = dataRequest->RequestID;	
		controlRequest->RequestType = dataRequest->RequestType;

		//Use buffer given			
		switch(controlRequest->RequestType){
			case GENII_QUERYDIRECTORY:
			{
				DbgPrint("QueryDirectory being serviced\n");				

				//Copies Directory and Target info into buffer with length
				controlRequest->RequestBufferLength = 
					GenesisPrepareDirectoryAndTarget(dataRequest->RxContext, 
					controlRequest->RequestBuffer, TRUE);

				// We've finished processing the request to this point.  Dispatch to the control application
				// for further processing.
				status = STATUS_SUCCESS;					
				break;
			}
			case GENII_CLOSE:
			{
				DbgPrint("Close being serviced in queue\n");				

				//Copies Directory and Target info into buffer with length
				controlRequest->RequestBufferLength = 
					GenesisPrepareClose(dataRequest->RxContext, 
					controlRequest->RequestBuffer);
				status = STATUS_SUCCESS;
				break;
			}
			case GENII_CREATE:
			{				
				DbgPrint("Create being serviced\n");	

				//Copies Directory and Target info into buffer with length
				controlRequest->RequestBufferLength = 
					GenesisPrepareCreate(dataRequest->RxContext, 
					controlRequest->RequestBuffer);						

				// We've finished processing the request to this point.  Dispatch to the control application
				// for further processing.
				status = STATUS_SUCCESS;
				break;
			}
			case GENII_WRITE:
			{	
				BOOLEAN isTruncateAppend=FALSE;
				BOOLEAN isTruncateWrite=FALSE;				

				DbgPrint("Write being serviced\n");

				//Copies Target info with other info
				controlRequest->RequestBufferLength = 
					GenesisPrepareWriteParams(dataRequest->RxContext, 
					controlRequest->RequestBuffer, &isTruncateAppend, &isTruncateWrite);	

				if(isTruncateAppend || isTruncateWrite){
					controlRequest->RequestType = GENII_TRUNCATEAPPEND;
				}
				status = STATUS_SUCCESS;
				break;
			}
			case GENII_READ:
			{		
				DbgPrint("Read being serviced\n");

				//Copies Target info with other info
				controlRequest->RequestBufferLength = 
					GenesisPrepareReadParams(dataRequest->RxContext, 
					controlRequest->RequestBuffer);										

				status = STATUS_SUCCESS;
				break;
			}
			case GENII_RENAME:
			{
				DbgPrint("Rename being serviced\n");

				//Copies Target info with other info
				controlRequest->RequestBufferLength = 
					GenesisPrepareRename(dataRequest->RxContext, 
					controlRequest->RequestBuffer);										

				status = STATUS_SUCCESS;
				break;

			}
			default:
				DbgPrint("Unsupported function placed in async queue %x\n",irpSp->MajorFunction);
				break;
		}						
	}
	if(status == STATUS_SUCCESS) {
		Irp->IoStatus.Information = sizeof(GENII_CONTROL_REQUEST);
	}
	return status;
}

// VCGRGeniiControl
//  This is the device control entry point
// Inputs:
//  DeviceObject - this is the device object on which we are operating
//  Irp - this is the device control IRP
// Outputs:
//  None.
// Returns:
//  SUCCESS - the operation was successful.
// Notes:
//  Modified from code about inverted calls from OSROnline
NTSTATUS VCGRGeniiControl(PDEVICE_OBJECT DeviceObject, PIRP Irp)
{
	PIO_STACK_LOCATION irpSp = IoGetCurrentIrpStackLocation(Irp);
	BOOLEAN sendResponse = FALSE;
	BOOLEAN getRequest = FALSE;
	BOOLEAN ufsStop = FALSE;
	NTSTATUS status;  

	//Double-check to make sure the IRP is meant for G-ICING
	if (GENII_CONTROL_TYPE != DeviceObject->DeviceType) {
		Irp->IoStatus.Status = STATUS_INVALID_DEVICE_REQUEST;
		Irp->IoStatus.Information = 0;
		IoCompleteRequest(Irp, IO_NO_INCREMENT);

		// Note that we don't do this...
		return STATUS_INVALID_DEVICE_REQUEST;
	}

	// Check the control code  
	switch (irpSp->Parameters.DeviceIoControl.IoControlCode) {	  
		case GENII_CONTROL_GET_REQUEST:			
			getRequest = TRUE;
			break;

		case GENII_CONTROL_SEND_RESPONSE:		
			sendResponse = TRUE;	
			break;

		case GENII_CONTROL_UFS_STOP:
			ufsStop = TRUE;
			break;

		//Incorrect IO control sent
		default:    	
			DbgPrint("G-ICING:  Error! Unknown control code received.\n");
			Irp->IoStatus.Status = STATUS_INVALID_DEVICE_REQUEST;
			Irp->IoStatus.Information = 0;
			IoCompleteRequest(Irp, IO_NO_INCREMENT);
			return STATUS_INVALID_DEVICE_REQUEST;		
	}    

	//Process the request from UFS (queue up request etc)
	if (getRequest) {
		if (irpSp->Parameters.DeviceIoControl.OutputBufferLength < sizeof(GENII_CONTROL_REQUEST)) {        			
			status = STATUS_BUFFER_OVERFLOW; 
		}
		else{
			// Now process the request.  This IRP may be queued as part of the processing here.
			status = ProcessControlRequest(Irp);
		}
	}

	//Process the response from UFS 
	if (sendResponse) {    	  
		// Validate response parameters
		if (irpSp->Parameters.DeviceIoControl.InputBufferLength < sizeof(GENII_CONTROL_RESPONSE)) {
			status = STATUS_BUFFER_TOO_SMALL;			
		}
		else{
			// Have to handle response first.
			status = ProcessResponse(Irp);
		}
	}

	//UFS is shutting down, clear queues, etc
	if(ufsStop){
		status = ProcessUFSClose();
	}

	// If the request was not pending, we complete it here. 
	if (STATUS_PENDING != status) {
		Irp->IoStatus.Status = status;
		if(status == STATUS_BUFFER_TOO_SMALL)
			Irp->IoStatus.Information = getRequest? sizeof(GENII_CONTROL_REQUEST) : sizeof(GENII_CONTROL_RESPONSE);
		else
			Irp->IoStatus.Information = 0;
		IoCompleteRequest(Irp, IO_NO_INCREMENT);
	}
	else{
		IoMarkIrpPending(Irp);
	}

	// Regardless, at this point we return the status back to the caller.
	return status;
}


// VCGRGeniiControlCreate
//  This is the create entry point
// Inputs:
//  DeviceObject - this is the device object on which we are operating
//  Irp - this is the create IRP
// Outputs:
//  None.
// Returns:
//  SUCCESS - the operation was successful.
// Notes:
//  None.
//
NTSTATUS VCGRGeniiControlCreate(PDEVICE_OBJECT DeviceObject, PIRP Irp)
{
	PGENESIS_CONTROL_EXTENSION controlExt;

	Irp->IoStatus.Status = STATUS_SUCCESS;
	Irp->IoStatus.Information = FILE_OPENED;		

	// If this was the control device, note that we now have
	// activated the data device.  
	controlExt = GeniiControlDeviceObject->DeviceExtension;
	controlExt->DeviceState = GENII_CONTROL_ACTIVE;

	IoCompleteRequest(Irp, IO_NO_INCREMENT);

	return STATUS_SUCCESS;
}

// VCGRGeniiControlClose
//  This is the close entry point
// Inputs:
//  DeviceObject - this is the device object on which we are operating
//  Irp - this is the close IRP
// Outputs:
//  None.
// Returns:
//  SUCCESS - the operation was successful.
// Notes:
//  None.
NTSTATUS VCGRGeniiControlClose(PDEVICE_OBJECT DeviceObject, PIRP Irp)
{  
	DbgPrint("G-ICING:  Close received on control object\n");
	Irp->IoStatus.Status = STATUS_SUCCESS;
	Irp->IoStatus.Information = 0;
	IoCompleteRequest(Irp, IO_NO_INCREMENT);
	return STATUS_SUCCESS;
}