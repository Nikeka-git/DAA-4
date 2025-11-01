package util;

/**
 * Простейший интерфейс для сборщиков метрик.
 */
public interface Metrics {
    void inc(String name);
    long getCount(String name);
    void startTimer(String name);
    void stopTimer(String name);
    long getTime(String name); // время в наносекундах
}
