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
             PreparedStatement ps = con.prepareStatement("SELECT MAX(level) as 'level' FROM cosmic.characters GROUP BY accountid"))
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

        var levelDiff = character.getLevel() - levelData.getThird();

        Float exp = 1f;
        Double drop = 1d;

        if(levelDiff < 0) {
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
            else if(levelDiff >= 5) {
                exp = .35f;
                drop = 1.66;
            }
            else if(levelDiff >= 10){
                exp = .2f;
                drop = 2.5;
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

}
