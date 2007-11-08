#ifndef __FILE_DESCRIPTOR_TABLE_HPP__
#define __FILE_DESCRIPTOR_TABLE_HPP__

#include "ogrsh/FileDescriptor.hpp"

namespace ogrsh
{
	class FileDescriptorTable
	{
		private:
			static FileDescriptorTable *_instance;


			FileDescriptor** _table;

			FileDescriptorTable();
			FileDescriptorTable(const FileDescriptorTable&);

			FileDescriptorTable& operator= (const FileDescriptorTable&);

		public:
			~FileDescriptorTable();

			int insert(FileDescriptor*);
			FileDescriptor* lookup(int descriptor);
			int close(int descriptor);

			static FileDescriptorTable& getInstance();
	};
}

#endif
