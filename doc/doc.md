#WiFiDroid
Ryan Lewellen
Android, Fall 2012

A simple application that utilizies the facilities in the `android.net.wifi` namespace to give the user information on
nearby WiFi networks.

##Definition
WiFiDroid will be a simple application for exploring WiFi signals in your vicinity; I'll start simple, with the
classical list of access points with their signal strengths, types, etc. In addition, I'll be exploring other views of
WiFi networks in the area in terms of such things like channel assignments (in which it would be plain to see overlaps
in channels), signal strength over time, and signal strength by location (if possible). Finally, I would like to
explore trilaterating the location of an access point by looking at signal strengths at various locations.

##User Stories
###Steve
Say we've got this guy, Steve. Steve is setting up a WiFi network in his house. However, Steve isn't sure where to
locate his access point for maximal WiFi coverage. Steve gets mad because this simple task seems so difficult. 

Steve smash.

Steve give up and place access point in living room.

If Steve had the WiFiDroid app, he could try placing the router in various places and then measuring the signal
strength in the places that he cares about to determine which position is best. Steve happy!

###Carol
Carol is trying to figure out what channel to place her router on, but for some god-awful reason, her ISP-supplied
router does not show what channels her neighbors are using for their shitty ISP-supplied routers. She wants to avoid
setting her router to occupy the same channel as her neighbors, as she enjoys her high-speed internet with minimal
dropped packets, so she uses the app to figure out the least occupied channel (if she's lucky enough that such a
channel exists).

###Neil
Like Carol, Neil has neighbors who just setup an access point without a single care in the world concerning channel
assignments. Unfortunately, unlike Carol, Neil is not lucky. Neil is always shafted when it comes to luck. Neil's 
neighbors aren't assholes, they just don't know any better. So Neil wants to figure out which neighbor owns which
access point so that he can kindly ask them to change the channel for minimum interference. He uses the app to 
determine where exactly these other access points are by taking signal strength measurements in various places around
his and his neighbors' houses. Neil successfully convinces his neighbors that their access points are angering their
dead relatives and that by simply reassigning channels they can calm their restless spirits. Neil enjoys awesome
internet until the Ghost Busters show up and break all his shit. Neil is unlucky.

##Use Cases
###User views current list of access points
###List of APs automatically refreshes
###User views all available information on AP
###Detail view displays signal strength over time
###User views signal strength of many signals over time
###User view channel assignments

##Domain Classes
AccessPoint             - Characteristics that uniquely identify a particular access point
AccessPointDataPoint    - All time-dependent data that is generated for a given AccessPoint

These will be organized as a map between access points (which are consistant through time) and a list of data points,
each associated with a particular time. Most of the operations in the program will consist of manipulations of this
central dataset.