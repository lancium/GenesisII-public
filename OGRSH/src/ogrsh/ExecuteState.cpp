#include <string>

#include "ogrsh/ExecuteState.hpp"
#include "ogrsh/Logging.hpp"

namespace ogrsh
{
/*
			char *_virtualProgramPath;
			char *_realProgramPath;
*/

	static const char *ENV_VAR = "OGRSH_EXEC_STATE";
	static const int ENV_VAR_LENGTH = 16;

	static char* duplicateString(const char *original);
	static char* createString(const char *variable, const char *origP,
		const char *newP);

	ExecuteState::ExecuteState(char *virtP, char *realP)
	{
		_virtualProgramPath = virtP;
		_realProgramPath = realP;
	}

	ExecuteState::ExecuteState()
	{
		_virtualProgramPath = NULL;
		_realProgramPath = NULL;
	}

	ExecuteState::ExecuteState(const ExecuteState &other)
	{
		_virtualProgramPath = duplicateString(other._virtualProgramPath);
		_realProgramPath = duplicateString(other._realProgramPath);
	}

	ExecuteState::~ExecuteState()
	{
		if (_virtualProgramPath != NULL)
			free(_virtualProgramPath);

		if (_realProgramPath != NULL)
			free(_realProgramPath);
	}

	ExecuteState& ExecuteState::operator= (const ExecuteState &other)
	{
		if (this == &other)
			return *this;

		if (_virtualProgramPath != NULL)
			free(_virtualProgramPath);

		if (_realProgramPath != NULL)
			free(_realProgramPath);

		_virtualProgramPath = duplicateString(other._virtualProgramPath);
		_realProgramPath = duplicateString(other._realProgramPath);

		return *this;
	}

	void ExecuteState::setVirtualPath(const char *virtualPath)
	{
		if (_virtualProgramPath != NULL)
			free(_virtualProgramPath);

		_virtualProgramPath = duplicateString(virtualPath);
	}

	void ExecuteState::setRealPath(const char *realPath)
	{
		if (_realProgramPath != NULL)
			free(_realProgramPath);

		_realProgramPath = duplicateString(realPath);
	}

	const char* ExecuteState::getVirtualPath() const
	{
		return _virtualProgramPath;
	}

	const char* ExecuteState::getRealPath() const
	{
		return _realProgramPath;
	}

	char** ExecuteState::addReplaceEnvironment(char *const envp[])
	{
		if (_realProgramPath == NULL || _virtualProgramPath == NULL)
		{
			OGRSH_FATAL("Error in ExecuteState.  Have a NULL virtual or "
				<< "real path.");
			ogrsh::shims::real_exit(1);
		}

		char **ret;
		int count;
		bool hasVar = false;

		for (count = 0; envp[count] != NULL; count++)
			;

		ret = (char**)malloc(sizeof(char*) * (count + 2));
		for (int lcv = 0; lcv < count; lcv++)
		{
			if ((strncmp(ENV_VAR, envp[lcv], ENV_VAR_LENGTH) == 0) &&
				(envp[lcv][ENV_VAR_LENGTH] == '='))
			{
				ret[lcv] = createString(ENV_VAR,
					_virtualProgramPath, _realProgramPath);
				hasVar = true;
			} else
			{
				ret[lcv] = duplicateString(envp[lcv]);
			}
		}

		if (hasVar)
		{
			ret[count] = NULL;
		} else
		{
			ret[count] = createString(ENV_VAR,
				_virtualProgramPath, _realProgramPath);
			ret[count + 1] = NULL;
		}

		return ret;
	}

	ExecuteState ExecuteState::fromEnvironment()
	{
		OGRSH_DEBUG("In ExecuteState::fromEnvironment()");

		char *virtP;
		char *realP;
		char *value;

		value = getenv(ENV_VAR);
		if (value == NULL)
		{
			OGRSH_DEBUG("ExecuteState::fromEnvironment() -- "
				<< "Couldn't find environment variable.");
			return ExecuteState(NULL, NULL);
		} else
		{
			OGRSH_DEBUG("ExecuteState::Environment variable value is \""
				<< value << "\".");
		}

		for (int lcv = 0; value[lcv] != (char)0; lcv++)
		{
			if (value[lcv] == ':')
			{
				virtP = (char*)malloc(sizeof(char) * (lcv + 1));
				memcpy(virtP, value, lcv);
				virtP[lcv] = (char)0;

				realP = duplicateString(value + lcv + 1);

				OGRSH_DEBUG(
					"ExecuteState::fromEnvironment -- Found a virtP of \""
					<< virtP << "\" and a realP of \""
					<< realP << "\".");
				return ExecuteState(virtP, realP);
			}
		}

		OGRSH_DEBUG("ExecuteState::fromEnvironment -- "
			<< "Couldn't parse environment variable.");
		return ExecuteState(NULL, NULL);
	}

	char* duplicateString(const char *original)
	{
		if (original == NULL)
			return NULL;

		char *ret = (char*)malloc(sizeof(char) * (strlen(original) + 1));
		strcpy(ret, original);
		return ret;
	}

	char* createString(const char *variable, const char *origP,
		const char *newP)
	{
		char *ret = (char*)malloc(sizeof(char) * (strlen(variable) +
			strlen(origP) + strlen(newP) + 3));
		sprintf(ret, "%s=%s:%s", variable, origP, newP);
		return ret;
	}
}
