#ifndef __LPC_H__
#define __LPC_H__

#ifdef __cplusplus
extern "C"
{
#endif

#define GII_LPC_PWD		 0x00000000
#define GII_LPC_LOGIN    0x00000001
#define GII_LPC_LOGOUT   0x00000002
#define GII_LPC_LISTING	 0x00000003
#define GII_LPC_CD		 0x00000004
#define GII_LPC_MKDIR	 0x00000005
#define GII_LPC_REMOVE	 0x00000006
#define GII_LPC_COPY	 0x00000007
#define GII_LPC_MOVE	 0x00000008
#define GII_LPC_OPEN	 0x00000009
#define GII_LPC_READ	 0x0000000A
#define GII_LPC_WRITE	 0x0000000B
#define GII_LPC_CLOSE	 0x0000000C
#define GII_LPC_STOP  	 0x0000000D

#define LARGE_MESSAGE_SIZE 0x9000

LPWSTR LpcPortName = L"\\GenesisPort";      // Name of the LPC port

#ifndef InitializePortHeader
#define InitializeMessageHeader( ph, l, t ) {                  \
    (ph)->u1.s1.TotalLength    = (USHORT)(l);                        \
    (ph)->u1.s1.DataLength     = (USHORT)(l - sizeof(PORT_MESSAGE)); \
    (ph)->u2.s2.Type           = (USHORT)(t);                        \
    (ph)->u2.s2.DataInfoOffset = 0;                             \
    }
#endif

// LPC defines and structures

#define MAX_LPC_DATA 0x130    // Maximum number of bytes that can be copied through LPC

// LPC connection types
typedef enum _LPC_TYPE
{
    LPC_NEW_MESSAGE,                    // (0) A new message    
    LPC_REQUEST,                        // (1) A request message
    LPC_REPLY,                          // (2) A reply to a request message
    LPC_DATAGRAM,                       // (3) 
    LPC_LOST_REPLY,                     // (4)
    LPC_PORT_CLOSED,                    // (5) Send when port is deleted
    LPC_CLIENT_DIED,                    // (6) Messages to thread termination ports
    LPC_EXCEPTION,                      // (7) Messages to thread exception ports
    LPC_DEBUG_EVENT,                    // (8) Messages to thread debug port
    LPC_ERROR_EVENT,                    // (9) Used by NtRaiseHardError
    LPC_CONNECTION_REQUEST              // (A) Used by NtConnectPort

} LPC_TYPE, *PLPC_TYPE;

// This is the data structure transferred through LPC.
// Every structure must begin with PORT_MESSAGE, and must NOT be
// greater that MAX_LPC_DATA
typedef struct _TRANSFERRED_MESSAGE
{
    PORT_MESSAGE Header;

    ULONG   Command;
	ULONG	Length;
    WCHAR   MessageText[48];

} TRANSFERRED_MESSAGE, *PTRANSFERRED_MESSAGE;

//------------------------------------------------------------------------------
// LPC Functions

/*++

    NtCreatePort
    ============

    Creates a LPC port object. The creator of the LPC port becomes a server
    of LPC communication

    PortHandle - Points to a variable that will receive the
        port object handle if the call is successful.

    ObjectAttributes - Points to a structure that specifies the object’s
        attributes. OBJ_KERNEL_HANDLE, OBJ_OPENLINK, OBJ_OPENIF, OBJ_EXCLUSIVE,
        OBJ_PERMANENT, and OBJ_INHERIT are not valid attributes for a port object.

    MaxConnectionInfoLength - The maximum size, in bytes, of data that can
        be sent through the port.

    MaxMessageLength - The maximum size, in bytes, of a message
        that can be sent through the port.

    MaxPoolUsage - Specifies the maximum amount of NonPaged pool that can be used for
        message storage. Zero means default value.

    ZwCreatePort verifies that (MaxDataSize <= 0x104) and (MaxMessageSize <= 0x148).
 
--*/

NTSTATUS
NTAPI
ZwCreatePort(
    OUT PHANDLE PortHandle,                     
    IN  POBJECT_ATTRIBUTES ObjectAttributes,
    IN  ULONG MaxConnectionInfoLength,
    IN  ULONG MaxMessageLength,
    IN  ULONG MaxPoolUsage
    );


/*++

    NtConnectPort
    =============

    Creates a port connected to a named port (cliend side).

    PortHandle - A pointer to a variable that will receive the client
        communication port object handle value.

    PortName - Points to a structure that specifies the name
        of the port to connect to.

    SecurityQos - Points to a structure that specifies the level
        of impersonation available to the port listener.

    ClientView - Optionally points to a structure describing
        the shared memory region used to send large amounts of data
        to the listener; if the call is successful, this will be updated.

    ServerView - Optionally points to a caller-allocated buffer
        or variable that receives information on the shared memory region
        used by the listener to send large amounts of data to the
        caller.

    MaxMessageLength - Optionally points to a variable that receives the size,
        in bytes, of the largest message that can be sent through the port.

    ConnectionInformation - Optionally points to a caller-allocated
        buffer or variable that specifies connect data to send to the listener,
        and receives connect data sent by the listener.

    ConnectionInformationLength - Optionally points to a variable that
        specifies the size, in bytes, of the connect data to send
        to the listener, and receives the size of the connect data
        sent by the listener.

--*/

