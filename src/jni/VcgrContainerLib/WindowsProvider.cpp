#include <windows.h>

#include "WindowsProvider.jh"

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getIndividualCPUSpeed
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getIndividualCPUSpeed (JNIEnv *env, jobject obj)
{
	long ret = 0;
	LONG Result;

    unsigned long tmp;
    unsigned char Buffer[16];

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
        NULL, &lpType, Buffer, &tmp);
    if (Result != ERROR_SUCCESS)
		goto done;
    else
    {
        if (lpType != REG_DWORD)
			goto done;
    }

	ret = (*((DWORD*)Buffer)) * 1024 * 1024;

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
// IsUserLoggedIn
//
// Determines if an interactive user is logged into the machine.  (This is done
// by determining if GUI shell is active.  Services wishing to check this much 
// be "allowed to interact with the desktop".
//-----------------------------------------------------------------------------

/*
 * Class:     edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider
 * Method:    getUserLoggedIn
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_container_sysinfo_WindowsProvider_getUserLoggedIn
(JNIEnv *env, jobject obj) {

    HWND hwndShell = NULL;

	//Get Desktop Window HWND 
    hwndShell = GetShellWindow();
    if (!hwndShell) 
		return false;

	return true;
}