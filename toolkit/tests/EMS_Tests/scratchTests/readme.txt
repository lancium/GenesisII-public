

if you put the example scratch configuration:
   example-ScratchFSManagerContainerService.xml
into place in (you will need to make the cservices directory):
   deployments/bootstrapped_grid/configuration/cservices/ScratchFSManagerContainerService.xml
and restart the bootstrap container, then the scratch test example here should work as expected.

previously a bug in DownloadManager.doDownload could cause the file not to be moved from
the download directory to the scratch space (caused by relying on File.renameTo which has some
weird issues beyond what we expected).


