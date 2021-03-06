package datadog.trace.instrumentation.ratpack;

import static datadog.trace.agent.tooling.ByteBuddyElementMatchers.safeHasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import com.google.auto.service.AutoService;
import datadog.trace.agent.tooling.Instrumenter;
import datadog.trace.instrumentation.ratpack.impl.RatpackHttpClientAdvice;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(Instrumenter.class)
public final class RatpackHttpClientInstrumentation extends Instrumenter.Default {

  public static final TypeDescription.ForLoadedType URI_TYPE_DESCRIPTION =
      new TypeDescription.ForLoadedType(URI.class);

  public RatpackHttpClientInstrumentation() {
    super(RatpackInstrumentation.EXEC_NAME);
  }

  @Override
  protected boolean defaultEnabled() {
    // FIXME: Injecting ContextualScopeManager is probably a bug. Verify and check all ratpack
    // helpers before enabling.
    return false;
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return not(isInterface()).and(safeHasSuperType(named("ratpack.http.client.HttpClient")));
  }

  @Override
  public String[] helperClassNames() {
    return new String[] {
      // http helpers
      "datadog.trace.instrumentation.ratpack.impl.RatpackHttpClientAdvice$RatpackHttpClientRequestAdvice",
      "datadog.trace.instrumentation.ratpack.impl.RatpackHttpClientAdvice$RatpackHttpClientRequestStreamAdvice",
      "datadog.trace.instrumentation.ratpack.impl.RatpackHttpClientAdvice$RatpackHttpGetAdvice",
      "datadog.trace.instrumentation.ratpack.impl.RatpackHttpClientAdvice$RequestAction",
      "datadog.trace.instrumentation.ratpack.impl.RatpackHttpClientAdvice$ResponseAction",
      "datadog.trace.instrumentation.ratpack.impl.RatpackHttpClientAdvice$StreamedResponseAction",
      "datadog.trace.instrumentation.ratpack.impl.RequestSpecInjectAdapter",
      "datadog.trace.instrumentation.ratpack.impl.WrappedRequestSpec",
    };
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    final Map<ElementMatcher<? super MethodDescription>, String> transformers = new HashMap<>();
    transformers.put(
        named("request")
            .and(
                takesArguments(
                    URI_TYPE_DESCRIPTION, RatpackInstrumentation.ACTION_TYPE_DESCRIPTION)),
        RatpackHttpClientAdvice.RatpackHttpClientRequestAdvice.class.getName());
    transformers.put(
        named("requestStream")
            .and(
                takesArguments(
                    URI_TYPE_DESCRIPTION, RatpackInstrumentation.ACTION_TYPE_DESCRIPTION)),
        RatpackHttpClientAdvice.RatpackHttpClientRequestStreamAdvice.class.getName());
    transformers.put(
        named("get")
            .and(
                takesArguments(
                    URI_TYPE_DESCRIPTION, RatpackInstrumentation.ACTION_TYPE_DESCRIPTION)),
        RatpackHttpClientAdvice.RatpackHttpGetAdvice.class.getName());
    return transformers;
  }
}
