#include <stdlib.h>

#include "StringPair.h"
#include "PairList.h"

typedef struct PairListNode
{
	struct PairListNode *_next;
	StringPair _data;
} PairListNode;

typedef struct PairListImpl
{
	PairListType listType;

	PairListNode *_head;
	PairListNode *_tail;
} PairListImpl;

typedef struct PairListIteratorImpl
{
	PairListIteratorType iterType;

	PairListNode *_next;
} PairListIteratorImpl;

static PairListNode* createNode(StringPair data);
static void destroyNode(PairListNode*);
static PairListIterator createPairListIterator(PairListNode *head);
static void destroyPairListIterator(PairListIterator);
static int iteratorHasNext(PairListIterator);
static StringPair iteratorNext(PairListIterator);
static PairListIterator createIterator(PairList);
static void append(PairList, StringPair);
static void destroyPairList(PairList);

PairList createPairList()
{
	PairListImpl *ret;

	ret = (PairListImpl*)malloc(sizeof(PairListImpl));
	ret->listType.iterator = createIterator;
	ret->listType.append = append;
	ret->listType.destroy = destroyPairList;

	ret->_head = NULL;
	ret->_tail = NULL;

	return (PairList)ret;
}

PairListNode* createNode(StringPair data)
{
	PairListNode *ret;

	ret = (PairListNode*)malloc(sizeof(PairListNode));
	ret->_next = NULL;
	ret->_data = data->copy(data);

	return ret;
}

void destroyNode(PairListNode *node)
{
	node->_data->destroy(node->_data);
	free(node);
}

PairListIterator createPairListIterator(PairListNode *head)
{
	PairListIteratorImpl *impl;

	impl = (PairListIteratorImpl*)malloc(sizeof(PairListIteratorImpl));
	impl->iterType.hasNext = iteratorHasNext;
	impl->iterType.next = iteratorNext;
	impl->iterType.destroy = destroyPairListIterator;
	impl->_next = head;

	return (PairListIterator)impl;
}

void destroyPairListIterator(PairListIterator iter)
{
	free(iter);
}

int iteratorHasNext(PairListIterator iter)
{
	PairListIteratorImpl *impl = (PairListIteratorImpl*)iter;

	return (impl->_next != NULL);
}

StringPair iteratorNext(PairListIterator iter)
{
	PairListIteratorImpl *impl = (PairListIteratorImpl*)iter;
	StringPair ret = NULL;

	if (impl->_next != NULL)
	{
		ret = impl->_next->_data;
		impl->_next = impl->_next->_next;
	}

	return ret;
}

PairListIterator createIterator(PairList list)
{
	return createPairListIterator(((PairListImpl*)list)->_head);
}

void append(PairList list, StringPair pair)
{
	PairListImpl *impl = (PairListImpl*)list;

	if (impl->_head == NULL)
	{
		impl->_head = impl->_tail = createNode(pair);
	} else
	{
		impl->_tail->_next = createNode(pair);
		impl->_tail = impl->_tail->_next;
	}
}

void destroyPairList(PairList list)
{
	PairListImpl *impl = (PairListImpl*)list;

	while (impl->_head != NULL)
	{
		impl->_tail = impl->_head->_next;
		destroyNode(impl->_head);
		impl->_head = impl->_tail;
	}

	free(impl);
}
