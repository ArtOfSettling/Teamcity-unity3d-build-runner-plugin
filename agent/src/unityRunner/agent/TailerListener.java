package unityRunner.agent;

import org.apache.commons.io.input.TailerListenerAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: clement.dagneau
 * Date: 14/12/2011
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class TailerListener extends TailerListenerAdapter {
    private final UnityRunner runner;
    private boolean stillIgnoringLines;
    private boolean ignoreLogBefore;
    private String ignoreLogBeforeText;

    public TailerListener(UnityRunner runner, boolean ignoreLogBefore, String ignoreLogBeforeText) {
        this.runner = runner;
        this.ignoreLogBefore = ignoreLogBefore;
        this.ignoreLogBeforeText = ignoreLogBeforeText;
        this.stillIgnoringLines = ignoreLogBefore;
        workAroundTailerBug = false;
    }
    
    private String firstLine;
    private int lineCount;
    private boolean workAroundTailerBug;
    private int skipLineCount;
    private Boolean hasFirstLogFired = false;

    @Override
    public void handle(String line) {
        if(!hasFirstLogFired) {
            hasFirstLogFired = true;

            if (ignoreLogBefore) {
                runner.logMessage("[Ignoring lines before text " + ignoreLogBeforeText +"]");
                runner.logMessage("##teamcity[blockOpened name='ignoredLines']");
            }
        }

        if (stillIgnoringLines && line.contentEquals(ignoreLogBeforeText)){
            stillIgnoringLines = false;
            runner.logMessage("##teamcity[blockClosed name='ignoredLines']");
        }

        if(stillIgnoringLines) {
            runner.logMessage(line);
            return;
        }

        lineCount++;
               
        if (workAroundTailerBug) {
            // skip the ENTIRE FILE that will be regurgitated up until the point we saw before
            if (lineCount < skipLineCount) {
                return;
            }

            workAroundTailerBug = false;
            runner.logMessage("WARNING: [SKIPPED SOME LINES TO WORK AROUND BUG IN TAILER]");
        }
        else if (lineCount == 0) {
            firstLine = line;
            runner.logMessage("[FIRST LOG LINE]");

        }
        else if (line.equals(firstLine)) {
            // I think this is generally a bug!
            // the ENTIRE file will be regurgitated to me now :(
            // https://issues.apache.org/jira/browse/IO-279 is the bug for them to fix
            workAroundTailerBug = true;
            skipLineCount = lineCount - 1;
            lineCount = 1;
            runner.logMessage(String.format("WARNING: [WORKING AROUND BUG IN TAILER - skipping next %d lines]", skipLineCount));
            return;
        }
        runner.logMessage(line);
    }
}
