package edu.kit.informatik;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class Network {

    private final Map<IP, Set<IP>> allNetworkSource = new LinkedHashMap<>();

    public Network(final IP root, final List<IP> children) {
        if(root==null || children==null || children.isEmpty() || children.contains(root) || new HashSet<>(children).size()!=children.size()){
            throw new IllegalArgumentException();
        }

        allNetworkSource.put(root, new TreeSet<>(children));
        children.forEach(item -> allNetworkSource.put(item, Set.of(root)));
    }

    public Network(final String bracketNotation) throws ParseException {
        if (bracketNotation == null || "".equals(bracketNotation.trim()) || !bracketNotation.matches("\\(.+\\)")) {
            throw new ParseException("Invalid network bracket notation provided: " + bracketNotation);
        }

        final Set<Edge> edges = calcEdges(bracketNotation);
        boolean hasCycle = doesTheyShapeACycle(edges);
        if (hasCycle) {
            throw new ParseException("A network notation with cycle provided: " + bracketNotation);
        }

        addEdges(edges);
    }

    private static void getSubLevels(List<IP> topLevel, Map<IP, Set<IP>> graphByRoot, List<List<IP>> levels) {
        List<IP> subLevel = new ArrayList<>();
        graphByRoot.forEach((ip, ips) -> {
            if (topLevel.contains(ip)) {
                subLevel.addAll(ips);
            }
        });

        if (subLevel.isEmpty()) {
            return;
        }
        Collections.sort(subLevel);
        levels.add(subLevel);
        getSubLevels(subLevel, graphByRoot, levels);
    }

    private static void getRoute(final IP start, final IP end, Map<IP, Set<IP>> graphByRoot, List<IP> result) {
        if (graphByRoot.get(start).contains(end)) {
            result.addAll(List.of(start,end));
            return;
        }

        for (IP item : graphByRoot.get(start)
        ) {
            if (graphByRoot.containsKey(item)) {
                int resultSize = result.size();
                getRoute(item, end, graphByRoot, result);
                if (!result.isEmpty() && result.size()>resultSize) {
                    result.add(0, start);
                }
            }
        }
    }

    private static boolean doesTheyShapeACycle(Set<Edge> edges) {
        return edges.stream().collect(groupingBy(Edge::getValue)).entrySet().stream().anyMatch(entry -> entry.getValue() != null && entry.getValue().size() > 1);
    }

    private static Set<Edge> calcEdges(String bracketNotation) {

        Map<IP, Set<IP>> adjList = new LinkedHashMap<>();
        Stack<IP> stack = new Stack<>();
        Iterator<String> iterator = List.of(bracketNotation.split("\\s+")).iterator();

        while (iterator.hasNext()) {
            String curr = iterator.next();
            if (curr.startsWith("(")) {
                IP preAdd = new IP(curr.replaceFirst("\\(", ""));
                if (!stack.empty()) {
                    adjList.get(stack.peek()).add(preAdd);
                }
                stack.add(preAdd);
                adjList.put(preAdd, new LinkedHashSet<>());
            } else if (curr.endsWith(")")) {
                Matcher matcher = Pattern.compile("(" + IP.REGEXP + ")\\)+").matcher(curr);
                matcher.find();
                IP preAdd = new IP(matcher.group(1));
                adjList.get(stack.pop()).add(preAdd);
            } else {
                adjList.get(stack.peek()).add(new IP(curr));
            }
        }

        return calcEdges(adjList);
    }

    private static Set<Edge> calcEdges(final Map<IP, Set<IP>> adjacentList) {
        final Set<Edge> result = new HashSet<>();
        adjacentList.forEach((parent, leaves) -> leaves.forEach(leaf -> result.add(new Edge(parent, leaf))));
        return result;
    }

    private static Map<IP, Set<IP>> calcGraphByRoot(IP root, Map<IP, Set<IP>> allGraph) {
        Map<IP, Set<IP>> rootedGraph = new LinkedHashMap<>();

        final Queue<IP> queue = new LinkedList<>();
        queue.add(root);
        final Set<IP> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            IP curr = queue.poll();
            if (!visited.contains(curr)) {
                visited.add(curr);
                List<IP> unvisitedReverseSorted = allGraph.get(curr).stream().filter(item -> !visited.contains(item)).collect(Collectors.toList());
                if (!unvisitedReverseSorted.isEmpty()) {
                    rootedGraph.putIfAbsent(curr, new HashSet<>());
                    rootedGraph.get(curr).addAll(unvisitedReverseSorted);
                }
                queue.addAll(new TreeSet<>(unvisitedReverseSorted));
            }
        }

        return rootedGraph;
    }

    public boolean add(final Network subnet) {
        if(subnet==null){
            return false;
        }
        final List<IP> networkNodes = this.list();
        final List<IP> subnetNodes = subnet.list();

        if (subnetNodes.stream().noneMatch(networkNodes::contains)) {
            addEdges(calcEdges(subnet.toString(subnetNodes.get(0))));
            return true;
        }

        subnetNodes.retainAll(networkNodes);
        final List<IP> commonNodes = subnetNodes;

        final List<Edge> networkInvolvedEdges = new ArrayList<>();
        commonNodes.forEach(node -> {
            networkInvolvedEdges.addAll(calcEdges(calcGraphByRoot(node, allNetworkSource)));
        });


        final Set<Edge> subnetworkEdges = new HashSet<>();//calcEdges(subnet.toString(commonNodes.get(0)));

        commonNodes.forEach(node -> {
            subnetworkEdges.addAll(calcEdges(subnet.toString(node)));
        });

        final Set<Edge> unionEdges = Stream.concat(networkInvolvedEdges.stream(), subnetworkEdges.stream()).collect(Collectors.toSet());
        if (doesTheyShapeACycle(unionEdges)) {
            return false;
        }

        final boolean allSubnetEdgesAreAlreadyIn = subnetworkEdges.stream().allMatch(entry -> {
                    if (!allNetworkSource.containsKey(entry.getKey())){
                        return false;
                    }
                    return allNetworkSource.get(entry.getKey()).contains(entry.getValue());
                }
        );
        if(allSubnetEdgesAreAlreadyIn){
            return false;
        }
        addEdges(subnetworkEdges);
        return true;
    }

    public List<IP> list() {
        final Set<IP> items = new TreeSet<>();
        items.addAll(allNetworkSource.keySet());
        items.addAll(allNetworkSource.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));

        return new ArrayList<>(items);
    }

    public boolean connect(final IP start, final IP end) {
        if (start == null || end == null) {
            return false;
        }

        if (start.equals(end)
                || !allNetworkSource.keySet().containsAll(List.of(start, end))
                || allNetworkSource.get(start).contains(end)
        ) {
            return false;
        }

        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(start, allNetworkSource);

        final Set<Edge> allEdges = new HashSet<>();
        allEdges.add(new Edge(start, end));
        allEdges.addAll(calcEdges(graphByRoot));

        if (doesTheyShapeACycle(allEdges)) {
            return false;
        }

        addEdges(Set.of(new Edge(start, end)));
        return true;
    }

    public boolean disconnect(final IP start, final IP end) {
        if (start == null || start == null || start.equals(end) || !list().containsAll(List.of(start,end))) {
            return false;
        }

        if (allNetworkSource.size() == 2) {
            return false;
        }

        return allNetworkSource.get(start).remove(end) && allNetworkSource.get(end).remove(start);
    }

    public boolean contains(final IP ip) {
        if(ip==null){
            return false;
        }
        return allNetworkSource.keySet().contains(ip) || allNetworkSource.values().stream().flatMap(Collection::stream).anyMatch(ip::equals);
    }

    public int getHeight(final IP root) {
        if (root == null || !allNetworkSource.keySet().contains(root)) {
            return 0;
        }

        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(root, this.allNetworkSource);
        return graphByRoot.get(root).stream().map(item->getSubHeight(item, graphByRoot)).max(Integer::compare).orElse(0)+1;
    }

    private static int getSubHeight(final IP root, final Map<IP, Set<IP>> graphByRoot){
        if(!graphByRoot.containsKey(root)){
            return 0;
        }
        return graphByRoot.get(root).stream().map(item->getSubHeight(item, graphByRoot)).max(Integer::compare).orElse(0)+1;
    }

    public List<List<IP>> getLevels(final IP root) {
        if (root == null || !allNetworkSource.keySet().contains(root)) {
            return List.of();
        }

        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(root, this.allNetworkSource);

        final List<List<IP>> levels = new ArrayList<>();

        List<IP> topLevel = List.of(root);
        levels.add(topLevel);

        getSubLevels(topLevel, graphByRoot, levels);

        return levels;
    }

    public List<IP> getRoute(final IP start, final IP end) {
        if(start==null || end==null){
            return List.of();
        }

        if (!list().containsAll(List.of(start, end))) {
            return List.of();
        }
        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(start, this.allNetworkSource);
        final List<IP> result = new ArrayList<>();
        getRoute(start, end, graphByRoot, result);
        return result;
    }

    public String toString(IP root) {
        if(root==null || !contains(root)){
            return "";
        }

        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(root, this.allNetworkSource);

        StringJoiner joiner = new StringJoiner(" ", "(", ")");
        joiner.add(root.toString());
        graphByRoot.get(root).stream().sorted(IP::compareTo).forEach(item -> {
            if (graphByRoot.containsKey(item)) {
                joiner.add(toString(item, graphByRoot));
            } else {
                joiner.add(item.toString());
            }
        });

        return joiner.toString();
    }

    private String toString(IP root, Map<IP, Set<IP>> graphByRoot) {
        StringJoiner joiner = new StringJoiner(" ", "(", ")");
        joiner.add(root.toString());
        graphByRoot.get(root).stream().sorted(IP::compareTo).forEach(item -> {
            if (graphByRoot.containsKey(item)) {
                joiner.add(toString(item, graphByRoot));
            } else {
                joiner.add(item.toString());
            }
        });
        return joiner.toString();
    }

    private void addEdges(Set<Edge> edges) {
        edges.forEach(entry -> {
            allNetworkSource.putIfAbsent(entry.getKey(), new LinkedHashSet<>(List.of(entry.getValue())));
            allNetworkSource.putIfAbsent(entry.getValue(), new LinkedHashSet<>(List.of(entry.getKey())));

            allNetworkSource.get(entry.getKey()).add(entry.getValue());
            allNetworkSource.get(entry.getValue()).add(entry.getKey());
        });
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Network network = (Network) other;

        return Objects.equals(list(), network.list());
    }

    @Override
    public int hashCode() {
        return list().stream().map(IP::toString).collect(Collectors.joining()).hashCode();
    }

    public static class Edge {
        private final IP key;
        private final IP value;

        public Edge(IP key, IP value) {
            this.key = key;
            this.value = value;
        }

        public IP getKey() {
            return key;
        }

        public IP getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Set.of(getKey(),getValue()).equals(Set.of(edge.getValue(), edge.getKey()));
        }

        @Override
        public int hashCode() {
            final IP[] arr = {key, value};
            Arrays.sort(arr);
            return Arrays.hashCode(arr);
        }
    }
}