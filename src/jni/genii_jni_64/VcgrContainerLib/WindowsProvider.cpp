#include <windows.h>
#include <Ntsecapi.h>
#include <tlhelp32.h>

// This define may be in Ntstatus.h, not relying on that
#define STATUS_SUCCESS ((NTSTATUS)0x00000000L)

#include "WindowsProvider.jh"

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getIndividualCPUSpeed
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getIndividualCPUSpeed (JNIEnv *env, jobject obj)
{
	unsigned long ret = 0;
	LONG Result;

    DWORD speed;
    DWORD bufferSize = sizeof(speed);

    DWORD lpType;

    HKEY RegistryKey_Hardware = NULL;
    HKEY RegistryKey_Description = NULL;
    HKEY RegistryKey_System = NULL;
    HKEY RegistryKey_CentralProcessor = NULL;
    HKEY RegistryKey_0 = NULL;

    Result = RegOpenKeyEx(HKEY_LOCAL_MACHINE, L"HARDWARE",
        0, KEY_READ, &RegistryKey_Hardware);
    if (Result != ERROR_SUCCESS)
		goto done;

    Result = RegOpenKeyEx(RegistryKey_Hardware, L"DESCRIPTION",
        0, KEY_READ, &RegistryKey_Description);
    if (Result != ERROR_SUCCESS)
		goto done;

    Result = RegOpenKeyEx(RegistryKey_Description, L"System",
        0, KEY_READ, &RegistryKey_System);
    if (Result != ERROR_SUCCESS)
		goto done;

    Result = RegOpenKeyEx(RegistryKey_System, L"CentralProcessor",
        0, KEY_READ, &RegistryKey_CentralProcessor);
	if (Result != ERROR_SUCCESS)
		goto done;

    Result = RegOpenKeyEx(RegistryKey_CentralProcessor, L"0",
        0, KEY_READ, &RegistryKey_0);
    if (Result != ERROR_SUCCESS)
		goto done;

    Result = RegQueryValueEx(RegistryKey_0, L"~MHz",
        NULL, &lpType, (LPBYTE) &speed, &bufferSize);
    if (Result != ERROR_SUCCESS)
		goto done;
    else
    {
        if (lpType != REG_DWORD)
			goto done;
    }

	ret = speed * 1024 * 1024;

done:
	if (RegistryKey_0 != NULL)
		RegCloseKey(RegistryKey_0);
	if (RegistryKey_CentralProcessor != NULL)
		RegCloseKey(RegistryKey_CentralProcessor);
	if (RegistryKey_System != NULL)
		RegCloseKey(RegistryKey_System);
	if (RegistryKey_Description != NULL)
		RegCloseKey(RegistryKey_Description);
	if (RegistryKey_Hardware != NULL)
		RegCloseKey(RegistryKey_Hardware);

    return ret;
}

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getPhysicalMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getPhysicalMemory (JNIEnv *env, jobject obj)
{
	MEMORYSTATUSEX status;
	status.dwLength = sizeof(status);

	GlobalMemoryStatusEx(&status);

	return status.ullTotalPhys;
}

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getPhysicalMemoryAvailable
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getPhysicalMemoryAvailable (JNIEnv *env, jobject obj)
{
	MEMORYSTATUSEX status;
	status.dwLength = sizeof(status);

	GlobalMemoryStatusEx(&status);

	return status.ullAvailPhys;
}

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getVirtualMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getVirtualMemory (JNIEnv *env, jobject obj)
{
	MEMORYSTATUSEX status;
	status.dwLength = sizeof(status);

	GlobalMemoryStatusEx(&status);

	return status.ullTotalVirtual;
}

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getVirtualMemoryAvailable
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getVirtualMemoryAvailable (JNIEnv *env, jobject obj)
{
	MEMORYSTATUSEX status;
	status.dwLength = sizeof(status);

	GlobalMemoryStatusEx(&status);

	return status.ullAvailVirtual;
}


//-----------------------------------------------------------------------------
// IsScreenSaverActive
//
// Determines if the screensaver is running, returns 1 if on, 0 if off.  
// What we have here is a hack to detect if a screen saver is running on 
// Windows NT/2000. We check for the existence of a desktop named 
// "screen-saver". This desktop is created dynamically by winlogon when a 
// screen saver needs to be launched.  It should be noted that for 
// WIN 98 and 2000 you can obtain this information more directly using the
// SystemParametersInfo() function wiht the SPI_GET_SCREENSAVERRUNNING 
// flag.
//-----------------------------------------------------------------------------



