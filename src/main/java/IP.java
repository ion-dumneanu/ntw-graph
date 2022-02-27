import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IP implements Comparable<IP> {

    private final static String PART_REGEXP = "([0-9]|([1-9]\\d)|(1\\d{2})|(2[1-4]\\d)|25[0-5])";
    public final static String REGEXP = PART_REGEXP + "\\." + PART_REGEXP + "\\." + PART_REGEXP + "\\." + PART_REGEXP;

    private List<Integer> parts = new ArrayList<Integer>();

    public IP(final String pointNotation) throws ParseException {
        if (pointNotation == null || pointNotation.isBlank()) {
            throw new ParseException("invalid IP, null or empty");
        } else if (!pointNotation.matches(REGEXP)) {
            throw new ParseException("invalid IP '" + pointNotation + "'");
        }

        for (String item : pointNotation.split("\\.")) {
            parts.add(Integer.valueOf(item));
        }
    }

    @Override
    public String toString() {
        return parts.stream().map(i -> "" + i).collect(Collectors.joining("."));
    }

    public List<Integer> getParts() {
        return new ArrayList<Integer>(parts);
    }

    @Override
    public int compareTo(IP o) {
        List<Integer> current = getParts();
        List<Integer> other = o.getParts();

        for (int i = 0; i < 4; i++) {
            int partCompare = Integer.compare(current.get(i), other.get(i));
            if (partCompare != 0) {
                return partCompare;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IP ip = (IP) o;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}