import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graph {

    private final int index;

    private final Set<Integer> nodes;

    private final Set<Set<Integer>> edges;

    private final int[] where;

    private final int support;

    private int diameter;

    public Graph(int index, Set<Integer> nodes, Set<Set<Integer>> edges, int[] where, int support) {
        this.index = index;
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
        this.where = where == null ? new int[0] : where;
        this.support = support;
        this.diameter = 0;
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

    public int[] getWhere() {
        return this.where;
    }

    public int getSupport() {
        return this.support;
    }

    public int getDiameter() {
        return this.diameter;
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }
}
