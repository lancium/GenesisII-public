#ifndef __JSDL_WRITER_H__
#define __JSDL_WRITER_H__

#include <stdio.h>

#include "JobDescription.h"

typedef int (*applicationWriterType)(FILE*, JobDescription);

int writeJSDL(FILE*, JobDescription, applicationWriterType);

#endif
