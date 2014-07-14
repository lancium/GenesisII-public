Summary: GenesisII Deployment for X Grid
Name: REPLACENAME-deployment
Version: 2.7
Release: 1
URL:     http://genesis2.virginia.edu/wiki/
License: Apache License 2.0
Group: Applications/Internet
BuildRoot: %{_tmppath}/%{name}-root
#Requires:
Source0: REPLACENAME-deployment.tar.gz
BuildArch: noarch
Prefix: /opt/genesis2

%description
A deployment package for GenesisII that provides the configuration information
for connecting to the grid called REPLACENAME.

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
echo after installing we have contents:
ls -alR ${RPM_BUILD_ROOT}
echo NOTE: the deployment override is not fully installed until the 
echo script /opt/genesis2/deployments/REPLACENAME/install_REPLACENAME.sh
echo is executed to apply the changes.

%clean
echo at clean the rpm build root is ${RPM_BUILD_ROOT}

%files
%defattr(644,root,root,755)
/opt/genesis2/deployments

%changelog
* Thu Dec 05 2013 Chris Koeritz <koeritz@virginia.edu>
Starting to bang this into shape.
* Sat Nov 23 2013 Chris Koeritz <koeritz@virginia.edu>
Initial version.


