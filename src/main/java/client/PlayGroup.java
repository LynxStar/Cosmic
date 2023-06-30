package client;

import com.oracle.truffle.js.runtime.util.Triple;
import com.oracle.truffle.js.runtime.util.Pair;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

public class PlayGroup {

    public static Triple<Integer,Integer,Double> getPGLevelData() {

        var levels = new ArrayList<Integer>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT MAX(level) as 'level' FROM cosmic.characters WHERE accountid != 1 GROUP BY accountid"))
        {
            try(ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    levels.add(rs.getInt("level"));
                }

            }
        }
        catch(SQLException e) {}

        var minLevel = Collections.min(levels);
        var maxLevel = Collections.max(levels);
        var median = calculateMedian(levels);

        return new Triple<>(minLevel,maxLevel,median);

    }

    public static Pair<Float, Double> calculateRates(Character character) {

        var levelData = getPGLevelData();

        var median = levelData.getThird();
        var average = levelData.getSecond() - levelData.getFirst();

        var level = character.getLevel();
        var levelDiff =  - average;

        Float exp = 1f;
        Double drop = 1d;

        if(level <= median) {
            if(levelDiff <= -5) {
                exp = 3.5f;
            }
            else if(levelDiff == -4) {
                exp = 2.5f;
            }
            else if(levelDiff == -3) {
                exp = 2.0f;
            }
            else if(levelDiff == -2) {
                exp = 1.5f;
            }
            else if(levelDiff == -1) {
                exp = 1.25f;
            }
        }
        else if(levelDiff > 1) {
            if(levelDiff == 2) {
                exp = .9f;
                drop = 1.1;
            }
            else if(levelDiff == 3) {
                exp = 0.75f;
                drop = 1.25;
            }
            else if(levelDiff == 4) {
                exp = 0.65f;
                drop = 1.35;
            }
            else if(levelDiff == 5) {
                exp = .45f;
                drop = 1.5;
            }
            else if(levelDiff == 6) {
                exp = .35f;
                drop = 1.75;
            }
            else if(levelDiff == 7) {
                exp = .30f;
                drop = 2.00;
            }
            else if(levelDiff < 10) {
                exp = .25f;
                drop = 2.25;
            }
            else if(levelDiff < 15){
                exp = .2f;
                drop = 2.75;
            }
            else {
                exp = .1f;
                drop = 3.0;
            }
        }

        return new Pair<>(exp, drop);

    }

    public static double calculateMedian(ArrayList<Integer> numbers) {
        Collections.sort(numbers);

        int size = numbers.size();
        if (size % 2 == 0) {
            return (numbers.get(size / 2 - 1) + numbers.get(size / 2)) / 2.0;
        } else {
            return numbers.get(size / 2);
        }
    }

    public static int getCardRedemption(Character character) {

        var id = character.getAccountID();

        var redemptions = 0;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT cardsRedeemed FROM cosmic.accounts WHERE id = ?"))
        {

            ps.setInt(1, id);

            try(ResultSet rs = ps.executeQuery())
            {

                while (rs.next())
                {
                    redemptions = rs.getInt("cardsRedeemed");
                }

            }
        }
        catch(SQLException e) {}

        return redemptions;

    }

    public static int getTotalExpNeeded(int card) {


        var growth = Math.pow(1.1, card - 1);

        return (int)(1000*card*growth);

    }

}
