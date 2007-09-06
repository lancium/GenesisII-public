#ifndef __MAKE_GENESISII_CALLS_H__
#define __MAKE_GENESISII_CALLS_H__

#include <jni.h>

#define DllExport __declspec( dllexport )

/*
	File handle for Genesis II resources
*/
#define GII_FILE_HANDLE int

#ifdef __cplusplus
extern "C" {
#endif	/* C Plus Plus */

/* 
	This is the alpha version of the GenesisII C Library

	Unless otherwise noted, all functions returns an int value >= 0 if 
	successful and a -1 if there was some failure 
*/

typedef struct _GII_JNI_INFO{
	JNIEnv *env;
	jclass jni_launcher;
}*PGII_JNI_INFO, GII_JNI_INFO;


/* 
	Gets the directory listing of the directory specified and the target specified
	from the directory.
	Listing - a pointer to the array of strings to be used
	Directory - target Directory (relative path to get directory listing
	Target - Optional path (can be * or something.* etc))
	Method Does it's own memory allocation. Returns number of items in listing
	Note:  The number of items in the listing = 2*size.  Every other is the type
	of file {F,text.txt,D, containers}
*/
DllExport int genesisII_directory_listing(PGII_JNI_INFO info, char *** listing, char * directory, char * target);

/*
	Return information about an object in the same style as directory listing
*/
DllExport int genesisII_get_information(PGII_JNI_INFO info, char *** gInfo, char * path);

/*	
	Changes the current directory to the new_directory given
*/
DllExport int genesisII_change_directory(PGII_JNI_INFO info, char* new_directory);

/*
	Returns a char* of the current working directory
*/
DllExport char* genesisII_get_working_directory(PGII_JNI_INFO info);

/*
	Performs a text-based login to the specified keystore with the given password
*/
DllExport int genesisII_login(PGII_JNI_INFO info, char * keystore_path, char * password, char * cert_pattern);

/*
	Logs out of genesisII
*/
DllExport int genesisII_logout(PGII_JNI_INFO info);

/*
	Creates the directory specfied
*/
DllExport int genesisII_make_directory(PGII_JNI_INFO info, char * new_directory);

/*
	Removes a the target path.  Force does it regardless of exceptions and 
	recursive does a recursive removal of a directory
*/
DllExport int genesisII_remove(PGII_JNI_INFO info, char * path, int recursive, int force);

/*
	Copies a file from src to dst and allows copying from/to local given the two booleans
*/
DllExport int genesisII_copy(PGII_JNI_INFO info, char *src, char* dst, int src_local, int dst_local);

/*
	Moves a file from src to dst.  Both paths must be in GenesisII
	If a failure occurs somewhere along the way, either nothing  was performed or a copy was made
*/
DllExport int genesisII_move(PGII_JNI_INFO info, char *src, char * dst);

/*
	This method returns the Genesis II file handle for the target specified.  It opens the file 
	with the three Booleans specified:  create (whether or not to create it – note that this will 
	only create if it does not already exist), read (to be able to read), and write (to be able to write).  
	If the user does not have permissions for any property of the resource that he/she is requesting, the 
	entire operation will abort.
*/
DllExport int genesisII_open(PGII_JNI_INFO info, char * target,	int create, int read, int write, char *** returnArray);

/*
	Tries to read up to length bytes starting at the offset for the specified file located at the target.  
	The data parameter must be initialized to hold up to length bytes.  Returns the number of bytes read or -1 
	if an error occurred.  
*/
DllExport int genesisII_read(PGII_JNI_INFO info, GII_FILE_HANDLE file, char* data, int offset, int length);

/*
	This method writes the data array to the file at the given offset.  The length field should be the 
	size of the data array.
*/
DllExport int genesisII_write(PGII_JNI_INFO info, GII_FILE_HANDLE file, char* data, int offset, int length);

/*
	This method closes a file.  Data is only committed when a file is properly closed.  
	Once closed, the file handle has no value.  
*/
DllExport int genesisII_close(PGII_JNI_INFO info, GII_FILE_HANDLE file);

/*
	Initializes the JavaVM (must be run first with master thread)
*/
DllExport int initializeJavaVM(char * genesis_directory, PGII_JNI_INFO newInfo);

/*
	Initializes the JavaVM for a new thread and returns the information
*/
DllExport int initializeJavaVMForThread(PGII_JNI_INFO newInfo);

#ifdef __cplusplus
}
#endif	/* C PLUS PLUS */

#endif
