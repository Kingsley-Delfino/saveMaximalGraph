import java.io.*;
import java.util.*;

public class MaximalGraph {
    private static final String G_SPAN_PATH = "/Users/kingsley/FDSE/gSpan/graphdata";

    // 需要分析的文件名称，14为窗口长，2为步长
    private static final String FILE_NAME = "/cassandra_file_28_7";

    // gSpan的结果导出文件名称，70为support
    private static final String G_SPAN_RESULT_NAME = FILE_NAME + "_48";

    // 对应分析文件的Map文件名称
    private static final String MAP_NAME = FILE_NAME + "(Map)";

    // 本程序输出结果文件名称
    private static final String MAXIMAL_RESULT_NAME = G_SPAN_RESULT_NAME + "(Maximal_more_node)";

    private static final String FILE_SUFFIX = ".txt";

    public static void main(String[] args) {
        Set<Graph> graphSet = readGSpanResult(G_SPAN_PATH + G_SPAN_RESULT_NAME + FILE_SUFFIX);
        Map<Integer, String> indexToFileNameMap = getIndexToFileNameMap(G_SPAN_PATH + MAP_NAME + FILE_SUFFIX);
        calculateMaximalGraph(graphSet);
        calculateGraphDiameter(graphSet);
        export(graphSet, indexToFileNameMap, G_SPAN_PATH + MAXIMAL_RESULT_NAME + FILE_SUFFIX);
    }

    public static Map<Integer,String> getIndexToFileNameMap(String filePath) {
        Map<Integer, String> indexToFileNameMap = new HashMap<>();
        try {
            String encoding="GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {// 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] array = lineTxt.split(": ");
                    indexToFileNameMap.put(Integer.parseInt(array[0]), array[1]);
                }
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
        return indexToFileNameMap;
    }

