# makefile for the genesis GFFS source rpm.

RPMBUILD = /usr/bin/rpmbuild
#KEYTOOL = /usr/bin/keytool
#OPENSSL = /usr/bin/openssl

RPMBUILD_TOPDIR := $(shell mktemp -d /tmp/genesis-rpmbuild.XXXXXX)

VER = $(shell sed -n -e 's/genii.app-version=\(.*\)/\1/p' ../../current.version)
REL = $(shell sed -n -e 's/genii.release-number=\(.*\)/\1/p' ../../current.version)
GRID = $(shell sed -n -e 's/genii.simple-name=\(.*\)/\1/p' ../../current.deployment)

BUNDLE_NAME = genesis2-$(GRID)-$(VER)

# the PLATFORM_ARCH variable comes from the environment and must be either x86_64 or i586 currently.

all: build-rpms

build-rpms: package-code
	@echo preparing to build genesis2 rpm.
	@echo top level build dir is $(RPMBUILD_TOPDIR)
	@echo ver is $(VER) and rel is $(REL) and platform arch is $(PLATFORM_ARCH)
	@echo BUILD_FLAGS=$(BUILD_FLAGS) and PLATFORM_BITSIZE=$(PLATFORM_BITSIZE)
	for i in BUILD BUILDROOT RPMS SOURCES SPECS SRPMS; do mkdir -p $(RPMBUILD_TOPDIR)/$$i; done
	cp ../../BUILDME $(RPMBUILD_TOPDIR)/SOURCES/
	cp -p $(BUNDLE_NAME).tar.gz $(RPMBUILD_TOPDIR)/SOURCES/
	rm $(BUNDLE_NAME).tar.gz 
	sed \
		-e "s/%VER%/$(VER)/g" \
		-e "s/%REL%/$(REL)/g" \
		-e "s/%GRID%/$(GRID)/g" \
		-e "s/%ARCH%/$(PLATFORM_ARCH)/g" \
		-e "s/%PLATFORM_BITSIZE%/$(PLATFORM_BITSIZE)/g" \
		-e "s/%BUILD_FLAGS%/$(BUILD_FLAGS)/g" \
		-e "s/%DEPLOYMENT_SOURCE_NAME%/$(DEPLOYMENT_SOURCE_NAME)/g" \
		-e "s/%DEPLOYMENT_TARGET_NAME%/$(DEPLOYMENT_TARGET_NAME)/g" \
		-e "s/%DEPLOYMENT_CONTEXT%/$(DEPLOYMENT_CONTEXT)/g" \
		genesis2.spec.in >$(RPMBUILD_TOPDIR)/SPECS/genesis2.spec
	$(RPMBUILD) --sign --define '_topdir $(RPMBUILD_TOPDIR)' --buildroot $(RPMBUILD_TOPDIR)/BUILDROOT -ba $(RPMBUILD_TOPDIR)/SPECS/genesis2.spec
	cp $(RPMBUILD_TOPDIR)/RPMS/*/* $(RPMBUILD_TOPDIR)/SRPMS/* $(HOME)/installer_products-$(GRID)
	bash md5sum_generator.sh $(HOME)/installer_products-$(GRID)
	rm -rf $(RPMBUILD_TOPDIR)

# some ugly extra steps since the packager doesn't make a sub-dir inside tar file.
package-code:
	@if [ -z $(PLATFORM_ARCH) ]; then echo "The build platform environment variable is not set in 'PLATFORM_ARCH'."; exit 1; fi
	rm -rf $(BUNDLE_NAME).tar.gz $(BUNDLE_NAME)
	mkdir $(BUNDLE_NAME) 
	bash ../../scripts/produce_build_package.sh $(shell pwd)/../../ $(TMP) "" $(BUNDLE_NAME)-temp
	(cd $(BUNDLE_NAME); tar -xf $(TMP)/$(BUNDLE_NAME)-temp.tar.gz)
	tar -czf $(BUNDLE_NAME).tar.gz $(BUNDLE_NAME)
	cp $(BUNDLE_NAME).tar.gz $(HOME)/installer_products-$(GRID)/$(BUNDLE_NAME)-source.tar.gz 
	rm -rf $(BUNDLE_NAME) $(TMP)/$(BUNDLE_NAME)-temp.tar.gz 

