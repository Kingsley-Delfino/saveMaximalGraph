import java.io.*;
import java.util.*;

public class MaximalGraph {
    private static final String G_SPAN_PATH = "/Users/kingsley/FDSE/gSpan/graphdata";

    // 需要分析的文件名称，7为步长
    private static final String FILE_NAME = "/pdfbox_file_2";

    // gSpan的结果导出文件名称，30为support
    private static final String G_SPAN_RESULT_NAME = FILE_NAME + "_100";

    // 对应分析文件的Map文件名称
    private static final String G_SPAN_MAP_NAME = FILE_NAME + "(Map)";

    // 本程序输出结果文件名称
    private static final String MAXIMAL_RESULT_NAME = G_SPAN_RESULT_NAME + "(Maximal)";

    private static final String FILE_SUFFIX = ".txt";

    public static void main(String[] args) {
        Set<Graph> graphs = readGSpanResult(G_SPAN_PATH + G_SPAN_RESULT_NAME + FILE_SUFFIX);
        Map<Integer,String> indexToFileNameMap = getIndexToFileNameMap(G_SPAN_PATH + G_SPAN_MAP_NAME + FILE_SUFFIX);
        calculateMaximalGraph(graphs);
        export(graphs, indexToFileNameMap, G_SPAN_PATH + MAXIMAL_RESULT_NAME + FILE_SUFFIX);
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
        Set<Graph> graphs = new HashSet<>();
        try {
            String encoding="GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                int index = -1;
                Set<Integer> nodes = new HashSet<>();
                Set<Set<Integer>> edges =new HashSet<>();
                Map<Integer,Integer> nodeReplace = new HashMap<>();
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] array = lineTxt.split(" ");
                    switch (array[0]) {
                        case "t":
                            if (index >= 0) {
                                graphs.add(new Graph(index, nodes, edges));
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
                    }
                }
                graphs.add(new Graph(index, nodes, edges));
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
        return graphs;
    }

//    public static void calculateMaximalGraph(Set<Graph> graphs) {
//        Graph[] graphsArray = new Graph[graphs.size()];
//        graphs.toArray(graphsArray);
//        for (int i = 0; i < graphsArray.length - 1; i ++) {
//            for (int j = i + 1; j < graphsArray.length; j ++) {
//                if (!(new HashSet<>(graphsArray[i].getNodes()).retainAll(new HashSet<>(graphsArray[j].getNodes()))) && !(new HashSet<>(graphsArray[i].getEdges()).retainAll(new HashSet<>(graphsArray[j].getEdges())))) {
//                    System.out.println(graphsArray[i].getIndex() + " killed by " + graphsArray[j].getIndex());
//                    graphs.remove(graphsArray[i]);
//                    calculateMaximalGraph(graphs);
//                    return;
//                }
//                else if (!(new HashSet<>(graphsArray[j].getNodes()).retainAll(new HashSet<>(graphsArray[i].getNodes()))) && !(new HashSet<>(graphsArray[j].getEdges()).retainAll(new HashSet<>(graphsArray[i].getEdges())))) {
//                    System.out.println(graphsArray[j].getIndex() + " killed by " + graphsArray[i].getIndex());
//                    graphs.remove(graphsArray[j]);
//                    calculateMaximalGraph(graphs);
//                    return;
//                }
//            }
//        }
//    }

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

    public static void export(Set<Graph> graphs, Map<Integer,String> indexToFileNameMap, String fileName) {
        try {
            PrintStream ps = new PrintStream(fileName);
            System.setOut(ps);
            for (Graph graph: graphs) {
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
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
