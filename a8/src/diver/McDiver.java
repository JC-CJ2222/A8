package diver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import graph.Node;
import graph.NodeStatus;
import graph.ScramState;
import graph.SeekState;
import graph.SewerDiver;

public class McDiver extends SewerDiver {

    /** Find the ring in as few steps as possible. Once you get there, <br>
     * you must return from this function in order to pick<br>
     * it up. If you continue to move after finding the ring rather <br>
     * than returning, it will not count.<br>
     * If you return from this function while not standing on top of the ring, <br>
     * it will count as a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the ring in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the ring at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * currentLocation(), neighbors(), and distanceToRing() in state.<br>
     * You know you are standing on the ring when distanceToRing() is 0.
     *
     * Use function moveTo(long id) in state to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the ring, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void seek(SeekState state) {
        // TODO : Look for the ring and return.
        // DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
        // Instead, write your method (it may be recursive) elsewhere, with a
        // good specification, and call it from this one.
        //
        // Working this way provides you with flexibility. For example, write
        // one basic method, which always works. Then, make a method that is a
        // copy of the first one and try to optimize in that second one.
        // If you don't succeed, you can always use the first one.
        //
        // Use this same process on the second method, scram.
        HashSet<Long> set= new HashSet<>();
        dfs2(state, set);

    }

    /** A method to move the McDiver to the ring. A depth-first search is implemented but it doesn't
     * minimize the step it takes to move out. */
    public void dfs(SeekState state, HashSet<Long> set) {
        if (state.distanceToRing() == 0) { return; }
        long currLoc= state.currentLocation();
        set.add(currLoc);
        for (NodeStatus n : state.neighbors()) {
            if (!set.contains(n.getId())) {
                state.moveTo(n.getId());
                dfs(state, set);
                if (state.distanceToRing() == 0) { return; }
                state.moveTo(currLoc);
            }
        }
    }

    /** A method to move the McDiver to the ring. A depth-first search is implemented but it is
     * optimized by visiting neighboring nodes that is the closest to the exit. */
    public void dfs2(SeekState state, HashSet<Long> set) {
        if (state.distanceToRing() == 0) { return; }
        long currLoc= state.currentLocation();
        set.add(currLoc);

        Heap<NodeStatus> hs= new Heap<>(true);

        for (NodeStatus n : state.neighbors()) {
            if (!set.contains(n.getId())) {
                hs.insert(n, n.getDistanceToRing());
            }
        }
        while (hs.size != 0) {
            NodeStatus ns= hs.poll();
            long nodeID= ns.getId();
            state.moveTo(nodeID);
            set.add(nodeID);
            dfs2(state, set);
            if (state.distanceToRing() == 0) { return; }
            state.moveTo(currLoc);
        }

    }

    /** Scram --get out of the sewer system before the steps are all used, trying to <br>
     * collect as many coins as possible along the way. McDiver must ALWAYS <br>
     * get out before the steps are all used, and this should be prioritized above<br>
     * collecting coins.
     *
     * You now have access to the entire underlying graph, which can be accessed<br>
     * through ScramState. currentNode() and exit() return Node objects<br>
     * of interest, and allNodes() returns a collection of all nodes on the graph.
     *
     * You have to get out of the sewer system in the number of steps given by<br>
     * stepsToGo(); for each move along an edge, this number is <br>
     * decremented by the weight of the edge taken.
     *
     * Use moveTo(n) to move to a node n that is adjacent to the current node.<br>
     * When n is moved-to, coins on node n are automatically picked up.
     *
     * You must return from this function while standing at the exit. Failing <br>
     * to do so before steps run out or returning from the wrong node will be<br>
     * considered a failed run.
     *
     * Initially, there are enough steps to get from the starting point to the<br>
     * exit using the shortest path, although this will not collect many coins.<br>
     * For this reason, a good starting solution is to use the shortest path to<br>
     * the exit. */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.

//        escape(state);
        collectCoins(state);
    }

    /** A method to move McDiver out of the sewer by taking the shortest path. The method guarantees
     * to not run out of step but doesn't optimize coins collected */
    public void escape(ScramState state) {
        List<Node> nodes= A7.dijkstra(state.currentNode(), state.exit());
        for (Node n : nodes) {
            if (n != state.currentNode()) {
//            if (state.allNodes().contains(n)) {
                state.moveTo(n);
            }
        }
    }

    /** A method to move McDiver out of the sewer within the steps allowed, coins collected along
     * the way are optimized. */
    public void collectCoins(ScramState state) {
        Node currNode= state.currentNode();
        if (currNode == state.exit()) { return; }

        ArrayList<Node> coinNodes= new ArrayList<>();
        Heap<Node> path= new Heap<>(true);//

        for (Node n : state.allNodes()) {
            int coinValue= n.getTile().coins();
            if (coinValue > 0) {
                coinNodes.add(n);
                List<Node> coinPath= A7.dijkstra(currNode, n);
                path.insert(n, A7.sumOfPath(coinPath));
            }
        }

        Node n= path.poll();

        if (coinNodes.size() > 0) {

            List<Node> coinPath= A7.dijkstra(currNode, n);

            List<Node> pathBack= A7.dijkstra(n, state.exit());

            if (A7.sumOfPath(pathBack) + A7.sumOfPath(coinPath) <= state.stepsToGo()) {
                for (Node nextNode : coinPath) {
                    if (nextNode != currNode) {
                        state.moveTo(nextNode);
                    }
                }
                collectCoins(state);

            }
            pathBack(state);

        }
        pathBack(state);

    }

    /** A method to move McDiver out of the sewer from the node he is standing on right now */
    public void pathBack(ScramState state) {
        Node currNode= state.currentNode();
        List<Node> pathBack= A7.dijkstra(currNode, state.exit());
        for (Node ns : pathBack) {
            if (ns != currNode) {
                state.moveTo(ns);
            }
        }
    }

}
