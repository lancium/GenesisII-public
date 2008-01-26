#include "WindowsProviderProcessManager.jh"

#include <windows.h>
#include <stdio.h>
#include <conio.h>
#include <tlhelp32.h>

//-----------------------------------------------------------------------------
// This is a function designed to work as a callback for killing processes.
// This method is in accordance with MS best practices for killing a process:
// http://support.microsoft.com/default.aspx?scid=KB;EN-US;Q178893&
//-----------------------------------------------------------------------------
bool CALLBACK Terminate(HWND hwnd, LPARAM lparam) 
{
	DWORD id;

	GetWindowThreadProcessId(hwnd, &id);

	// If this is the thread/process we're looking for, try to close it nicely
	if (id == (DWORD)lparam) {
		PostMessage(hwnd, WM_CLOSE, 0, 0);
	}

	return true;
}

//-----------------------------------------------------------------------------
// This is the actual kill method.
// This method is in accordance with MS best practices for killing a process:
// http://support.microsoft.com/default.aspx?scid=KB;EN-US;Q178893&
//-----------------------------------------------------------------------------

/*
 * Class:     edu_virginia_vcgr_genii_container_processmanager_WindowsProvider
 * Method:    kill
 * Signature: (D)Z
 */
JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_container_processmanager_WindowsProvider_kill
  (JNIEnv *env, jobject obj, jdouble thePID)
{
	DWORD pid = (DWORD)thePID;
	HANDLE processHandle = OpenProcess(SYNCHRONIZE | PROCESS_TERMINATE, FALSE, pid);

	if (processHandle == NULL) {
		return false;
	}

	// Get all child processes
	HANDLE processSnapshot;
	PROCESSENTRY32 processInfo;

	// Take a snapshot of all processes in the system.
	processSnapshot = CreateToolhelp32Snapshot( TH32CS_SNAPPROCESS, 0 );
	if( processSnapshot == INVALID_HANDLE_VALUE )
	{
	  wprintf(L"CreateToolhelp32Snapshot (of processes)");
	  return false;
	}

	// Set the size of the structure before using it.
	processInfo.dwSize = sizeof( PROCESSENTRY32 );

	// Retrieve information about the first process,
	// and exit if unsuccessful
	if( !Process32First( processSnapshot, &processInfo ) )
	{
	  wprintf(L"Process32First"); // show cause of failure
	  CloseHandle( processSnapshot ); // clean the snapshot object
	  return false;
	}

	// Now walk the snapshot of processes, and
	// find child processes
	do
	{
		// Recursively close all child processes
		if (processInfo.th32ParentProcessID == pid) {
			Java_edu_virginia_vcgr_genii_container_processmanager_WindowsProvider_kill(env, obj, (jdouble)processInfo.th32ProcessID);
		}

	} while( Process32Next( processSnapshot, &processInfo ) );

	CloseHandle( processSnapshot );

	// Try to close nicely using CALLBACK
	EnumWindows((WNDENUMPROC)Terminate, (LPARAM) pid);

	// Wait 10,000 milliseconds for the program to acknowledge the close command
	// otherwise, attempt forcible shutdown of the process
	if (WaitForSingleObject(processHandle, 10000) != WAIT_OBJECT_0) {
		if (TerminateProcess(processHandle, 0)) {
			CloseHandle(processHandle);
			return true;
		}
	} else {
		CloseHandle(processHandle);
		return true;
	}
	CloseHandle(processHandle);
	return false;
}

//-----------------------------------------------------------------------------
// This is the resume method.  It attempts to resume all thread and child
// processes of a certain PID.  If the thread count doesn't match the resumed
// number of threads, it will return false and somehow failed.
//-----------------------------------------------------------------------------

/*
 * Class:     edu_virginia_vcgr_genii_container_processmanager_WindowsProvider
 * Method:    resume
 * Signature: (D)Z
 */
JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_container_processmanager_WindowsProvider_resume
  (JNIEnv *env, jobject obj, jdouble thePID)
{
	HANDLE threadSnapshot = INVALID_HANDLE_VALUE;
	HANDLE tempHandler;
	THREADENTRY32 thread;
	DWORD pid = (DWORD)thePID;

	unsigned int resumeCounter = 0;
	unsigned int threadCounter = 0;

	threadSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, 0);
	if (threadSnapshot == INVALID_HANDLE_VALUE) {
		return false;
	}

	// Get all child processes
	HANDLE processSnapshot;
	PROCESSENTRY32 processInfo;

	// Take a snapshot of all processes in the system.
	processSnapshot = CreateToolhelp32Snapshot( TH32CS_SNAPPROCESS, 0 );
	if( processSnapshot == INVALID_HANDLE_VALUE )
	{
	  wprintf(L"CreateToolhelp32Snapshot (of processes)");
	  return false;
	}

	// Set the size of the structure before using it.
	processInfo.dwSize = sizeof( PROCESSENTRY32 );

	// Retrieve information about the first process,
	// and exit if unsuccessful
	if( !Process32First( processSnapshot, &processInfo ) )
	{
	  wprintf(L"Process32First"); // show cause of failure
	  CloseHandle( processSnapshot ); // clean the snapshot object
	  return false;
	}

	// Now walk the snapshot of processes, and
	// find child processes
	do
	{
		// Recursively resume all child processes
		if (processInfo.th32ParentProcessID == pid) {
			Java_edu_virginia_vcgr_genii_container_processmanager_WindowsProvider_resume(env, obj, (jdouble)processInfo.th32ProcessID);
		}

	} while( Process32Next( processSnapshot, &processInfo ) );

	CloseHandle( processSnapshot );

	thread.dwSize = sizeof(THREADENTRY32);

	if (!Thread32First(threadSnapshot, &thread)) {
		CloseHandle(threadSnapshot);
		return false;
	}

	// For all threads of the current PID, attempt to resume them
	// Keep count of resumed threads, and threads within the PID.
	do
	{
		if (thread.th32OwnerProcessID == pid) {
			tempHandler = OpenThread(THREAD_SUSPEND_RESUME, true, thread.th32ThreadID);
			if (ResumeThread(tempHandler) != (DWORD) -1) {
				resumeCounter++;
			}
			threadCounter++;
			CloseHandle(tempHandler);
		}
	} while (Thread32Next(threadSnapshot, &thread));

	CloseHandle(threadSnapshot);

	// If we resumed all of the threads of the PID, success
	if (resumeCounter == threadCounter) {
		return true;
	}

	return false;
}

JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_container_processmanager_WindowsProvider_suspend
  (JNIEnv *env, jobject obj, jdouble thePID)
{
	HANDLE threadSnapshot = INVALID_HANDLE_VALUE;
	HANDLE tempHandler;
	THREADENTRY32 thread;
	DWORD pid = (DWORD) thePID;

	unsigned int suspendCounter = 0;
	unsigned int threadCounter = 0;

	threadSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, 0);
	
	if (threadSnapshot == INVALID_HANDLE_VALUE) {
		return false;
	}

	// Get all child processes
	HANDLE processSnapshot;
	PROCESSENTRY32 processInfo;

	// Take a snapshot of all processes in the system.
	processSnapshot = CreateToolhelp32Snapshot( TH32CS_SNAPPROCESS, 0 );
	if( processSnapshot == INVALID_HANDLE_VALUE )
	{
	  wprintf(L"CreateToolhelp32Snapshot (of processes)");
	  return false;
	}

	// Set the size of the structure before using it.
	processInfo.dwSize = sizeof( PROCESSENTRY32 );

	// Retrieve information about the first process,
	// and exit if unsuccessful
	if( !Process32First( processSnapshot, &processInfo ) )
	{
	  wprintf(L"Process32First"); // show cause of failure
	  CloseHandle( processSnapshot ); // clean the snapshot object
	  return false;
	}

	// Now walk the snapshot of processes, and
	// find child processes
	do
	{
		// Recursively suspend all child processes
		if (processInfo.th32ParentProcessID == pid) {
			Java_edu_virginia_vcgr_genii_container_processmanager_WindowsProvider_suspend(env, obj, (jdouble)processInfo.th32ProcessID);
		}

	} while( Process32Next( processSnapshot, &processInfo ) );

	CloseHandle( processSnapshot );

	thread.dwSize = sizeof(THREADENTRY32);

	if (!Thread32First(threadSnapshot, &thread)) {
		CloseHandle(threadSnapshot);
		return false;
	}

	// Suspend all threads associated with a PID./
	// Keep count of how many threads there are, and how many successfully suspended.
	do
	{
		if (thread.th32OwnerProcessID == pid) {
			tempHandler = OpenThread(THREAD_SUSPEND_RESUME, true, thread.th32ThreadID);
			if (SuspendThread(tempHandler) != (DWORD)-1) {
				suspendCounter++;
			}
			threadCounter++;
			CloseHandle(tempHandler);
		}
	} while (Thread32Next(threadSnapshot, &thread));
	
	CloseHandle(threadSnapshot);

	// If we suspended all of the threads of the PID, success
	if (suspendCounter == threadCounter) {
		return true;
	}

	return false;
}