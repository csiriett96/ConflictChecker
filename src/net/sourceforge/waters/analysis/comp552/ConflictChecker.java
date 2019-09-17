//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.util.*;

import net.sourceforge.waters.model.des.*;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;

import static java.lang.Math.min;


/**
 * <P>A dummy implementation of a conflict checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is nonconflicting.</P>
 *
 * <P>You are welcome to edit this file as much as you like,
 * but please <STRONG>do not change</STRONG> the public interface.
 * Do not change the signature of the two constructors,
 * or of the {@link #run()} or {@link #getCounterExample()} methods.
 * You should expect a single constructor call, followed by several calls
 * to {@link #run()} and {@link #getCounterExample()}, so your code needs
 * to be reentrant.</P>
 *
 * <P><STRONG>WARNING:</STRONG> If you do not comply with these rules, the
 * automatic tester may fail to run your program, resulting in 0 marks for
 * your assignment.</P>
 *
 * @see ModelChecker
 *
 * @author Robi Malik
 */

public class ConflictChecker extends ModelChecker
{

    //#########################################################################
    //# Constructors
    /**
     * Creates a new conflict checker to check whether the given model
     * nonconflicting with respect to the default marking proposition.
     * @param  model      The model to be checked by this conflict checker.
     * @param  desFactory Factory used for trace construction.
     */
    public ConflictChecker(final ProductDESProxy model,
                           final ProductDESProxyFactory desFactory)
    {
        this(model, getDefaultMarkingProposition(model), desFactory);
    }


    /**
     * Creates a new conflict checker to check a particular model.
     * @param  model      The model to be checked by this conflict checker.
     * @param  marking    The proposition event that defines which states
     *                    are marked. Every state has a list of propositions
     *                    attached to it; the conflict checker considers only
     *                    those states as marked that are labelled by
     *                    <CODE>marking</CODE>, i.e., their list of
     *                    propositions must contain this event (exactly the
     *                    same object).
     * @param  desFactory Factory used for trace construction.
     */
    public ConflictChecker(final ProductDESProxy model,
                           final EventProxy marking,
                           final ProductDESProxyFactory desFactory)
    {
        super(model, desFactory);
        mMarking = marking;
    }

    //Class containing the state tuple object
    //A state tuple is has an array of states, the boolean flag for co-reachability, an int size and an index
    //This class also contains setter and getters for relevant information
    class StateTuple{
        private StateProxy[] states;
        private boolean coreachable;
        private int size;
        private int index;

        StateTuple(int size, int i){
            this.states = new StateProxy[size];
            coreachable = false;
            this.size = size;
            this.index = i;
        }

        public int getIndex(){
            return index;
        }

        public void setStates(int index, StateProxy s){
            states[index] = s;
        }

        public StateProxy getStates(int i){
            return states[i];
        }

        public int getSize(){
            return size;
        }

        public boolean getCoreachable(){
            return coreachable;
        }

        public void setCoreachable(boolean c){
            this.coreachable = c;
        }

        //Overrides equals and hashcode methods to use a hashset
        @Override
        public boolean equals(Object q) {
            return Arrays.equals(states, ((StateTuple)q).states);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(states);
        }
    }

    //Class containing the target states object
    //A target state has a target State tuple, a linked list of type source states tuple states
    class TargetStates{
        private LinkedList<SourceStates> sourceStates;
        private StateTuple target;
        public TargetStates(StateTuple t){
            sourceStates = new LinkedList<SourceStates>();
            this.target = t;
        }

        public LinkedList<SourceStates> getSourceStates(){
            return sourceStates;
        }

        public StateTuple getTarget(){
            return target;
        }

        public void setSourceStates(SourceStates s){
            sourceStates.add(s);
        }

        public void setTarget(StateTuple t){
            target = t;
        }
    }

    //Class containing the source states object
    //A source state has a state tuple of the source, the event that takes that results in the target from the source
    class SourceStates{
         private StateTuple source;
         private EventProxy event;
         public SourceStates(StateTuple s, EventProxy e){
            this.source = s;
            this.event = e;
         }

         public StateTuple getSource(){
             return source;
         }

         public EventProxy getEvent(){
             return event;
         }

        public void setEvent(EventProxy e) {
            this.event = e;
        }

        public void setSource(StateTuple s) {
            this.source = s;
        }
    }

    //Global variables that are needed to make counterexamples and check for marking
    HashMap<StateTuple, TargetStates> _targetTransitions;
    HashSet<StateTuple> _stateSpaces;
    ArrayList<Integer> unmarkedAutomaton;


