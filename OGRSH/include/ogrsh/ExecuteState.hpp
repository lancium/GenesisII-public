#ifndef __EXECUTE_STATE_HPP__
#define __EXECUTE_STATE_HPP__

#include <string>

namespace ogrsh
{
	class ExecuteState
	{
		private:
			char *_virtualProgramPath;
			char *_realProgramPath;

			ExecuteState(char *virtP, char *realP);

		public:
			ExecuteState();
			ExecuteState(const ExecuteState&);

			~ExecuteState();

			ExecuteState& operator= (const ExecuteState&);

			void setVirtualPath(const char *virtualPath);
			void setRealPath(const char *realPath);
			const char *getVirtualPath() const;
			const char *getRealPath() const;

			char** addReplaceEnvironment(char *const envp[]);

			static ExecuteState fromEnvironment();
	};
}

#endif
