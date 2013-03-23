
package org.smslib.http;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

public interface IHttpRequestHandler
{
	public Status process(Request request, Response response);
}