    public static Set<Graph> readGSpanResult(String filePath) {
        Set<Graph> graphSet = new HashSet<>();
        try {
            String encoding="GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                int index = -1;
                Set<Integer> nodes = new HashSet<>();
                Set<Set<Integer>> edges = new HashSet<>();
                Map<Integer, Integer> nodeReplace = new HashMap<>();
                int[] where = null;
                int support = 0;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] array = lineTxt.split(" ");
                    switch (array[0]) {
                        case "t":
                            if (index >= 0) {
                                graphSet.add(new Graph(index, nodes, edges, where, support));
                            }
                            index = Integer.parseInt(array[2]);
                            nodes.clear();
                            edges.clear();
                            nodeReplace.clear();
                            break;
                        case "v":
                            nodeReplace.put(Integer.parseInt(array[1]), Integer.parseInt(array[2]));
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
                graphSet.add(new Graph(index, nodes, edges, where, support));
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
        return graphSet;
    }

    public static void calculateMaximalGraph(Set<Graph> graphSet) {
        List<Graph> graphList = new ArrayList<>(graphSet);
        // 节点少的子图排在前面，若节点数量相等，则将边少的子图排在前面
        graphList.sort((graph1, graph2) -> {
            int nodeCount1 = graph1.getNodes().size();
            int nodeCount2 = graph2.getNodes().size();
            if (nodeCount1 == nodeCount2) {
                return Integer.compare(graph1.getEdges().size(), graph2.getEdges().size());
            }
            return Integer.compare(nodeCount1, nodeCount2);
        });
        int length = graphList.size();
        for (int i = 0; i < length; i ++) {
            for (int j = i + 1; j < length; j ++) {
                Set<Integer> nodeSet = new HashSet<>(graphList.get(i).getNodes());
                nodeSet.removeAll(graphList.get(j).getNodes());
                Set<Set<Integer>> edgeSet = new HashSet<>(graphList.get(i).getEdges());
                edgeSet.removeAll(graphList.get(j).getEdges());
                if (nodeSet.size() == 0 && edgeSet.size() == 0) {
                    System.out.println(graphList.get(i).getIndex() + " killed by " + graphList.get(j).getIndex());
                    graphSet.remove(graphList.get(i));
                    break;
                }
            }
        }
    }

    // 在一个无向图中，将任意两点间最短距离的最大值定义为图的直径
    public static void calculateGraphDiameter(Set<Graph> graphSet) {
        for (Graph graph : graphSet) {
            Set<Integer> nodes  = graph.getNodes();
            Set<Set<Integer>> edges = graph.getEdges();
            if (edges.size() == nodes.size() * (nodes.size() - 1) / 2) {
                graph.setDiameter(1);
            }
            else {
                int diameter = 0;
                int index = 0;
                // 全局下标->局部下标
                Map<Integer, Integer> map = new HashMap<>();
                int[][] K = new int[nodes.size()][nodes.size()];
                for (int i = 0; i < nodes.size(); i ++) {
                    for (int j = 0; j < nodes.size(); j ++) {
                        K[i][j] = nodes.size();
                    }
                }
                for (Set<Integer> edge : edges) {
                    Integer[] a = edge.toArray(new Integer[0]);
                    int startIndex = a[0];
                    int endIndex = a[1];
                    if (!map.containsKey(startIndex)) {
                        map.put(startIndex, index);
                        index ++;
                    }
                    if (!map.containsKey(endIndex)) {
                        map.put(endIndex, index);
                        index ++;
                    }
                    K[map.get(startIndex)][map.get(endIndex)] = 1;
                    K[map.get(endIndex)][map.get(startIndex)] = 1;
                }
                for (int k = 0; k < nodes.size(); k ++) {
                    for (int i = 0; i < nodes.size(); i ++) {
                        for (int j = 0; j < nodes.size(); j ++) {
                            if (K[i][j] > K[i][k] + K[k][j]) {
                                K[i][j] = K[i][k] + K[k][j];
                            }
                        }
                    }
                }
                for (int i = 0; i < nodes.size(); i ++) {
                    for (int j = i + 1; j < nodes.size(); j ++) {
                        diameter = Math.max(diameter, K[i][j]);
                    }
                }
                graph.setDiameter(diameter);
            }
            System.out.println("graph diameter of graph_" + graph.getIndex() + " is " + graph.getDiameter());
        }
    }

    public static void export(Set<Graph> graphSet, Map<Integer,String> indexToFileNameMap, String fileName) {
        try {
            List<Graph> graphList = new ArrayList<>(graphSet);
            graphList.sort((graph1, graph2) -> {
                int nodeCount1 = graph1.getNodes().size();
                int nodeCount2 = graph2.getNodes().size();
                int edgeCount1 = graph1.getEdges().size();
                int edgeCount2 = graph2.getEdges().size();
                int support1 = graph1.getSupport();
                int support2 = graph2.getSupport();
                if (nodeCount1 == nodeCount2) {
                    if (edgeCount1 == edgeCount2) {
                        return Integer.compare(support2, support1);
                    }
                    return Integer.compare(edgeCount2, edgeCount1);
                }
                return Integer.compare(nodeCount2, nodeCount1);
//                return Integer.compare(support2, support1);
            });
            PrintStream ps = new PrintStream(fileName);
            System.setOut(ps);
            int rank = 0;
            for (Graph graph: graphList) {
                System.out.println("Rank: " + rank ++);
                System.out.println();
                System.out.println("t # " + graph.getIndex());
                Map<Integer, Integer> nodeReplace = new HashMap<>();
                int index = 0;
                for (Integer node : graph.getNodes()) {
                    System.out.println("v " + index + " " + indexToFileNameMap.get(node)); // 输出文件名
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
                System.out.print("Where: [");
                int[] where = graph.getWhere();
                for (int i = 0; i < where.length; i ++) {
                    if (i > 0) {
                        System.out.print(", ");
                    }
                    System.out.print(where[i]);
                }
                System.out.println("]");
                System.out.println();
                System.out.println("-----------------");
                System.out.println();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}