Summary: GenesisII Deployment for X Grid
Name: genesis2-deployment
Version: 0.1
Release: 1
URL:     http://genesis2.virginia.edu/wiki/
License: Apache License 2.0
Group: Applications/Internet
BuildRoot: %{_tmppath}/%{name}-root
Requires: bash
#Source0: gen2-deployment-%{version}.tar.gz
BuildArch: noarch

%description
A deployment package for GenesisII that provides the configuration information
for connecting to a particular GFFS grid.

%prep

%setup

%build

%install
#rm -rf ${RPM_BUILD_ROOT}
#mkdir -p ${RPM_BUILD_ROOT}/usr/bin
cp -R ${RPM_BUILD_ROOT}/deployments $(mktemp ${RPM_BUILD_ROOT}/deployments.XXXXXX)
install -b -m 644 deployments ${RPM_BUILD_ROOT}/deployments

%clean
#rm -rf ${RPM_BUILD_ROOT}/deployments

%files
%defattr(-,root,root)
%attr(644,root,root) %{_bindir}/deployments
#what's bindir?

%changelog
* Sat Nov 23 2013 Chris Koeritz <koeritz@virginia.edu>


