#include "ExitResults.h"
#include "Memory.h"

static int exitCodeImpl(struct ExitResults*);
static void toXMLImpl(struct ExitResults*, FILE*);

typedef struct ExitResultsImpl
{
	ExitResults interface;

	int _exitCode;
	long long _userTimeMicroSeconds;
	long long _systemTimeMicroSeconds;
	long long _wallclockTimeMicroSeconds;
	long long _maximumRSSBytes;
} ExitResultsImpl;

ExitResults* createExitResults(int exitCode,
	long long userTimeMicroSeconds,
	long long systemTimeMicroSeconds,
	long long wallclockTimeMicroSeconds,
	long long maximumRSSBytes)
{
	ExitResultsImpl *impl = allocate(sizeof(ExitResultsImpl), NULL);

	impl->interface.exitCode = exitCodeImpl;
	impl->interface.toXML = toXMLImpl;

	impl->_exitCode = exitCode;
	impl->_userTimeMicroSeconds = userTimeMicroSeconds;
	impl->_systemTimeMicroSeconds = systemTimeMicroSeconds;
	impl->_wallclockTimeMicroSeconds = wallclockTimeMicroSeconds;
	impl->_maximumRSSBytes = maximumRSSBytes;

	return (ExitResults*)impl;
}

int exitCodeImpl(struct ExitResults *er)
{
	ExitResultsImpl *impl = (ExitResultsImpl*)er;

	return impl->_exitCode;
}

void toXMLImpl(struct ExitResults *er, FILE *out)
{
	ExitResultsImpl *impl = (ExitResultsImpl*)er;

	fprintf(out, "<exit-results exit-code=\"%d\">\n",
		impl->_exitCode);
	fprintf(out, "\t<user-time value=\"%lld\" units=\"MICROSECONDS\"/>\n",
		impl->_userTimeMicroSeconds);
	fprintf(out, "\t<system-time value=\"%lld\" units=\"MICROSECONDS\"/>\n",
		impl->_systemTimeMicroSeconds);
	fprintf(out, "\t<wallclock-time value=\"%lld\" units=\"MICROSECONDS\"/>\n",
		impl->_wallclockTimeMicroSeconds);
	fprintf(out, "\t<maximum-rss>%lld</maximum-rss>\n",
		impl->_maximumRSSBytes);
	fprintf(out, "</exit-results>\n");
}
