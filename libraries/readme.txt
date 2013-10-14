
This folder has all the library sub-projects for GenesisII.  These are built as part of
the trunk build, but can also be built here in this folder.

To build all libaries in the proper order of dependencies,
run the build script:

  bash scripts/build_dependencies.sh

If you want it to also update the libraries to the latest from svn before
building, then add the 'update' flag:

  bash scripts/build_dependencies.sh update

And to clean up all the generated files in the libraries, the 'clean' flag
is available:

  bash scripts/build_dependencies.sh clean


