#ifndef _CGMC3_H
#define _CGMC3_H

#include "f2c.h"

#ifdef __cplusplus
extern "C" {
#endif

void fill_commons(int nharm, int year); 
int corgeo_(real *sla, real *slo, real *rh, real *dla, real *dlo, real *cla, real *clo, real *pmi);
int geocor_(real *sla, real *slo, real *rh, real *dla, real *dlo, real *cla, real *clo, real *pmi);

#ifdef __cplusplus
}
#endif

#endif