NTSTATUS
NTAPI
ZwConnectPort(
    OUT PHANDLE PortHandle,
    IN  PUNICODE_STRING PortName,
    IN  PSECURITY_QUALITY_OF_SERVICE SecurityQos,
    IN  OUT PPORT_VIEW ClientView OPTIONAL,
    OUT PREMOTE_PORT_VIEW ServerView OPTIONAL,
    OUT PULONG MaxMessageLength OPTIONAL,
    IN  OUT PVOID ConnectionInformation OPTIONAL,
    IN  OUT PULONG ConnectionInformationLength OPTIONAL
    );


/*++

    NtListenPort
    ============

    Listens on a port for a connection request message on the server side.

    PortHandle - A handle to a port object. The handle doesn't need 
        to grant any specific access.

    ConnectionRequest - Points to a caller-allocated buffer
        or variable that receives the connect message sent to
        the port.

--*/


NTSTATUS
NTAPI
ZwListenPort(
    IN  HANDLE PortHandle,
    OUT PPORT_MESSAGE RequestMessage
    );
 
/*++

    NtAcceptConnectPort
    ===================

    Accepts or rejects a connection request on the server side.

    PortHandle - Points to a variable that will receive the port object
        handle if the call is successful.

    PortContext - A numeric identifier to be associated with the port.

    ConnectionRequest - Points to a caller-allocated buffer or variable
        that identifies the connection request and contains any connect
        data that should be returned to requestor of the connection

    AcceptConnection - Specifies whether the connection should
        be accepted or not

    ServerView - Optionally points to a structure describing
        the shared memory region used to send large amounts of data to the
        requestor; if the call is successful, this will be updated

    ClientView - Optionally points to a caller-allocated buffer
        or variable that receives information on the shared memory
        region used by the requestor to send large amounts of data to the
        caller

--*/


NTSTATUS
NTAPI
ZwAcceptConnectPort(
    OUT PHANDLE PortHandle,
    IN  PVOID PortContext OPTIONAL,
    IN  PPORT_MESSAGE ConnectionRequest,
    IN  BOOLEAN AcceptConnection,
    IN  OUT PPORT_VIEW ServerView OPTIONAL,
    OUT PREMOTE_PORT_VIEW ClientView OPTIONAL
    );

/*++

    NtCompleteConnectPort
    =====================

    Completes the port connection process on the server side.

    PortHandle - A handle to a port object. The handle doesn't need 
        to grant any specific access.

--*/

NTSTATUS
NTAPI
ZwCompleteConnectPort(
    IN  HANDLE PortHandle
    );


/*++

    NtRequestPort
    =============

    Sends a request message to a port (client side)

    PortHandle - A handle to a port object. The handle doesn't need 
        to grant any specific access.

    RequestMessage - Points to a caller-allocated buffer or variable
        that specifies the request message to send to the port.

NTSTATUS
NTAPI
NtRequestPort (
    IN  HANDLE PortHandle,
    IN  PPORT_MESSAGE RequestMessage
    );

--*/

NTSTATUS
NTAPI
ZwRequestPort (
    IN  HANDLE PortHandle,
    IN  PPORT_MESSAGE RequestMessage
    );

/*++

    NtRequestWaitReplyPort
    ======================

    Sends a request message to a port and waits for a reply (client side)

    PortHandle - A handle to a port object. The handle doesn't need 
        to grant any specific access.

    RequestMessage - Points to a caller-allocated buffer or variable
        that specifies the request message to send to the port.

    ReplyMessage - Points to a caller-allocated buffer or variable
        that receives the reply message sent to the port.



NTSTATUS
NTAPI
NtRequestWaitReplyPort(
    IN  HANDLE PortHandle,
    IN  PPORT_MESSAGE RequestMessage,
    OUT PPORT_MESSAGE ReplyMessage
    );

--*/

NTSYSAPI
NTSTATUS
NTAPI
ZwRequestWaitReplyPort(
    __in HANDLE PortHandle,
    __in PPORT_MESSAGE RequestMessage,
    __out PPORT_MESSAGE ReplyMessage
    );

/*++

    NtReplyPort
    ===========

    Sends a reply message to a port (Server side)

    PortHandle - A handle to a port object. The handle doesn't need 
        to grant any specific access.

    ReplyMessage - Points to a caller-allocated buffer or variable
        that specifies the reply message to send to the port.




NTSTATUS
NTAPI
NtReplyPort(
    IN  HANDLE PortHandle,
    IN  PPORT_MESSAGE ReplyMessage
    );

--*/

NTSTATUS
NTAPI
ZwReplyPort(
    IN  HANDLE PortHandle,
    IN  PPORT_MESSAGE ReplyMessage
    );

