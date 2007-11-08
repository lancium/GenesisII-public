#ifndef __PROTECTED_TABLE_HPP__
#define __PROTECTED_TABLE_HPP__

namespace ogrsh
{
	class ProtectedTable
	{
		private:
			static ProtectedTable *_instance;

			bool* _table;

			ProtectedTable();
			ProtectedTable(const ProtectedTable&);

			ProtectedTable& operator= (const ProtectedTable&);

		public:
			~ProtectedTable();

			void protect(int fd);
			bool isProtected(int fd);
			void unprotect(int fd);

			static ProtectedTable& getInstance();
	};
}

#endif
