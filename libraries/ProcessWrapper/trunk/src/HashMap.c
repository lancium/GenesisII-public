#include <stdlib.h>
#include <string.h>

#include "LinkedList.h"
#include "HashMap.h"
#include "Memory.h"

typedef struct HashMapEntry
{
	void *_key;
	void *_data;
} HashMapEntry;

static HashMapEntry* createHashMapEntry(void *key, void *data);
static void hashMapEntryDestructor(void *ptr);

typedef struct HashMapImpl
{
	HashMap iface;

	HashFunction _keyHashFunction;
	EqualsFunction _keyEqualsFunction;

	HashMapEntry **_table;

	unsigned int _size;
	unsigned int _length;
	unsigned int _resizePoint;
} HashMapImpl;

static void growTable(HashMapImpl *impl);
static void hashMapDestructor(void *ptr);
static void putImpl(struct HashMap*, void *key, void *data);
static void* getImpl(struct HashMap*, void *key);
static unsigned int lengthImpl(struct HashMap*);
static LinkedList* createKeyListImpl(struct HashMap*);

HashMap* createHashMap(unsigned int initialSize,
	HashFunction keyHashFunction, EqualsFunction keyEqualsFunction)
{
	HashMapImpl *impl = (HashMapImpl*)allocate(sizeof(HashMapImpl),
		hashMapDestructor);

	impl->iface.put = putImpl;
	impl->iface.get = getImpl;
	impl->iface.length = lengthImpl;
	impl->iface.createKeyList = createKeyListImpl;

	impl->_keyHashFunction = keyHashFunction;
	impl->_keyEqualsFunction = keyEqualsFunction;

	impl->_size = initialSize;
	impl->_length = 0;
	impl->_resizePoint = initialSize * 2 / 3;
	impl->_table = (HashMapEntry**)malloc(sizeof(HashMapEntry*) *
		impl->_size);
	memset(impl->_table, 0, sizeof(HashMapEntry*) * impl->_size);

	return (HashMap*)impl;
}

HashMapEntry* createHashMapEntry(void *key, void *data)
{
	HashMapEntry *entry = (HashMapEntry*)allocate(sizeof(HashMapEntry),
		hashMapEntryDestructor);

	entry->_key = retain(key);
	entry->_data = retain(data);

	return entry;
}

void hashMapEntryDestructor(void *ptr)
{
	HashMapEntry *entry = (HashMapEntry*)ptr;
	release(entry->_key);
	release(entry->_data);
}

void growTable(HashMapImpl *impl)
{
	unsigned int lcv;
	HashMapEntry *entry;
	HashMapEntry **tmpTable;
	HashMap *tmp = createHashMap(
		impl->_size * 2, impl->_keyHashFunction,
		impl->_keyEqualsFunction);
	HashMapImpl *tmpImpl = (HashMapImpl*)tmp;

	for (lcv = 0; lcv < impl->_size; lcv++)
	{
		entry = impl->_table[lcv];
		if (entry)
			tmp->put(tmp, entry->_key, entry->_data);
	}

	tmpTable = impl->_table;
	impl->_table = tmpImpl->_table;
	tmpImpl->_table = tmpTable;

	tmpImpl->_size = impl->_size;
	impl->_size *= 2;
	
	impl->_resizePoint = tmpImpl->_resizePoint;
	
	release(tmp);
}

void hashMapDestructor(void *ptr)
{
	HashMapImpl *impl = (HashMapImpl*)ptr;
	HashMapEntry *entry;
	unsigned int lcv;

	for (lcv = 0; lcv < impl->_size; lcv++)
	{
		entry = impl->_table[lcv];
		if (entry != NULL)
			release(entry);
	}

	free(impl->_table);
}

void putImpl(struct HashMap *map, void *key, void *data)
{
	HashMapImpl *impl = (HashMapImpl*)map;
	unsigned int index = impl->_keyHashFunction(key);
	HashMapEntry *entry;

	if (impl->_length >= impl->_resizePoint)
		growTable(impl);

	index %= impl->_size;
	while (1)
	{
		entry = impl->_table[index];
		if (entry != NULL)
		{
			if (impl->_keyEqualsFunction(key, entry->_key))
			{
				release(entry->_data);
				entry->_data = retain(data);
				return;
			}
		} else
		{
			impl->_table[index] = createHashMapEntry(key, data);
			impl->_length++;
			return;
		}

		index = (index + 1) % impl->_size;
	}
}

void* getImpl(struct HashMap *map, void *key)
{
	HashMapImpl *impl = (HashMapImpl*)map;
	unsigned int index = impl->_keyHashFunction(key) % impl->_size;
	HashMapEntry *entry;

	while (1)
	{
		entry = impl->_table[index];
		if (entry == NULL)
			return NULL;
		if (impl->_keyEqualsFunction(key, entry->_key))
			return entry->_data;

		index = (index + 1) % impl->_size;
	}

	return NULL;
}

unsigned int lengthImpl(struct HashMap *map)
{
	HashMapImpl *impl = (HashMapImpl*)map;
	return impl->_length;
}

LinkedList* createKeyListImpl(struct HashMap *map)
{
	HashMapImpl *impl = (HashMapImpl*)map;
	LinkedList *list = createLinkedList();
	unsigned int lcv;
	HashMapEntry *entry;

	for (lcv = 0; lcv < impl->_size; lcv++)
	{
		entry = impl->_table[lcv];
		if (entry)
			list->addLast(list, entry->_key);
	}

	return list;
}
