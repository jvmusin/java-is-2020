package impl.weather;

import api.weather.DayTemperatureInfo;
import api.weather.YearTemperatureStats;
import api.weather.YearTemperatureStatsParser;

import java.time.Month;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearTemperatureStatsParserImpl implements YearTemperatureStatsParser {
    private static final Pattern PATTERN = Pattern.compile("(?<day>\\d+)\\.(?<month>\\d+) (?<temperature>.+)");

    @Override
    public YearTemperatureStats parse(Collection<String> rawData) {
        return rawData.stream()
                .map(PATTERN::matcher)
                .peek(Matcher::find)
                .map(m -> new DayTemperatureInfo() {
                    @Override
                    public int getDay() {
                        return Integer.parseInt(m.group("day"));
                    }

                    @Override
                    public Month getMonth() {
                        return Month.of(Integer.parseInt(m.group("month")));
                    }

                    @Override
                    public int getTemperature() {
                        return Integer.parseInt(m.group("temperature"));
                    }
                })
                .collect(YearTemperatureStatsFactory::getInstance, YearTemperatureStats::updateStats, (a, b) -> {
                });
    }
}
