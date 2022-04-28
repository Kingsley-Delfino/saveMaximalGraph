import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GraphNodeCounter {

    private static final String GRAPH_PATH = "/Users/kingsley/FDSE/gSpan/graphdata/commons-io(1003)_file_k3_7_2_0415(Spliced_Link).txt";

    public static void main(String[] args) {
        // 从文件中读取所有的图
        Set<Graph> graphSet = GraphUtils.readGraph(GRAPH_PATH);
        Map<Integer, Integer> nodeTimesMap = new TreeMap<>();
        for (Graph graph : graphSet) {
            Set<Integer> nodeSet = graph.getNodes();
            for (int node : nodeSet) {
                int times = nodeTimesMap.getOrDefault(node, 0);
                nodeTimesMap.put(node, times + 1);
            }
        }
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : nodeTimesMap.entrySet()) {
            System.out.println(index + ": " + entry.getKey() + "(" + entry.getValue() + ")");
            index ++;
        }
    }
}
