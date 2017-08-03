package tracedws

import cinnamon.opentracing.{ TraceGlobal, TraceLocal }
import io.opentracing.propagation.{ Format, TextMap }
import io.opentracing.tag.Tags
import io.opentracing.{ Span, SpanContext, Tracer }
import play.api.libs.ws._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Wrap WSClient requests in OpenTracing Spans and transfer context in headers.
 */
class TracedWSClient(ws: WSClient) {
  // get the global tracer set by the actor instrumentation for futures
  // could also get the tracer attached to the actor system
  val tracer: Tracer = TraceGlobal.getTracer

  def get(url: String)(implicit executor: ExecutionContext): Future[WSResponse] = {
    TraceLocal.log(s"Client request: $url")
    val span: Span = startSpanFor(url)
    val request: WSRequest = injectContext(ws.url(url), span)
    // since WS uses AsyncHttpClient internally, this request runs in an AsyncCompletionHandler,
    // so there's no support currently for getting the context to this response Future
    val response: Future[WSResponse] = request.get
    // add a callback to finish the request span --- not ideal and would be instrumented more directly
    response.onComplete(_ => span.finish())
    response
  }

  private def startSpanFor(url: String): Span = {
    val parentSpan: SpanContext = TraceLocal.currentContext
    val spanBuilder: Tracer.SpanBuilder =
      if (parentSpan ne null) tracer.buildSpan(url).asChildOf(parentSpan)
      else tracer.buildSpan(url)
    spanBuilder
      .withTag(Tags.COMPONENT.getKey(), "play.ws")
      .withTag(Tags.SPAN_KIND.getKey(), "play.ws.request")
      .withTag(Tags.HTTP_URL.getKey(), url)
      .withTag(Tags.HTTP_METHOD.getKey(), "GET")
      .start()
  }

  private def injectContext(request: WSRequest, span: Span): WSRequest = {
    val adapter = new TracedWSClient.WSRequestContextAdapter(request)
    tracer.inject(span.context, Format.Builtin.TEXT_MAP, adapter)
    adapter.request
  }
}

object TracedWSClient {
  class WSRequestContextAdapter(var request: WSRequest) extends TextMap {
    override def put(key: String, value: String): Unit = {
      request = request.addHttpHeaders(key -> value)
    }

    // don't need to extract the context --- not implemented
    override def iterator(): java.util.Iterator[java.util.Map.Entry[String, String]] = ???
  }
}
