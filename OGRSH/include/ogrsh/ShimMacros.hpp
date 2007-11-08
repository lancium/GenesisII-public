#ifndef __SHIM_MACROS_HPP__
#define __SHIMS__MACROSPP__

#define SHIM_DECL(RETURN_TYPE, FUNCTION_NAME, PARAMETER_LIST)				\
	RETURN_TYPE real_##FUNCTION_NAME PARAMETER_LIST;						\
	RETURN_TYPE interceptor_##FUNCTION_NAME PARAMETER_LIST

#define SHIM_DEF(RETURN_TYPE, FUNCTION_NAME, PARAMETER_LIST, ARG_LIST)		\
	static RETURN_TYPE __initialize_##FUNCTION_NAME PARAMETER_LIST;			\
	static RETURN_TYPE (*_real_##FUNCTION_NAME) PARAMETER_LIST = __initialize_##FUNCTION_NAME;				\
	static RETURN_TYPE (*_current_##FUNCTION_NAME) PARAMETER_LIST = real_##FUNCTION_NAME;			\
	RETURN_TYPE __initialize_##FUNCTION_NAME PARAMETER_LIST					\
	{																		\
		_real_##FUNCTION_NAME = (RETURN_TYPE (*)PARAMETER_LIST)dlsym(		\
			RTLD_NEXT, #FUNCTION_NAME);										\
		return _real_##FUNCTION_NAME ARG_LIST;								\
	}																		\
	RETURN_TYPE real_##FUNCTION_NAME PARAMETER_LIST							\
	{																		\
		return _real_##FUNCTION_NAME ARG_LIST;								\
	}																		\
	extern "C" {															\
		RETURN_TYPE FUNCTION_NAME PARAMETER_LIST							\
		{																	\
			return _current_##FUNCTION_NAME ARG_LIST;						\
		}																	\
	}																		\
	RETURN_TYPE interceptor_##FUNCTION_NAME PARAMETER_LIST

#define START_SHIM(FUNCTION_NAME)											\
	_current_##FUNCTION_NAME = interceptor_##FUNCTION_NAME
#define STOP_SHIM(FUNCTION_NAME)											\
	_current_##FUNCTION_NAME = _real_##FUNCTION_NAME

#endif
