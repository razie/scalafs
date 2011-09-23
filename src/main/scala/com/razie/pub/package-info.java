/**
 * Razie's public code
 * 
 * This is my playground. Lots of basic code that can be reused and doesn't really have any
 * marketable smarts, so why not make it public - I will, at the very least, get some useful
 * comments.
 * 
 * This is how we evolve: a step at a time, enriching borrowed ideas and feedback. The Internet
 * speeds it all up. If you don't care to even comment, maybe there's some new ideas you can pickup.
 * 
 * I generally like to play around with fuzzy concepts and see where they take me. I could, of
 * course, sit down, smoke a lot (later edit: I stopped smoking altogether - feel great) and just
 * think abstract concepts, but often it is more interesting to just start banging some light
 * prototypes, after a few ideas and/or beers. English is my thrid language - code is first and
 * romanian second :) At the very least, I end up with a model and the ideas are documented and
 * crystallised that way.
 * 
 * When adding features to base/common code, it generally becomes too coupled. Such as a plain
 * action supporting http thus requiring authentication services...which also know about assets
 * which are drawables etc :)
 * 
 * However, the packages here provide functionality we're most likely to need most of the time, so
 * they can be bundled together.
 * 
 * 
 * <h1>Quality</h1>
 * 
 * One good thing from sharing this code is that I have to make it good enough. Documented,
 * tested...and clean. Any contributors will have to stick the same quality requirements. Of course
 * you'll find hacks here and there, but I'll keep them to a minimum.
 * 
 * 
 * <h1>Lightweight distributed application support</h1>
 * 
 * The main purpose of this code here was to create a framework for developing some smart
 * distributed applications. I needed it to be very lightweight (run the entire server inside a
 * junit test case) and simplify development of future smarts. I generally focus on simple client
 * code rather than simple APIs - the two are not always the same thing.
 * 
 * 
 * <h1>Some principles</h1>
 * 
 * Here are some of my fundamental programming principles.
 * 
 * <h2>Embedded presentation</h2>
 * 
 * All artifacts end up being used by some user. In this framework, presentation is embedded and as
 * generic as possible. The tough part is getting the presentation to be as independent as possible
 * so as not to have to rewrite this for every "platform".
 * 
 * <h2>Performance</h2>
 * 
 * Response time is paramount and this is reflected throughout. All code tries to reply to the user
 * as fast as possible. You can see this from the streamable drawables to the background
 * Initialisation of irrelevant services.
 * 
 * The idea behind the entire comm package is a future optimization of sequences of calls via new
 * protocols like SCTP or similar.
 * 
 * Note that this performance requirement does not conflict with the use of Java: the code must
 * respond to the user in a timely fashion, but does not have to be the most optimal code from an
 * assembly language perspective. This means that using patterns etc to make the code easier to
 * maintain/flex is more important than using the most efficient constructs all the time.
 * 
 * <h2>Remote management</h2>
 * 
 * READ http://blog.homecloud.ca/remote-management
 * 
 * All software must be able to be remotely controlled, best option is http (upnp and similar work
 * as well). This is to say that all important internal functionality must be presented via a simple
 * telnet/http interface, for others to automate. No component is limited to just user interaction.
 * 
 * Just think how easy it would be to automate playing a batch of files from a remote server, if
 * only Windows Media Player had a very simple interface for play/pause/etc...
 * 
 * This is an extension of the unix command lines, where you could easily script higher-level
 * functionality from available commands.
 * 
 * <h2>Unique naming/addressing</h2>
 * 
 * Each and every object, attribute, concept that is worth reaching by someone should be reached via
 * a unified API and have a unique ID/handle of sorts. Of course we're talking URL/URI/xpath/XCAP
 * stuff here...I don't want to have to say "the current user's home
 * directory" but "/user[@name=$env.USER]/@homeDir", which is already usable...
 * 
 * 
 * <h1>Main concepts</h1>
 * 
 * Let's see if we can identify the main concepts used be everyone, everywhere...that'd be fun!
 * Besides, if we can settle those in a common format, aliens would surely take over earth, wouldn't
 * they?
 * 
 * <h2>Action: ActionToInvoke.java</h2>
 * 
 * Well, we got: object method, remote procedures, URLs, but these are just mechanisms for
 * transmitting actions and sometimes communication.
 * 
 * So what's an action? In this view, it is a (possibly parameterized) uniquely identifiable piece
 * of functionality or data that can be requested of/from some software. It is part of the
 * advertised "interface" of the something... I suck at wording definitions like this...
 * 
 * Actions can be invoked via command lines, web links, web forms, menus and other action views.
 * They can obviously be automated and organized into other, higher-level actions.
 * 
 * At this level, invoking the actions <b>must be abstracted from the implementation technology</b>.
 * This is the one defining aspect of what I call "action" as a main application-building concept.
 * Thus, Java class methods need not apply, but http://amazon.com/buy qualifies.
 * 
 * Why are actions important? Well, they connect pieces of functionality! Duh? Look around at the
 * proliferation of CORBA, RPC, then WS etc...everyone needs to invoke some piece of remote
 * functionality. What's special about my view is that main functionality of any piece of software
 * or application MUST be presented as actions via URLs. Absolutely, MUST!
 * 
 * It is imperative then that actions be easy to code in clients: client languages, scripts, even
 * tools.
 * 
 * <h2>Properties: AttrAccess.java</h2>
 * 
 * Well, If Eve was "virgin", that was maybe her n-th property: female, name, approximate age etc
 * being other. Everything has properties. In particular, every object has properties. Period. I'm
 * not sure it is worth distinguishing between Java class members and a person's properties, but
 * here I mean the later.
 * 
 * Properties fly through all kinds of formats, including URLs, xml and JSON. Are persisted as XML,
 * text, table columns or file names...They can be accessed from many different languages and
 * environments and be translated into all kinds of other representations.
 * 
 * The best representation of this concept is NVP Name-Value Pairs, i.e. a Map<String,Object> for
 * javaistas. Wether the values are NVPs themselves or complex structured objects is another
 * discussion and it really only matters in the context of how you'll handle them.
 * 
 * 
 * @version $Id: package-info.java,v 1.1 2007-10-02 11:54:36 razvanc Exp $
 */
package com.razie.pub;

