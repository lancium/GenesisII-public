#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "Utility.h"

void print_listing(char** listing, int size){		
	int i;
	for(i = 0; i < size; i++){				
		printf("%s\n",listing[i]);		
	}	
}

int convert_listing(JNIEnv *env, char *** clisting, jarray listing){
	int i;
	if(listing != NULL){
		jsize size = (*env)->GetArrayLength(env, listing);	
		char ** string_array;	
		*clisting = ((char**)malloc(size * sizeof(char**)));	
		string_array = *clisting;

		for(i = 0; i < size; i+=3){		
			jstring type = (*env)->GetObjectArrayElement(env, listing, (jsize)i);
			jstring length =  (*env)->GetObjectArrayElement(env, listing, (jsize)i+1);
			jstring name = (*env)->GetObjectArrayElement(env, listing, (jsize)i+2);
			string_array[i] = convert_jstring(env, type);
			string_array[i+1] = convert_jstring(env, length);			
			string_array[i+2] = convert_jstring(env, name);			
		}	
		return (size/3);
	}
	else{
		return JNI_ERR;
	}
}

void print_jarray_listing(JNIEnv *env, jarray listing){	
	jsize size = (*env)->GetArrayLength(env, listing);	
	int i;
	for(i = 0; i < size; i++){		
		jstring item = (*env)->GetObjectArrayElement(env, listing, (jsize)i);
		const char * item_string =(*env)->GetStringUTFChars(env, item, NULL);
		printf("%s\n",item_string);
		(*env)->ReleaseStringUTFChars(env, item, item_string);
	}	
}
char* convert_jstring(JNIEnv *env, jstring the_string){
	char *to_return = NULL;
	int size;
	if(the_string != NULL){
		const char* item_string = (*env)->GetStringUTFChars(env, the_string, NULL);
		size = (*env)->GetStringLength(env, the_string);
		to_return = malloc(size + 1);
		memcpy(to_return, item_string, size);
		to_return[size] = '\0';
		(*env)->ReleaseStringUTFChars(env, the_string, item_string);
	}

	return to_return;
}

int convert_jstring_using_data(JNIEnv *env, jstring the_string, char * to_return){	
	int size = -1;
	if(the_string != NULL){
		const char* item_string = (*env)->GetStringUTFChars(env, the_string, NULL);
		size = (*env)->GetStringLength(env, the_string);		
		memcpy(to_return, item_string, size);		
		(*env)->ReleaseStringUTFChars(env, the_string, item_string);
	}
	return size;
}