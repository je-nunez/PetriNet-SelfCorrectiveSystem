
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;

public final class SelfCorrectiveSystem {

  private static String defaultPetriNetPNMLFile =
                        "src/main/resources/sample_petri_net.pnml";

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

      if (src instanceof Place
             && dst instanceof Transition) {
        System.out.println(
             String.format("Alert: '%s' -> '%s'", src, dst)
        );
      }
    }
  }

  protected void dumpPetriNetCorrectiveActions() {

    System.out.println("\nCorrective Actions in the system:");

    for (Flow arc: petriNet.getFlow()) {
      Node src = arc.getSource();
      Node dst = arc.getTarget();
      if (src instanceof Transition
             && dst instanceof Place) {
        System.out.println(
             String.format("Corrective-Action: '%s' -> '%s'", src, dst)
        );
      }
    }
  }

  protected void dumpAlertsWithoutCorrectiveActions() {

    System.out.println("\nAlerts without Corrective Actions:");

    // The end of an alert (arc in the Petri Net) is a transition in the
    // Petri Net. A corrective action is an arc starting in a transition
    // and ending in a place in the Petri Net. So an alert without
    // corrective action is, in the Petri Net, a transition which doesn't
    // have an outgoing arc from it to any place. (This method doesn't
    // not trigger an execution in the Petri Net. Nor it analyzes how fast
    // the outgoing arc consumes the amount in the transition.)

    Set<Transition> transitionsWithoutOutgoingArcs =
                                petriNet.getTransitions();
    Set<Transition> transitionsWithIncomingArcs = new HashSet<Transition>();

    for (Flow arc: petriNet.getFlow()) {
      Node src = arc.getSource();
      Node dst = arc.getTarget();
      if (src instanceof Transition
          && dst instanceof Place) {
        // "src" has a corrective action
        transitionsWithoutOutgoingArcs.remove(src);
      } else if (src instanceof Place
                 && dst instanceof Transition) {
        // "dst" has an incoming alert
        transitionsWithIncomingArcs.add((Transition) dst);
      } else {
        System.err.println(
             String.format("Unknown extremes of arc: '%s'", arc)
        );
      }
    }

    transitionsWithoutOutgoingArcs.retainAll(transitionsWithIncomingArcs);

    for (Transition transition: transitionsWithoutOutgoingArcs) {
      System.out.println(
           String.format("Alert without corrective-action: '%s'",
                         transition)
      );
    }

  }

  public static void main(final String[] args) throws Exception {

    SelfCorrectiveSystem scSystem = new SelfCorrectiveSystem();

    String fnamePNML = defaultPetriNetPNMLFile;
    // try to get this PNML from the command-line arguments, if given,
    // otherwise try to use the default demo PNML file.
    if (args.length >= 1) {
      fnamePNML = args[0];
    }

    scSystem.loadPetriNet(fnamePNML);
    scSystem.dumpPetriNetAlerts();
    scSystem.dumpPetriNetCorrectiveActions();

    scSystem.dumpAlertsWithoutCorrectiveActions();
  }

}
