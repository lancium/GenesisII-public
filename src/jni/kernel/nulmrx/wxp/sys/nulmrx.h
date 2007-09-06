/*++

Copyright (c) 1989 - 1999 Microsoft Corporation

Module Name:

    nulmrx.h

Abstract:

    This header exports all symbols and definitions shared between
    user-mode clients of nulmrx and the driver itself.

Notes:

    This module has been built and tested only in UNICODE environment

--*/

#ifndef _NULMRX_H_
#define _NULMRX_H_

#define MAX_PATH_LENGTH 512

// Device name for this driver
#define NULMRX_DEVICE_NAME_A "NullMiniRdr"
#define NULMRX_DEVICE_NAME_U L"NullMiniRdr"

#define GENESIS_VOLUME_NAME L"Genesis II Volume"
#define GENESIS_FILE_SYSTEM L"GenesisFS"


// Provider name for this driver
#define NULMRX_PROVIDER_NAME_A "Genesis II Network"
#define NULMRX_PROVIDER_NAME_U L"Genesis II Network"

// The following constant defines the length of the above name.
#define NULMRX_DEVICE_NAME_A_LENGTH (15)

// The following constants define the paths in the ob namespace
#define DD_NULMRX_FS_DEVICE_NAME_U L"\\Device\\NullMiniRdr"

#ifndef NULMRX_DEVICE_NAME
#define NULMRX_DEVICE_NAME

//
//  The Devicename string required to access the nullmini device 
//  from User-Mode. Clients should use DD_NULMRX_USERMODE_DEV_NAME_U.
//
//  WARNING The next two strings must be kept in sync. Change one and you must 
//  change the other. These strings have been chosen such that they are 
//  unlikely to coincide with names of other drivers.
//
#define DD_NULMRX_USERMODE_SHADOW_DEV_NAME_U     L"\\??\\NullMiniRdrDN"
#define DD_NULMRX_USERMODE_DEV_NAME_U            L"\\\\.\\NullMiniRdrDN"

//  Prefix needed for disk filesystems
#define DD_NULMRX_MINIRDR_PREFIX                 L"\\;E:"

#endif // NULMRX_DEVICE_NAME

// BEGIN WARNING WARNING WARNING WARNING
//  The following are from the ddk include files and cannot be changed
#define FILE_DEVICE_NETWORK_FILE_SYSTEM 0x00000014 // from ddk\inc\ntddk.h
#define METHOD_BUFFERED 0
#define FILE_ANY_ACCESS 0

// END WARNING WARNING WARNING WARNING

#define IOCTL_NULMRX_BASE FILE_DEVICE_NETWORK_FILE_SYSTEM

#define _NULMRX_CONTROL_CODE(request, method, access) \
                CTL_CODE(IOCTL_NULMRX_BASE, request, method, access)

//  IOCTL codes supported by NullMini Device.
#define IOCTL_CODE_ADDCONN          100
#define IOCTL_CODE_GETCONN          101
#define IOCTL_CODE_DELCONN          102
#define IOCTL_CODE_GETLIST			103

//  Following is the IOCTL definition and associated structs.
//  for IOCTL_CODE_SAMPLE1
#define IOCTL_NULMRX_ADDCONN     _NULMRX_CONTROL_CODE(IOCTL_CODE_ADDCONN, METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_NULMRX_GETCONN     _NULMRX_CONTROL_CODE(IOCTL_CODE_GETCONN, METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_NULMRX_DELCONN     _NULMRX_CONTROL_CODE(IOCTL_CODE_DELCONN, METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_NULMRX_GETLIST     _NULMRX_CONTROL_CODE(IOCTL_CODE_GETLIST, METHOD_BUFFERED, FILE_ANY_ACCESS)

#define MRXGEN_FCB_POOLTAG        ('fCmS')
#define MRXGEN_CCB_POOLTAG        ('cCmS')

static LONG GeniiRequestID = 0xFFFFFFFE; // checks for "roll-over" problems

// Genesis Control Codes etc etc
#define GENII_CONTROL_TYPE 45936

//Device name for Genesis Controller (for inverted calls) 
#define DD_GENII_CONTROL_DEVICE_NAME_U L"\\Device\\GeniiControl"
#define DD_GENII_CONTROL_DEVICE_USER_SHADOW_NAME     L"\\??\\GeniiControl"
#define DD_GENII_CONTROL_DEVICE_USER_NAME            L"\\\\.\\GeniiControl"

#define IOCTL_CODE_GETREQUEST		201
#define IOCTL_CODE_SENDRESPONSE		202
#define IOCTL_CODE_GET_AND_SEND		203

#define GENII_CONTROL_GET_REQUEST CTL_CODE(GENII_CONTROL_TYPE, IOCTL_CODE_GETREQUEST, METHOD_BUFFERED, FILE_READ_ACCESS)
#define GENII_CONTROL_SEND_RESPONSE CTL_CODE(GENII_CONTROL_TYPE, IOCTL_CODE_SENDRESPONSE, METHOD_BUFFERED, FILE_WRITE_ACCESS)
#define GENII_CONTROL_GET_AND_SEND CTL_CODE(GENII_CONTROL_TYPE, IOCTL_CODE_GET_AND_SEND, METHOD_BUFFERED, FILE_READ_ACCESS|FILE_WRITE_ACCESS)

//types of calls out to user service
#define GENII_READ_REQUEST 0x10
#define GENII_WRITE_REQUEST 0x20

#define GENII_QUERYDIRECTORY 0x30
#define GENII_QUERYFILEINFO 0x40
#define GENII_CREATE 0x50

#define GENII_CONTROL_ACTIVE   0x10
#define GENII_CONTROL_INACTIVE 0x20

// Data structures used for communicating between service and driver
typedef struct _GENII_CONTROL_REQUEST {  
  // The request ID is used to match up the response to the original request  
  ULONG RequestID;
  
  // The request type indicates the operation to be performed
  ULONG RequestType;

  // The data buffer allows the application to receive arbitrary data
  // Note that this is done OUT OF BOUNDS from the IOCTL.  Thus, the driver
  // is responsible for managing this.  
  PVOID RequestBuffer;
  
  // This specifies the size of the request buffer
  ULONG RequestBufferLength;

} GENII_CONTROL_REQUEST, *PGENII_CONTROL_REQUEST;

typedef struct GENII_CONTROL_RESPONSE {
  // The request ID is used to match up this response to the original request
  ULONG RequestID;

  // The response type indicates the type of response information
  ULONG ResponseType;

  // The data buffer allows the application to return arbitrary data
  // Note that this is done OUT OF BOUNDS from the IOCTL.  Thus, the driver
  // should not trust this data field to be correct.
  PVOID ResponseBuffer;

  ULONG ResponseBufferLength;

  //Optional status code of called function
  ULONG StatusCode;

} GENII_CONTROL_RESPONSE, *PGENII_CONTROL_RESPONSE;

#endif _NULMRX_H_


