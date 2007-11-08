#ifndef __OREF_HPP__
#define __OREF_HPP__

namespace ogrsh
{
	template <class Type>
	class ORef
	{
		private:
			enum AllocationType
			{
				ALLOCATION_TYPE_STATIC,
				ALLOCATION_TYPE_DYNAMIC,
				ALLOCATION_TYPE_ARRAY_DYNAMIC
			};

			Type *_basePointer;
			Type *_currentPointer;
			AllocationType _allocationType;

			int *_referenceCount;

		public:
			ORef(Type *ptr = NULL);
			ORef(const ORef<Type>&);

			~ORef();

			ORef<Type>& operator= (const ORef<Type>&);
			ORef<Type>& operator= (Type *ptr);

			void setStatic();
			void setDynamic();
			void setArrayDynamic();

			bool operator== (const ORef<Type>&) const;
			bool operator== (Type*) const;
			bool operator!= (const ORef<Type>&) const;
			bool operator!= (Type*) const;
			bool operator<= (const ORef<Type>&) const;
			bool operator<= (Type*) const;
			bool operator>= (const ORef<Type>&) const;
			bool operator>= (Type*) const;
			bool operator< (const ORef<Type>&) const;
			bool operator< (Type*) const;
			bool operator> (const ORef<Type>&) const;
			bool operator> (Type*) const;
	};
}

#endif
