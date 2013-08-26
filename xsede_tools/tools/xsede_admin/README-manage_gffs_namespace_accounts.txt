manage_gffs_namespace_accounts:

Prerequisites:

   - Perl version 5.10.1 or greater
   - Perl modules: DBI, DBD::Pg, List::Compare, Config::Simple, Unix::Syslog

One can either install appropriate packages for the Linux distribution in use or
use the CPAN tool for downloading perl packages.

-------

Centos Dependencies:

perl-DBI
perl-DBD-Pg
perl-List-Compare*
perl-Config-Simple
perl-Unix-Syslog

* package is available from the RPMforge repository
  (see http://wiki.centos.org/AdditionalResources/Repositories/RPMForge
  for access instructions)
-------

Ubuntu Dependencies:

libdbi-perl
libdbd-pg-perl
liblist-compare-perl
libconfig-simple-perl
libunix-syslog-perl

-------

CPAN approach for loading Perl modules:

sudo cpan
> install DBI
> install List::Compare
> install Unix::Syslog
> install Config::Simple
> install DBD::Pg

-------

