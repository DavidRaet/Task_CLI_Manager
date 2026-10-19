import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
class AcceptanceCheck {
 public static void main(String[] args) {
  var listener = new SummaryGeneratingListener();
  LauncherFactory.create().execute(LauncherDiscoveryRequestBuilder.request().selectors(selectClass("test.java.CliAcceptanceTest")).build(), listener);
  listener.getSummary().printTo(new java.io.PrintWriter(System.out, true));
  listener.getSummary().printFailuresTo(new java.io.PrintWriter(System.out, true));
 }
}
