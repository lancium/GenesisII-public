#include <stdio.h>

#include "PairList.h"

int main(int agrc, char **argv)
{
	PairList list;
	StringPair pair;
	PairListIterator iter;

	list = createPairList();

	pair = createStringPair("Mark", "32");
	list->append(list, pair);
	pair->destroy(pair);

	pair = createStringPair("Matt", "30");
	list->append(list, pair);
	pair->destroy(pair);

	pair = createStringPair("Jodie", "30");
	list->append(list, pair);
	pair->destroy(pair);

	iter = list->iterator(list);
	while (iter->hasNext(iter))
	{
		pair = iter->next(iter);
		fprintf(stdout, "%s[%s]\n", pair->getFirst(pair),
			pair->getSecond(pair));
	}
	iter->destroy(iter);

	list->destroy(list);
	return 0;
}
