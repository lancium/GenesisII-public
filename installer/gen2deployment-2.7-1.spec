Summary: GenesisII Deployment for X Grid
Name: genesis2-deployment
Version: 2.7
Release: 1
URL:     http://genesis2.virginia.edu/wiki/
License: Apache License 2.0
Group: Applications/Internet
BuildRoot: %{_tmppath}/%{name}-root
#Requires:
Source0: genesis2_deployment.tar.gz
BuildArch: noarch
Prefix: /opt/genesis2

%description
A deployment package for GenesisII that provides the configuration information
for connecting to a particular GFFS grid.

%prep
echo at prep the rpm build root is ${RPM_BUILD_ROOT}
%setup -n newdep -c

%build
echo at build the rpm build root is ${RPM_BUILD_ROOT}

%install
echo at install the rpm build root is ${RPM_BUILD_ROOT}
rm -rf ${RPM_BUILD_ROOT}
mkdir -p ${RPM_BUILD_ROOT}/opt/genesis2/deployments
echo "the source area we're in right now is: $(pwd)"
echo "and what we see there is:"
ls -al .
cp -R deployments/* ${RPM_BUILD_ROOT}/opt/genesis2/deployments
echo after stuffing we have contents:
ls -alR ${RPM_BUILD_ROOT}

%clean
echo at clean the rpm build root is ${RPM_BUILD_ROOT}
#no. rm -rf ${RPM_BUILD_ROOT}/deployments

%files
%defattr(644,root,root,755)
#%attr(755,root,root)
/opt/genesis2/deployments

%changelog
* Thu Dec 05 2013 Chris Koeritz <koeritz@virginia.edu>
Starting to bang this into shape.
* Sat Nov 23 2013 Chris Koeritz <koeritz@virginia.edu>
Initial version.


