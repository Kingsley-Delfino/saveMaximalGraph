import java.util.*;

public class Graph {

    private final int index;

    private final Set<Integer> nodes;

    private final Set<Set<Integer>> edges;

    private int[] where;

    private Map<Integer, List<Integer>> update;

    private Map<Integer, List<String>> centralCommits;

    private Set<String> relativeCommits;

    private final int support;

    private int diameter;

    private Map<Integer, List<Integer>> nodeBeforeOrAfter;

    private Map<Integer, String> indexToPath;

    private Map<Integer, String> nodeLabel;

    public Graph(int index, Set<Integer> nodes, Set<Set<Integer>> edges, int[] where, Map<Integer, List<Integer>> update, int support) {
        this.index = index;
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
        this.where = where == null ? new int[0] : where;
        this.update = update == null ? new HashMap<>() : new HashMap<>(update);
        this.nodeBeforeOrAfter = new HashMap<>();
        this.indexToPath = new HashMap<>();
        this.support = support;
        this.diameter = 0;
    }

    public Graph(int index, Set<Integer> nodes, Set<Set<Integer>> edges, int[] where, Map<Integer, List<Integer>> update,
                 int support, Map<Integer, String> nodeLabel){
        this(index, nodes, edges, where, update, support);
        this.nodeLabel = nodeLabel == null ? new HashMap<>() : new HashMap<>(nodeLabel);;
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

    public Map<Integer, List<Integer>> getUpdate() {
        return this.update;
    }

    public int getDiameter() {
        return this.diameter;
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    public void setWhere(Set<Integer> whereSet) {
        List<Integer> whereList = new ArrayList<>(whereSet);
        Collections.sort(whereList);
        this.where = new int[whereList.size()];
        for (int i = 0; i < whereList.size(); i ++) {
            this.where[i] = whereList.get(i);
        }
    }

    public void setUpdate(Map<Integer, List<Integer>> update) {
        this.update = new TreeMap<>(update);
    }

    public void setNodeBeforeOrAfter(Map<Integer, List<Integer>> nodeBeforeOrAfter) {
        this.nodeBeforeOrAfter = new HashMap<>(nodeBeforeOrAfter);
    }

    public Map<Integer, List<Integer>> getNodeBeforeOrAfter() {
        return nodeBeforeOrAfter;
    }

    public void setCentralCommits(Map<Integer, List<String>> centralCommits) {
        this.centralCommits = new TreeMap<>(centralCommits);
    }

    public Map<Integer, List<String>> getCentralCommits() {
        return centralCommits;
    }

    public void setRelativeCommits(Set<String> relativeCommits) {
        this.relativeCommits = new TreeSet<>(relativeCommits);
    }

    public Set<String> getRelativeCommits() {
        return relativeCommits;
    }

    public void setIndexToPath(Map<Integer, String> indexToPath) {
        this.indexToPath = new HashMap<>(indexToPath);
    }

    public Map<Integer, String> getIndexToPath() {
        return indexToPath;
    }

    public Map<Integer, String> getNodeLabel() {
        return nodeLabel;
    }
}
