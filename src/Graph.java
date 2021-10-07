import java.util.HashSet;
import java.util.Set;

public class Graph {
    private final int index;
    private final Set<Integer> nodes;
    private final Set<Set<Integer>> edges;

    public Graph(int index, Set<Integer> nodes, Set<Set<Integer>> edges) {
        this.index = index;
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
    }

    public int getIndex() {
        return index;
    }

    public Set<Integer> getNodes() {
        return nodes;
    }

    public Set<Set<Integer>> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "index=" + index +
                ", nodes=" + nodes +
                ", edges=" + edges +
                "}\n";
    }
}
