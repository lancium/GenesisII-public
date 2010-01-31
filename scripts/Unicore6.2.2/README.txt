Getting a Unicore endpoint up and running for the GII XCG grid is 
fairly straightforward.  This refers to the path where you are installing
the Unicore services as $(UNICORE).

(1)	Install the Unicore6 server distribution.  It is recommended to use the 
	GUI installer (e.g., "java -jar unicore-servers-6.2.2.jar").  The following
	installation options are recommended: 
		(i)		Check the "Registry" pack if this is the first Unicore installation
				for the XCG grid     
		(ii)	Replace all instances of "DEMO-SITE" with something more specific,
				e.g., "XCG".
		(iii)	Leave the "classic Perl TSI" unchecked.  This will cause the XNJS
				to run jobs as the installing user (java TSI), but this can be changed and 
				customized in $(UNICORE)/unicorex/conf/uas.config later.    
		(iv)	Leave all autodiscovery and external registry settings 
				unchecked/unchanged.
		(v)		Do not install DEMO-USER cert (uncheck).
		
(2)	Run "configure-for-gii.sh" to configure the Unicore site installation for use 
	with the XCG GII grid.  This will go and grab a bunch of security information 
	from the XCG bootstrap installation, generate appropriate identities and 
	trust stores for the Unicore services, and perform necessary edits to 
	configuration files.  You must provide the "containergrp" keystore password 
	to unlock that certificate/keypair for use as the Unicore service CA for generating
	service identities.  
	
	For future GII releases and XCG incarnations, you may need to edit the variables in 
	this script to change server/path location of the the XCG bootstrap installation's 
	security directory from which the XCG grid's security certs/etc. can be found. 
		 
(3) Start the Unicore services: cd into $(UNICORE) and run "start.sh"

(4) Run the "wait-for-start.sh" script to wait for them to fully initialize and 
	start up.  This may take several minutes.
	
(5) "Connect" to the UAS to initialize the TargetSystem service: 
	"$(UNICORE)/unicorex/bin/ucc connect"
	
(6) At this point, only the unicorex container identity can use job/data services.  
	Add the GII admin to the XUUDB user database by running the "add-admin-acl.sh" 
	script.
	
(7) The URI for linking the BES instance into context space can be retrieved using
	the "get-bes-uri.sh" script.
	
(8) Storage alias expansion is not performed for JSDL target URIs.  Remote locations 
	for storage must be explicitly specified (see example-jsdl/unicore-jsdl.xml).  
	The "Home" storage service URI can be retrieved by invoking  
	"$(UNICORE)/unicorex/bin/ucc resolve u6://XCG/Home". 