#include "GetPassword.jh"

#ifndef WIN32
#include <pwd.h>
#include <unistd.h>
#include <sys/types.h>
#else
#include <conio.h>
#include <stdio.h>
#include <memory.h>

	static char* getpass(const char*);
#endif

/*
 * Class:     edu_virginia_vcgr_genii_client_io_GetPassword
 * Method:    getPassword
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_virginia_vcgr_genii_client_io_GetPassword_getPassword
  (JNIEnv *env, jclass jc, jstring prompt)
{
	const char *_prompt;
	char *ret;
	jstring jstr;
	int lcv;

	_prompt = (*env)->GetStringUTFChars(env, prompt, 0);
#if defined (sparc)
	ret = getpassphrase(_prompt);
#else
	ret = getpass(_prompt);
#endif
	(*env)->ReleaseStringUTFChars(env, prompt, _prompt);

	jstr = (*env)->NewStringUTF(env, ret);

	for (lcv = 0; ret[lcv] != (char)0; lcv++)
	{
		ret[lcv] = (char)0;
	}

	return jstr;
}

#ifdef WIN32
/**
 * @memo	Prompts the user for a password, and keeps their response from
 *			being echoed to the console
 * @param	Prompt String to prompt the user with
 * @return	128-byte character buffer containing the password the user typed,
 *			caller should not delete or free this memory
 */
char* getpass(const char *Prompt)
{
	static char Buffer[129];
	int CurrentPosition = 0;
	int CharRead;

	// Duane Merrill, Feb 2001 - we want to zero out the password buffer
	// so old password remnants can't be returned
	memset(Buffer, '\0', sizeof(Buffer));

	fprintf(stdout, "%s", Prompt);
	fflush(stdout);

	while (CurrentPosition < 128)
	{
		CharRead = _getch();
		if (CharRead == '\n')
			break;
		if (CharRead == '\r')
			break;
		if (CharRead == '\a')
			continue;
		if (CharRead == '\b')
		{
			Buffer[--CurrentPosition] = (char)0;
			if (CurrentPosition < 0)
				CurrentPosition = 0;
//			fprintf(stdout, "\b");
			continue;
		}

		Buffer[CurrentPosition++] = CharRead;
//		fprintf(stdout, "*");
	}

	fprintf(stdout, "\n");
	Buffer[CurrentPosition] = (char)0;
	return Buffer;
}
#endif
