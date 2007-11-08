
CONTAINERS:

	Containers require two types of keystores for secure operation: a 
	key-store and a trust-store, both of which are designated within the 
	container-crypto.properties file.  The trust-store contains 
	digital certificates that identify trusted third parties.  The key-store 
	must contain a certificate+keypair entry with the alias "VCGR Container".
	The container uses this certificate+keypair for incoming HTTPS connections
	as well as for creating digital certificates for individual resources. (As
	an optimization, this keypair is shared for all resource-certificates.)

CLIENTS:

	Because GII servers do not perform client-authentication at the 
	transport- or the message- level, clients are free to generate their 
	own key material as needed (and therefore do not require a key-store).  
	Clients do, however, require a trust-store, which is designated within 
	the client-crypto.properties file.  The trust-store contains digital 
	certificates that identify trusted third parties.  

KEYSTORE CONFIGURATION:

	To faciliate the configuraiton of these keystores, we have included the 
	"cert-tool" command-line tool.  It allows you to:

		- Generate new certificates and corresponding keypairs (both 
		  self-signed or issued, the latter requires having access to the 
		  issuer)
	
		- Import trusted certificates from Base64 endoded certificate files
		  or other keystores
  
  	For usage of the cert-tool, invoke the tool with no options on the command
  	line.
  	
  	As an example, consider the case when deploying a grid from scratch:
  	
  	- First, a root or intermediate CA/keypair is needed.  If you do not 
  	  already have one, you can generate your own root CA as follows:

	  	cert-tool gen 
	  		-dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=VCGR CA" 
	        -output-keystore=root.pfx 
	        -output-keystore-pass=root 
	        -output-alias="VCGR Certificate Authority"  	  
	                
	- We can now add this certificate to a trust-store that can be used
	  by both clients and servers as follows:
	  
		cert-tool import 
			-input-keystore=root.pfx 
			-input-keystore-pass=root 
			-input-alias="VCGR Certificate Authority" 
			-output-keystore=trusted.pfx 
			-output-keystore-pass=trusted 
			-output-alias="VCGR Certificate Authority"
	  
	- We can add other trusted certificates to this trust store as well.  In
	  Windows, you can export certificates using the functionality found in 
	  IExplorer->Tools->Internet Options...->Content->Certificates.  For 
	  example, after using the Certificate Export Wizard to export the 
	  "Education and Research Client CA" as a base-64 .cer file, we can now 
	  add this certificate to our trust-store as follows:
  	  
		cert-tool import 
			-base64-cert-file=education.cer 
			-output-keystore=trusted.pfx 
			-output-keystore-pass=trusted 
			-output-alias="Education CA"
  	  
	- Now, for each container, we should create a key-store keystore that
	  contains a key entry aliased as "VCGR Container" for the container 
	  to use.  You will likely want to have the common-name (CN) portion 
	  of the distinguished-name (DN) set to the fully-qualified host name 
	  so that TLS clients may verify their target network address with that 
	  in the certificate. We can use the CA in root.pfx to issue such 
	  certificates as follows:
	  
		cert-tool gen 
			-dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=skynet1.cs.virginia.edu" 
			-input-keystore=root.pfx 
			-input-keystore-pass=root 
			-input-alias="VCGR Certificate Authority" 
			-output-keystore=keys.pfx 
			-output-keystore-pass=keys 
			-output-alias="VCGR Container"	  
	  

