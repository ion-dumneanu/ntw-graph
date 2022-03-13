import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestTest {


    @Test
    public void test1() throws ParseException {
        IP root = new IP("141.255.1.133");
        List<List<IP>> levels = List.of(List.of(root), List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
        final Network network = new Network(root, levels.get(1));

        final String expected = "(141.255.1.133 0.146.197.108 122.117.67.158)";

        assertEquals(expected,network.toString(root), "invalid toString(IP)");
        assertEquals(levels.size()-1, network.getHeight(root), "invalid height");

        final List<List<IP>> expectedLevels = List.of(List.of(root), levels.get(1));
        assertEquals(expectedLevels, network.getLevels(root),"invalid levels");
    }

    @Test
    public void test2() throws ParseException {
        final Network network = new Network("(141.255.1.133 0.146.197.108 122.117.67.158)");

        // "Change" root and call toString, getHeight and getLevels again
        IP root = new IP("122.117.67.158");
        List<List<IP>> levels = List.of(List.of(root), List.of(new IP("141.255.1.133")), List.of(new IP("0.146.197.108")));

        assertEquals("(122.117.67.158 (141.255.1.133 0.146.197.108))", network.toString(root));

        assertEquals(levels.size() - 1, network.getHeight(root));

        assertEquals(levels,network.getLevels(root));
    }

    @Test
    void test3(){
        Map<Character, Set<Character>> graph = new HashMap<>();
        graph.put('A', Set.of('B'));
        graph.put('B', Set.of('A','C'));
        graph.put('C',Set.of('B','D','E'));
        graph.put('D',Set.of('C'));
        graph.put('E',Set.of('C'));

        Character root = 'D';

        Map<Character, Set<Character>> rootedGraph = new HashMap<>();

        final Queue<Character> queue = new LinkedList<>();
        queue.add(root);
        final Set<Character> visited = new HashSet<>();

        while(!queue.isEmpty()){
            Character curr = queue.poll();
            if(!visited.contains(curr)){
                visited.add(curr);
                List<Character> unvisitedReverseSorted = graph.get(curr).stream().filter(item -> !visited.contains(item)).collect(Collectors.toList());
                if(!unvisitedReverseSorted.isEmpty()){
                    rootedGraph.putIfAbsent(curr, new HashSet<>());
                    rootedGraph.get(curr).addAll(unvisitedReverseSorted);
                }
                queue.addAll(new TreeSet<>(unvisitedReverseSorted));
            }
        }

        System.out.println(rootedGraph);
    }

}
