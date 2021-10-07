import java.util.HashSet;
import java.util.Set;

public class Graph {
    private int index;
    private Set<Integer> nodes;
    private Set<Set<Integer>> edges;

    public Graph(int index, Set<Integer> nodes, Set<Set<Integer>> edges) {
        this.index = index;
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Set<Integer> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Integer> nodes) {
        this.nodes = nodes;
    }

    public Set<Set<Integer>> getEdges() {
        return edges;
    }

    public void setEdges(Set<Set<Integer>> edges) {
        this.edges = edges;
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
