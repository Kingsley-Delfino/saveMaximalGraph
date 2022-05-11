import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.*;

public class MaximalGraphWithBeforeOrAfterExtractor {

    private static final String G_SPAN_PATH = "D:\\workspace\\project\\code-change-analysis\\dataset\\tomcat5";

    // Map所在的路径
    private static final String ID_TO_INDEX_MAP_NAME = "/tomcat1000_file(Map)_0402.txt";

    // 需要分析的文件名称，7为窗口长
    private static final String FILE_NAME = "/tomcat_file_m15_k2_tw4";

    //support
    private static final String SUPPORT = "_3";

    //日期
    private static final String DATE = "_0511";

    // gSpan的结果导出文件名称
    private static final String G_SPAN_RESULT_NAME = FILE_NAME + SUPPORT + DATE;

    // 原始图及对应的json文件名，二者一致
    private static final String G_SPAN_ORIGINAL_GRAPH_NAME = FILE_NAME + DATE;

    // 本程序输出结果文件名称
    private static final String MAXIMAL_RESULT_NAME = G_SPAN_RESULT_NAME + "(Maximal_Graph)";

    // txt文件后缀名
    private static final String TXT_SUFFIX = ".txt";

    // json文件后缀名
    private static final String JSON_SUFFIX = ".json";

    public static void main(String[] args) {
        // 从文件中读取所有的图
        Set<Graph> graphSet = GraphUtils.readGraph(G_SPAN_PATH + G_SPAN_RESULT_NAME + TXT_SUFFIX);
        // 计算最大的图，消除子图
        GraphUtils.calculateMaximalGraph(graphSet);
        // 逐个计算图的直径
        for (Graph graph : graphSet) {
            GraphUtils.calculateGraphDiameter(graph);
        }

        // 读取原始图
        List<Graph> originalGraphList = readGraphWithNodeAttribute(G_SPAN_PATH + G_SPAN_ORIGINAL_GRAPH_NAME + TXT_SUFFIX);

        //读取对应的json文件
        JSONObject jsonObject = GraphUtils.readJson(G_SPAN_PATH + G_SPAN_ORIGINAL_GRAPH_NAME + JSON_SUFFIX);

        Map<String, Map<Integer, String>> indexToPathMap = GraphUtils.getIndexToPathMap(G_SPAN_PATH + ID_TO_INDEX_MAP_NAME);

        //更新图的节点的前后变更信息
        updateGraphNodeAttribute(graphSet, originalGraphList, jsonObject, indexToPathMap);

        // 导出图
        exportWithNodeAttribute(graphSet, G_SPAN_PATH + MAXIMAL_RESULT_NAME + TXT_SUFFIX);
    }