/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getScreenSaverActive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getScreenSaverActive
(JNIEnv *env, jobject obj) {

	jboolean ssActive = 0;
	HDESK hDesktop;

	
	ssActive = false;
	hDesktop = OpenDesktop(TEXT("screen-saver"),0,FALSE, MAXIMUM_ALLOWED);
	if ((hDesktop != NULL) || (GetLastError() == ERROR_ACCESS_DENIED))
	{
		ssActive = true;
		if (hDesktop != NULL)
			CloseDesktop(hDesktop);
	}
	else if ((FindWindow(L"WindowsScreenSaverClass", NULL) != NULL) || 
		(GetLastError() == ERROR_ACCESS_DENIED))
	{
		ssActive = true;
	}

	return ssActive;
}


//-----------------------------------------------------------------------------
// This is a helper function for the getUserLoggedIn function.  This function
// will determine information about a passed in session variable.  It can 
// determine whether or not an interactive session is currently running.
//  This is a robust method for checking if a user is logged in at a computer.
//-----------------------------------------------------------------------------
bool GetSessionData(PLUID session)
{
  PSECURITY_LOGON_SESSION_DATA sessionData = NULL;
  NTSTATUS retval;

  // Check for a valid session.
  if (!session ) {
    wprintf(L"Error - Invalid logon session identifier.\n");
    return false;
  }

  // Get the session information.
  retval = LsaGetLogonSessionData(session, &sessionData);

  // This will check to ensure that we were able to get the session data.
  if (retval != STATUS_SUCCESS) {
    // An error occurred.
    wprintf (L"LsaGetLogonSessionData failed %lu \n",
      LsaNtStatusToWinError(retval));
    // If session information was returned, free it.
    if (sessionData) {
      LsaFreeReturnBuffer(sessionData);
    }
    return false;
  } 

  // Determine whether there is session data to parse. 
  if (!sessionData) { // no data for session
    wprintf(L"Invalid logon session data. \n");
    return false;
  }

  // Check to see if an interactive user is currently logged on
  if ((SECURITY_LOGON_TYPE) sessionData->LogonType == Interactive) {
    return true;
  }

  // Free the memory returned by the LSA.
  LsaFreeReturnBuffer(sessionData);
  return false;
}


//-----------------------------------------------------------------------------
// getUserLoggedIn
//
// Determines if an interactive user is logged into the machine.  This is done
// by enumerating all sessions on the computer and determining if any currently
// active sessions are 'interactive' sessions.  This conforms to MS documents
// located: http://msdn2.microsoft.com/en-us/library/aa375400.aspx
//-----------------------------------------------------------------------------

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getUserLoggedIn
 * Signature: ()Z
 */

/* We used to do it this way, but Microsoft escalated the security requirements for these OS
calls to an eggregious level.

JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getUserLoggedIn
(JNIEnv *env, jobject obj)
{
  PLUID sessions;
  ULONG count;
  NTSTATUS retval;
  int i;

  // Get all of the logon sessions
  retval = LsaEnumerateLogonSessions(&count, &sessions);

  // Check to ensure there was no error enumerating the sessions
  if (retval != STATUS_SUCCESS) {
     wprintf (L"LsaEnumerate failed %lu\n",
       LsaNtStatusToWinError(retval));
    return false;
  }

  // Process the array of session LUIDs...
  bool userLoggedIn = false;
  for (i = 0; i < (int) count; i++) {
	  userLoggedIn = GetSessionData(&sessions[i]);
	  if (userLoggedIn) {
		  // Free the array of session LUIDs allocated by the LSA.
		  LsaFreeReturnBuffer(sessions);
		  return userLoggedIn;
	  }
  }

  return false;
}
*/

JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getUserLoggedIn
(JNIEnv *env, jobject obj)
{
	HANDLE snapshot;
	PROCESSENTRY32 pe32;
	bool ret;

	ret = false;
	pe32.dwSize = sizeof(PROCESSENTRY32);

	snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	if (snapshot == INVALID_HANDLE_VALUE)
	{
		// We should throw a java exception here, but we don't have
		// time to implement that right now.
		fprintf(stderr, "Unable to create process snapshot.");
		return false;
	}

	if (Process32First(snapshot, &pe32))
	{
		do
		{
			if (wcscmp(pe32.szExeFile, L"explorer.exe") == 0)
			{
				ret = true;
				break;
			}
		} while (Process32Next(snapshot, &pe32));
	}

	CloseHandle(snapshot);
	return ret;
}