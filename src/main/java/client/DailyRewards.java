package client;

import com.oracle.truffle.js.runtime.util.Pair;
import tools.DatabaseConnection;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class DailyRewards {

    public static void recordDailyLogin(Character character) {

        var sql = """
            SELECT dailyLast, dailyEarned, dailyClaimed
            FROM cosmic.playgroups p
            WHERE p.characterid = ?
        """;

        java.sql.Date lastLogin = null;
        var dailyRewards = 0;
        var dailyClaimed = 0;

        var id = character.getId();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {

            ps.setInt(1, id);

            try(ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {

                    lastLogin = rs.getDate(1);
                    dailyRewards = rs.getInt(2);
                    dailyClaimed = rs.getInt(3);

                }

            }
        }
        catch(SQLException e)
        {
        }

        var days = 1l;

        var today = new java.sql.Date(System.currentTimeMillis());

        if(lastLogin != null) {
            var diffInMillies = Math.abs(today.getTime() - lastLogin.getTime());
            days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        }

        if(days >= 0) {

            dailyRewards++;

            sql = """
                UPDATE playgroups 
                SET 
                    dailyRewards = ?,
                    dailyLast = ?
                WHERE characterid = ?
                """;

            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql))
            {

                ps.setInt(1,dailyRewards);
                ps.setDate(2, today);
                ps.setInt(3, id);

                ps.executeUpdate();
            }
            catch(SQLException e) {}

        }

    }

    public static Pair<Integer, Integer> getAvailableDayRewards(Character character) {

        var sql = """
            SELECT dailyLast, dailyEarned, dailyClaimed
            FROM cosmic.playgroups p
            WHERE p.characterid = ?
        """;

        java.sql.Date lastLogin = null;
        var dailyRewards = 0;
        var dailyClaimed = 0;

        var id = character.getId();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {

            ps.setInt(1, id);

            try(ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {

                    lastLogin = rs.getDate(1);
                    dailyRewards = rs.getInt(2);
                    dailyClaimed = rs.getInt(3);

                }

            }
        }
        catch(SQLException e)
        {
        }

        return new Pair<>(dailyRewards, dailyClaimed);

    }

    public void claimReward(Character character) {

        var rewards = getAvailableDayRewards(character);

        var available = rewards.getFirst() - rewards.getSecond();

        if() {

        }

    }

}
