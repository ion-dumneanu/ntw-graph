package edu.kit.informatik;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class Network {

    private final Map<IP, Set<IP>> allNetworkSource = new LinkedHashMap<>();

    public Network(final IP root, final List<IP> children) {
        allNetworkSource.put(root, new TreeSet<>(children));
        children.forEach(item -> allNetworkSource.put(item, Set.of(root)));
    }

    public Network(final String bracketNotation) throws ParseException {
        if (bracketNotation == null || "".equals(bracketNotation.trim()) || !bracketNotation.matches("\\(.+\\)")) {
            throw new ParseException("Invalid network bracket notation provided: " + bracketNotation);
        }

        final Set<Entry<IP, IP>> edges = calcEdges(bracketNotation);
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

    private static boolean doesTheyShapeACycle(Set<Entry<IP, IP>> edges) {
        return edges.stream().collect(groupingBy(Entry::getValue)).entrySet().stream().anyMatch(entry -> entry.getValue() != null && entry.getValue().size() > 1);
    }

    private static Set<Entry<IP, IP>> calcEdges(String bracketNotation) {

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

    private static Set<Entry<IP, IP>> calcEdges(final Map<IP, Set<IP>> adjacentList) {
        final Set<Entry<IP, IP>> result = new HashSet<>();
        adjacentList.forEach((parent, leaves) -> leaves.forEach(leaf -> result.add(new SimpleImmutableEntry<>(parent, leaf))));
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

        final List<Entry<IP, IP>> networkInvolvedEdges = new ArrayList<>();
        commonNodes.forEach(node -> {
            networkInvolvedEdges.addAll(calcEdges(calcGraphByRoot(node, allNetworkSource)));
        });


        final Set<Entry<IP, IP>> subnetworkEdges = new HashSet<>();//calcEdges(subnet.toString(commonNodes.get(0)));

        commonNodes.forEach(node -> {
            subnetworkEdges.addAll(calcEdges(subnet.toString(node)));
        });

        final Set<Entry<IP, IP>> unionEdges = Stream.concat(networkInvolvedEdges.stream(), subnetworkEdges.stream()).distinct().collect(Collectors.toSet());
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

        final Set<Entry<IP, IP>> allEdges = new HashSet<>();
        allEdges.add(new SimpleImmutableEntry<>(start, end));
        allEdges.addAll(calcEdges(graphByRoot));

        if (doesTheyShapeACycle(allEdges)) {
            return false;
        }

        addEdges(Set.of(new SimpleImmutableEntry<>(start, end)));
        return true;
    }

    public boolean disconnect(final IP ip1, final IP ip2) {
        if (!contains(ip1) || !contains(ip2)) {
            return false;
        }

        if (allNetworkSource.size() == 2) {
            return false;
        }

        allNetworkSource.get(ip1).remove(ip2);
        allNetworkSource.get(ip2).remove(ip1);
        if (allNetworkSource.get(ip1).size() == 0) {
            allNetworkSource.remove(ip1);
        }
        if (allNetworkSource.get(ip2).size() == 0) {
            allNetworkSource.remove(ip2);
        }

        return true;
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
        return getLevels(root).size() - 1;
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

    private void addEdges(Set<Entry<IP, IP>> edges) {
        edges.forEach(entry -> {
            allNetworkSource.putIfAbsent(entry.getKey(), new LinkedHashSet<>(List.of(entry.getValue())));
            allNetworkSource.putIfAbsent(entry.getValue(), new LinkedHashSet<>(List.of(entry.getKey())));

            allNetworkSource.get(entry.getKey()).add(entry.getValue());
            allNetworkSource.get(entry.getValue()).add(entry.getKey());
        });
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Network network = (Network) o;
//
//        return allNetworkSource != null ? allNetworkSource.equals(network.allNetworkSource) : network.allNetworkSource == null;
//    }
//
//    @Override
//    public int hashCode() {
//        return allNetworkSource != null ? allNetworkSource.hashCode() : 0;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;

        return Objects.equals(allNetworkSource, network.allNetworkSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allNetworkSource);
    }
}