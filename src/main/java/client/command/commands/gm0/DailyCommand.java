package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.DailyRewards;
import client.PlayGroup;
import client.command.Command;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DailyCommand extends Command {

    {
        setDescription("Interact with the daily reward system");
    }

    @Override
    public void execute(Client client, String[] params) {

        Character character = client.getPlayer();

        var command = params.length < 1
            ? "default"
            : params[0]
            ;

        var rewards = DailyRewards.getAvailableDayRewards(character);

        var claimed = rewards.getSecond();
        var available = rewards.getFirst() - claimed;
        var nextReward = (int)claimed + 1;

        if(command.equals("list")) {
            listRewards(nextReward, character);
        }
        else if (command.equals("claim") && available <= 0) {
            character.yellowMessage("No daily rewards available to claim. Next reward tomorrow.");
        }
        else if (command.equals("claim")) {
            claimReward(nextReward, character);
            sendClaimable(available - 1, character);
        }
        else {
            sendClaimable(available, character);

            character.yellowMessage("Syntax: @daily <list|claim>");

            character.yellowMessage("List: Outputs the next 7 rewards");
            character.yellowMessage("Claim: Claims the next reward. Must be used multiple times if there are multiple rewards");

        }

    }

    public void sendClaimable(int available, Character character) {
        if (available > 0) {

            var text = available == 1
                ? "%d more reward is claimable"
                : "%d more rewards are claimable"
                ;

            character.dropMessage(0, String.format(text, available));
        }
        else {
            character.dropMessage(5, "No more rewards are claimable");
        }
    }


    public void claimReward(int reward, Character character) {

        var cashShop = character.getCashShop();

        cashShop.gainCash(1, 250);

        DailyRewards.setClaimedRewards(reward, character);

        rewardDescription(reward, true, character);

    }

    public void listRewards(int tier, Character character) {

        rewardDescription(tier + 0, false, character);
        rewardDescription(tier + 1, false,character);
        rewardDescription(tier + 2, false,character);
        rewardDescription(tier + 3, false,character);
        rewardDescription(tier + 4, false,character);
        rewardDescription(tier + 5, false,character);
        rewardDescription(tier + 6, false,character);

    }

    public void rewardDescription(int tier, boolean earned, Character character) {

        //0 notice
        //5 pink
        //6 lightblue

        var type = 6;
        var rewardDesc = "250 NX";

        var action = earned
            ? "Claimed "
            : "Tier %d: "
            ;

        var message = String.format(action + rewardDesc, tier);

        if(type == -1) {
            character.yellowMessage(message);
        }
        else {
            character.dropMessage(type, message);
        }



    }

}
