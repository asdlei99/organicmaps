#pragma once
#include "common_defines.hpp"

#ifdef new
#undef new
#endif

#if __cplusplus > 199711L

#include <memory>
using std::weak_ptr;

#else

#include <boost/weak_ptr.hpp>
using boost::weak_ptr;

#endif

#ifdef DEBUG_NEW
#define new DEBUG_NEW
#endif
