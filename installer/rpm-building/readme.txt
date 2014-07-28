
This is a signed RPM, and it needs to be signed by a developer that XSEDE has
listed in their valid developer certificates (as per SDIACT-124).

To specify which one of your gnupg keys is to be used for signing, create a
file called $HOME/.rpmmacros that contains content similar to this:

=====
%_signature gpg
%_gpg_name  Chris Koeritz (XSEDE Key) <koeritz@virginia.edu>
=====

The %_signature specifies which mechanism is used to sign the RPM.
The %_gpg_name specifies which certificate to use for signing, by its public
name.


