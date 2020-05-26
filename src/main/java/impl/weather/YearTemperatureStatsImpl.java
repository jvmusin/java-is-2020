package impl.weather;

import api.weather.DayTemperatureInfo;
import api.weather.YearTemperatureStats;

import java.time.Month;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class YearTemperatureStatsImpl implements YearTemperatureStats {

    private static class MonthInfo {
        int temperatureSum;
        int maxTemperature;
        DayTemperatureInfo[] temperatureByDay;
        List<DayTemperatureInfo> days;

        MonthInfo(Month m) {
            days = new ArrayList<>();
            temperatureByDay = new DayTemperatureInfo[m.maxLength() + 1];
        }

        Integer getMaxTemperature() {
            return isEmpty() ? null : maxTemperature;
        }

        Double getAverageTemperature() {
            return isEmpty() ? null : temperatureSum / (double) days.size();
        }

        void update(DayTemperatureInfo info) {
            int temperature = info.getTemperature();
            maxTemperature = isEmpty() ? temperature : Math.max(maxTemperature, temperature);
            temperatureSum += info.getTemperature();
            days.add(info);
            temperatureByDay[info.getDay()] = info;
        }

        List<DayTemperatureInfo> getSortedTemperature() {
            days.sort(Comparator.comparingInt(DayTemperatureInfo::getTemperature).reversed());
            return days;
        }

        DayTemperatureInfo getTemperature(int day) {
            return temperatureByDay[day];
        }

        boolean isEmpty() {
            return days.isEmpty();
        }
    }

    private final Map<Month, MonthInfo> infos;

    public YearTemperatureStatsImpl() {
        infos = new EnumMap<>(Month.class);
        for (Month m : Month.values()) infos.put(m, new MonthInfo(m));
    }

    @Override
    public void updateStats(DayTemperatureInfo info) {
        infos.get(info.getMonth()).update(info);
    }

    @Override
    public Double getAverageTemperature(Month month) {
        return infos.get(month).getAverageTemperature();
    }

    @Override
    public Map<Month, Integer> getMaxTemperature() {
        return infos.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().getMaxTemperature()));
    }

    @Override
    public List<DayTemperatureInfo> getSortedTemperature(Month month) {
        return infos.get(month).getSortedTemperature();
    }

    @Override
    public DayTemperatureInfo getTemperature(int day, Month month) {
        return infos.get(month).getTemperature(day);
    }
}
