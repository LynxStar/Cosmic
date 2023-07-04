package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.PlayGroup;
import client.command.Command;

public class PlayGroupCommand extends Command {

    {
        setDescription("Displays playgroup information.");
    }

    @Override
    public void execute(Client client, String[] params) {


        Character character = client.getPlayer();

        character.CalculatePlaygroupRates();

        var levelData = PlayGroup.getPGLevelData();

        var level = character.getLevel();

        var average = (int)Math.round(levelData.getThird());

        var diff = level - average;
        character.yellowMessage(String.format("[Playgroup]: %d to %d", levelData.getFirst(), levelData.getSecond(), average));
        character.yellowMessage(String.format("[Distance]: %d from Anchor Point: %d", diff, average));
        character.yellowMessage(String.format("[EXP Rate]: %.2f%%", character.playgroupEXPRate * 100));
        character.yellowMessage(String.format("[Drop Rate]: %.2f%%", character.playgroupDropRate * 100));

        var redemptions = PlayGroup.getCardRedemption(character);
        var redeemable = 0;

        var expNeeded = PlayGroup.getExpNeededForLevel(redemptions + 1);

        var exp = character.cashexp;

        while(exp >= expNeeded)
        {
            redeemable++;
            exp -= expNeeded;
            expNeeded = PlayGroup.getExpNeededForLevel(redemptions + 1 + redeemable);
        }

        var redemptionProgress = exp / expNeeded * 100;

        character.yellowMessage(String.format("[Cash EXP]: %d / %d %.2f%%", (int)exp, expNeeded, redemptionProgress));
        character.yellowMessage(String.format("[Redeemable]: %d Cash Level: %d", redeemable, redemptions));

    }

}
