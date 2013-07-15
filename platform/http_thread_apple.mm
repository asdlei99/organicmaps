#import "http_thread_apple.h"

#include "http_thread_callback.hpp"
#include "platform.hpp"

#include "../base/logging.hpp"
#include "../base/macros.hpp"

#ifdef OMIM_OS_IPHONE
  #include "../iphone/Maps/Classes/MapsAppDelegate.h"
#endif

#define TIMEOUT_IN_SECONDS 15.0

@implementation HttpThread

- (void) dealloc
{
  LOG(LDEBUG, ("ID:", [self hash], "Connection is destroyed"));
  [m_connection cancel];
  [m_connection release];
#ifdef OMIM_OS_IPHONE
  [[MapsAppDelegate theApp] enableStandby];
  [[MapsAppDelegate theApp] disableDownloadIndicator];
#endif
  [super dealloc];
}

- (void) cancel
{
  [m_connection cancel];
}

- (id) initWith:(string const &)url callback:(downloader::IHttpThreadCallback &)cb begRange:(int64_t)beg
       endRange:(int64_t)end expectedSize:(int64_t)size postBody:(string const &)pb
{
  self = [super init];

	m_callback = &cb;
	m_begRange = beg;
	m_endRange = end;
	m_downloadedBytes = 0;
	m_expectedSize = size;

	NSMutableURLRequest * request = [NSMutableURLRequest requestWithURL:
			[NSURL URLWithString:[NSString stringWithUTF8String:url.c_str()]]
			cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:TIMEOUT_IN_SECONDS];

	// use Range header only if we don't download whole file from start
	if (!(beg == 0 && end < 0))
	{
		NSString * val;
		if (end > 0)
		{
			LOG(LDEBUG, (url, "downloading range [", beg, ",", end, "]"));
			val = [[NSString alloc] initWithFormat: @"bytes=%qi-%qi", beg, end];
		}
		else
		{
			LOG(LDEBUG, (url, "resuming download from position", beg));
			val = [[NSString alloc] initWithFormat: @"bytes=%qi-", beg];
		}
		[request addValue:val forHTTPHeaderField:@"Range"];
		[val release];
	}

	if (!pb.empty())
	{
		NSData * postData = [NSData dataWithBytes:pb.data() length:pb.size()];
		[request setHTTPBody:postData];
		[request setHTTPMethod:@"POST"];
		[request addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
	}
	// set user-agent with unique client id only for mapswithme requests
	if (url.find("mapswithme.com") != string::npos)
	{
		static string const uid = GetPlatform().UniqueClientId();
		[request addValue:[NSString stringWithUTF8String: uid.c_str()] forHTTPHeaderField:@"User-Agent"];
	}

#ifdef OMIM_OS_IPHONE
  [[MapsAppDelegate theApp] disableStandby];
  [[MapsAppDelegate theApp] enableDownloadIndicator];
#endif

	// create the connection with the request and start loading the data
	m_connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];

  if (m_connection == 0)
  {
    LOG(LERROR, ("Can't create connection for", url));
    [self release];
    return nil;
  }
  else
    LOG(LDEBUG, ("ID:", [self hash], "Starting connection to", url));

  return self;
}

/// We cancel and don't support any redirects to avoid data corruption
-(NSURLRequest *)connection:(NSURLConnection *)connection
            willSendRequest:(NSURLRequest *)request
           redirectResponse:(NSURLResponse *)redirectResponse
{
  if (!redirectResponse)
  {
    // Special case, system just normalizes request, it's not a real redirect
    return request;
  }
  // In all other cases we are cancelling redirects
  LOG(LWARNING, ("Canceling because of redirect from", [[[redirectResponse URL] absoluteString] UTF8String],
      "to", [[[request URL] absoluteString] UTF8String]));
  [connection cancel];
  m_callback->OnFinish(-3, m_begRange, m_endRange);
  return nil;
}

/// @return -1 if can't decode
+ (int64_t) getContentRange:(NSDictionary *)httpHeader
{
	NSString * cr = [httpHeader valueForKey:@"Content-Range"];
	if (cr)
	{
		NSArray * arr = [cr componentsSeparatedByString:@"/"];
		if (arr && [arr count])
			return [(NSString *)[arr objectAtIndex:[arr count] - 1] longLongValue];
	}
	return -1;
}

- (void) connection: (NSURLConnection *)connection didReceiveResponse: (NSURLResponse *)response
{
  UNUSED_VALUE(connection);
	// This method is called when the server has determined that it
	// has enough information to create the NSURLResponse.

  // check if this is OK (not a 404 or the like)
  if ([response isKindOfClass:[NSHTTPURLResponse class]])
  {
    NSInteger const statusCode = [(NSHTTPURLResponse *)response statusCode];
    LOG(LDEBUG, ("Got response with status code", statusCode));
    if (statusCode < 200 || statusCode > 299)
    {
      LOG(LWARNING, ("Received HTTP error, canceling download", statusCode));
      [m_connection cancel];
      m_callback->OnFinish(statusCode, m_begRange, m_endRange);
      return;
    }
    else if (m_expectedSize > 0)
    {
      // get full file expected size from Content-Range header
      int64_t sizeOnServer = [HttpThread getContentRange:[(NSHTTPURLResponse *)response allHeaderFields]];
      // if it's absent, use Content-Length instead
      if (sizeOnServer < 0)
        sizeOnServer = [response expectedContentLength];
      // We should always check returned size, even if it's invalid (-1)
      if (m_expectedSize != sizeOnServer)
      {

        LOG(LWARNING, ("Canceling download - server replied with invalid size",
                       sizeOnServer, "!=", m_expectedSize));
        [m_connection cancel];
        m_callback->OnFinish(-2, m_begRange, m_endRange);
        return;
      }
      // @TODO Else display content to user - router is redirecting us somewhere
    }
  }
  else
  { // in theory, we should never be here
    LOG(LWARNING, ("Invalid non-http response, aborting request"));
    [m_connection cancel];
    m_callback->OnFinish(-1, m_begRange, m_endRange);
  }
}

- (void) connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
  UNUSED_VALUE(connection);
  int64_t const length = [data length];
  m_downloadedBytes += length;
  m_callback->OnWrite(m_begRange + m_downloadedBytes - length, [data bytes], length);
}

- (void) connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
  UNUSED_VALUE(connection);
  LOG(LWARNING, ("Connection failed", [[error localizedDescription] cStringUsingEncoding:NSUTF8StringEncoding]));
  m_callback->OnFinish([error code], m_begRange, m_endRange);
}

- (void) connectionDidFinishLoading:(NSURLConnection *)connection
{
  UNUSED_VALUE(connection);
  m_callback->OnFinish(200, m_begRange, m_endRange);
}

@end

///////////////////////////////////////////////////////////////////////////////////////
namespace downloader
{
HttpThread * CreateNativeHttpThread(string const & url,
                                    downloader::IHttpThreadCallback & cb,
                                    int64_t beg,
                                    int64_t end,
                                    int64_t size,
                                    string const & pb)
{
  return [[HttpThread alloc] initWith:url callback:cb begRange:beg endRange:end expectedSize:size postBody:pb];
}

void DeleteNativeHttpThread(HttpThread * request)
{
  [request cancel];
  [request release];
}

} // namespace downloader
