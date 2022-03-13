import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class NetworkFlex {

    private final  Map<IP, Set<IP>> graph = new LinkedHashMap<>();

    public NetworkFlex(final IP root, final List<IP> children) {
        graph.put(root, new TreeSet<>(children));
        children.forEach(item->graph.put(item,Set.of(root)));
    }

    public NetworkFlex(final String bracketNotation) throws ParseException {
        if(bracketNotation==null || bracketNotation.isBlank() || !bracketNotation.matches("\\(.+\\)")){
            throw new ParseException("Invalid network bracket notation provided: "+bracketNotation);
        }

        final List<Entry<IP, IP>> edges = calcEdges(bracketNotation);
        boolean hasCycle = doesTheyShapeACycle(edges);
        if(hasCycle){
            throw new ParseException("A network notation with cycle provided: "+bracketNotation);
        }

        edges.forEach(entry->{
            graph.putIfAbsent(entry.getKey(), new LinkedHashSet<>(List.of(entry.getValue())));
            graph.putIfAbsent(entry.getValue(), new LinkedHashSet<>(List.of(entry.getKey())));

            graph.get(entry.getKey()).add(entry.getValue());
            graph.get(entry.getValue()).add(entry.getKey());
        });
    }

    private static boolean doesTheyShapeACycle(List<Entry<IP, IP>> edges) {
        return edges.stream().collect(groupingBy(Entry::getValue)).entrySet().stream().anyMatch(entry -> entry.getValue() != null && entry.getValue().size() > 1);
    }

    public static List<Entry<IP, IP>> calcEdges(String bracketNotation) throws ParseException {

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
//        final List<Entry<IP,IP>> result = new ArrayList<>();
//        adjList.forEach((parent, leaves) -> leaves.forEach(leaf->result.add(new SimpleImmutableEntry<>(parent,leaf))));

        return calcEdges(adjList);
    }

    private static List<Entry<IP,IP>> calcEdges(final Map<IP,Set<IP>> adjacentList){
        final List<Entry<IP,IP>> result = new ArrayList<>();
        adjacentList.forEach((parent, leaves) -> leaves.forEach(leaf->result.add(new SimpleImmutableEntry<>(parent,leaf))));
        return result;
    }

    public IP getRoot() throws ParseException {
        return  graph.entrySet().stream().map(Entry::getKey).findFirst().orElseThrow();
    }

    public boolean add(final NetworkFlex subnet) throws ParseException {
        final List<Entry<IP, IP>> networkEdges = calcEdges(toString(getRoot()));
        List<Entry<IP, IP>> subnetEdges = calcEdges(subnet.toString(subnet.getRoot()));

        if(networkEdges.containsAll(subnetEdges)){
            return false;
        }

        final  List<IP> potentialRootsOfSubnet = list();
        potentialRootsOfSubnet.retainAll(subnet.list());
        
        if(!potentialRootsOfSubnet.isEmpty() && !potentialRootsOfSubnet.contains(subnet.getRoot())){
            final IP newSubnetRoot = potentialRootsOfSubnet.get(0);
            //Reroot network tree
            subnetEdges = subnetEdges.stream().map(item->{
                if(newSubnetRoot.equals(item.getValue())){
                    return new SimpleImmutableEntry<IP,IP>(item.getValue(),item.getKey());
                }
                return item;
            }).collect(Collectors.toList());
        }

        final List<Entry<IP,IP>> allEdges = new ArrayList<>();
        allEdges.addAll(networkEdges);
        allEdges.addAll(subnetEdges);
        if(doesTheyShapeACycle(allEdges)){
            return false;
        }

        subnetEdges.forEach(entry->{
            graph.putIfAbsent(entry.getKey(), new LinkedHashSet<>());
            graph.get(entry.getKey()).add(entry.getValue());
        });

        return true;
    /*
This method copies the tree topologies of the passed object instance into its own instance.
If connections or network nodes from the two instances are the same due to their IP addresses, they are merged.
If the tree topologies could be successfully copied and thus the own instance has changed internally, true is returned, else false is always returned.
Make sure that there are no side effects between these two object instances, so that, for example, a later change to one of the instances does not affect the other.
    * */
    }

    public List<IP> list() {
        final Set<IP> items = new TreeSet<>();
        items.addAll(graph.keySet());
        items.addAll(graph.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));

	    return new ArrayList<>(items);
    }

    public boolean connect(final IP ip1, final IP ip2) {
        final List<IP> nodes = list();
        if (!nodes.containsAll(List.of(ip1,ip2))){
            return false;
        }

        final List<IP> edge = List.of(ip1, ip2);
        if(graph.keySet().stream().noneMatch(edge::contains)){
            //Preventing adding cycle in graph.
            return false;
        }

        if(graph.containsKey(ip1)){
            if(graph.get(ip1).contains(ip2)){
                return false;
            }
            graph.get(ip1).add(ip2);
            return true;
        }

        if(graph.containsKey(ip2)){
            if(graph.get(ip2).contains(ip1)){
                return false;
            }
            graph.get(ip2).add(ip1);
            return true;
        }

        return true;
/*
This method adds a
new connection between two existing network nodes.

These are through the two arguments for their respective IP addresses. Failed a new connection successfully
are added, returns true, else false always returns.
* */
    }

    public boolean disconnect(final IP ip1, final IP ip2) {
        final List<IP> nodes = list();
        if(!nodes.containsAll(List.of(ip1,ip2))){
            return false;
        }
        if(graph.size()==1 && graph.entrySet().iterator().next().getValue().size()==1){
            return false;
        }

        if(graph.containsKey(ip1)){
            graph.get(ip1).remove(ip2);
            if(graph.get(ip1).isEmpty()){
                graph.remove(ip1);
            }
        }else {
            graph.get(ip2).remove(ip1);
            if(graph.get(ip2).isEmpty()){
                graph.remove(ip2);
            }
        }

        return true;

    /*
Removed this method an existing connection between two network nodes. 
These two network nodes will determined based on the transferred IP addresses. 
If a node then has degree 0, it will removed from the object instance so that its IP address can be reassigned. 

If only there was there is a connection, it must not be removed, otherwise there will be no more network nodes would exist. 

If a connection could be removed successfully, true is returned, otherwise always returns false.
*/
    }

    public boolean contains(final IP ip) {
        return graph.keySet().contains(ip) || graph.values().stream().flatMap(Collection::stream).anyMatch(ip::equals);
    }

    public int getHeight(final IP root) {
        final Map<IP,Set<IP>> rootedGraph = calcGraphByRoot(root);
        return rootedGraph.size();
    }

    private Map<IP, Set<IP>> calcGraphByRoot(IP root) {
        Map<IP, Set<IP>> rootedGraph = new LinkedHashMap<>();

        final Queue<IP> queue = new LinkedList<>();
        queue.add(root);
        final Set<IP> visited = new HashSet<>();

        while(!queue.isEmpty()){
            IP curr = queue.poll();
            if(!visited.contains(curr)){
                visited.add(curr);
                List<IP> unvisitedReverseSorted = graph.get(curr).stream().filter(item -> !visited.contains(item)).collect(Collectors.toList());
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
        final Map<IP,Set<IP>> graphByRoot = calcGraphByRoot(root);

        final List<List<IP>> levels = new ArrayList<>();
        if(!contains(root)){
            return levels;
        }

        levels.add(List.of(root));

        graphByRoot.forEach((ip, ips) -> {
            levels.add(new ArrayList<>(ips).stream().sorted().collect(Collectors.toList()));
        });

	    return levels;
    }

    public List<IP> getRoute(final IP start, final IP end) {
        if(!list().containsAll(List.of(start,end))){
            return List.of();
        }
        final Map<IP,Set<IP>> graphByRoot = calcGraphByRoot(start);
        final List<IP> result = new ArrayList<>();
        getRoute(start,end,graphByRoot,result);

//        if(graphByRoot.get(start).contains(end)){
//            return List.of(start,end);
//        }
//        final List<IP> result = new ArrayList<>();
//        for (IP item: graphByRoot.get(start)
//        ) {
//            result.addAll(getRoute(item, end));
//            if(!result.isEmpty()){
//                result.add(0, start);
//            }
//        }
        return result;
    }

    private void getRoute(final IP start, final IP end, Map<IP, Set<IP>> graphByRoot, List<IP> result) {
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


//    public String toString(IP root) {
//        if(!list().contains(root)){
//            return null;
//        }
//        StringBuilder builder = new StringBuilder("("+root);
//        if (graph.containsKey(root)) {
//            for (IP ipEntry : graph.get(root)) {
//                if (graph.containsKey(ipEntry)) {
//                    builder.append(" "+toString(ipEntry));
//                    continue;
//                }
//                builder.append(" "+ipEntry);
//            }
//        }
//        builder.append(")");
////            return Stream.concat(List.of(root).stream(), graph.get(root).stream()).map(IP::toString).collect(Collectors.joining(" ", "(", ")"));
//	    return builder.toString();
//    /*
//        This method returns the bracket notation as
//    string for a tree topology.
//
//    At this point, within this bracket notation must
//    the IP addresses for each level must be sorted in ascending order according to their 32-bit value.
//
//    Consequently, the IP addresses with the lowest bit value are in a left-to-right plane
//    listed first. This tree topology will be attached to the existing one specified by the argument
//    Network nodes picked up so that this is considered to be the top network node. If the specified
//    Address not assigned within the instance is just an instantiated empty string
//    returned.
//    * */
//    }

    public String toString(IP root) {
        if (!contains(root)) {
            return null;
        }

        final Map<IP, Set<IP>> graphByRoot = calcGraphByRoot(root);

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

//        StringBuilder builder = new StringBuilder("(" + root);
//        if (graph.containsKey(root)) {
//            for (IP ipEntry : graph.get(root)) {
//                if (graph.containsKey(ipEntry)) {
//                    builder.append(" " + toString(ipEntry));
//                    continue;
//                }
//                builder.append(" " + ipEntry);
//            }
//        }
//        builder.append(")");
////            return Stream.concat(List.of(root).stream(), graph.get(root).stream()).map(IP::toString).collect(Collectors.joining(" ", "(", ")"));
//        return builder.toString();
    }
    

    private String toString(IP root, Map<IP,Set<IP>> graph){
        if(!graph.keySet().contains(graph.get(root))){
            return Stream.concat(Stream.of(root), graph.get(root).stream().sorted(IP::compareTo)).map(IP::toString).collect(Collectors.joining(" ", "(",")"));
        }
        return "";
    }

}