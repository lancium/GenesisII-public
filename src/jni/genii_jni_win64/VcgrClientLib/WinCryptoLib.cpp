

#include "WinCryptoLib.jh"

#include <windows.h>
#include <stdio.h>
#include <wincrypt.h>
#include <wininet.h>
#include <sys/stat.h>
#include <io.h>
#include <fcntl.h>
#include <tchar.h>

static const char* EXCEPTION_CLASS = 
	"edu/virginia/vcgr/genii/client/security/wincrypto/WinCryptoException";
static const char* BAD_CHAIN_EXCEPTION_CLASS = 
	"edu/virginia/vcgr/genii/client/security/wincrypto/WinCryptoChainInvalidException";


/**
 * Utility function for throwing exceptions
 */
static void throwException (JNIEnv *env, const char *exceptionClassName, const char *message)
{
	jclass exceptionClass = env->FindClass(exceptionClassName);
	env->ThrowNew(exceptionClass, message);

	return;
}

/**
 * Utility function to convert endian-ness
 */
static void reverse(BYTE *buff, DWORD len) {
	BYTE tmp;
	for (DWORD i = 0; i < len / 2; i++) {
		tmp = buff[i];
		buff[i] = buff[len - i - 1];
		buff[len - i - 1] = tmp;
	}
}

/**
 * Attempts to locate a certificate context matching the specified keyId in the given store
 */
static PCCERT_CONTEXT findCertContext(JNIEnv *env, HCERTSTORE hSystemStore, BYTE *searchKeyId, DWORD searchKeyIdLen) {

	// First try and find the specified certificate using 
	// CertFindCertificateInStore().  In rare circumstances, it sometimes misses
	// perfect matches on the key identifier, so we will do a linear 
	// search upon miss.
	PCCERT_CONTEXT pCertContext = NULL;
	CRYPT_HASH_BLOB findData;
	findData.cbData = searchKeyIdLen;
	findData.pbData = searchKeyId;
	if (pCertContext = CertFindCertificateInStore(
			hSystemStore, X509_ASN_ENCODING,
			0,
			CERT_FIND_KEY_IDENTIFIER,
			&findData,
			pCertContext)) {

		return pCertContext;
	}

	// we did't find the cert; try again using linear search
	pCertContext = NULL;
	DWORD keyIdLen = 0;
	BYTE *keyId = NULL;
	while(pCertContext = CertEnumCertificatesInStore(hSystemStore, pCertContext)) {

		// get the size of the CERT_KEY_IDENTIFIER_PROP_ID property
		if (!CertGetCertificateContextProperty(pCertContext, CERT_KEY_IDENTIFIER_PROP_ID,
				NULL, &keyIdLen)) {

			throwException(env, EXCEPTION_CLASS, "Could not get alias for certificate");
			CertCloseStore(hSystemStore, 0);
			return NULL;
		}

		// allocate and retrieve the CERT_KEY_IDENTIFIER_PROP_ID property
		keyId = (BYTE *) malloc(keyIdLen);
		if (!CertGetCertificateContextProperty(pCertContext, CERT_KEY_IDENTIFIER_PROP_ID,
				keyId, &keyIdLen)) {
		
			throwException(env, EXCEPTION_CLASS, "Could not get alias for certificate");
			CertCloseStore(hSystemStore, 0);
			return NULL;
		}

		// do a byte-comparo on the key id
		bool found = true;
		if (findData.cbData != keyIdLen) {
			found = false;
		} else {
			for (DWORD i = 0; i < findData.cbData; i++) {
				if (keyId[i] != findData.pbData[i]) {
					found = false;
				}
			}
		}
		if (found) {
			return pCertContext;
		}
	}
	return NULL;
}

/**
 * Utility function for retrieving a chain context, returning NULL and throwing a java exception
 * if the chain is invalid 
 */
