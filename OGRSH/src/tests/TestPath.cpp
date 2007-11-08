#include <iostream>
#include "ogrsh/Path.hpp"

using namespace ogrsh;

int main(int argc, char **argv)
{
	const Path &p = Path::getCurrentWorkingDirectory();

	std::cout << "Current:  " << (const std::string&)p << std::endl;
	std::cout << "Looking up \"///a//b/c/d/../f/././../g\":  "
		<< (const std::string&)p.lookup("///a//b/c/d/../f/././../g")
		<< std::endl;
	std::cout << "Looking up \"///a//b/c/d/../f/./.\\//../g\":  "
		<< (const std::string&)p.lookup("///a//b/c/d/../f/./.\\//../g")
		<< std::endl;
	std::cout << "Looking up \"../././bark\":  "
		<< (const std::string&)p.lookup("../././bark") << std::endl;

	Path p2 = p.lookup("/home/mmm2a/usr/bin");
	std::cout << "sub-path'ing \"/home/mmm2a/usr/bin\":  "
		<< (const std::string&)p2.subPath(2) << std::endl;

	return 0;
}
