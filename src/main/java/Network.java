import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Network {

    private final  Map<IP, Set<IP>> graph = new LinkedHashMap<>();

    public Network(final IP root, final List<IP> children) {
        graph.put(root, new TreeSet<>(children));

        /*This constructor
creates a new object instance and adds the given non-empty and valid tree topology
added to the object instance. The tree topology has height 1 with the argument root as
root and the node (at least one) in the children list as directly connected
node. Be sure to copy the items from this list for internal storage,
since the list could be immutable or subsequently modified by the caller.
If the arguments do not describe a valid tree topology, or if one of the arguments does not
is instantiated or contains non-instantiated elements, an appropriate RuntimeException
thrown.*/

    }

    public Network(final String bracketNotation) throws ParseException {
        if(bracketNotation==null || bracketNotation.isBlank() || !bracketNotation.matches("\\(.+\\)")){
            throw new ParseException("invalid network bracket notation provided: "+bracketNotation);
        }

        Map<IP, Set<IP>> adjList = graph;
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
//        System.out.println(adjList);

/*
This constructor creates a new object instance and appends the passed non-empty and
valid tree topology. The tree topology is written as a character string in brackets
specified and must correspond to the previously specified format of the parentheses notation.
If this is not possible or the format is violated, a ParseException is thrown in the constructor
thrown.
*/
    }

    public IP getRoot(){
        return  graph.entrySet().stream().map(Entry::getKey).findFirst().orElseThrow();
    }

    public boolean add(final Network subnet) {

        //FIXME: to be continue...
        final List<IP> subnetList = subnet.list();
        final List<IP> thisList = list();
        final IP subnetRoot = subnet.getRoot();
        final List<List<IP>> subnetLevels = subnet.getLevels(subnetRoot);

        if(thisList.stream().noneMatch(subnetList::contains)){
              subnetLevels.forEach(item->{
                  graph.put(item.get(0), new TreeSet<>(item.subList(1,item.size())));
              });
              return true;
        }

        return false;


    /*
This method copies the tree topologies of the passed object instance into its own instance.
If connections or network nodes from the two instances are the same due to their IP addresses, they are merged.
If the tree topologies could be successfully copied and thus the own instance has changed internally, true is returned, else false is always returned.
Make sure that there are no side effects between these two object instances, so that, for example, a later change to one of the instances does not affect the other.
    * */
    }

    public List<IP> list() {
        final Set<IP> items = new TreeSet<>();
        for (Entry<IP,Set<IP>> entry:graph.entrySet()
             ) {
            items.add(entry.getKey());
            items.addAll(entry.getValue());
        }
	    return new ArrayList<>(items);

    /*
    This method returns the IP addresses of all currently in the object instance
    existing nodes in a new and independent list. The addresses in
    this list are sorted in ascending order according to their overall natural order. Be sure to,
    that this returned list has no side effects on the instance.
    * */
    }

    public boolean connect(final IP ip1, final IP ip2) {
        final List<IP> nodes = list();
        if (!nodes.containsAll(List.of(ip1,ip2))){
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

        graph.put(ip1, new LinkedHashSet<>(List.of(ip2)));
	    return true;

/*
This method adds a
new connection between two existing network nodes.

These are through the two
arguments for their respective IP addresses. Failed a new connection successfully
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
	    return list().contains(ip);
    }

    public int getHeight(final IP root) {
        int height = 0;
        boolean hasRoot = false;

        for (Entry<IP, Set<IP>> entry : graph.entrySet()) {
           if(!hasRoot && entry.getKey().equals(root)){
               hasRoot = true;
//               height++;
           }

           if(hasRoot){
               height++;
           }
        }
        return height;

    /*
     This method returns the integer height of a
tree topology.
This tree topology is attached to the existing one determined by the argument
Picked up a node so that it is considered the top node.
If the specified
If the IP address is not assigned internally, 0 is always returned.
    * */
    }

    public List<List<IP>> getLevels(final IP root) {
        final List<List<IP>> levels = new ArrayList<>();
        if(!list().contains(root)){
            return levels;
        }else{
            levels.add(List.of(root));
        }

        boolean reachedRoot = false;
        for (Entry<IP, Set<IP>> entry : graph.entrySet()) {
            if(!reachedRoot && entry.getKey().equals(root)){
                reachedRoot = true;
                levels.add(new ArrayList<>(entry.getValue()));
                continue;
            }

            if(reachedRoot){
                levels.add(new ArrayList<>(entry.getValue()));
            }
        }

	    return levels;

        /*
        This method returns the level structure
a tree topology in list form. The addresses of the network nodes of a level
are inserted into a sorted list. The whole level structure returned is again
a list of these lists of the individual levels sorted in ascending order. This tree topology is attached
the existing node determined by the argument, so that this as her
top node applies. This given node is assigned the first level and
its IP address is inserted as the only element of the first inner list. The IP addresses of the

subsequent levels are then inserted into the subsequent list. The addresses in the inner
Lists for the levels are in ascending order according to their overall natural order
sorted.
If there is no node with the specified IP address, only one is instantiated
empty list returned. Be careful that any returned lists have no side effects
have on the instance.
        * */
    }

    public List<IP> getRoute(final IP start, final IP end) {
        if(graph.get(start)==null || graph.get(start).isEmpty()){
            return List.of();
        }
        if(graph.get(start).contains(end)){
            return List.of(start,end);
        }
        final List<IP> result = new ArrayList<>();
        for (IP item: graph.get(start)
        ) {
            result.addAll(getRoute(item, end));
            if(!result.isEmpty()){
                result.add(0,start);
            }
        }
        return result;
        /*
This method gives a list of the individual IP addresses of the network nodes of the shortest route between the
the start and end node specified by the respective argument.
The IP address of The starting node is the first item in this list and the IP address of the ending node is the last Element.

The consecutive network nodes in the list must always go through a connection
be connected in the tree topology.

If one of the two specified network nodes does not exist
or there is no path between the two, just an instantiated empty list is returned.
Care must be taken that the returned list has no side effects on the instance.
        * */
    }

    public String toString(IP root) {
        if(!list().contains(root)){
            return null;
        }
        StringBuilder builder = new StringBuilder("("+root);
        if (graph.containsKey(root)) {
            for (IP ipEntry : graph.get(root)) {
                if (graph.containsKey(ipEntry)) {
                    builder.append(" "+toString(ipEntry));
                    continue;
                }
                builder.append(" "+ipEntry);
            }
        }
        builder.append(")");
//            return Stream.concat(List.of(root).stream(), graph.get(root).stream()).map(IP::toString).collect(Collectors.joining(" ", "(", ")"));
	    return builder.toString();
    /*
    This method returns the bracket notation as
string for a tree topology.

At this point, within this bracket notation must
the IP addresses for each level must be sorted in ascending order according to their 32-bit value.

Consequently, the IP addresses with the lowest bit value are in a left-to-right plane
listed first. This tree topology will be attached to the existing one specified by the argument
Network nodes picked up so that this is considered to be the top network node. If the specified
Address not assigned within the instance is just an instantiated empty string
returned.
    * */
    }


}