    //#########################################################################
    //# Invocation
    /**
     * Runs this conflict checker.
     * This method starts the model checking process on the model given
     * as parameter to the constructor of this object. On termination,
     * if the result is false, a counterexample can be queried using the
     * {@link #getCounterExample()} method.
     * Presently, this is a dummy implementation that does nothing but always
     * returns <CODE>true</CODE>.
     * @return <CODE>true</CODE> if the model is nonconflicting, or
     *         <CODE>false</CODE> if it is not.
     */
    @Override
    public boolean run()
    {
        boolean nonConflicting = true;
        // First get the model
        final ProductDESProxy model = getModel();

        //Initialise the synchronous state space HashSet, the open deque and the target transitions HashMap
        final int size = model.getAutomata().size();
        _stateSpaces = new HashSet<StateTuple>();
        ArrayDeque<StateTuple> _open = new ArrayDeque<StateTuple>();
        _targetTransitions = new HashMap<>();

        //Get Initial Tuple. Add to State Space and push onto open
        StateTuple initTuple = getInitialState(model);
        _stateSpaces.add(initTuple);
        _open.push(initTuple);

        //Get a list of automata in the model and create variables for next and current.
        List<AutomatonProxy> automata = new ArrayList<AutomatonProxy>(model.getAutomata());
        StateTuple next, current;
        int index = 0;
        //While the queue is not empty continue to process new state tuples
        while(!_open.isEmpty()){
            current = _open.pop();
            //For each event check if there exists a transaction that will change the state tuple
            for(EventProxy event: model.getEvents()){
                next = copyStateTuple(current,index);
                boolean found = false;
                for(int i = 0; i < size; i++){
                    AutomatonProxy aut = automata.get(i);
                    //If the automaton doesn't contain this event in its alphabet keep this state the same
                    if(!aut.getEvents().contains(event)) {
                        next.setStates(i, current.getStates(i));
                    }else{
                        //For each transition in this automaton if there exists on with the same current state and event
                        //setStates the next state tuple to equal the target of this transition
                        for (TransitionProxy trans : aut.getTransitions()) {
                            if (trans.getSource() == current.getStates(i) && trans.getEvent() == event) {
                                next.setStates(i, trans.getTarget());
                                found = true;
                                break;
                            } else {
                                found = false;
                            }
                        }
                        if (!found) {
                            break;
                        }
                    }
                }
                //If a new state tuple is found and it isn't in the state space add to open and the state space
                if(!_stateSpaces.contains(next) && found) {
                    _stateSpaces.add(next);
                    _open.push(next);
                }
                //If a new state tuple is found
                if(found){
                    //If the target is already in the Hashmap add another source to the linked list
                    //else create a new target state and add it to the Hashmap
                    if(_targetTransitions.containsKey(next)){
                        SourceStates source = new SourceStates(current, event);
                        _targetTransitions.get(next).sourceStates.addLast(source);
                    }else{
                        TargetStates t = new TargetStates(next);
                        SourceStates source = new SourceStates(current, event);
                        t.sourceStates.add(source);
                        _targetTransitions.put(next,t);
                    }
                }
            }
            index++;
        }

        //Prints the number of reachable states in the synchronous product
        System.out.print(_stateSpaces.size() + " ");

        //Before checking which state tuples are marked,
        //check which automaton don't contain the marking proposition
        unmarkedAutomaton = checkAccepting(model);

        //Loops through the synchronous state space
        //setting co-reachable to be true for all marked state tuples
        //then adds it to the open queue to be processed
        for(StateTuple stateTuple: _stateSpaces){
            if(markedState(stateTuple)){
                _open.push(stateTuple);
                stateTuple.setCoreachable(true);
            }
        }

        //While the queue is not empty pop the first element
        //Check that it is a target state in the _targetTransitions Hashmap
        //for each source state for this target if it isn't co-reachable
        //mark it as co-reachable and add it to the queue.
        while(!_open.isEmpty()){
            StateTuple q = _open.pop();
            if(_targetTransitions.containsKey(q)) {
                TargetStates targetState = _targetTransitions.get(q);
                    for (SourceStates state : targetState.sourceStates) {
                        if (!state.source.getCoreachable()) {
                            _open.push(state.source);
                            state.source.setCoreachable(true);
                        }
                    }

            }
        }


        int coReachable = 0;

        //For each state tuple in the state space count the number of co-reachable states
        for(StateTuple stateTuple: _stateSpaces){
            if(stateTuple.getCoreachable()){
                coReachable++;
            }
        }

        //If all states are co-reachable then the model is nonconflicting else it is conflicting.
        if(coReachable != _stateSpaces.size()){
            nonConflicting = false;
        }

        // If conflicting try to compute a counterexample
        if(!nonConflicting) {
            mCounterExample = computeCounterExample();
        }

        return nonConflicting;
    }

    //Private method to check for automatons that don't contain the marked proposition
    //Therefore all states in this automaton are marked.
    private ArrayList<Integer> checkAccepting(ProductDESProxy model){
        ArrayList<Integer> automata = new ArrayList<Integer>();
        int autIndex = 0;
        for(AutomatonProxy aut: model.getAutomata()){
            final Collection<EventProxy> events = aut.getEvents();
            if (!events.contains(mMarking)) {
                automata.add(autIndex);
            }
            autIndex++;
        }
        return automata;
    }

