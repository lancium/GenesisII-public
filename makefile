include variables.def

PROJECT = gffs-build
FIRST_TARGETS = build

include rules.def

clean:
	ant clean

build:
	ant build

