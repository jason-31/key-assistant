package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment;

public interface Equipment {

    public int action (int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange);

    public String getName();

    public String getDescription();

    public int getMaxLevel();

    public boolean canGetOrUpgrade(long qq);
}
