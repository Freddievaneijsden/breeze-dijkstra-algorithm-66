package org.fungover.breeze.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class DijkstraTest {

    Dijkstra<String> dijkstra;
    WeightedGraph<String> graph;
    List<Node<String>> nodes;
    List<Edge<String>> edges;

    @BeforeEach
    void setUp() {
        nodes = List.of(
                new Node<>("A"), new Node<>("B"), new Node<>("C"),
                new Node<>("D"), new Node<>("E"), new Node<>("F")
        );

        edges = List.of(
                new Edge<>(nodes.get(0), nodes.get(2), 2),  //A to C
                new Edge<>(nodes.get(0), nodes.get(1), 5),  //A to B
                new Edge<>(nodes.get(1), nodes.get(2), 1),  //B to C
                new Edge<>(nodes.get(1), nodes.get(3), 4),  //B to D
                new Edge<>(nodes.get(1), nodes.get(4), 2),  //B to E
                new Edge<>(nodes.get(2), nodes.get(4), 7),  //C to E
                new Edge<>(nodes.get(3), nodes.get(4), 6),  //D to E
                new Edge<>(nodes.get(3), nodes.get(5), 3),  //D to F
                new Edge<>(nodes.get(4), nodes.get(5), 1)   //E to F
        );

        graph = new WeightedGraph<>(nodes, edges);
        dijkstra = new Dijkstra<>();

        //Update start node to 0
        nodes.get(0).setDistance(0);
    }

    @Test
    @DisplayName("UpdateDistance should update distance of destinations if sum of source and weight is less")
    void updateDistanceShouldUpdateDistanceOfDestinationsIfSumOfSourceAndWeightIsLess() {
        double expectedSourceDistance = 0;
        double expectedDistanceB = 5;
        double expectedDistanceC = 2;

        dijkstra.findShortestPath(graph, nodes.get(0), nodes.get(2));

        assertAll(
                "Distances A, B and C",
                () -> assertThat(nodes.get(0).getDistance()).isEqualTo(expectedSourceDistance),
                () -> assertThat(nodes.get(1).getDistance()).isEqualTo(expectedDistanceB),
                () -> assertThat(nodes.get(2).getDistance()).isEqualTo(expectedDistanceC)
        );
    }

    @Test
    @DisplayName("MarkNodeAsVisited should add current node to visited")
    void markNodeAsVisitedShouldAddCurrentNodeToVisited() {
        Node<String> nodeA = nodes.get(0);

        dijkstra.markNodeAsVisited(nodeA);
        boolean visitedNodeContainsA = dijkstra.getVisitedNodes().contains(nodeA);

        assertThat(visitedNodeContainsA).isTrue();
    }

    @Test
    @DisplayName("MarkNodeAsVisited should remove current node from unvisited")
    void markNodeAsVisitedShouldRemoveCurrentNodeFromUnvisited() {
        Node<String> nodeA = nodes.get(0);

        dijkstra.markNodeAsVisited(nodeA);
        boolean unvisitedNodeDoesNotContainA = dijkstra.getUnvisitedNodes().contains(nodeA);

        assertThat(unvisitedNodeDoesNotContainA).isFalse();
    }

    @Test
    @DisplayName("FindShortestUnvisitedDistance returns node with lowest distance")
    void findShortestUnvisitedDistanceReturnsNodeWithLowestDistance() {
        double expectedLowestNodeDistance = 2;

        dijkstra.findShortestPath(graph, nodes.get(0), nodes.get(2));

        Optional<Node<String>> lowestNode = dijkstra.findShortestUnvisitedDistance();
        assertThat(lowestNode.get().getDistance()).isEqualTo(expectedLowestNodeDistance);
    }

    @Test
    @DisplayName("SetPreviousNode updates destination with it´s former node")
    void setPreviousNodeUpdatesDestinationWithItSFormerNode() {
        Node<String> expectedFormerNode = nodes.get(0);

        dijkstra.setPreviousNode(nodes.get(0), edges.get(0));

        assertThat(nodes.get(2).getPreviousNode()).isEqualTo(expectedFormerNode);
    }

    @Test
    @DisplayName("IsDestinationNodeUnvisited return true if node exist")
    void isDestinationNodeUnvisitedReturnTrueIfNodeExist() {

        dijkstra.findShortestPath(graph, nodes.get(0), nodes.get(1));
        boolean nodeExists = dijkstra.isDestinationNodeUnvisited(edges.get(5));

        assertThat(nodeExists).isTrue();
    }

    @Test
    @DisplayName("FindShortestPath should stop when reaching end node")
    void findShortestPathShouldStopWhenReachingEndNode() {
        Node<String> end = nodes.getLast();
        Node<String> start = nodes.getFirst();

        double expectedEndDistance = 8;

        dijkstra.findShortestPath(graph, start, end);

        assertThat(end.getDistance()).isEqualTo(expectedEndDistance);
    }


    @Test
    @DisplayName("UnvisitedNodes should be empty after running findAllShortestPaths")
    void unvisitedNodesShouldBeEmptyAfterRunningFindAllShortestPaths() {
        Node<String> start = nodes.getFirst();
        boolean emptyList = true;

        dijkstra.findAllShortestPaths(graph, start);

        assertThat(dijkstra.getUnvisitedNodes().isEmpty()).isEqualTo(emptyList);
    }

    @Test
    @DisplayName("GetDistance return the target distance")
    void getDistanceReturnTheTargetDistance() {
        double expectedDistance = 8;

        dijkstra.findShortestPath(graph, nodes.get(0), nodes.get(5));

        assertThat(dijkstra.getDistance(nodes.get(5))).isEqualTo(expectedDistance);
    }

    @Test
    @DisplayName("GetPath returns the path from start node to end node")
    void getPathReturnsThePathFromStartNodeToEndNode() {
        List<Node<String>> expectedNodes = new ArrayList<>();
        expectedNodes.add(nodes.get(0));
        expectedNodes.add(nodes.get(1));
        expectedNodes.add(nodes.get(4));
        expectedNodes.add(nodes.get(5));

        dijkstra.findShortestPath(graph, nodes.get(0), nodes.get(5));

        assertThat(dijkstra.getPath(nodes.get(5))).isEqualTo(expectedNodes);
    }

    @Test
    @DisplayName("FindShortestPath with Integer nodes")
    void findShortestPathWithIntegerNodes() {
        List<Node<Integer>> nodesInteger;
        List<Edge<Integer>> edgesInteger;

        nodesInteger = List.of(
                new Node<>(1), new Node<>(2), new Node<>(3),
                new Node<>(4), new Node<>(5), new Node<>(6)
        );

        edgesInteger = List.of(
                new Edge<>(nodesInteger.get(0), nodesInteger.get(2), 2),
                new Edge<>(nodesInteger.get(0), nodesInteger.get(1), 5),
                new Edge<>(nodesInteger.get(1), nodesInteger.get(2), 1),
                new Edge<>(nodesInteger.get(1), nodesInteger.get(3), 4),
                new Edge<>(nodesInteger.get(1), nodesInteger.get(4), 2),
                new Edge<>(nodesInteger.get(2), nodesInteger.get(4), 7),
                new Edge<>(nodesInteger.get(3), nodesInteger.get(4), 6),
                new Edge<>(nodesInteger.get(3), nodesInteger.get(5), 3),
                new Edge<>(nodesInteger.get(4), nodesInteger.get(5), 1)
        );

        WeightedGraph<Integer> graphInteger = new WeightedGraph<>(nodesInteger, edgesInteger);
        Dijkstra<Integer> dijkstraInteger = new Dijkstra<>();

        List<Node<Integer>> expectedNodes = new ArrayList<>();
        expectedNodes.add(nodesInteger.get(0));
        expectedNodes.add(nodesInteger.get(1));
        expectedNodes.add(nodesInteger.get(4));
        expectedNodes.add(nodesInteger.get(5));

        Node<Integer> start = nodesInteger.getFirst();
        Node<Integer> end = nodesInteger.getLast();

        dijkstraInteger.findShortestPath(graphInteger, start, end);

        assertThat(dijkstraInteger.getPath(end)).isEqualTo(expectedNodes);
    }

    @Test
    @DisplayName("Instantiate edge with negative weight should throw exception")
    void instantiateEdgeWithNegativeWeightShouldThrowException() {
        Node<String> nodeA = nodes.get(0);
        Node<String> nodeB = nodes.get(0);

        assertThatThrownBy(
                () -> new Edge<>(nodeA, nodeB, -0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Weight can't be a negative number");
    }

    @Test
    @DisplayName("FindShortestPath should not update distance of self looping nodes")
    void findShortestPathShouldNotUpdateDistanceOfSelfLoopingNodes() {
        WeightedGraph<String> graphSelfLoop;
        Dijkstra<String> dijkstraSelfLoop;

        nodes = List.of(
                new Node<>("A"),
                new Node<>("B"));

        edges = List.of(
                new Edge<>(nodes.get(0), nodes.get(0), 10),
                new Edge<>(nodes.get(0), nodes.get(1), 4));

        graphSelfLoop = new WeightedGraph<>(nodes, edges);
        dijkstraSelfLoop = new Dijkstra<>();

        dijkstraSelfLoop.findShortestPath(graphSelfLoop, nodes.get(0), nodes.get(1));

        assertAll(
                () -> assertThat(nodes.get(0).getDistance()).isZero(),
                () -> assertThat(nodes.get(1).getDistance()).isEqualTo(4)
        );
    }

    @Test
    @DisplayName("FindShortestPath should handle cyclic graphs")
    void findShortestPathShouldHandleCyclicGraphs() {
        WeightedGraph<String> graphCyclic;
        Dijkstra<String> dijkstraCyclic;

        nodes = List.of(
                new Node<>("A"),
                new Node<>("B"),
                new Node<>("C")
        );

        edges = List.of(
                new Edge<>(nodes.get(0), nodes.get(1), 1),
                new Edge<>(nodes.get(1), nodes.get(2), 2),
                new Edge<>(nodes.get(2), nodes.get(0), 3)
        );

        graphCyclic = new WeightedGraph<>(nodes, edges);
        dijkstraCyclic = new Dijkstra<>();

        List<Node<String>> expectedPath = new ArrayList<>();
        expectedPath.add(nodes.get(0));
        expectedPath.add(nodes.get(1));
        expectedPath.add(nodes.get(2));

        dijkstraCyclic.findShortestPath(graphCyclic, nodes.get(0), nodes.get(2));

        List<Node<String>> path = dijkstraCyclic.getPath(nodes.get(2));
        assertThat(path).isEqualTo(expectedPath);
    }

    @Nested
    @DisplayName("Disconnected Graph Tests")
    class DisconnectedGraphTests {

        private List<Node<String>> nodeList;
        private WeightedGraph<String> disconnectedGraph;
        private Dijkstra<String> dijkstraIsDisconnected;

        @BeforeEach
        void setUp() {
            nodeList = List.of(
                    new Node<>("A"),
                    new Node<>("B"),
                    new Node<>("C"),
                    new Node<>("D")
            );

            List<Edge<String>> edgesList = List.of(
                    new Edge<>(nodeList.get(0), nodeList.get(1), 5),
                    new Edge<>(nodeList.get(2), nodeList.get(3), 3)
            );

            disconnectedGraph = new WeightedGraph<>(nodeList, edgesList);
            dijkstraIsDisconnected = new Dijkstra<>();
        }

        @Test
        @DisplayName("Dijkstra should return infinity for disconnected nodes")
        void dijkstraShouldHandleDisconnectedGraphs() {
            dijkstraIsDisconnected.findShortestPath(disconnectedGraph, nodeList.get(0), nodeList.get(3));

            assertThat(nodeList.get(2).getDistance()).isEqualTo(Double.MAX_VALUE);
        }

        @Test
        @DisplayName("FindShortestPath should stop updating distance of nodes if disconnected")
        void findShortestPathShouldStopUpdatingDistanceOfNodesIfDisconnected() {
            List<Node<String>> expectedUncheckedNodes = new ArrayList<>();
            expectedUncheckedNodes.add(nodeList.get(2));
            expectedUncheckedNodes.add(nodeList.get(3));


            dijkstraIsDisconnected.findShortestPath(disconnectedGraph, nodeList.get(0), nodeList.get(3));

            assertThat(dijkstraIsDisconnected.getUnvisitedNodes()).isEqualTo(expectedUncheckedNodes);
        }

    }

    @Test
    @DisplayName("Initiate edge with null should throw exception")
    void initiateEdgeWithNullShouldThrowException() {
        Node<String> nodeA = nodes.get(0);

        assertThatThrownBy(
                () -> new Edge<>(null, nodeA, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source and destination nodes cannot be null");

        assertThatThrownBy(
                () -> new Edge<>(nodeA, null, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source and destination nodes cannot be null");
    }


    @Test
    @DisplayName("Dijkstra methods should throw exception for null parameters")
    void dijkstraMethodsShouldThrowExceptionForNullParameters() {
        Node<String> nodeA = nodes.get(0);
        Node<String> nodeB = nodes.get(1);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> dijkstra.findShortestPath(null, nodeA, nodeB)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> dijkstra.findShortestPath(graph, null, nodeB)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> dijkstra.findShortestPath(graph, nodeA, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> dijkstra.findAllShortestPaths(null, nodeA)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> dijkstra.findAllShortestPaths(graph, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> dijkstra.getPath(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> dijkstra.getDistance(null))
        );
    }

}