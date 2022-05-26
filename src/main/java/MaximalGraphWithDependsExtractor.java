import java.io.*;
import java.util.*;

public class MaximalGraphWithDependsExtractor {

    private static final String G_SPAN_PATH = "D:\\workspace\\project\\code-change-analysis\\dataset\\tomcat";

    // 需要分析的文件名称
    private static final String FILE_NAME = "/test_convert_30";

    // gSpan的结果导出文件名称，7为support
    private static final String G_SPAN_RESULT_NAME = FILE_NAME + "_5_0526";

    // 本程序输出结果文件名称
    private static final String MAXIMAL_RESULT_NAME = G_SPAN_RESULT_NAME + "(Maximal_Graph)";

    // txt文件后缀名
    private static final String TXT_SUFFIX = ".txt";

    public static void main(String[] args) {
        // 从文件中读取所有的图
        Set<Graph> graphSet = readGraphWithDepends(G_SPAN_PATH + G_SPAN_RESULT_NAME + TXT_SUFFIX);
        // 计算最大的图，消除子图
        GraphUtils.calculateMaximalGraph(graphSet);
        // 逐个计算图的直径
        for (Graph graph : graphSet) {
            GraphUtils.calculateGraphDiameter(graph);
        }
        // 导出图
        exportWithDepends(graphSet, G_SPAN_PATH + MAXIMAL_RESULT_NAME + TXT_SUFFIX);
    }

    public static Set<Graph> readGraphWithDepends(String filePath) {
        Set<Graph> graphSet = new HashSet<>();
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
                Map<Integer, String> nodeLabel = new HashMap<>();
                int[] where = null;
                int support = 0;
                // 按行读取数据
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] array = lineTxt.split(" ");
                    switch (array[0]) {
                        case "t":
                            if (index != -1) {
                                graphSet.add(new Graph(index, nodes, edges, where, null, support, nodeLabel));
                            }
                            index = Integer.parseInt(array[2]);
                            nodes.clear();
                            edges.clear();
                            nodeLabel.clear();
                            break;
                        case "v":
                            nodeLabel.put(Integer.parseInt(array[1]), array[2]);
                            nodes.add(Integer.parseInt(array[1]));
                            break;
                        case "e":
                            Set<Integer> edge = new HashSet<>();
                            if (Integer.parseInt(array[1]) < Integer.parseInt(array[2])) {
                                edge.add(Integer.parseInt(array[1]));
                                edge.add(Integer.parseInt(array[2]));
                            }
                            else {
                                edge.add(Integer.parseInt(array[2]));
                                edge.add(Integer.parseInt(array[1]));
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
                graphSet.add(new Graph(index, nodes, edges, where, null, support, nodeLabel));
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

    public static void exportWithDepends(Set<Graph> graphSet, String fileName) {
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
                Map<Integer, String> nodeLabel = graph.getNodeLabel();
                for (Integer node : graph.getNodes()) {
                    System.out.println("v " + node + " " + nodeLabel.get(node)); // 输出文件名
                }
                for (Set<Integer> edgeSet: graph.getEdges()) {
                    List<Integer> edgeList = new ArrayList<>(edgeSet);
                    System.out.println("e " + edgeList.get(0) + " " + edgeList.get(1) + " 1");
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
                if (update != null && !update.isEmpty()) {
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

//                Map<Integer, List<String>> centralCommits = graph.getCentralCommits();
//                if (centralCommits != null && !centralCommits.isEmpty()) {
//                    System.out.println("centralCommit: [");
//                    for (Map.Entry<Integer, List<String>> entry : centralCommits.entrySet()) {
//                        System.out.print(entry.getKey());
//                        System.out.print(": [");
//                        List<String> commitList = entry.getValue();
//                        int size = commitList.size();
//                        for (int i = 0; i < size; i ++) {
//                            System.out.print(commitList.get(i));
//                            if (i < size - 1){
//                                System.out.println(", ");
//                            }
//                        }
//                        System.out.println("]");
//                    }
//                    System.out.println("]");
//                }
//
//                Set<String> relativeCommits = graph.getRelativeCommits();
//                if (relativeCommits != null && !relativeCommits.isEmpty()) {
//                    System.out.println("relativeCommits: [");
//                    int size = relativeCommits.size();
//                    int i = 0;
//                    for (String commit : relativeCommits) {
//                        System.out.print(commit);
//                        if (i < size - 1){
//                            System.out.println(", ");
//                        }
//                        i++;
//                    }
//                    System.out.println("]");
//                }

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