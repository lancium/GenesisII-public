

grid tester...
this is a post-build test that was hidden inside the build.
it was meant to be built into the client jar, then run as a client tool.
however, it's also based on junit, which doesn't make a ton of sense for a heavy-weight
post-build style test.
it needs to be refactored as a separate unit from genesis2, or to be made into
something that makes sense to run as a build-time unit test, which it is currently not.

the assorted parts of it are being left in the unit tests hierarchy for now, because
some of them are easier to make into build-time unit tests than others.
they're going to be disabled and marked up as to what's missing.
