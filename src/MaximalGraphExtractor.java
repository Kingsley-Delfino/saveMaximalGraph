import java.util.*;

public class MaximalGraphExtractor {

    private static final String G_SPAN_PATH = "/Users/kingsley/FDSE/gSpan/graphdata";

    // 需要分析的文件名称，14为窗口长
    private static final String FILE_NAME = "/commons-io(675)_file_7";

    // gSpan的结果导出文件名称，7为support
    private static final String G_SPAN_RESULT_NAME = FILE_NAME + "_5_0310";

    // 本程序输出结果文件名称
    private static final String MAXIMAL_RESULT_NAME = G_SPAN_RESULT_NAME + "(Maximal_Graph)";

    // 文件后缀名
    private static final String FILE_SUFFIX = ".txt";

    public static void main(String[] args) {
        Set<Graph> graphSet = GraphUtils.readGraph(G_SPAN_PATH + G_SPAN_RESULT_NAME + FILE_SUFFIX);
        GraphUtils.calculateMaximalGraph(graphSet);
        for (Graph graph : graphSet) {
            GraphUtils.calculateGraphDiameter(graph);
        }
        GraphUtils.export(graphSet, G_SPAN_PATH + MAXIMAL_RESULT_NAME + FILE_SUFFIX);
    }
}