
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.throwable.SerializationException;
import org.jbpt.utils.IOUtils;

import org.w3c.dom.Document;

public final class SelfCorrectiveSystem {

  private static String defaultPetriNetPNMLFile =
    "src/main/resources/sample_petri_net_resources_dimensions.pnml";
  // "src/main/resources/sample_petri_net.pnml";
  // "src/main/resources/sample_petri_net_resources_dimensions.pnml";

  protected NetSystem petriNet = null;
  protected
      HashMap<String, ImmutablePair<String, String>> correctiveActions = null;

  protected void loadPetriNet(String fileNamePNML) throws IOException {
    // We need to have our own PNMLSerializer in order to read the
    // "toolspecific" element under a flow or arc (containing, for us, the
    // corrective command to execute for the corrective-action associated to
    // that flow or arc).
    // See:
    // https://github.com/jbpt/codebase/blob/master/jbpt-petri/src/main/java/org/jbpt/petri/io/PNMLSerializer.java#L150
    MyPNMLReaderToolSpecific ser = new MyPNMLReaderToolSpecific();
    petriNet = ser.parse(fileNamePNML);
    correctiveActions = ser.getCorrectiveActions();
  }

  protected void dumpPetriNetAlerts() {

    System.out.println("\nAlerts in the system:");

    for (Flow arc: petriNet.getFlow()) {
      Node src = arc.getSource();
      Node dst = arc.getTarget();

      if (src instanceof Place
             && dst instanceof Transition) {
        System.out.println(
             String.format(
                   "Alert: from '%s' -- to --> '%s'\n"
                   + "             Note: '%s'\n",
                   src, dst, dst.getLabel()
             )
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
        String command = "not-set";
        String args = "";
        ImmutablePair<String, String> correctAction =
             correctiveActions.get(arc.getId());
        if (correctAction != null) {
          command = correctAction.getLeft();
          args = correctAction.getRight();
        }
        System.out.println(
             String.format(
                   "Corrective-Action: from '%s' -- to --> '%s'\n"
                   + "             run : command: '%s' args: '%s'",
                   src, dst,
                   command, args)
        );
      }
    }
  }

  protected void dumpAlertsWithoutCorrectiveActions() {

    System.out.println("\nAlerts without Corrective Actions"
                       + " (possible page out?):");

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

    if (transitionsWithoutOutgoingArcs.size() > 0) {
      System.out.println();
    }
  }

  protected void printPetriNet() {

    try {
      Document doc = PNMLSerializer.serialize(petriNet, PNMLSerializer.LOLA);

      DOMSource domSource = new DOMSource(doc);

      StreamResult streamResult = new StreamResult(System.out);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer serializer;
      serializer = tf.newTransformer();
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      serializer.transform(domSource, streamResult);
    } catch (SerializationException
             | TransformerException e) {
      e.printStackTrace();
    }
  }

  protected void renderPetriNetPNG(String directory, String pngFname) {
    // NOTE: JBPT uses the `dot` program from GraphViz to render the Petri
    // net model (and other models as well, like graphs, etc), and this
    // progrem and/or package needs to be installed locally
    // (see: https://graphviz.gitlab.io/download/)

    // we need to clone the original model, because it has long strings and
    // we need to shorten those strings so that the model can be readable in
    // its PNG rendering
    NetSystem clone = (NetSystem) petriNet.clone();
    System.out.println("Meaning of the PNG rendering:");
    int idx = 1;
    for (Node node: clone.getPlaces()) {
      System.out.println(
          String.format("Idx 'p%d' means place '%s'",
                        idx, node.getLabel())
      );
      node.setName(String.format("p%d", idx));
      node.setLabel(String.format("p%d", idx));
      // node.setDescription("");
      idx++;
    }
    for (Transition transition: clone.getTransitions()) {
      System.out.println(
          String.format("Idx 't%d' means transition '%s'",
                        idx, transition.getLabel())
      );
      transition.setName(String.format("t%d", idx));
      transition.setLabel(String.format("t%d", idx));
      // transition.setDescription("");
      idx++;
    }

    try {
      IOUtils.invokeDOT(directory, pngFname, clone.toDOT());
      System.out.println(
          String.format("Rendered model to PNG: %s%s%s",
                        directory, File.separator, pngFname)
      );
    } catch (IOException e) {
      System.err.println(
          String.format("Failed to render model to PNG: %s%s%s",
                        directory, File.separator, pngFname)
      );
      e.printStackTrace();
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
    scSystem.printPetriNet();
    scSystem.dumpPetriNetAlerts();
    scSystem.dumpPetriNetCorrectiveActions();

    scSystem.dumpAlertsWithoutCorrectiveActions();
    scSystem.renderPetriNetPNG(
        System.getProperty("java.io.tmpdir"),
        "rendered_model.png"
    );
  }

}
