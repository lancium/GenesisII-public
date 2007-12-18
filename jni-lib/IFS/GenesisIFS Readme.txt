Genesis Kernel Driver (nulmrx)

 
This is a simple network provider and driver provided to serve as an IFS for Windows for Genesis II
After installing the driver you can use it by:

1.    Have a container running (normal client set up through Grid.bat)
2.    Run GenesisIFSServer.exe in the GenesisII directory (must give notificaiton sent to kernel driver before moving onto to step (3)
3.    Map a drive letter (G?) to \\nulsvr\share or using the NET USE command in the cmd prompt
 
Installation:

//Copy files

   1. Copy nulmrx.sys to your C:\WindowsDirectory\system32\Drivers
   2. Copy nulmrxnp.dll to your C:\WindwosDirectory\system32\
   3. Copy GenesisIFSServer.exe to your GenesisII root install directory (or dev directory)

//Edit registry

   4. Run regini nulmrx.ini
   5. Edit the following registry entry to add NulMRx as the first network provider: \\HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\NetworkProvider\Order\ProviderOrder REG_SZ   NulMRx, LanmanWorkstation
   6. Reboot to let the nulmrx driver register with the system (no reboot=>no install)

