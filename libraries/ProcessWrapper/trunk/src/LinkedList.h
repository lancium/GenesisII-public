#ifndef __LINKED_LIST_H__
#define __LINKED_LIST_H__

typedef struct Iterator
{
	int (*hasNext)(struct Iterator*);
	void* (*next)(struct Iterator*);
} Iterator;

typedef struct LinkedList
{
	void (*addFirst)(struct LinkedList*, void*);
	void (*addLast)(struct LinkedList*, void*);

	unsigned int (*length)(struct LinkedList*);

	Iterator* (*createIterator)(struct LinkedList*);
} LinkedList;

LinkedList* createLinkedList();

#endif
