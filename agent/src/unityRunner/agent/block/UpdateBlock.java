package unityRunner.agent.block;


public class UpdateBlock extends Block {
    public UpdateBlock() {
        beginning = "Updating (.+) - GUID: .*";
        end = ".*done. \\[Time\\:.*";
        name = "Update";
    }
}