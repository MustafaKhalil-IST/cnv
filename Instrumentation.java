import BIT.highBIT.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import MetricDataStorage.*;

public class Instrumentation {
    private static Map<Long, Long> metrics = new HashMap<>();

    private static String cnvProject = System.getProperty("user.dir");
    private static String metricsOutputFile = "metrics.txt";

    public static synchronized void writeMetricsToFile(String request) {
        Metric metric = getMetricByThread();
        BufferedWriter outputMetrics = null;
        try {
            FileWriter fstream = new FileWriter(cnvProject + "/" + metricsOutputFile, true);
            outputMetrics = new BufferedWriter(fstream);

            outputMetrics.write(request + "=" + metric.toString());
            System.out.println("> [Instrumentation]: Stored metrics (of T" + Thread.currentThread().getId() + ") in: " + cnvProject + "/" + metricsOutputFile);

            metric.resetMetrics();

            outputMetrics.close();
            return;
        }
        catch (Exception e) {
            System.out.println("> [Instrumentation] EXCEPTION: " + e.getMessage());
        }
    }

    public static void doInstrumentation(File in_dir, File out_dir, String metric) {
        String filelist[] = in_dir.list();

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    if (metric.equals("bbl")) {
                        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                            BasicBlock bb = (BasicBlock) b.nextElement();
                            bb.addBefore("Instrumentation", "count", new Integer(0));
                        }
                    }
                    else {
                        for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); )
                        {
                            Instruction instr = (Instruction) instrs.nextElement();
                            int opcode = instr.getOpcode();
                            int selected_opcode = -9779;
                            if (metric.equals("ttl")) {
                                instr.addBefore("Instrumentation", "count", new Integer(0));
                            }
                            if (metric.equals("comp")) {
                                selected_opcode = InstructionTable.COMPARISON_INSTRUCTION;
                            }
                            if (metric.equals("store")) {
                                selected_opcode = InstructionTable.STORE_INSTRUCTION;
                            }
                            if (metric.equals("new")) {
                                selected_opcode = InstructionTable.NEW;
                            }
                            if (metric.equals("cond")) {
                                selected_opcode = InstructionTable.CONDITIONAL_INSTRUCTION;
                            }
                            if (metric.equals("arith")) {
                                selected_opcode = InstructionTable.ARITHMETIC_INSTRUCTION;
                            }
                            if (metric.equals("log")) {
                                selected_opcode = InstructionTable.LOGICAL_INSTRUCTION;
                            }
                            if (opcode == selected_opcode)
                            {
                                instr.addBefore("Instrumentation", "count", new Integer(0));
                            }
                        }
                    }
                }
                ci.addAfter("Instrumentation", "writeMetricsToFile", metric);
                ci.write(out_filename);
            }
        }
    }

    public static synchronized void count(int incr) {
        long tid = Thread.currentThread().getId();
        Long metric = metrics.get(tid);
        if(m == null) {
            metrics.put(tid, new Long(0));
        }
        metric ++;
        DynamoStore().updateMetric(tid, m);
    }

    public static void main(String argv[]) throws Exception {
        if (argv.length != 3) {
            System.out.println("> [Instrumentation] Error: Run with \"java Instrumentation indir/ outdir/ [comp|store|new|cond|arith]\"");
            System.exit(-1);
        }

        try {
            File in_dir = new File(argv[0]);
            File out_dir = new File(argv[1]);
            String metric = argv[2];

            if (in_dir.isDirectory() && out_dir.isDirectory()) {
                System.out.println("> [Instrumentation]: Instrumenting classes ...");
                doInstrumentation(in_dir, out_dir, metric);
                System.out.println("> [Instrumentation]: Done.");
            } else {
                System.exit(-1);
            }
        } catch (NullPointerException e) {
            System.exit(-1);
        }
    }
}



