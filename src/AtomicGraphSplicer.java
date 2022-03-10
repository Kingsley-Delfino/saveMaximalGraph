import java.util.*;

public class AtomicGraphSplicer {

    private static final String G_SPAN_PATH = "/Users/kingsley/FDSE/gSpan/graphdata";

    // 需要分析的文件名称，14为窗口长
    private static final String FILE_NAME = "/commons-io(675)_file_7";

    // gSpan的结果导出文件名称，7为support
    private static final String G_SPAN_RESULT_NAME = FILE_NAME + "_5_0310";

    // 本程序读取文件名称
    private static final String MAXIMAL_RESULT_NAME = G_SPAN_RESULT_NAME + "(Maximal_Graph)";

    // 本程序输出结果文件名称
    private static final String SPLICED_RESULT_NAME = G_SPAN_RESULT_NAME + "(Spliced_Link)";

    // 文件后缀名
    private static final String FILE_SUFFIX = ".txt";

    public static void main(String[] args) {
        Set<Graph> graphSet = GraphUtils.readGraph(G_SPAN_PATH + MAXIMAL_RESULT_NAME + FILE_SUFFIX);
        Set<Graph> allSplicedGraph = spliceAtomicGraph(graphSet);
        GraphUtils.calculateMaximalGraph(allSplicedGraph);
        for (Graph graph : allSplicedGraph) {
            GraphUtils.calculateGraphDiameter(graph);
        }
        GraphUtils.export(allSplicedGraph, G_SPAN_PATH + SPLICED_RESULT_NAME + FILE_SUFFIX);
    }

    public static Set<Graph> spliceAtomicGraph(Set<Graph> graphSet) {
        Set<Graph> allSplicedGraph = new HashSet<>();
        // commit下标-在当前commit出现的频繁子图的下标集合的Map
        Map<Integer, Set<Integer>> commitIndexToGraphIndexSetMap = new HashMap<>();
        Map<Integer, Graph> graphIndexToGraphMap = new HashMap<>();
        for (Graph graph : graphSet) {
            int index = graph.getIndex();
            int[] whereArray = graph.getWhere();
            for (int where : whereArray) {
                Set<Integer> graphIndexSet = commitIndexToGraphIndexSetMap.getOrDefault(where, new HashSet<>());
                graphIndexSet.add(index);
                commitIndexToGraphIndexSetMap.put(where, graphIndexSet);
            }
            graphIndexToGraphMap.put(index, graph);
        }
        List<Integer> whereList = new ArrayList<>(commitIndexToGraphIndexSetMap.keySet());
        Collections.sort(whereList);
        List<List<Integer>> continueWhereListList = new ArrayList<>();
        List<Set<Integer>> graphIndexSetList = new ArrayList<>();
        List<Integer> continueWhereList = new ArrayList<>();
        Set<Integer> graphIndexSet = new HashSet<>();
        if (!whereList.isEmpty()) {
            int where = whereList.get(0);
            continueWhereList.add(where);
            graphIndexSet.addAll(commitIndexToGraphIndexSetMap.get(where));
        }
        for (int i = 1; i < whereList.size(); i ++) {
            int where = whereList.get(i);
            if (where - whereList.get(i - 1) > 1) {
                continueWhereListList.add(new ArrayList<>(continueWhereList));
                graphIndexSetList.add(new HashSet<>(graphIndexSet));
                continueWhereList.clear();
                graphIndexSet.clear();
            }
            continueWhereList.add(where);
            graphIndexSet.addAll(commitIndexToGraphIndexSetMap.get(where));
        }
        continueWhereListList.add(new ArrayList<>(continueWhereList));
        graphIndexSetList.add(new HashSet<>(graphIndexSet));
        continueWhereList.clear();
        graphIndexSet.clear();
        int continueGraphSize = continueWhereListList.size();
        for (int i = 0; i < continueGraphSize; i ++) {
            continueWhereList = continueWhereListList.get(i);
            graphIndexSet = graphIndexSetList.get(i);
            Set<Graph> atomicGraphSet = new HashSet<>();
            for (int atomicGraphIndex : graphIndexSet) {
                atomicGraphSet.add(graphIndexToGraphMap.get(atomicGraphIndex));
            }
            Set<Graph> splicedGraphSet = spliceRelativeAtomicGraph(continueWhereList, atomicGraphSet);
            allSplicedGraph.addAll(splicedGraphSet);
        }
        return allSplicedGraph;
    }

    private static Set<Graph> spliceRelativeAtomicGraph(List<Integer> continueWhereList, Set<Graph> atomicGraphSet) {
        Set<Graph> splicedGraphSet = new HashSet<>();
        Map<Integer, Integer> nodeIdToNodeIndexMap = new HashMap<>();
        Map<Integer, Integer> nodeIndexToNodeIdMap = new HashMap<>();
        Set<Integer> nodeIdSet = new HashSet<>();
        for (Graph atomicGraph : atomicGraphSet) {
            nodeIdSet.addAll(atomicGraph.getNodes());
        }
        int nodeIndex = 0;
        for (int nodeId : nodeIdSet) {
            nodeIdToNodeIndexMap.put(nodeId, nodeIndex);
            nodeIndexToNodeIdMap.put(nodeIndex, nodeId);
            nodeIndex ++;
        }
        boolean[][] boolGraph = new boolean[nodeIndex][nodeIndex];
        boolean[] visited = new boolean[nodeIndex];
        for (Graph atomicGraph : atomicGraphSet) {
            Set<Set<Integer>> edgeSet = atomicGraph.getEdges();
            for (Set<Integer> edge : edgeSet) {
                Integer[] edgeArray = edge.toArray(new Integer[0]);
                int startNodeIndex = nodeIdToNodeIndexMap.get(edgeArray[0]);
                int endNodeIndex = nodeIdToNodeIndexMap.get(edgeArray[1]);
                boolGraph[startNodeIndex][endNodeIndex] = true;
                boolGraph[endNodeIndex][startNodeIndex] = true;
            }
        }
        for (int i = 0; i < visited.length; i ++) {
            if (!visited[i]) {
                Graph splicedGraph = findConnectedGraph(boolGraph, visited, i, nodeIndexToNodeIdMap, continueWhereList);
                splicedGraphSet.add(splicedGraph);
            }
        }
        return splicedGraphSet;
    }

    private static Graph findConnectedGraph(boolean[][] boolGraph, boolean[] visited, int n, Map<Integer, Integer> nodeIndexToNodeIdMap, List<Integer> continueWhereList) {
        Set<Integer> nodes = new HashSet<>();
        Set<Set<Integer>> edges = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        int length = visited.length;
        queue.offer(n);
        while (!queue.isEmpty()) {
            int i = queue.poll();
            nodes.add(nodeIndexToNodeIdMap.get(i));
            visited[i] = true;
            for (int j = 0; j < length; j ++) {
                if (boolGraph[i][j] && !visited[j]) {
                    Set<Integer> edge = new HashSet<>();
                    int id_i = nodeIndexToNodeIdMap.get(i);
                    int id_j = nodeIndexToNodeIdMap.get(j);
                    if (id_i < id_j) {
                        edge.add(id_i);
                        edge.add(id_j);
                    }
                    else {
                        edge.add(id_j);
                        edge.add(id_i);
                    }
                    edges.add(edge);
                    queue.offer(j);
                }
            }
        }
        int[] where  = new int[continueWhereList.size()];
        for (int i = 0; i < continueWhereList.size(); i ++) {
            where[i] = continueWhereList.get(i);
        }
        return new Graph(-1, nodes, edges, where, -1);
    }
}