static PCCERT_CHAIN_CONTEXT getCertChain(JNIEnv *env, PCCERT_CONTEXT pCertContext) {

	// create parameters for chain context construction
	PCCERT_CHAIN_CONTEXT pChainContext = NULL;

	CERT_ENHKEY_USAGE enhkeyUsage;
	enhkeyUsage.cUsageIdentifier = 0;
	enhkeyUsage.rgpszUsageIdentifier = NULL;

	CERT_USAGE_MATCH certUsage;  
	certUsage.dwType = USAGE_MATCH_TYPE_AND;
	certUsage.Usage = enhkeyUsage;

	CERT_CHAIN_PARA chainPara;
	chainPara.cbSize = sizeof(CERT_CHAIN_PARA);
	chainPara.RequestedUsage = certUsage;

	if (!CertGetCertificateChain(
			NULL,                  // use the default chain engine
			pCertContext,          // pointer to the end certificate
			NULL,                  // use the default time
			NULL,                  // search no additional stores
			&chainPara,            // use AND logic and enhanced key usage 
								   //  as indicated in the ChainPara 
								   //  data structure
			0,                     // no flags, may consider 
								   //  CERT_CHAIN_DISABLE_PASS1_QUALITY_FILTERING 
								   //  for performance later
			NULL,                  // currently reserved
			&pChainContext)) {     // return a pointer to the chain created
		
		throwException(env, EXCEPTION_CLASS, "Could not create chain");
		return NULL;
	}

	// check for chain validity
	if (pChainContext->TrustStatus.dwErrorStatus != CERT_TRUST_NO_ERROR) {
		
		if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_NOT_TIME_VALID) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "This certificate or one of the certificates in the certificate chain is not time-valid.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_NOT_TIME_NESTED) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "Certificates in the chain are not properly time-nested.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_REVOKED) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "Trust for this certificate or one of the certificates in the certificate chain has been revoked.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_NOT_SIGNATURE_VALID) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "The certificate or one of the certificates in the certificate chain does not have a valid signature.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_NOT_VALID_FOR_USAGE) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "The certificate or certificate chain is not valid in its proposed usage.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_UNTRUSTED_ROOT) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "The certificate or certificate chain is based on an untrusted root.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_REVOCATION_STATUS_UNKNOWN) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "The revocation status of the certificate or one of the certificates in the certificate chain is unknown.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_CYCLIC) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "One of the certificates in the chain was issued by a certification authority that the original certificate had certified.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_IS_PARTIAL_CHAIN) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "The certificate chain is not complete.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_CTL_IS_NOT_TIME_VALID) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "A CTL used to create this chain was not time-valid.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_CTL_IS_NOT_SIGNATURE_VALID) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "A CTL used to create this chain did not have a valid signature.");
		else if (pChainContext->TrustStatus.dwErrorStatus & CERT_TRUST_CTL_IS_NOT_VALID_FOR_USAGE) 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "A CTL used to create this chain is not valid for this usage.");
		else 
			 throwException(env, BAD_CHAIN_EXCEPTION_CLASS, "Invalid certificate chain");

		CertFreeCertificateChain(pChainContext);
		return NULL;
	}

	return pChainContext;
}



/*
 * Class:     edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib
 * Method:    getByteArrayAliases
 * Signature: (Ljava/lang/String;)Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib_getByteArrayAliases
		(JNIEnv *env, jobject objInstance, jstring certStore) {

	// create the return ArrayList
	jclass arrayListClass = env->FindClass("java/util/ArrayList");
    if (arrayListClass == NULL) {
		throwException(env, EXCEPTION_CLASS, "Could not prepare return value");
		return NULL; 
	}
    jmethodID constructorId = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID addId = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
	if ((constructorId == NULL) || (addId == NULL)) {
		throwException(env, EXCEPTION_CLASS, "Could not prepare return value");
		return NULL; 
	}
	jobject aliases = env->NewObject(arrayListClass, constructorId);
 
	// open the specified certificate store
	const jchar* certStoreChars = env->GetStringChars(certStore, FALSE);
	HCERTSTORE hSystemStore;
	if (!(hSystemStore = CertOpenStore(CERT_STORE_PROV_SYSTEM,
			0, 0, CERT_SYSTEM_STORE_CURRENT_USER, certStoreChars))) {
	
		throwException(env, EXCEPTION_CLASS, "Could not open certificate store");
		return NULL;
	}

	// iterate through the certificates
	PCCERT_CONTEXT pCertContext = NULL;
	DWORD propLen = 0;
	BYTE *keyId = NULL;
	while(pCertContext = CertEnumCertificatesInStore(hSystemStore, pCertContext)) {

		// get the size of the CERT_KEY_IDENTIFIER_PROP_ID property
		if (!CertGetCertificateContextProperty(pCertContext, CERT_KEY_IDENTIFIER_PROP_ID,
				NULL, &propLen)) {

			throwException(env, EXCEPTION_CLASS, "Could not get alias for certificate");
			CertCloseStore(hSystemStore, 0);
			return NULL;
		}

		// allocate and retrieve the CERT_KEY_IDENTIFIER_PROP_ID property
		keyId = (BYTE *) malloc(propLen);
		if (!CertGetCertificateContextProperty(pCertContext, CERT_KEY_IDENTIFIER_PROP_ID,
				keyId, &propLen)) {
		
			throwException(env, EXCEPTION_CLASS, "Could not get alias for certificate");
			CertCloseStore(hSystemStore, 0);
			return NULL;
		}

		// add the keyId to the return list
		jbyteArray keyIdArray = env->NewByteArray(propLen);
		env->SetByteArrayRegion(keyIdArray, 0, propLen, (const jbyte *) keyId);
		env->CallBooleanMethod(aliases, addId, keyIdArray);
		free(keyId);
	}

	CertCloseStore(hSystemStore, 0);
	return aliases;
}

/*
 * Class:     edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib
 * Method:    getPrivateKeySpec
 * Signature: (Ljava/lang/String;[B)Ljava/security/spec/RSAPrivateCrtKeySpec;
 */
