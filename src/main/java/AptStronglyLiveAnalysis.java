
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.throwable.SerializationException;

public final class AptStronglyLiveAnalysis {

  private static String urlAptBuildInstructions =
      "https://github.com/CvO-Theory/apt/blob/master/doc/obtaining.md#build-your-own-version";

  private static String envVarPathCoVTheoryAptJar = "MY_CoV_THEORY_APT_JAR";

  private static String templateAptStrongLivenessAnalysis =
      "java -jar %s examine_pn pnml:%s";
  // "examine_pn" includes several tests, as well as "strongly_live"
  // -although, while the option "examine_pn" is more comprehensive, the
  // option "strongly_live" is able to return a witness transition and arc
  // why the Petri Net model is not strongly live. Probably an option could
  // be to call the option "strongly_live" after calling "examine_pn" if this
  // one returns that the Petri Net model is not strongly live; or another
  // alternative is using the CoV-Theory's APT-JSON.jar instead of theirs
  // APT.jar, for the former is able to run multiple tests at the same
  // program "java -jar apt-json.jar" fork/exec invocation.

  private AptStronglyLiveAnalysis() {
     // this class is a utility class (so instance constructor is private)
  }

  protected static
      String getCmdLineToExecAptStrongLivenessAnalysis(String pnmlFileName) {
    String aptJar = System.getenv(envVarPathCoVTheoryAptJar);
    if (aptJar == null) {
      System.err.println(
           String.format("You need to set the environment variable '%s' to "
                         + "the [full] pathname where APT.JAR is located.\n"
                         + "For installation of this external JAR, see:\n%s",
                         envVarPathCoVTheoryAptJar, urlAptBuildInstructions
                        )
      );
      return null;
    }

    return String.format(templateAptStrongLivenessAnalysis,
                         aptJar, pnmlFileName);
  }


  static void livenessAnalysisApt(final NetSystem petriNet) {
    // Liveness analysis via APT

    try {
      String petriNetString =
          PNMLSerializer.serializePetriNet(petriNet, PNMLSerializer.LOLA);

      File temp = File.createTempFile("alert-model-petri-net-", ".pnml");
      BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
      bw.write(petriNetString);
      bw.close();
      // System.out.println("Temp file : " + temp.getAbsolutePath());

      String aptAnalysisCmd =
          getCmdLineToExecAptStrongLivenessAnalysis(temp.getAbsolutePath());
      if (aptAnalysisCmd == null) {
        return;
      }

      System.out.println("Executing APT strongly_live behavioral analysis:\n"
                         + "    " + aptAnalysisCmd);

      // TODO: use ProcessBuilder instead of Runtime...exec()
      Process aptAnalysisProcess = Runtime.getRuntime().exec(aptAnalysisCmd);

      // read from the called program's standard output stream
      BufferedReader inStream =
          new BufferedReader(
              new InputStreamReader(aptAnalysisProcess.getInputStream())
          );

      String inputLine = null;
      while ((inputLine = inStream.readLine()) != null) {
        System.out.println(inputLine);
      }

      aptAnalysisProcess.waitFor();

    } catch (InterruptedException exc) {
      exc.printStackTrace();
    } catch (IOException exc) {
      exc.printStackTrace();
    } catch (SerializationException exc) {
      exc.printStackTrace();
    }

    System.out.println();
  }

}
