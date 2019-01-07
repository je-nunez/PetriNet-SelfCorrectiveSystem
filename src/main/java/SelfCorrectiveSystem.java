
import java.io.IOException;

import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;

public final class SelfCorrectiveSystem {

  private static String defaultPetriNetPNMLFile = "src/main/resources/sample_petri_net.pnml";

  protected NetSystem petriNet = null;

  protected void loadPetriNet(String fileNamePNML) throws IOException {
    PNMLSerializer ser = new PNMLSerializer();
    petriNet = ser.parse(fileNamePNML);
  }

  protected void dumpPetriNetAlerts() {

    System.out.println("\nAlerts in the system:");

    for (Flow arc: petriNet.getFlow()) {
         Node src = arc.getSource();
         Node dst = arc.getTarget();

         if (src instanceof Node &&
             dst instanceof Transition) {
              System.out.println("Alert: " + arc);
         }
    }
  }

  protected void dumpPetriNetCorrectiveActions() {

    System.out.println("\nCorrective Actions in the system:");

    for (Flow arc: petriNet.getFlow()) {
         Node src = arc.getSource();
         Node dst = arc.getTarget();
         if (src instanceof Transition &&
             dst instanceof Node) {
              System.out.println("Corrective-Action: " + arc);
         }
    }
  }

  public static void main(final String[] args) throws Exception {

    SelfCorrectiveSystem scSystem = new SelfCorrectiveSystem();

    String fnamePNML;
    // try to get this PNML from the command-line arguments, if given,
    // otherwise try to use the default demo PNML file.
    fnamePNML = (args.length >= 1) ? args[0] : defaultPetriNetPNMLFile;

    scSystem.loadPetriNet(fnamePNML);
    scSystem.dumpPetriNetAlerts();
    scSystem.dumpPetriNetCorrectiveActions();

  }

}
