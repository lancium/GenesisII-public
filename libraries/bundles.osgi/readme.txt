
this directory holds our projects that actually build any osgi bundles needed
for GenesisII.

there are no ant build files; these are all created in eclipse.
when one needs to create a new bundle,

1) up the version number!  otherwise things will load old bundles possibly.

2) export the bundle in eclipse using the export wizard on first tab of
the plugin manifest editor (open META-INF/MANIFEST.MF).


