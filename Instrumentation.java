import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;
import store.Store;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;

public class Instrumentation {
    private static HashMap<Long, Integer> count =  new HashMap<>();

    public static void main(String[] args) {
	System.out.println("Instrumenting");
        String outputFolder = args[1];

        if (args[0].endsWith(".class")) {
            if (!args[0].contains(System.getProperty("file.separator"))) {
                instrument(args[0], ".", outputFolder);
            } else {
                int index = args[0].lastIndexOf(System.getProperty("file.separator"));
                String sourceFolder = args[0].substring(0, index);
                String classFile = args[0].substring(index + 1);
                instrument(classFile, sourceFolder, outputFolder);
            }
        } else {
            File file_in = new File(args[0]);
            String infilenames[] = file_in.list();
            if (infilenames == null) {
                return;
            }
            for (int i = 0; i < infilenames.length; i++) {
                String infilename = infilenames[i];
                if (infilename.endsWith(".class")) {
                    instrument(infilename, args[0], outputFolder);
                }
            }
        }
    }

    private static synchronized void instrument(String filename, String srcFolder, String destFolder) {
        // create class info object
        ClassInfo ci;
        ci = new ClassInfo(srcFolder + System.getProperty("file.separator") + filename);
        System.out.println("Instrumenting ...");
        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            routine.addBefore("Instrumentation", "updateCount", new Integer(1));
            if (routine.getMethodName().contentEquals("solveSudoku")) {
		System.out.println("Stored: " + Thread.currentThread().getId());
                routine.addAfter("Instrumentation", "storeCount", ci.getClassName());
            }
        }
        ci.write(destFolder + System.getProperty("file.separator") + filename);
    }

    public static synchronized void storeCount(String str) {
        Store.getStore().storeCallsCount(Thread.currentThread().getId(), count.get(Thread.currentThread().getId()));
        count.put(Thread.currentThread().getId(), 0);
    }

    public static synchronized void updateCount(int incr) {
        Integer calls_count = count.get(Thread.currentThread().getId());
        if (calls_count == null) {
            count.put(Thread.currentThread().getId(), 0);
        } else {
            count.put(Thread.currentThread().getId(), calls_count + 1);
            Store.getStore().updateCallsCount(Thread.currentThread().getId(), calls_count + 1);
        }
    }
}

