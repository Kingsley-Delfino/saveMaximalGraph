import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class AtomicGraphSplicer {

    private static final String G_SPAN_PATH = "/Users/kingsley/FDSE/gSpan/graphdata";

    // 需要分析的文件名称，14为窗口长
    private static final String FILE_NAME = "/commons-io(1361)_file_7";

    // gSpan的结果导出文件名称，7为support
    private static final String G_SPAN_RESULT_NAME = FILE_NAME + "_3_0316";

    // 本程序读取文件名称
    private static final String MAXIMAL_RESULT_NAME = G_SPAN_RESULT_NAME + "(Maximal_Graph)";

    // 本程序输出结果文件名称
    private static final String SPLICED_RESULT_NAME = G_SPAN_RESULT_NAME + "(Spliced_Link3)";

    // txt文件后缀名
    private static final String TXT_SUFFIX = ".txt";

    // json文件后缀名
    private static final String JSON_SUFFIX = ".json";

    public static void main(String[] args) {
        // 从文件中读取所有的图
        Set<Graph> graphSet = GraphUtils.readGraph(G_SPAN_PATH + MAXIMAL_RESULT_NAME + TXT_SUFFIX);
        // 对图进行拼接
        Set<Graph> allSplicedGraph = spliceAtomicGraph(graphSet);
        // 计算最大的图，消除子图
        GraphUtils.calculateMaximalGraph(allSplicedGraph);
        // 逐个计算图的直径
        for (Graph graph : allSplicedGraph) {
            GraphUtils.calculateGraphDiameter(graph);
        }
        // 寻找每个拼接之后的图所在的commit
        findWhereList(allSplicedGraph, graphSet);
        // 读取json文件
        JSONObject jsonObject = GraphUtils.readJson(G_SPAN_PATH + FILE_NAME + "_0316" + JSON_SUFFIX);
        // 寻找拼接图所涉及到的每个commit修改的文件
        findUpdate(allSplicedGraph, jsonObject);
        // 导出图
        GraphUtils.export(allSplicedGraph, G_SPAN_PATH + SPLICED_RESULT_NAME + TXT_SUFFIX);
    }

    /**
     *
     * 对图进行拼接
     * 将每个图放入其所在的where中（即出现在commit中），将连续有图出现的commit视为连续commit子串
     * 将每个连续commit子串内的所有子图拼接起来，其中会存在很多重复的图以及被其他图完全包含的子图
     * 所以还需要进一步的去重和计算最大图
     * 两个图若有共同节点，即可拼接
     *
     * @param graphSet 所有图的集合
     * @return 将每个连续commit子串内的所有子图拼接之后的结果，会存在重复的图和被其他图包含的子图，需要进一步去重和计算最大图
     *
     */
    public static Set<Graph> spliceAtomicGraph(Set<Graph> graphSet) {
        Set<Graph> allSplicedGraph = new HashSet<>();
        // commit下标-在当前commit出现的频繁子图的下标集合的Map
        Map<Integer, Set<Integer>> commitIndexToGraphIndexSetMap = new HashMap<>();
        // 图下标-图的Map
        Map<Integer, Graph> graphIndexToGraphMap = new HashMap<>();
        // 遍历图，将其存入map
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
        // 全部有子图的出现的commit
        List<Integer> whereList = new ArrayList<>(commitIndexToGraphIndexSetMap.keySet());
        Collections.sort(whereList);
        // 连续的commit子串集合
        List<List<Integer>> continueWhereListList = new ArrayList<>();
        // 每个连续的commit子串对应的所有子图下标
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
        // 遍历每个连续的commit子串
        int continueGraphSize = continueWhereListList.size();
        for (int i = 0; i < continueGraphSize; i ++) {
            graphIndexSet = graphIndexSetList.get(i);
            Set<Graph> atomicGraphSet = new HashSet<>();
            for (int atomicGraphIndex : graphIndexSet) {
                atomicGraphSet.add(graphIndexToGraphMap.get(atomicGraphIndex));
            }
            // 将当前连续的commit子串中的图尽量拼接在一起
            Set<Graph> splicedGraphSet = spliceRelativeAtomicGraph(atomicGraphSet);
            // 将得到的拼接后的所有图全部存入全局图集中
            allSplicedGraph.addAll(splicedGraphSet);
        }
        return allSplicedGraph;
    }

    /**
     *
     * 对当前连续commit子串中涉及到的原子图进行拼接
     *
     * @param atomicGraphSet 当前连续commit子串中涉及到的全部原子图
     * @return 将当前连续commit子串内的所有原子图进行拼接
     *
     */
    private static Set<Graph> spliceRelativeAtomicGraph(Set<Graph> atomicGraphSet) {
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
        // 布尔型图矩阵，存在边则为true
        boolean[][] boolGraph = new boolean[nodeIndex][nodeIndex];
        // 访问标记
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
        // 寻找全部联通分量
        for (int i = 0; i < visited.length; i ++) {
            if (!visited[i]) {
                Graph splicedGraph = findConnectedGraph(boolGraph, visited, i, nodeIndexToNodeIdMap);
                splicedGraphSet.add(splicedGraph);
            }
        }
        return splicedGraphSet;
    }

    /**
     *
     * 寻找联通分离
     *
     * @param boolGraph 布尔型图矩阵
     * @param visited 访问标记
     * @param n 当前未访问过的节点下标
     * @param nodeIndexToNodeIdMap 节点下标-节点ID的Map
     * @return 当前未访问过的节点所在的联通分量
     *
     */
    private static Graph findConnectedGraph(boolean[][] boolGraph, boolean[] visited, int n, Map<Integer, Integer> nodeIndexToNodeIdMap) {
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
        return new Graph(-1, nodes, edges, null, null, -1);
    }

    /**
     *
     * 寻找拼接图所在的commit
     * 遍历全部原子图，若该原子图被拼接图包含，则将其where加入拼接图的where
     *
     * @param splicedGraphSet 全部拼接图
     * @param graphSet 全部原子图
     *
     */
    private static void findWhereList(Set<Graph> splicedGraphSet, Set<Graph> graphSet) {
        for (Graph splicedGraph: splicedGraphSet) {
            Set<Integer> whereSet = new HashSet<>();
            Set<Integer> splicedGraphNodeSet = splicedGraph.getNodes();
            Set<Set<Integer>> splicedGraphEdgeSet = splicedGraph.getEdges();
            for (Graph graph : graphSet) {
                Set<Integer> graphNodeSet = new HashSet<>(graph.getNodes());
                Set<Set<Integer>> graphEdgeSet = new HashSet<>(graph.getEdges());
                graphNodeSet.removeAll(splicedGraphNodeSet);
                graphEdgeSet.removeAll(splicedGraphEdgeSet);
                if (graphNodeSet.size() == 0 && graphEdgeSet.size() == 0) {
                    for (int where : graph.getWhere()) {
                        whereSet.add(where);
                    }
                }
            }
            splicedGraph.setWhere(whereSet);
        }
    }

    /**
     *
     * 寻找拼接图涉及到的每个commit在当前图中修改的文件下标
     *
     * @param splicedGraphSet 全部拼接图
     * @param jsonObject 存有所有窗口中心commit修改的全部文件下标的json
     *
     */
    private static void findUpdate(Set<Graph> splicedGraphSet, JSONObject jsonObject) {
        Map<Integer, List<Integer>> allUpdateMap = new HashMap<>();
        // 读取json的内容，存入Map中
        JSONArray windowJsonArray = jsonObject.getJSONArray("windows");
        int windowSize = windowJsonArray.size();
        for (int i = 0; i < windowSize; i ++) {
            JSONObject windowJsonObject = windowJsonArray.getJSONObject(i);
            int windowIndex = windowJsonObject.getInteger("windowIndex");
            JSONArray updateJsonArray = windowJsonObject.getJSONArray("centralUpdate");
            Object[] updateArray = updateJsonArray.toArray();
            List<Integer> updateList = new ArrayList<>();
            for (Object update : updateArray) {
                updateList.add((Integer) update);
            }
            allUpdateMap.put(windowIndex, updateList);
        }
        // 寻找每个拼接图涉及到的commit在当前图中的修改文件下标
        for (Graph splicedGraph : splicedGraphSet) {
            Map<Integer, List<Integer>> updateMap = new TreeMap<>();
            int[] whereArray = splicedGraph.getWhere();
            Set<Integer> nodeSet = splicedGraph.getNodes();
            for (int where : whereArray) {
                List<Integer> updateList = new ArrayList<>(allUpdateMap.get(where));
                updateList.retainAll(nodeSet);
                Collections.sort(updateList);
                updateMap.put(where, updateList);
            }
            splicedGraph.setUpdate(updateMap);
        }
    }
}