JNIEXPORT jobject JNICALL Java_edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib_getPrivateKeySpec
		(JNIEnv *env, jobject objInstance, jstring certStore, jbyteArray alias) {
	
	// get the classes and constructors to create RSAPrivateCrtKeySpecs and BigIntegers
	jclass rsaPrivateCrtKeySpecClass = env->FindClass("java/security/spec/RSAPrivateCrtKeySpec");
	jclass bigIntegerClass = env->FindClass("java/math/BigInteger");
    if ((rsaPrivateCrtKeySpecClass == NULL) || (bigIntegerClass == NULL)) {
		throwException(env, EXCEPTION_CLASS, "Could not prepare return value");
		return NULL; 
	}
    jmethodID rsaConstructorId = env->GetMethodID(rsaPrivateCrtKeySpecClass, "<init>", "(Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/math/BigInteger;)V");
    jmethodID bigIntConstructorId = env->GetMethodID(bigIntegerClass, "<init>", "(I[B)V");
    if ((rsaConstructorId == NULL) || (bigIntConstructorId == NULL)) {
		throwException(env, EXCEPTION_CLASS, "Could not prepare return value");
		return NULL; 
	}

	// open the specified certificate store
	const jchar* certStoreChars = env->GetStringChars(certStore, FALSE);
	HCERTSTORE hSystemStore;
	if (!(hSystemStore = CertOpenStore(CERT_STORE_PROV_SYSTEM,
			0, 0, CERT_SYSTEM_STORE_CURRENT_USER, certStoreChars))) {
	
		throwException(env, EXCEPTION_CLASS, "Could not open certificate store");
		return NULL;
	}

	// find the specified certificate
	PCCERT_CONTEXT pCertContext = findCertContext(
		env,
		hSystemStore, 
		(BYTE *) env->GetByteArrayElements(alias, false),
		env->GetArrayLength(alias));

	if (pCertContext == NULL) {
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}

	// get the key provider handle
	HCRYPTPROV hCryptProv = NULL;
	DWORD dwKeySpec = 0;
	BOOL bCallerFreeProv = false;
	if (!CryptAcquireCertificatePrivateKey(
			pCertContext,
			CRYPT_ACQUIRE_SILENT_FLAG,
			NULL,
			&hCryptProv,
			&dwKeySpec,
			&bCallerFreeProv)) {

		// Some certificates are not allowed to export private keys: only throw an 
		// exception if the keystate is bad
		if (GetLastError() == NTE_BAD_KEY_STATE) {
			throwException(env, EXCEPTION_CLASS, "Could not acquire key blob size");
		}
		CertFreeCertificateContext(pCertContext);
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}

	// close the cert context since we're done with it
	CertFreeCertificateContext(pCertContext);

	// get the key handle
	HCRYPTKEY hCryptKey = NULL;
	if (!CryptGetUserKey(hCryptProv, dwKeySpec, &hCryptKey)) {
		
		if (bCallerFreeProv) CryptReleaseContext(hCryptProv, 0);
		CertCloseStore(hSystemStore, 0);
		throwException(env, EXCEPTION_CLASS, "Could not acquire key handle");
		return NULL;
	}

	// get the keyblob
	DWORD dwKeyBlobSize = 0;
	if (!CryptExportKey(hCryptKey, NULL, PRIVATEKEYBLOB, 0, NULL, &dwKeyBlobSize)) {
		
		// Some certificates are not allowed to export private keys: NTE_BAD_KEY_STATE 
		// implies we do not have permission to export the key
		if (GetLastError() != NTE_BAD_KEY_STATE) {
			throwException(env, EXCEPTION_CLASS, "Could not acquire key blob size");
		}

		if (bCallerFreeProv) CryptReleaseContext(hCryptProv, 0);
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}
	BYTE *keyBlob = (BYTE *) malloc(dwKeyBlobSize);
	if (!CryptExportKey(hCryptKey, NULL, PRIVATEKEYBLOB, 0, keyBlob, &dwKeyBlobSize) ) {
		free(keyBlob);
		if (bCallerFreeProv) CryptReleaseContext(hCryptProv, 0);
		CertCloseStore(hSystemStore, 0);
		throwException(env, EXCEPTION_CLASS, "Could not acquire key blob");
		return NULL;
	}

	// check to make sure it's a private key
	BLOBHEADER *header = (BLOBHEADER *) keyBlob;
	if (header->bType != PRIVATEKEYBLOB) {
		free(keyBlob);
		if (bCallerFreeProv) CryptReleaseContext(hCryptProv, 0);
		CertCloseStore(hSystemStore, 0);
		throwException(env, EXCEPTION_CLASS, "Key blob does not contain a private key");
		return NULL;
	}

	// pick apart the key blob
	RSAPUBKEY *rsaPubKey = (RSAPUBKEY *) &keyBlob[sizeof(BLOBHEADER)];

	// public exponent
	BYTE *publicExponent = (BYTE *) rsaPubKey + (sizeof(DWORD) * 2);
	DWORD publicExponentLen = sizeof(rsaPubKey->pubexp);
	reverse(publicExponent, publicExponentLen);
	jbyteArray jbaPublicExponent = env->NewByteArray(publicExponentLen);
	env->SetByteArrayRegion(jbaPublicExponent, 0, publicExponentLen, (const jbyte *) publicExponent);
	jobject jPublicExponent = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaPublicExponent);

	// modulus (n)
	BYTE *modulus = (BYTE *) &keyBlob[sizeof(BLOBHEADER) + sizeof(RSAPUBKEY)];	
	DWORD modulusLen = rsaPubKey->bitlen / 8;
	reverse(modulus, modulusLen);
	jbyteArray jbaModulus = env->NewByteArray(modulusLen);
	env->SetByteArrayRegion(jbaModulus, 0, modulusLen, (const jbyte *) modulus);
	jobject jModulus = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaModulus);
	jbyte * jbytes = env->GetByteArrayElements(jbaModulus, false);

	// prime1 (p)
	BYTE *prime1 = (BYTE *) &modulus[modulusLen];	
	DWORD prime1Len = rsaPubKey->bitlen / 16;
	reverse(prime1, prime1Len);
	jbyteArray jbaPrime1 = env->NewByteArray(prime1Len);
	env->SetByteArrayRegion(jbaPrime1, 0, prime1Len, (const jbyte *) prime1);
	jobject jPrime1 = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaPrime1);

	// prime2 (q)
	BYTE *prime2 = (BYTE *) &prime1[prime1Len];	
	DWORD prime2Len = rsaPubKey->bitlen / 16;
	reverse(prime2, prime2Len);
	jbyteArray jbaPrime2 = env->NewByteArray(prime2Len);
	env->SetByteArrayRegion(jbaPrime2, 0, prime2Len, (const jbyte *) prime2);
	jobject jPrime2 = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaPrime2);

	// exponent1 (d mod (p - 1))
	BYTE *exponent1 = (BYTE *) &prime2[prime2Len];	
	DWORD exponent1Len = rsaPubKey->bitlen / 16;
	reverse(exponent1, exponent1Len);
	jbyteArray jbaExponent1 = env->NewByteArray(exponent1Len);
	env->SetByteArrayRegion(jbaExponent1, 0, exponent1Len, (const jbyte *) exponent1);
	jobject jExponent1 = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaExponent1);

	// exponent2 (d mod (q - 1))
	BYTE *exponent2 = (BYTE *) &exponent1[exponent1Len];	
	DWORD exponent2Len = rsaPubKey->bitlen / 16;
	reverse(exponent2, exponent2Len);
	jbyteArray jbaExponent2 = env->NewByteArray(exponent2Len);
	env->SetByteArrayRegion(jbaExponent2, 0, exponent2Len, (const jbyte *) exponent2);
	jobject jExponent2 = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaExponent2);

	// coefficient ((inverse of q) mod p)
	BYTE *coefficient = (BYTE *) &exponent2[exponent2Len];	
	DWORD coefficientLen = rsaPubKey->bitlen / 16;
	reverse(coefficient, coefficientLen);
	jbyteArray jbaCoefficient = env->NewByteArray(coefficientLen);
	env->SetByteArrayRegion(jbaCoefficient, 0, coefficientLen, (const jbyte *) coefficient);
	jobject jCoefficient = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaCoefficient);

	// private exponent (d)
	BYTE *privateExponent = (BYTE *) &coefficient[coefficientLen];	
	DWORD privateExponentLen = rsaPubKey->bitlen / 8;
	reverse(privateExponent, privateExponentLen);
	jbyteArray jbaPrivateExponent = env->NewByteArray(privateExponentLen);
	env->SetByteArrayRegion(jbaPrivateExponent, 0, privateExponentLen, (const jbyte *) privateExponent);
	jobject jPrivateExponent = env->NewObject(bigIntegerClass, bigIntConstructorId, 1, jbaPrivateExponent);

	// create the retval
	jobject retval = env->NewObject(
		rsaPrivateCrtKeySpecClass, 
		rsaConstructorId, 
		jModulus,
		jPublicExponent,
		jPrivateExponent,
		jPrime1,
		jPrime2,
		jExponent1,
		jExponent2,
		jCoefficient);

	free(keyBlob);
	if (bCallerFreeProv) CryptReleaseContext(hCryptProv, 0);
	CertCloseStore(hSystemStore, 0);
	return retval;
}

