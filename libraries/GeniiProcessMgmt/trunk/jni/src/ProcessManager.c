#include <windows.h>
#include <tlhelp32.h>
#include <tchar.h>

#include "ProcessManager.h"
#include "JavaUtils.h"
#include "StringUtils.h"

#include <jni.h>

typedef BOOL (*ProcessHandlerFunctionType)(JNIEnv *env, HANDLE pHandle);

static BOOL iterateProcessTree(JNIEnv *env, HANDLE pHandle, DWORD pid,
	ProcessHandlerFunctionType processHandler);
static BOOL terminateHandler(JNIEnv *env, HANDLE pHandle);

JNIEXPORT void JNICALL 
	Java_edu_virginia_vcgr_genii_procmgmt_ProcessManager_kill
		(JNIEnv *env, jclass pMgmtClass, jobject processObject)
{
	jclass processClass;
	jlong pHandle;
	jfieldID pHandleField;

	processClass = (*env)->GetObjectClass(env, processObject);
	if (!processClass)
		return;

	pHandleField = getJavaField(env, processClass, "handle", "J");
	if (!pHandleField)
		return;

	pHandle = (*env)->GetLongField(env, processObject, pHandleField);
	iterateProcessTree(env, (HANDLE)pHandle, 0, terminateHandler);
}

BOOL iterateProcessTree(JNIEnv *env, HANDLE pHandle, DWORD pid,
	ProcessHandlerFunctionType processHandler)
{
	HANDLE snapshot;
	HANDLE childProcessHandle;
	PROCESSENTRY32 pe32;
	BOOL ret = TRUE;

	fprintf(stderr, "Iterate process tree called.\n"); fflush(stderr);

	pe32.dwSize = sizeof(PROCESSENTRY32);

	if (pid <= 0)
		pid = GetProcessId(pHandle);

	if (pid == 0)
	{
		throwNewRuntimeException(env, 
			"Unable to get process id for process handle.");
		return FALSE;
	}

	snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	if (snapshot == INVALID_HANDLE_VALUE)
	{
		throwNewRuntimeException(env, 
			"Unable to take snapshot of OS process table.");
		return FALSE;
	}

	if (Process32First(snapshot, &pe32))
	{
		do
		{
			if (pe32.th32ParentProcessID == pid)
			{
				childProcessHandle = OpenProcess(PROCESS_TERMINATE, FALSE, 
					pe32.th32ProcessID);
				if (childProcessHandle == INVALID_HANDLE_VALUE)
				{
					throwNewRuntimeException(env, "Unable to open process for pid.");
					ret = FALSE;
					break;
				}

				ret = iterateProcessTree(env, childProcessHandle, pe32.th32ProcessID,
					processHandler);
				CloseHandle(childProcessHandle);
				if (!ret)
					break;
			}
		} while (Process32Next(snapshot, &pe32));
	}

	CloseHandle(snapshot);

	if (ret)
		ret = processHandler(env, pHandle);

	return ret;
}

BOOL terminateHandler(JNIEnv *env, HANDLE pHandle)
{
	if (!TerminateProcess(pHandle, 0))
	{
		throwNewIOException(env, "Unable to terminate process.");
		return FALSE;
	}

	return TRUE;
}