#include <stdlib.h>

#include "LinkedList.h"
#include "Memory.h"

typedef struct LinkedListNode
{
	void *_data;

	struct LinkedListNode *_next;
} LinkedListNode;

static LinkedListNode* createLinkedListNode(void *data, LinkedListNode *next);
static void linkedListNodeDestructor(void *ptr);

typedef struct IteratorImpl
{
	Iterator iface;

	LinkedListNode *_next;
} IteratorImpl;

static Iterator* createIterator(LinkedListNode *head);
static int hasNextImpl(struct Iterator*);
static void* nextImpl(struct Iterator*);

typedef struct LinkedListImpl
{
	LinkedList iface;

	unsigned int _length;

	LinkedListNode *_head;
	LinkedListNode *_tail;
} LinkedListImpl;

static void linkedListDestructor(void *ptr);
static void addFirstImpl(struct LinkedList*, void*);
static void addLastImpl(struct LinkedList*, void*);
static unsigned int lengthImpl(struct LinkedList*);
static Iterator* createIteratorImpl(struct LinkedList*);

LinkedList* createLinkedList()
{
	LinkedListImpl *ret = (LinkedListImpl*)allocate(sizeof(LinkedListImpl),
		linkedListDestructor);

	ret->iface.addFirst = addFirstImpl;
	ret->iface.addLast = addLastImpl;
	ret->iface.length = lengthImpl;
	ret->iface.createIterator = createIteratorImpl;

	ret->_length = 0;
	ret->_head = ret->_tail = NULL;

	return (LinkedList*)ret;
}

LinkedListNode* createLinkedListNode(void *data, LinkedListNode *next)
{
	LinkedListNode *node = (LinkedListNode*)allocate(sizeof(LinkedListNode),
		linkedListNodeDestructor);
	node->_data = retain(data);
	node->_next = next;

	return node;
}

void linkedListNodeDestructor(void *ptr)
{
	LinkedListNode *node = (LinkedListNode*)ptr;
	release(node->_data);
}

Iterator* createIterator(LinkedListNode *head)
{
	IteratorImpl *impl = (IteratorImpl*)allocate(sizeof(IteratorImpl),
		NULL);

	impl->iface.hasNext = hasNextImpl;
	impl->iface.next = nextImpl;

	impl->_next = head;

	return (Iterator*)impl;
}

int hasNextImpl(struct Iterator *iter)
{
	IteratorImpl *impl = (IteratorImpl*)iter;

	return impl->_next != NULL;
}

void* nextImpl(struct Iterator *iter)
{
	IteratorImpl *impl = (IteratorImpl*)iter;
	void *ret;

	ret = impl->_next->_data;
	impl->_next = impl->_next->_next;

	return ret;
}

void linkedListDestructor(void *ptr)
{
	LinkedListImpl *impl = (LinkedListImpl*)ptr;

	while (impl->_head)
	{
		impl->_tail = impl->_head->_next;
		release(impl->_head);
		impl->_head = impl->_tail;
	}
}

void addFirstImpl(struct LinkedList *list, void *data)
{
	LinkedListImpl *impl = (LinkedListImpl*)list;
	LinkedListNode *node = createLinkedListNode(data, impl->_head);

	if (!impl->_tail)
		impl->_tail = node;
	impl->_head = node;

	impl->_length++;
}

void addLastImpl(struct LinkedList *list, void *data)
{
	LinkedListImpl *impl = (LinkedListImpl*)list;
	LinkedListNode *node = createLinkedListNode(data, NULL);

	if (impl->_tail)
		impl->_tail->_next = node;
	else
		impl->_head = node;

	impl->_tail = node;

	impl->_length++;
}

unsigned int lengthImpl(struct LinkedList *list)
{
	LinkedListImpl *impl = (LinkedListImpl*)list;
	return impl->_length;
}

Iterator* createIteratorImpl(struct LinkedList *list)
{
	LinkedListImpl *impl = (LinkedListImpl*)list;
	return createIterator(impl->_head);
}