/*
 * Class:     edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib
 * Method:    getFriendlyName
 * Signature: (Ljava/lang/String;[B)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib_getFriendlyName
  (JNIEnv *env, jobject objInstance, jstring certStore, jbyteArray alias)
{
	// open the specified certificate store
	const jchar* certStoreChars = env->GetStringChars(certStore, FALSE);
	HCERTSTORE hSystemStore;
	if (!(hSystemStore = CertOpenStore(CERT_STORE_PROV_SYSTEM,
			0, 0, CERT_SYSTEM_STORE_CURRENT_USER, certStoreChars))) {
	
		throwException(env, EXCEPTION_CLASS, "Could not open certificate store");
		return NULL;
	}

	// find the specified certificate
	PCCERT_CONTEXT pCertContext = findCertContext(
		env,
		hSystemStore, 
		(BYTE *) env->GetByteArrayElements(alias, false),
		env->GetArrayLength(alias));

	if (pCertContext == NULL) {
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}

	// Get the friendly name for the certificate
	// get the size of the CERT_FRIENDLY_NAME_PROP_ID property
	DWORD propLen = 0;
	BYTE *friendlyName = NULL;
	if (!CertGetCertificateContextProperty(pCertContext, CERT_FRIENDLY_NAME_PROP_ID,
		NULL, &propLen)) 
	{
		throwException(env, EXCEPTION_CLASS, "Could not get friendly name for certificate");
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}

	// allocate and retrieve the CERT_FRIENDLY_NAME_PROP_ID property
	friendlyName = (BYTE *) malloc(propLen);
	if (!CertGetCertificateContextProperty(pCertContext, CERT_FRIENDLY_NAME_PROP_ID,
		friendlyName, &propLen)) 
	{
		throwException(env, EXCEPTION_CLASS, "Could not get friendly name for certificate");
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}
	CertCloseStore(hSystemStore, 0);
	jstring str = env->NewString((const jchar*)friendlyName, propLen / 2 - 1);
	free(friendlyName);
	return str;
}

/*
 * Class:     edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib
 * Method:    getCertFromByteAlias
 * Signature: (Ljava/lang/String;[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib_getCertFromByteAlias
		(JNIEnv *env, jobject objInstance, jstring certStore, jbyteArray alias) {

	jbyteArray retval = NULL;
	
	// open the specified certificate store
	const jchar* certStoreChars = env->GetStringChars(certStore, FALSE);
	HCERTSTORE hSystemStore;
	if (!(hSystemStore = CertOpenStore(CERT_STORE_PROV_SYSTEM,
			0, 0, CERT_SYSTEM_STORE_CURRENT_USER, certStoreChars))) {
	
		throwException(env, EXCEPTION_CLASS, "Could not open certificate store");
		return NULL;
	}

	// find the specified certificate
	PCCERT_CONTEXT pCertContext = findCertContext(
		env,
		hSystemStore, 
		(BYTE *) env->GetByteArrayElements(alias, false),
		env->GetArrayLength(alias));

	if (pCertContext == NULL) {
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}

	retval = env->NewByteArray(pCertContext->cbCertEncoded);
	env->SetByteArrayRegion(retval, 0, pCertContext->cbCertEncoded, (const jbyte *) pCertContext->pbCertEncoded);
	CertFreeCertificateContext(pCertContext);
	CertCloseStore(hSystemStore, 0);
	return retval;
}

/*
 * Class:     edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib
 * Method:    getCertChain
 * Signature: (Ljava/lang/String;[B)Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib_getCertChain
	(JNIEnv *env, jobject objInstance, jstring certStore, jbyteArray alias) {

	// open the specified certificate store
	const jchar* certStoreChars = env->GetStringChars(certStore, FALSE);
	HCERTSTORE hSystemStore;
	if (!(hSystemStore = CertOpenStore(CERT_STORE_PROV_SYSTEM,
			0, 0, CERT_SYSTEM_STORE_CURRENT_USER, certStoreChars))) {
	
		throwException(env, EXCEPTION_CLASS, "Could not open certificate store");
		return NULL;
	}

	// find the specified certificate
	PCCERT_CONTEXT pCertContext = findCertContext(
		env,
		hSystemStore, 
		(BYTE *) env->GetByteArrayElements(alias, false),
		env->GetArrayLength(alias));

	if (pCertContext == NULL) {
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}

	// get the cert chain context
	PCCERT_CHAIN_CONTEXT pChainContext = getCertChain(env, pCertContext);

	// close the cert context since we're done with it
	CertFreeCertificateContext(pCertContext);

	if (pChainContext == NULL) {
		// an exception should have already been set
		CertCloseStore(hSystemStore, 0);
		return NULL;
	}

	// create the return ArrayList
	jclass arrayListClass = env->FindClass("java/util/ArrayList");
    if (arrayListClass == NULL) {
		CertCloseStore(hSystemStore, 0);
		CertFreeCertificateChain(pChainContext);
		throwException(env, EXCEPTION_CLASS, "Could not prepare return value");
		return NULL; 
	}
    jmethodID constructorId = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID addId = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
	if ((constructorId == NULL) || (addId == NULL)) {
		CertCloseStore(hSystemStore, 0);
		CertFreeCertificateChain(pChainContext);
		throwException(env, EXCEPTION_CLASS, "Could not prepare return value");
		return NULL; 
	}
	jobject certChain = env->NewObject(arrayListClass, constructorId);
	
	// cycle through the first chain list and add the certs 
	// into the return value
	PCERT_SIMPLE_CHAIN simpleChain = pChainContext->rgpChain[0];
	for (DWORD i = 0; i < simpleChain->cElement; i++) {
		PCERT_CHAIN_ELEMENT chainElement = simpleChain->rgpElement[i];

		jbyteArray certBlob = env->NewByteArray(chainElement->pCertContext->cbCertEncoded);
		env->SetByteArrayRegion(certBlob, 0, chainElement->pCertContext->cbCertEncoded, (const jbyte *) chainElement->pCertContext->pbCertEncoded);

		env->CallBooleanMethod(certChain, addId, certBlob);
	}

	CertFreeCertificateChain(pChainContext);
	CertCloseStore(hSystemStore, 0);
	return certChain;
}


/*
 * Class:     edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib
 * Method:    isCertTrusted
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_edu_virginia_vcgr_genii_client_security_wincrypto_WinCryptoLib_isCertTrusted
	(JNIEnv *env, jobject objInstance, jbyteArray certBlob) {

	// create a cert context for the blob
	PCCERT_CONTEXT pCertContext = CertCreateCertificateContext(
		X509_ASN_ENCODING | PKCS_7_ASN_ENCODING,
		(BYTE *) env->GetByteArrayElements(certBlob, false),
		env->GetArrayLength(certBlob));
	
	if (pCertContext == NULL) {
		throwException(env, EXCEPTION_CLASS, "Could not decode certificate blob");
		return;
	}

	// get the chain context (setting the exception if invalid)
	PCCERT_CHAIN_CONTEXT pChainContext = getCertChain(env, pCertContext);

	// close the contexts and return
	if (pChainContext != NULL) {
		CertFreeCertificateChain(pChainContext);
	}

	CertFreeCertificateContext(pCertContext);

	return;
}
