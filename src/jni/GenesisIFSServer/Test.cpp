//int main(int argc, char* argv[]){
	////Status code in Query Directory = # of entries
	//int StatusCode, bytes;
	//char ** directoryListing;
	//char buffer[8192];	

	//initializeJavaVM("C:/GenesisIIDevelopment/GenesisII");
	//
	////Do listing					
	//genesisII_login("security/keys.pfx", "keys", "skynet1");
	//printf("Login successful\n");
	//printf("Getting Listing \n");

	//StatusCode = genesisII_get_information(&directoryListing, "");
	//printf("Got actual information\n");
	//bytes = copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);

	//StatusCode = genesisII_directory_listing(&directoryListing, "", "");
	//printf("The status cod returned is %d\n", StatusCode);
	//printf("Got actual listing\n");
	//bytes = copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);


	//printf("Getting Listing again (should be cached)\n");
	//StatusCode = genesisII_directory_listing(&directoryListing, "", "");
	//printf("The status cod returned is %d\n", StatusCode);
	//printf("Got actual listing\n");
	//bytes = copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);

	//printf("Now testing aFile.xml (should be cached)\n");
	//StatusCode = genesisII_get_information(&directoryListing, "/aFile.xml");
	//printf("Got actual information\n");
	//copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);	

	//printf("Now testing desktop.ini:ugly (should send immediately back)\n");
	//StatusCode = genesisII_get_information(&directoryListing, "/desktop.ini:ugly");
	//printf("Got actual information\n");
	//copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);	

	//printf("Now testing notHere.txt (should cache non existence)\n");
	//StatusCode = genesisII_get_information(&directoryListing, "/notHere.txt");
	//printf("Got actual information\n");
	//copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);	

	//printf("Now testing notHere.txt again (should send immediately back (a cached not found)\n");
	//StatusCode = genesisII_get_information(&directoryListing, "/notHere.txt");
	//printf("Got actual information\n");
	//copyListing(buffer, directoryListing, StatusCode);
	//printListing(buffer, StatusCode);	


	//genesisII_logout();

//	return 0;
//}