/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    mrxglobs.h

Abstract:

    The global include file for NULMRX mini-redirector

--*/

#ifndef _MRXGLOBS_H_
#define _MRXGLOBS_H_

extern PRDBSS_DEVICE_OBJECT NulMRxDeviceObject;
#define RxNetNameTable (*(*___MINIRDR_IMPORTS_NAME).pRxNetNameTable)

// The following enum type defines the various states associated with the null
// mini redirector. This is used during initialization

typedef enum _NULMRX_STATE_ {
   NULMRX_STARTABLE,
   NULMRX_START_IN_PROGRESS,
   NULMRX_STARTED
} NULMRX_STATE,*PNULMRX_STATE;

extern NULMRX_STATE NulMRxState;
extern ULONG        LogRate;
extern ULONG        NulMRxVersion;

//
//  Reg keys
//
#define NULL_MINIRDR_PARAMETERS \
    L"\\Registry\\Machine\\System\\CurrentControlSet\\Services\\NulMRx\\Parameters"

//
//  Use the RxDefineObj and RxCheckObj macros
//  to enforce signed structs.
//

#define RxDefineObj( type, var )            \
        var.Signature = type##_SIGNATURE;

#define RxCheckObj( type, var )             \
        ASSERT( (var).Signature == type##_SIGNATURE );

// A DbgPrint for this driver.  Turns off when bit set to 0 (expensive printf's in debug mode)
#define GENESIS_DBG 0
#if GENESIS_DBG
#define GIIPrint(Args) DbgPrint##Args
#else
#define GIIPrint(Args) NOP_FUNCTION
#endif

//
//  Use the RxDefineNode and RxCheckNode macros
//  to enforce node signatures and sizes.
//

#define RxDefineNode( node, type )          \
        node->NodeTypeCode = NTC_##type;    \
        node->NodeByteSize = sizeof(type);

#define RxCheckNode( node, type )           \
        ASSERT( NodeType(node) == NTC_##type );

//
// struct node types - start from 0xFF00
//
typedef enum _NULMRX_STORAGE_TYPE_CODES {
    NTC_NULMRX_DEVICE_EXTENSION      =   (NODE_TYPE_CODE)0xFF00,
    NTC_NULMRX_SRVCALL_EXTENSION     =   (NODE_TYPE_CODE)0xFF01,
    NTC_NULMRX_NETROOT_EXTENSION     =   (NODE_TYPE_CODE)0xFF02,
    NTC_NULMRX_FCB_EXTENSION         =   (NODE_TYPE_CODE)0xFF03
    
} NULMRX_STORAGE_TYPE_CODES;

//
// typedef our device extension - stores state global to the driver
//
typedef struct _NULMRX_DEVICE_EXTENSION {
    //
    //  Node type code and size
    //
    NODE_TYPE_CODE          NodeTypeCode;
    NODE_BYTE_SIZE          NodeByteSize;
    
    //  Back-pointer to owning device object
    PRDBSS_DEVICE_OBJECT    DeviceObject;

    //  Count of active nodes
    //  Driver can be unloaded iff ActiveNodes == 0
    ULONG                   ActiveNodes;
	
	//	Keep a list of local connections used
	CHAR					LocalConnections[26];
	FAST_MUTEX				LCMutex;

	//GENII specific stuff	
	ERESOURCE VCBResource;

	// Queue - only one because synchronous when talking to user service
	// Maybe make more efficient later (would require server able to multi-process)

	// Write Request Queue
	LIST_ENTRY GeniiRequestQueue;

	// Write Request Queue Lock  
	FAST_MUTEX GeniiRequestQueueLock;

} NULMRX_DEVICE_EXTENSION, *PNULMRX_DEVICE_EXTENSION;

#define GENESIS_CONTROL_EXTENSION_MAGIC_NUMBER 0x1d88f503

// typedef our device extension - stores state global to the driver
typedef struct _GENESIS_CONTROL_EXTENSION {		

	// Driver Name 
	UNICODE_STRING DriverName;
  
	// Symbolic Link Name
	UNICODE_STRING SymbolicLinkName;

	// This is used to indicate the state of the device (GENII)
	ULONG DeviceState;

	// Control Thread Service queue  
	LIST_ENTRY ServiceQueue;

	// Control Thread Service Queue Lock  
	FAST_MUTEX ServiceQueueLock;

	// Control Request Queue - awaiting dispatch to control threads
	LIST_ENTRY RequestQueue;

	// Control Request Queue Lock
	FAST_MUTEX RequestQueueLock;

	//  Back-pointer to owning device object
	PRDBSS_DEVICE_OBJECT    DeviceObject;

} GENESIS_CONTROL_EXTENSION, *PGENESIS_CONTROL_EXTENSION;

typedef struct _GENII_REQUEST {
  // This is used to thread the requests onto a data request queue
  LIST_ENTRY ListEntry;
  
  // This is used to thread the requests onto a service request queue
  LIST_ENTRY ServiceListEntry;

  // The request ID is used to match up the response.
  ULONG RequestID;

  // The request type indicates the operation to be performed
  ULONG RequestType;

  // The IRP is the one associated with this particular operation.
  PIRP Irp;  

  // In the Data Queue this points to the originating RxContext
  PRX_CONTEXT RxContext;

} GENII_REQUEST, *PGENII_REQUEST;

// Pointer to the device Object for this minirdr. Since the device object is 
// created by the wrapper when this minirdr registers, this pointer is 
// initialized in the DriverEntry routine below (see RxRegisterMinirdr)
PRDBSS_DEVICE_OBJECT      NulMRxDeviceObject;

//Pointer to the device object for the genii control device
PDEVICE_OBJECT			  GeniiControlDeviceObject;

//
// typedef our srv-call extension - stores state global to a node
// NYI since wrapper does not allocate space for this..........!
//
typedef struct _NULMRX_SRVCALL_EXTENSION {
    //
    //  Node type code and size
    //
    NODE_TYPE_CODE          NodeTypeCode;
    NODE_BYTE_SIZE          NodeByteSize;
    
} NULMRX_SRVCALL_EXTENSION, *PNULMRX_SRVCALL_EXTENSION;

//
// NET_ROOT extension - stores state global to a root
//
typedef struct _NULMRX_NETROOT_EXTENSION {
    //
    //  Node type code and size
    //
    NODE_TYPE_CODE          NodeTypeCode;
    NODE_BYTE_SIZE          NodeByteSize;

} NULMRX_NETROOT_EXTENSION, *PNULMRX_NETROOT_EXTENSION;

//
//  reinitialize netroot data
//

#define     RxResetNetRootExtension(pNetRootExtension)                          \
            RxDefineNode(pNetRootExtension,NULMRX_NETROOT_EXTENSION);          

//
//  typedef our FCB extension
//  the FCB uniquely represents an IFS stream
//  NOTE: Since we are not a paging file, this mem is paged !!!
//

typedef struct _NULMRX_FCB_EXTENSION_ {
    //
    //  Node type code and size
    //
    NODE_TYPE_CODE          NodeTypeCode;
    NODE_BYTE_SIZE          NodeByteSize;
    
} NULMRX_FCB_EXTENSION, *PNULMRX_FCB_EXTENSION;

/* Genesis II File Control Block Extension (part of FSContext)*/
typedef struct _GENESIS_FCB_EXTENSION{	
	UINT FcbFlags;

	//This is either the file size or directory size
	BOOLEAN isDirectory;

	BOOLEAN DeleteOnCloseSpecified;

	LONG DirectorySize;

	BOOLEAN isStreamable;	

	PWCHAR DirectoryListing;

	FAST_MUTEX ExclusiveLock;

	KSEMAPHORE InvertedCallSemaphore;

	USHORT State;		

	//Used to fake times (for now)
	LARGE_INTEGER OpenTime;

	LARGE_INTEGER CreateTime;

	LARGE_INTEGER ModifiedTime;

	LARGE_INTEGER AccessedTime;

	//Obtained from last create
	ULONG GenesisTempFileID;

}GenesisFCB, *PGENESIS_FCB;

/* Genesis II Context Control Block Extension (part of FOBX)*/
typedef struct _GENESIS_CCB_EXTENSION{
	//For Query Directory
	LONG			FileIndex;	
	
	ANSI_STRING		Target;	

	LONG			GenesisFileID;

	//For normal files
	LARGE_INTEGER	CurrentByteOffset; 

	//Last status message
	NTSTATUS lastStatus;

}GenesisCCB, *PGENESIS_CCB;

/* 
	Genesis II SrvOpen Control Block Extension (part of SrvOpen) 
	This stands for the open context on the server side
	i.e. can compare times or size to see if meta-data has changed
*/
typedef struct _GENESIS_SRV_OPEN_EXTENSION{
	
	LARGE_INTEGER ServerFileSize;

	LARGE_INTEGER ServerCreateTime;

	LARGE_INTEGER ServerModifiedTime;

	LARGE_INTEGER ServerAccessedTime;

}GenesisSrvOpen, *PGENESIS_SRV_OPEN;

#define GENII_FILE_INVALID 0xFFFF

#define GENII_STATE_NOT_INITIALIZED 0x00
#define GENII_STATE_NOT_FOUND 0x01
#define GENII_STATE_HAVE_INFO 0x02
#define GENII_STATE_HAVE_LISTING 0x03

//
//  Macros to get & validate extensions
//

#define NulMRxGetDeviceExtension(RxContext,pExt)        \
        PNULMRX_DEVICE_EXTENSION pExt = (PNULMRX_DEVICE_EXTENSION)((PBYTE)(RxContext->RxDeviceObject) + sizeof(RDBSS_DEVICE_OBJECT))

#define NulMRxGetSrvCallExtension(pSrvCall, pExt)       \
        PNULMRX_SRVCALL_EXTENSION pExt = (((pSrvCall) == NULL) ? NULL : (PNULMRX_SRVCALL_EXTENSION)((pSrvCall)->Context))

#define NulMRxGetNetRootExtension(pNetRoot,pExt)        \
        PNULMRX_NETROOT_EXTENSION pExt = (((pNetRoot) == NULL) ? NULL : (PNULMRX_NETROOT_EXTENSION)((pNetRoot)->Context))

#define NulMRxGetFcbExtension(pFcb,pExt)                \
        PNULMRX_FCB_EXTENSION pExt = (((pFcb) == NULL) ? NULL : (PNULMRX_FCB_EXTENSION)((pFcb)->Context))

#define GenesisGetFcbExtension(pFcb,pExt)                \
		PGENESIS_FCB pExt = (((pFcb) == NULL) ? NULL : (PGENESIS_FCB)((pFcb)->Context2))

#define GenesisGetCcbExtension(pFobx,pExt)                \
		PGENESIS_CCB pExt = (((pFobx) == NULL) ? NULL : (PGENESIS_CCB)((pFobx)->Context2))

#define GenesisGetSrvOpenExtension(pSrvOpen,pExt)                \
		PGENESIS_SRV_OPEN pExt = (((pSrvOpen) == NULL) ? NULL : (PGENESIS_SRV_OPEN)((pSrvOpen)->Context2))

//
// forward declarations for all dispatch vector methods.
//

extern NTSTATUS
NulMRxStart (
    IN OUT struct _RX_CONTEXT * RxContext,
    IN OUT PRDBSS_DEVICE_OBJECT RxDeviceObject
    );

extern NTSTATUS
NulMRxStop (
    IN OUT struct _RX_CONTEXT * RxContext,
    IN OUT PRDBSS_DEVICE_OBJECT RxDeviceObject
    );

extern NTSTATUS
NulMRxMinirdrControl (
    IN OUT PRX_CONTEXT RxContext,
    IN OUT PVOID pContext,
    IN OUT PUCHAR SharedBuffer,
    IN     ULONG InputBufferLength,
    IN     ULONG OutputBufferLength,
    OUT PULONG CopyBackLength
    );

extern NTSTATUS
NulMRxDevFcb (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxDevFcbXXXControlFile (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxCreate (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxCollapseOpen (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxShouldTryToCollapseThisOpen (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxRead (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxWrite (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxLocks(
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxFlush(
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxFsCtl(
    IN OUT PRX_CONTEXT RxContext
    );

NTSTATUS
NulMRxIoCtl(
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxNotifyChangeDirectory(
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxComputeNewBufferingState(
    IN OUT PMRX_SRV_OPEN pSrvOpen,
    IN     PVOID         pMRxContext,
       OUT ULONG         *pNewBufferingState);

extern NTSTATUS
NulMRxFlush (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxCloseWithDelete (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxZeroExtend (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxTruncate (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxCleanupFobx (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxCloseSrvOpen (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxClosedSrvOpenTimeOut (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxQueryDirectory (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxQueryEaInformation (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxSetEaInformation (
    IN OUT struct _RX_CONTEXT * RxContext
    );

extern NTSTATUS
NulMRxQuerySecurityInformation (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxSetSecurityInformation (
    IN OUT struct _RX_CONTEXT * RxContext
    );

extern NTSTATUS
NulMRxQueryVolumeInformation (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxSetVolumeInformation (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxLowIOSubmit (
    IN OUT PRX_CONTEXT RxContext
    );

extern NTSTATUS
NulMRxCreateVNetRoot(
    IN OUT PMRX_CREATENETROOT_CONTEXT pContext
    );

extern NTSTATUS
NulMRxFinalizeVNetRoot(
    IN OUT PMRX_V_NET_ROOT pVirtualNetRoot,
    IN     PBOOLEAN    ForceDisconnect);

extern NTSTATUS
NulMRxFinalizeNetRoot(
    IN OUT PMRX_NET_ROOT pNetRoot,
    IN     PBOOLEAN      ForceDisconnect);

extern NTSTATUS
NulMRxUpdateNetRootState(
    IN  PMRX_NET_ROOT pNetRoot);

VOID
NulMRxExtractNetRootName(
    IN PUNICODE_STRING FilePathName,
    IN PMRX_SRV_CALL   SrvCall,
    OUT PUNICODE_STRING NetRootName,
    OUT PUNICODE_STRING RestOfName OPTIONAL
    );

extern NTSTATUS
NulMRxCreateSrvCall(
      PMRX_SRV_CALL                      pSrvCall,
      PMRX_SRVCALL_CALLBACK_CONTEXT  pCallbackContext);

extern NTSTATUS
NulMRxFinalizeSrvCall(
      PMRX_SRV_CALL    pSrvCall,
      BOOLEAN    Force);

extern NTSTATUS
NulMRxSrvCallWinnerNotify(
      IN OUT PMRX_SRV_CALL      pSrvCall,
      IN     BOOLEAN        ThisMinirdrIsTheWinner,
      IN OUT PVOID          pSrvCallContext);


extern NTSTATUS
NulMRxQueryFileInformation (
    IN OUT PRX_CONTEXT            RxContext
    );

extern NTSTATUS
NulMRxQueryNamedPipeInformation (
    IN OUT PRX_CONTEXT            RxContext,
    IN     FILE_INFORMATION_CLASS FileInformationClass,
    IN OUT PVOID              Buffer,
    IN OUT PULONG             pLengthRemaining
    );

extern NTSTATUS
NulMRxSetFileInformation (
    IN OUT PRX_CONTEXT            RxContext
    );

extern NTSTATUS
NulMRxSetNamedPipeInformation (
    IN OUT PRX_CONTEXT            RxContext,
    IN     FILE_INFORMATION_CLASS FileInformationClass,
    IN     PVOID              pBuffer,
    IN     ULONG              BufferLength
    );

NTSTATUS
NulMRxSetFileInformationAtCleanup(
      IN OUT PRX_CONTEXT            RxContext
      );

NTSTATUS
NulMRxDeallocateForFcb (
    IN OUT PMRX_FCB pFcb
    );

NTSTATUS
NulMRxDeallocateForFobx (
    IN OUT PMRX_FOBX pFobx
    );

extern NTSTATUS
NulMRxForcedClose (
    IN OUT PMRX_SRV_OPEN SrvOpen
    );

extern NTSTATUS
NulMRxExtendFile (
    IN OUT struct _RX_CONTEXT * RxContext,
    IN OUT PLARGE_INTEGER   pNewFileSize,
       OUT PLARGE_INTEGER   pNewAllocationSize
    );

extern NTSTATUS
NulMRxTruncateFile (
    IN OUT struct _RX_CONTEXT * RxContext,
    IN OUT PLARGE_INTEGER   pNewFileSize,
       OUT PLARGE_INTEGER   pNewAllocationSize
    );

extern NTSTATUS
NulMRxCompleteBufferingStateChangeRequest (
    IN OUT PRX_CONTEXT RxContext,
    IN OUT PMRX_SRV_OPEN   SrvOpen,
    IN     PVOID       pContext
    );


extern NTSTATUS
NulMRxExtendForCache (
    IN OUT PRX_CONTEXT RxContext,
    IN OUT PFCB Fcb,
    OUT    PLONGLONG pNewFileSize
    );

extern
NTSTATUS
NulMRxInitializeSecurity (VOID);

extern
NTSTATUS
NulMRxUninitializeSecurity (VOID);

extern
NTSTATUS
NulMRxInitializeTransport(VOID);

extern
NTSTATUS
NulMRxUninitializeTransport(VOID);

/* Genesis Defined Functions */

extern 
NTSTATUS 
GenesisCompleteQueryDirectory(PRX_CONTEXT RxContext);

extern 
NTSTATUS 
GenesisCompleteQueryFileInformation(PRX_CONTEXT RxContext);

extern
VOID
GenesisSaveInfoIntoFCB(PMRX_FCB commonFcb, PVOID info, int size);

extern
VOID
GenesisSaveDirectoryListing(PGENESIS_FCB fcb, PVOID directoryListing, int size);

/* Used for Low-IO to prepare params for inverted call */
extern
ULONG
GenesisPrepareReadParams(PRX_CONTEXT RxContext, PVOID buffer);

/* Used for Low-IO to prepare params for inverted call */
extern
ULONG
GenesisPrepareWriteParams(PRX_CONTEXT RxContext, PVOID buffer, PBOOLEAN, PBOOLEAN);

/* Prepares params for close */
extern
ULONG 
GenesisPrepareClose(PRX_CONTEXT RxContext, PVOID buffer);

/* Used for Create, GetInfo and GetListing to prepare parameters for inverted call */
extern
ULONG
GenesisPrepareDirectoryAndTarget(PRX_CONTEXT RxContext, PVOID buffer, BOOLEAN hasTarget);

extern
ULONG 
GenesisPrepareCreate(PRX_CONTEXT RxContext, PVOID buffer);

extern
ULONG
GenesisPrepareRename(PRX_CONTEXT RxContext, PVOID buffer);

extern
NTSTATUS
GenesisSendInvertedCall(PRX_CONTEXT RxContext, ULONG callType, BOOLEAN markAsPending);

extern
NTSTATUS 
GenesisLockCallersBuffer(PIRP PtrIrp, BOOLEAN IsReadOperation, ULONG Length);

#define NulMRxMakeSrvOpenKey(Tid,Fid) \
        (PVOID)(((ULONG)(Tid) << 16) | (ULONG)(Fid))

#include "mrxprocs.h"   // crossreferenced routines

#endif _MRXGLOBS_H_

