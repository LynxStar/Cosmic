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



        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE playgroups SET redemptions = ? WHERE characterid = ?"))
        {

            //ps.setInt(1, redemptions);
            //ps.setInt(2, id);

            ps.executeUpdate();
        }
        catch(SQLException e) {}

    }

    public void claimReward(Character character) {

        var rewards = DailyRewards.getAvailableDayRewards(character);

        var claimed = rewards.getSecond();

        var available = rewards.getFirst() - claimed;

        var reward = (int)claimed;

        if(available <= 0) {
            character.yellowMessage("No daily rewards available to claim. Next reward tomorrow.");
        }
        else {
            reward++;
        }

        listRewards(reward, character);

    }

    public void listRewards(int tier, Character character) {

        rewardDescription(tier + 0, character);
        rewardDescription(tier + 1, character);
        rewardDescription(tier + 2, character);
        rewardDescription(tier + 3, character);
        rewardDescription(tier + 4, character);

    }

    public void rewardDescription(int tier, Character character) {

        //0 notice
        //5 pink
        //6 lightblue

        character.dropMessage(6, String.format("Tier %d: 250 NX", tier));

    }

}
