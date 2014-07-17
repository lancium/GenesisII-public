# makefile for the genesis GFFS source rpm.

RPMBUILD = /usr/bin/rpmbuild
KEYTOOL = /usr/bin/keytool
OPENSSL = /usr/bin/openssl

RPMBUILD_TOPDIR = $(PWD)/rpmbuild

VER = $(shell sed -n -e 's/genii.app-version=\(.*\)/\1/p' ../installer/current.version)
REL = $(shell sed -n -e 's/genii.release-number=\(.*\)/\1/p' ../installer/current.version)

BUNDLE_NAME = genesis2-source-$(VER)

all: build-srpm

build-srpm: package-code
	echo ver is $(VER)
	echo rel is $(REL)
	for i in BUILD BUILDROOT RPMS SOURCES SPECS SRPMS; do mkdir -p $(RPMBUILD_TOPDIR)/$$i; done
	cp BUILDME $(RPMBUILD_TOPDIR)/SOURCES/
	cp -p $(BUNDLE_NAME).tar.gz $(RPMBUILD_TOPDIR)/SOURCES/
	sed -e "s/%VER%/$(VER)/g" -e "s/%REL%/$(REL)/g" genesis2-source-rpm.spec.in >$(RPMBUILD_TOPDIR)/SPECS/genesis2-source-rpm.spec
	$(RPMBUILD) --define '_topdir $(RPMBUILD_TOPDIR)' --buildroot $(RPMBUILD_TOPDIR)/BUILDROOT -bs $(RPMBUILD_TOPDIR)/SPECS/genesis2-source-rpm.spec

package-code: clean
	mkdir $(BUNDLE_NAME)
	bash ../scripts/produce_build_package.sh $(shell pwd)/../ $(TMP) "" $(BUNDLE_NAME)-temp
	(cd $(BUNDLE_NAME); tar -xf $(TMP)/$(BUNDLE_NAME)-temp.tar.gz)
	tar -czf $(BUNDLE_NAME).tar.gz $(BUNDLE_NAME)
	rm $(TMP)/$(BUNDLE_NAME)-temp.tar.gz

clean:
	rm -rf $(BUNDLE_NAME).tar.gz $(RPMBUILD_TOPDIR) $(BUNDLE_NAME)

