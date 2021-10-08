import java.util.HashSet;
import java.util.Set;

public class Graph {
    private final int index;
    private final Set<Integer> nodes;
    private final Set<Set<Integer>> edges;
    private final int support;

    public Graph(int index, Set<Integer> nodes, Set<Set<Integer>> edges, int support) {
        this.index = index;
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
        this.support = support;
    }

    public int getIndex() {
        return this.index;
    }

    public Set<Integer> getNodes() {
        return this.nodes;
    }

    public Set<Set<Integer>> getEdges() {
        return this.edges;
    }

    public int getSupport() {
        return this.support;
    }
}