    /**
     *
     * 从指定的文件中读取图，图的格式一般如下：
     *
     * # 1
     * v 0 132
     * v 1 2343
     * v 2 23
     * e 0 1 1
     * e 0 2 21
     *
     * Support: 5
     * where: [3, 4, 5, 6, 7]
     *
     * -----------------
     *
     * @param filePath 读取的文件路径
     * @return 从文件中读取到的图的集合
     *
     */
    public static List<Graph> readGraphWithNodeAttribute(String filePath) {
        List<Graph> graphList = new ArrayList<>();
        try {
            String encoding="GBK";
            File file = new File(filePath);
            // 判断文件是否存在
            if (file.isFile() && file.exists()) {
                // 考虑到编码格式
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                int index = -1;
                Set<Integer> nodes = new HashSet<>();
                Set<Set<Integer>> edges = new HashSet<>();
                Map<Integer, Integer> nodeReplace = new HashMap<>();
                Map<Integer, List<Integer>> nodeBeforeOrAfter = new HashMap<>();
                int[] where = null;
                int support = 0;
                // 按行读取数据
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] array = lineTxt.split(" ");
                    switch (array[0]) {
                        case "t":
                            if (index >= 0) {
                                Graph graph = new Graph(index, nodes, edges, where, null, support);
                                graph.setNodeBeforeOrAfter(nodeBeforeOrAfter);
                                graphList.add(graph);
                            }
                            index = Integer.parseInt(array[2]);
                            nodes.clear();
                            edges.clear();
                            nodeReplace.clear();
                            nodeBeforeOrAfter.clear();
                            break;
                        case "v":
                            nodeReplace.put(Integer.parseInt(array[1]), Integer.parseInt(array[2]));
                            ArrayList<Integer> position = new ArrayList<>(1);
                            position.add(Integer.parseInt(array[4]));
                            nodeBeforeOrAfter.put(Integer.parseInt(array[2]), position);
                            nodes.add(Integer.parseInt(array[2]));
                            break;
                        case "e":
                            Set<Integer> edge = new HashSet<>();
                            if (nodeReplace.get(Integer.parseInt(array[1])) < nodeReplace.get(Integer.parseInt(array[2]))) {
                                edge.add(nodeReplace.get(Integer.parseInt(array[1])));
                                edge.add(nodeReplace.get(Integer.parseInt(array[2])));
                            }
                            else {
                                edge.add(nodeReplace.get(Integer.parseInt(array[2])));
                                edge.add(nodeReplace.get(Integer.parseInt(array[1])));
                            }
                            edges.add(edge);
                            break;
                        case "Support:":
                            support = Integer.parseInt(array[1]);
                            break;
                        case "where:":
                            array = lineTxt.split("\\[");
                            array = array[1].split("]");
                            array = array[0].split(", ");
                            where = new int[array.length];
                            for (int i = 0; i < array.length; i ++) {
                                where[i] = Integer.parseInt(array[i]);
                            }
                            Arrays.sort(where);
                            break;
                    }
                }
                Graph graph = new Graph(index, nodes, edges, where, null, support);
                graph.setNodeBeforeOrAfter(nodeBeforeOrAfter);
                graphList.add(graph);
                read.close();
            }
            else {
                System.out.println("找不到指定的文件");
            }
        }
        catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return graphList;
    }

    /**
     *
     * 将图导出到目标文件，导出图的格式一般为：
     *
     * # 1
     * v 0 132
     * v 1 2343
     * v 2 23
     * e 0 1 1
     * e 0 2 21
     *
     * Support: 5
     * where: [3, 4, 5, 6, 7]
     *
     * -----------------
     *
     * @param graphSet 需要导出的图的集合
     * @param fileName 导出文件的目标文件名
     *
     */
    public static void exportWithNodeAttribute(Set<Graph> graphSet, String fileName) {
        try {
            List<Graph> graphList = new ArrayList<>(graphSet);
            GraphUtils.sortForGraph(graphList);
            PrintStream ps = new PrintStream(fileName);
            System.setOut(ps);
            int rank = 0;
            for (Graph graph: graphList) {
                System.out.println("Rank: " + rank ++);
                System.out.println();
                System.out.println("t # " + graph.getIndex());
                Map<Integer, Integer> nodeReplace = new HashMap<>();
                Map<Integer, List<Integer>> nodeBeforeOrAfter = graph.getNodeBeforeOrAfter();
                Map<Integer, String> indexToPath = graph.getIndexToPath();
                int index = 0;
                for (Integer node : graph.getNodes()) {
                    System.out.print("v " + index + " " + node); // 输出文件名
                    System.out.print(" [");
                    for (int i = 0; i < nodeBeforeOrAfter.get(node).size(); i ++){
                        if (i > 0) {
                            System.out.print(", ");
                        }
                        System.out.print(nodeBeforeOrAfter.get(node).get(i));
                    }
                    System.out.println("] " + indexToPath.get(node));
                    nodeReplace.put(node, index);
                    index ++;
                }
                for (Set<Integer> edgeSet: graph.getEdges()) {
                    List<Integer> edgeList = new ArrayList<>(edgeSet);
                    System.out.println("e " + nodeReplace.get(edgeList.get(0)) + " " + nodeReplace.get(edgeList.get(1)) + " 1");
                }
                System.out.println();
                System.out.println("Diameter: " + graph.getDiameter());
                System.out.println("Support: " + graph.getSupport());
                System.out.print("where: [");
                int[] where = graph.getWhere();
                for (int i = 0; i < where.length; i ++) {
                    if (i > 0) {
                        System.out.print(", ");
                    }
                    System.out.print(where[i]);
                }
                System.out.println("]");

                Map<Integer, List<Integer>> update = graph.getUpdate();
                if (!update.isEmpty()) {
                    System.out.println("Update: [");
                    for (Map.Entry<Integer, List<Integer>> entry : update.entrySet()) {
                        System.out.print(entry.getKey());
                        System.out.print(": [");
                        List<Integer> updateList = entry.getValue();
                        for (int i = 0; i < updateList.size(); i ++) {
                            if (i > 0) {
                                System.out.print(", ");
                            }
                            System.out.print(updateList.get(i));
                        }
                        System.out.println("]");
                    }
                    System.out.println("]");
                }

                Map<Integer, List<String>> centralCommits = graph.getCentralCommits();
                if (centralCommits != null && !centralCommits.isEmpty()) {
                    System.out.println("centralCommit: [");
                    for (Map.Entry<Integer, List<String>> entry : centralCommits.entrySet()) {
                        System.out.print(entry.getKey());
                        System.out.print(": [");
                        List<String> commitList = entry.getValue();
                        int size = commitList.size();
                        for (int i = 0; i < size; i ++) {
                            System.out.print(commitList.get(i));
                            if (i < size - 1){
                                System.out.println(", ");
                            }
                        }
                        System.out.println("]");
                    }
                    System.out.println("]");
                }

                Set<String> relativeCommits = graph.getRelativeCommits();
                if (relativeCommits != null && !relativeCommits.isEmpty()) {
                    System.out.println("relativeCommits: [");
                    int size = relativeCommits.size();
                    int i = 0;
                    for (String commit : relativeCommits) {
                        System.out.print(commit);
                        if (i < size - 1){
                            System.out.println(", ");
                        }
                        i++;
                    }
                    System.out.println("]");
                }

                System.out.println();
                System.out.println("-----------------");
                System.out.println();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateGraphNodeAttribute(Set<Graph> graphSet, List<Graph> originalGraphList,
                                                JSONObject jsonObject, Map<String, Map<Integer, String>> indexToPathMap){
        // 读取json的内容，存入Map中
        JSONArray windowJsonArray = jsonObject.getJSONArray("windows");
        for (Graph graph : graphSet) {
            int[] allWhere = graph.getWhere();
            Map<Integer, List<Integer>> nodePositions = new HashMap<>();
            Map<Integer, List<String>> centralCommit = new TreeMap<>();
            Map<Integer, String> indexToPath = new HashMap<>();
            Set<String> relativeCommits = new TreeSet<>();
            Set<Integer> nodes = graph.getNodes();
            for (int where : allWhere){
                Graph originalGraph = originalGraphList.get(where);
                Map<Integer, List<Integer>> nodeBeforeOrAfter = originalGraph.getNodeBeforeOrAfter();
                nodes.forEach(node ->{
                    List<Integer> positions = nodePositions.getOrDefault(node, new ArrayList<>());
                    positions.addAll(nodeBeforeOrAfter.get(node));
                    nodePositions.put(node, positions);
                });

                JSONObject windowJsonObject = windowJsonArray.getJSONObject(where);
                JSONArray centralJsonArray = windowJsonObject.getJSONArray("centralCommit");
                List<String> centralCmts = centralJsonArray.toJavaList(String.class);
                centralCommit.put(where, centralCmts);

                JSONArray relativeJsonArray = windowJsonObject.getJSONArray("relativeCommits");
                List<String> relativeCmts = relativeJsonArray.toJavaList(String.class);
                relativeCommits.addAll(relativeCmts);
            }

            int where_size = allWhere.length;
            JSONObject windowJsonObject = windowJsonArray.getJSONObject(allWhere[where_size -1]);
            JSONArray centralJsonArray = windowJsonObject.getJSONArray("centralCommit");
            List<String> centralCmts = centralJsonArray.toJavaList(String.class);
            int centralCmtsSize = centralCmts.size();
            String latestCommit = centralCmts.get(centralCmtsSize - 1);
            String commit =  latestCommit.split("_")[1];
            Map<Integer, String> allIndexToPath = indexToPathMap.get(commit);
            for (Integer index : nodes){
                String path = allIndexToPath.get(index);
                if (path == null){
                    path = "";
                }
                indexToPath.put(index, path);
            }

            graph.setNodeBeforeOrAfter(nodePositions);
            graph.setCentralCommits(centralCommit);
            graph.setRelativeCommits(relativeCommits);
            graph.setIndexToPath(indexToPath);
        }
    }
}