#include <stdio.h>
#include <string.h>
#include "StringManipulation.h"
#include "Utility.h"
#include "MakeGenesisIICalls.h"

/* Static variables for library to keep track of JVM */
static int DEBUG = 0;
static JNIEnv *env;
static JavaVM *jvm;
static jclass jni_launcher;

/* Helper functions */
int initializeJavaVM();
int get_static_method(jclass *my_class, char* method, char* method_type, jmethodID *mid);

DllExport int genesisII_directory_listing(char *** listing){		
	jmethodID mid;

	if(get_static_method(&jni_launcher, "getDirectoryListing", "()[Ljava/lang/Object;", &mid) != JNI_ERR)
	{
		/* Invoke Method */
		jarray jlisting = (*env)->CallStaticObjectMethod(env, jni_launcher, mid);						

		/* Convert to char** and return */
		return convert_listing(env, listing, jlisting);
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}

DllExport int genesisII_change_directory(char* new_directory){	
	jmethodID mid;

	if(new_directory == NULL){
		return JNI_ERR;
	}

	if(get_static_method(&jni_launcher, "changeDirectory", "(Ljava/lang/String;)Z", &mid) != JNI_ERR)
	{
		/* Build argument */
		jstring j_argument = NewPlatformString(env, new_directory, -1);			

		 /* Invoke method. */			
		jboolean if_success = (*env)->CallStaticBooleanMethod(env, jni_launcher, mid, j_argument);
		return ( if_success -1 );
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;				
}

DllExport char* genesisII_get_working_directory(){
	jmethodID mid;

	if(get_static_method(&jni_launcher, "getCurrentDirectory", "()Ljava/lang/String;", &mid) != JNI_ERR)
	{		
		jstring jworking_directory = (*env)->CallStaticObjectMethod(env, jni_launcher, mid);
		return convert_jstring(env, jworking_directory);
		
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return NULL;	
}

DllExport int genesisII_login(char * keystore_path, char * password){
	jmethodID mid;

	if(keystore_path == NULL || password == NULL){
		return JNI_ERR;
	}

	if(get_static_method(&jni_launcher, "login", "(Ljava/lang/String;Ljava/lang/String;)Z", &mid) != JNI_ERR)
	{
		/* Build arguments */
		jstring j_arg1 = NewPlatformString(env, keystore_path, -1);
		jstring j_arg2 = NewPlatformString(env, password, -1);		

		/* Invoke Method */
		jboolean if_success = (*env)->CallStaticBooleanMethod(env, jni_launcher, mid, j_arg1, j_arg2);					

		/* Convert to char** and return */
		return (if_success - 1);
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}
DllExport int genesisII_logout(){
	jmethodID mid;

	if(get_static_method(&jni_launcher, "logout", "()V", &mid) != JNI_ERR)
	{
		/* Invoke Method */
		(*env)->CallStaticVoidMethod(env, jni_launcher, mid);		
		return JNI_OK;
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;	
}

DllExport int genesisII_make_directory(char * new_directory){
	jmethodID mid;

	if(new_directory == NULL){
		return JNI_ERR;
	}

	if(get_static_method(&jni_launcher, "makeDirectory", "(Ljava/lang/String;)Z", &mid) != JNI_ERR)
	{		
		/* Build argument */
		jstring j_argument = NewPlatformString(env, new_directory, -1);		

		jboolean if_success = (*env)->CallStaticBooleanMethod(env, jni_launcher, mid, j_argument);
		return (if_success - 1);		
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}

DllExport int genesisII_remove(char * path, int recursive, int force){
	jmethodID mid;

	if((recursive != JNI_TRUE && recursive != JNI_FALSE) || 
		force != JNI_TRUE && force != JNI_FALSE){
			return JNI_ERR;
	}

	if(get_static_method(&jni_launcher, "remove", "(Ljava/lang/String;ZZ)Z", &mid) != JNI_ERR)
	{		
		/* Build argument */
		jstring j_argument = NewPlatformString(env, path, -1);		

		jboolean if_success = (*env)->CallStaticBooleanMethod(env, jni_launcher, mid, j_argument,
			recursive, force);
		return (if_success -1 );		
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}

DllExport GII_FILE_HANDLE genesisII_open(char * target, int create, int read, int write){
	jmethodID mid;

	if(get_static_method(&jni_launcher, "open", "(Ljava/lang/String;ZZZ)I", &mid) != JNI_ERR)
	{		
		/* Build argument */
		jstring j_argument = NewPlatformString(env, target, -1);

		GII_FILE_HANDLE handle = (*env)->CallStaticIntMethod(env, jni_launcher, mid, j_argument, create, 
			read, write);
		return handle;
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}

DllExport int genesisII_read(GII_FILE_HANDLE target, char* data, int offset, int length){
	jmethodID mid;

	if(get_static_method(&jni_launcher, "read", "(III)Ljava/lang/String;", &mid) != JNI_ERR)
	{			
		jstring my_read = (*env)->CallStaticObjectMethod(env, jni_launcher, mid, target, offset, length);
		return convert_jstring_using_data(env, my_read, data);
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}

DllExport int genesisII_write(GII_FILE_HANDLE target, char* data, int offset, int length){
	jmethodID mid;
	int bytes_written;

	if(get_static_method(&jni_launcher, "write", "(ILjava/lang/String;I)I", &mid) != JNI_ERR)
	{		
		jstring j_data = NewPlatformString(env, data, length);		

		bytes_written = (*env)->CallStaticIntMethod(env, jni_launcher, mid, target, j_data, offset);
		return bytes_written;
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}

DllExport int genesisII_close(GII_FILE_HANDLE handle){
	jmethodID mid;
	int return_val;

	if(get_static_method(&jni_launcher, "close", "(I)Z", &mid) != JNI_ERR)
	{				
		return_val = (*env)->CallStaticBooleanMethod(env, jni_launcher, mid, handle);
		return (return_val - 1);
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;
}

DllExport int genesisII_copy(char * src, char * dst, int src_local, int dst_local){
	jmethodID mid;
	if((src_local != JNI_TRUE && src_local != JNI_FALSE) || 
		dst_local != JNI_TRUE && dst_local != JNI_FALSE){
			return JNI_ERR;
	}
	if(src == NULL || dst == NULL){
		return JNI_ERR;
	}

	if(get_static_method(&jni_launcher, "copy", "(Ljava/lang/String;Ljava/lang/String;ZZ)Z", &mid) != JNI_ERR)
	{		
		/* Build arguments */
		jstring j_arg1 = NewPlatformString(env, src, -1);		
		jstring j_arg2 = NewPlatformString(env, dst, -1);		

		return ((*env)->CallStaticBooleanMethod(env, jni_launcher, mid, j_arg1, j_arg2, src_local, dst_local) - 1);
	}
	else{
		printf("GenesisII Error:  Could not find the method specified for this call\n");
	}	
	return JNI_ERR;

}

DllExport int genesisII_move(char * src, char * dst){
	if(src == NULL || dst == NULL){
		return JNI_ERR;
	}
	if(genesisII_copy(src, dst, JNI_FALSE, JNI_FALSE) != JNI_ERR){
		return genesisII_remove(src, JNI_FALSE, JNI_TRUE);
	}
	else{
		return JNI_ERR;
	}
}

/* Extra initialization and helper functions */

DllExport int initializeJavaVM(char * genesis_directory){
	JavaVMOption options[4];	
	JavaVMInitArgs vm_args;
	long status;	
	char op0[512],op1[255],op2[255],op3[255];

	/* default directory */
	if(genesis_directory == NULL){
		genesis_directory = "C:/Program\ Files/Genesis\ II";
	}

	sprintf_s(op0, 512, "-Djava.class.path=%s/ext/bouncycastle/bcprov-jdk15-133.jar;%s/lib/GenesisII-security.jar;%s/lib/morgan-utilities.jar;%s/lib;%s/security;", genesis_directory, genesis_directory, genesis_directory, genesis_directory, genesis_directory);	
	sprintf_s(op1, 255, "-Dlog4j.configuration=genesisII.log4j.properties");
	sprintf_s(op2, 255, "-Djava.library.path=%s/jni-lib", genesis_directory);
	sprintf_s(op3, 255, "-Dedu.virginia.vcgr.genii.client.configuration.base-dir=%s/", genesis_directory);	

	options[0].optionString = op0;
	options[1].optionString = op1;
	options[2].optionString = op2;
	options[3].optionString = op3;
	//options[4].optionString = "-Xms256M";
	//options[5].optionString = "-Xmx512M";

	memset(&vm_args, 0, sizeof(vm_args));
	vm_args.version = JNI_VERSION_1_2;
	vm_args.nOptions = 4;
	vm_args.options = options;	

	if(DEBUG == 1)
	{
		printf("Initializing JVM with %s directory ... ", genesis_directory);
	}

	status = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	jni_launcher = (*env)->FindClass(env, "org/morgan/util/launcher/JNILibraryLauncher");

	if(jni_launcher == 0){
		printf("GenesisII Error: Missing JNILibraryLauncher class");
		status = JNI_ERR;
	}
	else if(DEBUG == 1)
	{
		printf("done.\n");
	}

	return status;
}

int get_static_method(jclass *my_class, char* method, char* method_type, jmethodID *mid){	

	if(*my_class == 0){
		if(initializeJavaVM(NULL) == JNI_ERR){
			return JNI_ERR;
		}
	}
	*mid = (*env)->GetStaticMethodID(env, *my_class, method, method_type);
	if(*mid != 0)
	{
		return JNI_OK;
	}

	return JNI_ERR;
}