    //Private method to determine if a given state tuple is marked
    private boolean markedState(StateTuple stateTuple){
        boolean marked = true;
        for (int i = 0; i < stateTuple.size; i++) {
            if(stateTuple.getStates(i) != null) {
                if (!stateTuple.getStates(i).getPropositions().contains(mMarking)) {
                    //Check if this state is a state that doesn't contain the marked event
                    if (!unmarkedAutomaton.contains(i)) {
                        marked = false;
                    }
                }
            }
        }
        return marked;
    }

    //Private method that gets the initial tuple for a given model
    private StateTuple getInitialState(ProductDESProxy model){
        StateTuple initialTuple = new StateTuple(model.getAutomata().size(),0);
        int i = 0;
        for (final AutomatonProxy aut : model.getAutomata()) {
            for(StateProxy state: aut.getStates()){
                if(state.isInitial()){
                    initialTuple.setStates(i,state);
                    i++;
                    break;
                }
            }
        }
        return initialTuple;
    }

    //Private method that copies a state tuple into a new state tuple
    private StateTuple copyStateTuple(StateTuple s, int index){
        StateTuple next = new StateTuple(s.size,index);
        for(int i =0; i < s.states.length; i++){
            next.states[i] = s.states[i];
        }
        return next;
    }

    //Private method to check if a given state tuple is the initial state tuple
    private boolean initialState(StateTuple q){
        for(int i = 0; i < q.size; i++){
            if(!q.getStates(i).isInitial()){
                return false;
            }
        }
        return true;
    }

    //Private method to get the next target state with the lowest index which
    //therefore should be closet to the initial state
    private SourceStates getNextTransition(TargetStates targetState){
        SourceStates next = targetState.sourceStates.getFirst();
        for(SourceStates sourceStates : targetState.sourceStates){
            if(sourceStates.source.index < next.source.index){
                next = sourceStates;
            }
        }
        return next;
    }

    //#########################################################################
    //# Simple Access Methods
    /**
     * Gets a counterexample if the model was found to be conflicting,
     * representing a conflict error trace. A conflict error trace is a
     * sequence of events that takes the model to a state that is not
     * coreachable. That is, after executing the counterexample, the automata
     * are in a state from where it is no longer possible to reach a state
     * where all automata are marked at the same time.
     * @return A conflict trace object representing the counterexample.
     *         The returned trace is constructed for the input product DES
     *         of this conflict checker and shares its automata and
     *         event objects.
     */
    @Override
    public ConflictCounterExampleProxy getCounterExample()
    {
        // Just return a stored counterexample. This is the recommended way
        // of doing this, because we may no longer be able to use the
        // data structures used by the algorithm once the run() method has
        // finished. The counterexample can be computed by a method similar to
        // computeCounterExample() below or otherwise.
        return mCounterExample;
    }

    /**
     * Computes a counterexample.
     * This method is to be called from {@link #run()} after the model was
     * found to be conflicting, before the BDD factory has been closed. It
     * uses BDD operations to build the counterexample.
     * @return The computed counterexample.
     */
    private ConflictCounterExampleProxy computeCounterExample()
    {
        final ProductDESProxyFactory desFactory = getFactory();
        final ProductDESProxy model = getModel();
        final String modelName = model.getName();
        final String traceName = modelName + "-conflicting";

        final List<EventProxy> eventList = new LinkedList<>();

        //Find the first un-coreachable state and pick it for the trace
        StateTuple q = new StateTuple(model.getAutomata().size(),0);
        for(StateTuple tuple: _stateSpaces){
            if(!tuple.getCoreachable()){
                q = tuple;
                break;
            }
        }

        //Create the trace by finding the closet transition to the initial state
        //and making that the current state until the state is equal to the initial state
        //Add each event to the eventlist
        while(!initialState(q)){
            TargetStates target = _targetTransitions.get(q);
            SourceStates next = getNextTransition(target);
            eventList.add(next.event);
            q = next.source;
        }

        //Reverse the event list so the counterexample is in the correct order
        Collections.reverse(eventList);

        // Note. The conflict kind field of the trace is optional for
        // this assignment---it will not be tested.
        return desFactory.createConflictCounterExampleProxy
                (traceName, model, eventList, ConflictKind.CONFLICT);
    }


    //#########################################################################
    //# Auxiliary Methods
    /**
     * Searches the given model for a proposition event with the default
     * marking name and returns this event.
     * @throws IllegalArgumentException to indicate that the given model
     *         does not contain any proposition with the default marking
     *         name.
     */
    private static EventProxy getDefaultMarkingProposition
    (final ProductDESProxy model)
    {
        for (final EventProxy event : model.getEvents()) {
            if (event.getKind() == EventKind.PROPOSITION &&
                    event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
                return event;
            }
        }
        throw new IllegalArgumentException
                ("ProductDESProxy '" + model.getName() +
                        "' does not contain any proposition called '" +
                        EventDeclProxy.DEFAULT_MARKING_NAME + "'!");
    }


    //#########################################################################
    //# Data Members
    /**
     * The proposition chosen by the user to identify the marked states
     * for conflict checking.
     */
    private final EventProxy mMarking;
    /**
     * The computed counterexample or null if the model is nonblocking.
     */
    private ConflictCounterExampleProxy mCounterExample;

}