/*++

    NtReplyWaitReplyPort
    ====================

    Sends a reply message to a port and waits for a reply message

    PortHandle - A handle to a port object. The handle doesn't need 
        to grant any specific access.

    ReplyMessage - Points to a caller-allocated buffer or variable
        that specifies the reply message to send to the port.

NTSTATUS
NTAPI
NtReplyWaitReplyPort(
    IN  HANDLE PortHandle,
    IN  OUT PPORT_MESSAGE ReplyMessage
    );

--*/

NTSTATUS
NTAPI
ZwReplyWaitReplyPort(
    IN  HANDLE PortHandle,
    IN  OUT PPORT_MESSAGE ReplyMessage
    );


/*++

    NtReplyWaitReceivePort
    ======================

    Optionally sends a reply message to a port and waits for a
    message

    PortHandle - A handle to a port object. The handle doesn't need 
        to grant any specific access.

    PortContext - Optionally points to a variable that receives
        a numeric identifier associated with the port.

    ReplyMessage - Optionally points to a caller-allocated buffer
        or variable that specifies the reply message to send to the port.

    ReceiveMessage - Points to a caller-allocated buffer or variable
        that receives the message sent to the port.



NTSTATUS
NTAPI
NtReplyWaitReceivePort(
    IN  HANDLE PortHandle,
    OUT PVOID *PortContext OPTIONAL,
    IN  PPORT_MESSAGE ReplyMessage OPTIONAL,
    OUT PPORT_MESSAGE ReceiveMessage
    );

--*/

NTSTATUS
NTAPI
ZwReplyWaitReceivePort(
    IN  HANDLE PortHandle,
    OUT PVOID *PortContext OPTIONAL,
    IN  PPORT_MESSAGE ReplyMessage OPTIONAL,
    OUT PPORT_MESSAGE ReceiveMessage
    );


/*++

    NtClose
    =======

    Closes an NT handle

    Handle - Handle to be closed



NTSTATUS
NTAPI
ZwClose(
    IN  HANDLE Handle
    );

--*/

/*++

    NtCreateSection
    ===============

    Creates a section object.

    SectionHandle - Points to a variable that will receive the section
        object handle if the call is successful.

    DesiredAccess - Specifies the type of access that the caller requires
        to the section object. This parameter can be zero, or any combination
        of the following flags:

        SECTION_QUERY       - Query access
        SECTION_MAP_WRITE   - Can be written when mapped
        SECTION_MAP_READ    - Can be read when mapped
        SECTION_MAP_EXECUTE - Can be executed when mapped
        SECTION_EXTEND_SIZE - Extend access
        SECTION_ALL_ACCESS  - All of the preceding +
                              STANDARD_RIGHTS_REQUIRED

    ObjectAttributes - Points to a structure that specifies the object’s attributes.
        OBJ_OPENLINK is not a valid attribute for a section object.

    MaximumSize - Optionally points to a variable that specifies the size,
        in bytes, of the section. If FileHandle is zero, the size must be
        specified; otherwise, it can be defaulted from the size of the file
        referred to by FileHandle.

    SectionPageProtection - The protection desired for the pages
        of the section when the section is mapped. This parameter can take
        one of the following values:

        PAGE_READONLY
        PAGE_READWRITE
        PAGE_WRITECOPY
        PAGE_EXECUTE
        PAGE_EXECUTE_READ
        PAGE_EXECUTE_READWRITE
        PAGE_EXECUTE_WRITECOPY

    AllocationAttributes - The attributes for the section. This parameter must
        be a combination of the following values:

        SEC_BASED     0x00200000    // Map section at same address in each process
        SEC_NO_CHANGE 0x00400000    // Disable changes to protection of pages
        SEC_IMAGE     0x01000000    // Map section as an image
        SEC_VLM       0x02000000    // Map section in VLM region
        SEC_RESERVE   0x04000000    // Reserve without allocating pagefile storage
        SEC_COMMIT    0x08000000    // Commit pages; the default behavior
        SEC_NOCACHE   0x10000000    // Mark pages as non-cacheable

    FileHandle - Identifies the file from which to create the section object.
        The file must be opened with an access mode compatible with the protection
        flags specified by the Protect parameter. If FileHandle is zero,
        the function creates a section object of the specified size backed
        by the paging file rather than by a named file in the file system.



NTSTATUS
NTAPI
ZwCreateSection(
    OUT PHANDLE SectionHandle,
    IN  ACCESS_MASK DesiredAccess,
    IN  POBJECT_ATTRIBUTES ObjectAttributes OPTIONAL,
    IN  PLARGE_INTEGER MaximumSize OPTIONAL,
    IN  ULONG SectionPageProtection,
    IN  ULONG AllocationAttributes,
    IN  HANDLE FileHandle OPTIONAL
    );

--*/

#ifdef __cplusplus
} // extern "C"
#endif

#endif // __LPC_H__
