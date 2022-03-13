import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class Network {

    private final Map<IP, Set<IP>> allNetworkSource;

    public Network(final IP root, final List<IP> children) {
        allNetworkSource = new LinkedHashMap<>();
        allNetworkSource.put(root, new TreeSet<>(children));
        children.forEach(item-> allNetworkSource.put(item,Set.of(root)));
    }

    public Network(final String bracketNotation) throws ParseException {
        if(bracketNotation==null || bracketNotation.isBlank() || !bracketNotation.matches("\\(.+\\)")){
            throw new ParseException("Invalid network bracket notation provided: "+bracketNotation);
        }

        final List<Entry<IP, IP>> edges = calcEdges(bracketNotation);
        boolean hasCycle = doesTheyShapeACycle(edges);
        if(hasCycle){
            throw new ParseException("A network notation with cycle provided: "+bracketNotation);
        }
        allNetworkSource = new LinkedHashMap<>();

        addEdges(edges);
    }

    public boolean add(final Network subnet) throws ParseException {
        final List<IP> networkNodes = this.list();
        final List<IP> subnetNodes = subnet.list();

        if(subnetNodes.stream().noneMatch(networkNodes::contains)){
            addEdges(calcEdges(subnet.toString(subnetNodes.get(0))));
            return true;
        }

        subnetNodes.retainAll(networkNodes);
        final List<IP> commonNodes = subnetNodes;

        final List<Entry<IP, IP>> networkInvolvedEdges = new ArrayList<>();
        commonNodes.forEach(node->{
            networkInvolvedEdges.addAll(calcEdges(calcGraphByRoot(node, allNetworkSource)));
        });

        final List<Entry<IP, IP>> subnetworkEdges = calcEdges(subnet.toString(commonNodes.get(0)));

        final List<Entry<IP, IP>> unionEdges = Stream.concat(networkInvolvedEdges.stream(), subnetworkEdges.stream()).distinct().collect(Collectors.toList());
        if(doesTheyShapeACycle(unionEdges)){
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
        if(start==null || end==null){
            return false;
        }

        if (start.equals(end)
                || !allNetworkSource.keySet().containsAll(List.of(start, end))
                || allNetworkSource.get(start).contains(end)
        ) {
            return false;
        }

        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(start, allNetworkSource);

        final List<Entry<IP, IP>> allEdges = new ArrayList<>();
        allEdges.add(new SimpleImmutableEntry<>(start,end));
        allEdges.addAll(calcEdges(graphByRoot));

        if(doesTheyShapeACycle(allEdges)){
            return false;
        }

        addEdges(List.of(new SimpleImmutableEntry<>(start,end)));
        return true;
    }

    public boolean disconnect(final IP ip1, final IP ip2) {
        if(!contains(ip1) || !contains(ip2)){
            return false;
        }

        if(allNetworkSource.size()==2){
            return false;
        }

        allNetworkSource.get(ip1).remove(ip2);
        allNetworkSource.get(ip2).remove(ip1);
        if(allNetworkSource.get(ip1).size()==0){
            allNetworkSource.remove(ip1);
        }
        if(allNetworkSource.get(ip2).size()==0){
            allNetworkSource.remove(ip2);
        }

        return true;
    }

    public boolean contains(final IP ip) {
        return allNetworkSource.keySet().contains(ip) || allNetworkSource.values().stream().flatMap(Collection::stream).anyMatch(ip::equals);
    }

    public int getHeight(final IP root) {
        if(root==null || !allNetworkSource.keySet().contains(root)){
            return 0;
        }
//        final Map<IP,Set<IP>> rootedGraph = calcGraphByRoot(root, this.allNetworkSource);
//        return rootedGraph.size();
        return getLevels(root).size()-1;
    }

    private static Map<IP, Set<IP>> calcGraphByRoot(IP root, Map<IP, Set<IP>> allGraph) {
        Map<IP, Set<IP>> rootedGraph = new LinkedHashMap<>();

        final Queue<IP> queue = new LinkedList<>();
        queue.add(root);
        final Set<IP> visited = new HashSet<>();

        while(!queue.isEmpty()){
            IP curr = queue.poll();
            if(!visited.contains(curr)){
                visited.add(curr);
                List<IP> unvisitedReverseSorted = allGraph.get(curr).stream().filter(item -> !visited.contains(item)).collect(Collectors.toList());
                if(!unvisitedReverseSorted.isEmpty()){
                    rootedGraph.putIfAbsent(curr, new HashSet<>());
                    rootedGraph.get(curr).addAll(unvisitedReverseSorted);
                }
                queue.addAll(new TreeSet<>(unvisitedReverseSorted));
            }
        }

        return rootedGraph;
    }

    public List<List<IP>> getLevels(final IP root) {
        if(root==null || !allNetworkSource.keySet().contains(root)){
            return List.of();
        }

        final Map<IP,Set<IP>> graphByRoot = calcGraphByRoot(root, this.allNetworkSource);

        final List<List<IP>> levels = new ArrayList<>();

        List<IP> topLevel = List.of(root);
        levels.add(topLevel);

        getSubLevels(topLevel, graphByRoot, levels);

	    return levels;
    }

    private static void getSubLevels(List<IP> topLevel, Map<IP, Set<IP>> graphByRoot, List<List<IP>> levels) {
        List<IP> subLevel = new ArrayList<>();
        graphByRoot.forEach((ip, ips) -> {
            if(topLevel.contains(ip)){
                subLevel.addAll(ips);
            }
        });

        if(subLevel.isEmpty()){
            return;
        }
        Collections.sort(subLevel);
        levels.add(subLevel);
        getSubLevels(subLevel, graphByRoot, levels);
    }


    public List<IP> getRoute(final IP start, final IP end) {
        if(!list().containsAll(List.of(start,end))){
            return List.of();
        }
        final Map<IP,Set<IP>> graphByRoot = calcGraphByRoot(start, this.allNetworkSource);
        final List<IP> result = new ArrayList<>();
        getRoute(start,end,graphByRoot,result);
        return result;
    }

    private static void getRoute(final IP start, final IP end, Map<IP, Set<IP>> graphByRoot, List<IP> result) {
        if (graphByRoot.get(start).contains(end)) {
            result.addAll(List.of(start, end));
            return;
        }

        for (IP item : graphByRoot.get(start)
        ) {
            if (graphByRoot.containsKey(item)) {
                getRoute(item, end, graphByRoot, result);
                if (!result.isEmpty()) {
                    result.add(0, start);
                }
            }
        }
    }

    public String toString(IP root) {
        if (!contains(root)) {
            return null;
        }

        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(root, this.allNetworkSource);

        StringJoiner joiner = new StringJoiner(" ","(",")");
        joiner.add(root.toString());
        graphByRoot.get(root).stream().sorted(IP::compareTo).forEach(item->{
            if(graphByRoot.containsKey(item)){
                joiner.add(toString(item, graphByRoot));
            }else{
                joiner.add(item.toString());
            }
        });

        return joiner.toString();
    }

    private String toString(IP root, Map<IP, Set<IP>> graphByRoot){
        StringJoiner joiner = new StringJoiner(" ","(",")");
        joiner.add(root.toString());
        graphByRoot.get(root).stream().sorted(IP::compareTo).forEach(item->{
            if(graphByRoot.containsKey(item)){
                joiner.add(toString(item, graphByRoot));
            }else{
                joiner.add(item.toString());
            }
        });
        return joiner.toString();
    }

//    private String toString(IP root, Map<IP,Set<IP>> graph){
//        if(!graph.keySet().contains(graph.get(root))){
//            return Stream.concat(Stream.of(root), graph.get(root).stream().sorted(IP::compareTo)).map(IP::toString).collect(Collectors.joining(" ", "(",")"));
//        }
//        return "";
//    }

    private void addEdges(List<Entry<IP, IP>> edges) {
        edges.forEach(entry->{
            allNetworkSource.putIfAbsent(entry.getKey(), new LinkedHashSet<>(List.of(entry.getValue())));
            allNetworkSource.putIfAbsent(entry.getValue(), new LinkedHashSet<>(List.of(entry.getKey())));

            allNetworkSource.get(entry.getKey()).add(entry.getValue());
            allNetworkSource.get(entry.getValue()).add(entry.getKey());
        });
    }

    private static boolean doesTheyShapeACycle(List<Entry<IP, IP>> edges) {
        return edges.stream().collect(groupingBy(Entry::getValue)).entrySet().stream().anyMatch(entry -> entry.getValue() != null && entry.getValue().size() > 1);
    }

    private static List<Entry<IP, IP>> calcEdges(String bracketNotation) throws ParseException {

        Map<IP, Set<IP>> adjList = new LinkedHashMap<>();
        Stack<IP> stack = new Stack<>();
        Iterator<String> iterator = List.of(bracketNotation.split("\\s+")).iterator();

        while(iterator.hasNext()){
            String curr = iterator.next();
            if(curr.startsWith("(")){
                IP preAdd = new IP(curr.replaceFirst("\\(",""));
                if(!stack.empty()){
                    adjList.get(stack.peek()).add(preAdd);
                }
                stack.add(preAdd);
                adjList.put(preAdd, new LinkedHashSet<>());
            } else if(curr.endsWith(")")){
                Matcher matcher = Pattern.compile("("+IP.REGEXP+")\\)+").matcher(curr);
                matcher.find();
                IP preAdd = new IP(matcher.group(1));
                adjList.get(stack.pop()).add(preAdd);
            } else{
                adjList.get(stack.peek()).add(new IP(curr));
            }
        }

        return calcEdges(adjList);
    }

    private static List<Entry<IP,IP>> calcEdges(final Map<IP,Set<IP>> adjacentList){
        final List<Entry<IP,IP>> result = new ArrayList<>();
        adjacentList.forEach((parent, leaves) -> leaves.forEach(leaf->result.add(new SimpleImmutableEntry<>(parent,leaf))));
        return result;
    }


}