#ifndef __LOGGING_HPP__
#define __LOGGING_HPP__

#include <iostream>

#include "ogrsh/shims/System.hpp"

#define OGRSH_LEVEL_TRACE 0
#define OGRSH_LEVEL_DEBUG 1
#define OGRSH_LEVEL_INFO 2
#define OGRSH_LEVEL_WARN 3
#define OGRSH_LEVEL_ERROR 4
#define OGRSH_LEVEL_FATAL 5

#ifdef OGRSH_DEBUG_LEVEL
	#if OGRSH_DEBUG_LEVEL > OGRSH_LEVEL_ERROR
		#define OGRSH_TRACE(OUTPUTS)
		#define OGRSH_DEBUG(OUTPUTS)
		#define OGRSH_INFO(OUTPUTS)
		#define OGRSH_WARN(OUTPUTS)
		#define OGRSH_ERROR(OUTPUTS)
		#define OGRSH_FATAL(OUTPUTS)									\
			std::cerr << "[(FATAL)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
	#elif OGRSH_DEBUG_LEVEL > OGRSH_LEVEL_WARN
		#define OGRSH_TRACE(OUTPUTS)
		#define OGRSH_DEBUG(OUTPUTS)
		#define OGRSH_INFO(OUTPUTS)
		#define OGRSH_WARN(OUTPUTS)
		#define OGRSH_ERROR(OUTPUTS)									\
			std::cerr << "[(ERROR)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_FATAL(OUTPUTS)									\
			std::cerr << "[(FATAL)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
	#elif OGRSH_DEBUG_LEVEL > OGRSH_LEVEL_INFO
		#define OGRSH_TRACE(OUTPUTS)
		#define OGRSH_DEBUG(OUTPUTS)
		#define OGRSH_INFO(OUTPUTS)
		#define OGRSH_WARN(OUTPUTS)										\
			std::cerr << "[(WARN)" << __FILE__ << ":" << __LINE__		\
				<< "] " <<	OUTPUTS << std::endl
		#define OGRSH_ERROR(OUTPUTS)									\
			std::cerr << "[(ERROR)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_FATAL(OUTPUTS)									\
			std::cerr << "[(FATAL)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
	#elif OGRSH_DEBUG_LEVEL > OGRSH_LEVEL_DEBUG
		#define OGRSH_TRACE(OUTPUTS)
		#define OGRSH_DEBUG(OUTPUTS)
		#define OGRSH_INFO(OUTPUTS)										\
			std::cerr << "[(INFO)" << __FILE__ << ":" << __LINE__		\
				<< "] " <<	OUTPUTS << std::endl
		#define OGRSH_WARN(OUTPUTS)										\
			std::cerr << "[(WARN)" << __FILE__ << ":" << __LINE__		\
				<< "] " <<	OUTPUTS << std::endl
		#define OGRSH_ERROR(OUTPUTS)									\
			std::cerr << "[(ERROR)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_FATAL(OUTPUTS)									\
			std::cerr << "[(FATAL)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
	#elif OGRSH_DEBUG_LEVEL > OGRSH_LEVEL_TRACE
		#define OGRSH_TRACE(OUTPUTS)
		#define OGRSH_DEBUG(OUTPUTS)									\
			std::cerr << "[(DEBUG)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_INFO(OUTPUTS)										\
			std::cerr << "[(INFO)" << __FILE__ << ":" << __LINE__		\
				<< "] " <<	OUTPUTS << std::endl
		#define OGRSH_WARN(OUTPUTS)										\
			std::cerr << "[(WARN)" << __FILE__ << ":" << __LINE__		\
				<< "] " <<	OUTPUTS << std::endl
		#define OGRSH_ERROR(OUTPUTS)									\
			std::cerr << "[(ERROR)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_FATAL(OUTPUTS)									\
			std::cerr << "[(FATAL)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
	#else
		#define OGRSH_TRACE(OUTPUTS)									\
			std::cerr << "[(TRACE)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_DEBUG(OUTPUTS)									\
			std::cerr << "[(DEBUG)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_INFO(OUTPUTS)										\
			std::cerr << "[(INFO)" << __FILE__ << ":" << __LINE__		\
				<< "] " <<	OUTPUTS << std::endl
		#define OGRSH_WARN(OUTPUTS)										\
			std::cerr << "[(WARN)" << __FILE__ << ":" << __LINE__		\
				<< "] " <<	OUTPUTS << std::endl
		#define OGRSH_ERROR(OUTPUTS)									\
			std::cerr << "[(ERROR)" << __FILE__ << ":" << __LINE__		\
				<< "] " << OUTPUTS << std::endl
		#define OGRSH_FATAL(OUTPUTS)									\
			std::cerr << "[(FATAL)" << __FILE__ << ":" << __LINE__				\
				<< "] " << OUTPUTS << std::endl
	#endif
#else
	#define OGRSH_TRACE(OUTPUTS)
	#define OGRSH_DEBUG(OUTPUTS)
	#define OGRSH_INFO(OUTPUTS)
	#define OGRSH_WARN(OUTPUTS)
	#define OGRSH_ERROR(OUTPUTS)
	#define OGRSH_FATAL(OUTPUTS)
#endif /* OGRSH_DEBUG */

#endif
