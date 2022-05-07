import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GraphNodeCounter {

    private static final String GRAPH_PATH = "/Users/kingsley/FDSE/gSpan/graphdata/commons-io(1003)_file_k2_tw7_s3_0504";

    private static final String MAXIMAL_GRAPH = "(Maximal_Graph)";

    private static final String CHANGE_TIMES = "(Change_Times)";

    private static final String TXT_SUFFIX = ".txt";

    public static void main(String[] args) {
        try {
            PrintStream terminal = System.out;
            PrintStream ps = new PrintStream(GRAPH_PATH + CHANGE_TIMES + TXT_SUFFIX);
            System.setOut(ps);
            // 从文件中读取所有的图
            Set<Graph> graphSet = GraphUtils.readGraph(GRAPH_PATH + MAXIMAL_GRAPH + TXT_SUFFIX);
            Map<Integer, Integer> nodeTimesMap = new TreeMap<>();
            for (Graph graph : graphSet) {
                Set<Integer> nodeSet = graph.getNodes();
                for (int node : nodeSet) {
                    int times = nodeTimesMap.getOrDefault(node, 0);
                    nodeTimesMap.put(node, times + 1);
                }
            }
            for (Map.Entry<Integer, Integer> entry : nodeTimesMap.entrySet()) {
                System.out.println(entry.getKey() + "_" + entry.getValue());
            }
            System.setOut(terminal);
            System.out.println("导出成功！");
        }
        catch (Exception e) {
            System.out.println("导出失败！");
        }
    }
}
