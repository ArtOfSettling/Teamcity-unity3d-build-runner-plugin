package unityRunner.agent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.input.Tailer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: clement.dagneau
 * Date: 13/12/2011
 * Time: 14:36
 */
public class UnityRunner
{
    final UnityRunnerConfiguration configuration;
    private volatile boolean stop = false;
    private final LogParser logParser;
    private Thread runnerThread;
    private Tailer tailer;
    private TailerListener listener;

    UnityRunner(UnityRunnerConfiguration configuration, LogParser logParser) {
        this.configuration = configuration;
        this.logParser = logParser;
    }

    /**
     * @return executable name/path
     */
    @NotNull
    String getExecutable() {
        logMessage(String.format("Unity version requested: %s ", configuration.unityVersion));
        logMessage(String.format("Unity executable path: %s ", configuration.getUnityPath()));

        return configuration.getUnityPath();
    }

    /**
     * @return get arguments for executable
     */
    @NotNull
    List<String> getArgs() {
        List<String> args = new ArrayList<String>();

        if (configuration.batchMode)
            args.add("-batchmode");

        if (configuration.noGraphics)
            args.add("-nographics");

        if (configuration.quit)
            args.add("-quit");

        if (!configuration.unitySerial.equals("")) {
            args.add("-serial");
            args.add(configuration.unitySerial);
        }

        if (!configuration.buildPlayer.equals("")) {
            args.add(String.format("-%s", configuration.buildPlayer));
            args.add(String.format("%s", configuration.buildPath));
        }

        if (!configuration.projectPath.equals("")) {
            args.add("-projectPath");
            args.add(configuration.projectPath);
        }

        if (!configuration.executeMethod.equals("")) {
            args.add("-executeMethod");
            args.add(configuration.executeMethod);
        }

        if(!configuration.buildTarget.equals("")) {
            args.add("-buildTarget");
            args.add(configuration.buildTarget);
        }

        if (configuration.useCleanedLog) {
            args.add("-cleanedLogFile");
            args.add(configuration.getCleanedLogPath());
        }

        args.add(configuration.extraOpts);

        return args;
    }


    /**
     * start the unity runner
     */
    public void start()
    {
        logMessage("[Starting UnityRunner]");
        tailLogFile();
    }

    /**
     * tail the log file during running
     */
    private void tailLogFile()
    {
        initialise();

        logMessage("[tailing log file: " + configuration.getInterestedLogPath() + "]");

        File file = new File(configuration.getInterestedLogPath());
        listener = new TailerListener(this, configuration.ignoreLogBefore, configuration.ignoreLogBeforeText);
        tailer = new Tailer(file, listener, 1000);

        runnerThread = new Thread(tailer);
        runnerThread.start();
    }

    private void logMessages(List<String> lines)
    {
        for (String line : lines){
            logMessage(line);
        }
    }

    /**
     * stop the runner
     */
    public void stop()
    {
        tailer.stop();

        logMessage("[log tail process end]");
        logMessage("[Stop UnityRunner]");
    }

    private void initialise()
    {
        deleteLogFile(configuration.getInterestedLogPath());
    }

    private void deleteLogFile(String path)
    {
        File logFile = new File(path);

        if (logFile.exists()) {
            logMessage("[delete old log file]");

            if (!logFile.delete()) {
                logMessage("[FAILED TO DELETE OLD LOG FILE]");
            }
        }
    }

    void logMessage(String message)
    {
        logParser.log(message);
    }
}


