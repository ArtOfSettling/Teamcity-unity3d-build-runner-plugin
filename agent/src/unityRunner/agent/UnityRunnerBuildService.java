package unityRunner.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.TerminationAction;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import org.jetbrains.annotations.NotNull;

public class UnityRunnerBuildService extends BuildServiceAdapter {
    private UnityRunner runner;
    private boolean failOnError;

    public UnityRunnerBuildService() {
    }

    @Override
    public void afterInitialized() {
        java.io.File lineListDefinition = new java.io.File(getConfig().lineListPath);
        java.io.File logBlockDefinition = new java.io.File(getConfig().logBlockPath);
        failOnError = getConfig().failOnError;
        runner = new UnityRunner(getConfig(), new LogParser(getLogger(), getConfig().failOnError, lineListDefinition, logBlockDefinition));
    }

    @Override
    public void beforeProcessStarted() {
        runner.start();
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        return createProgramCommandline(runner.getExecutable(), runner.getArgs());
    }

    @NotNull
    private UnityRunnerConfiguration getConfig() {
        return new UnityRunnerConfiguration(getAgentConfiguration(), getRunnerParameters(), getBuild());
    }

    @Override
    @NotNull
    public TerminationAction interrupt() {
        runner.stop();
        return super.interrupt();
    }

    @Override
    public void afterProcessFinished() {
        // called when process is finished, BEFORE getting return code
        runner.stop();
    }

    @Override
    public void afterProcessSuccessfullyFinished() {
    }

    @Override
    @NotNull
    public BuildFinishedStatus getRunResult(final int exitCode) {
        if(!failOnError)
            return BuildFinishedStatus.FINISHED_SUCCESS;

        if(runner != null)
        {
            int errorCount = runner.getErrorCount();
            if(errorCount > 0)
                return BuildFinishedStatus.FINISHED_FAILED;
        }

        if (exitCode == 0)
            return BuildFinishedStatus.FINISHED_SUCCESS;

        return BuildFinishedStatus.FINISHED_FAILED;
    }
}
