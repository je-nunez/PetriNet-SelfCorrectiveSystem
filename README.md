# Brief

Modelling a self-corrective system using PetriNets on the jBPT Java library

# WIP

This project is a *work in progress*. The implementation is *incomplete* and
subject to change. The documentation can be inaccurate.

# Notes:

A self-corrective system is related to Systems Engineering, for, ideally, a
system in this discipline should have corrective actions, either
interior (self) or exterior corrective actions. Thus, for example, the more
self-corrective a system is, then the more autonomous it can be (ie., the
former is a necessary condition for the latter). Besides, Petri-Nets are a
well-known model to represent Systems Engineering (see, e.g.,
[ISO/IEC 15909-1:2004 Systems and software engineering -- High-level Petri nets -- Part 1: Concepts, definitions and graphical notation](https://www.iso.org/standard/38225.html)
and
[ISO/IEC 15909-2:2011 Systems and software engineering -- High-level Petri nets -- Part 2: Transfer format](https://www.iso.org/standard/43538.html).)

# Demo

This program reads a Petri Net system from a XML file in [PNML format](https://en.wikipedia.org/wiki/Petri_Net_Markup_Language).

Example execution through Maven:

      $ mvn
      [...]
      Alerts in the system:
      Alert: 'This is my normal state #1' -> 'handling_my_alert_2'
      Alert: 'This is my normal state #1' -> 'handling_my_alert_1'
      Alert: 'This is my normal state #1' -> 'handling_my_alert_0'
            
      Corrective Actions in the system:
      Corrective-Action: 'handling_my_alert_1' -> 'This is my corrected state #2'
      Corrective-Action: 'handling_my_alert_0' -> 'This is my corrected state #2'
            
      Alerts without Corrective Actions:
      Alert without corrective-action: 'handling_my_alert_2'


