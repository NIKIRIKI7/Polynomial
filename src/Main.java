import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import math.Point;
import math.LagrangePolynomial;
import math.NewtonPolynomial;
import math.Polynomial;

public class Main {
    // Константы для тестирования
    private static final int WARM_UP_ITERATIONS = 5;
    private static final int BENCHMARK_ITERATIONS = 10;
    private static final double EVALUATION_POINT = 0.5;
    private static final long SEED = 42; // Фиксированное зерно для воспроизводимости результатов
    
    public static void main(String[] args) {
        System.out.println("=== Сравнение производительности полиномов Ньютона и Лагранжа ===");
        
        // Тестируем на разных размерах для сравнения производительности
        testPerformance(10, "Маленький размер (10 точек)");
        testPerformance(50, "Средний размер (50 точек)");
        testPerformance(100, "Большой размер (100 точек)");
        testPerformance(500, "Очень Большой размер (500 точек)");
        
        // Демонстрационный пример для проверки корректности
        demonstrationExample();
    }
    
    private static void testPerformance(int degree, String testName) {
        System.out.println("\n" + testName);
        System.out.println("--------------------------------------------");
        
        List<Point> points = generateRandomPoints(degree + 1);
        
        // Прогрев JVM перед замерами производительности
        warmup(points);
        
        // Тестирование с повторениями для более точных результатов
        long[] lagrangeCreationTimes = new long[BENCHMARK_ITERATIONS];
        long[] newtonCreationTimes = new long[BENCHMARK_ITERATIONS];
        long[] lagrangeEvalTimes = new long[BENCHMARK_ITERATIONS];
        long[] newtonEvalTimes = new long[BENCHMARK_ITERATIONS];
        long[] addPointLagrangeTimes = new long[BENCHMARK_ITERATIONS];
        long[] addPointNewtonTimes = new long[BENCHMARK_ITERATIONS];
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            // Тестирование создания полинома Лагранжа
            long startLagrange = System.nanoTime();
            LagrangePolynomial lagrange = new LagrangePolynomial(new ArrayList<>(points));
            lagrangeCreationTimes[i] = System.nanoTime() - startLagrange;
            
            // Тестирование вычисления в точке полинома Лагранжа
            long startLagrangeEval = System.nanoTime();
            lagrange.evaluate(EVALUATION_POINT);
            lagrangeEvalTimes[i] = System.nanoTime() - startLagrangeEval;
            
            // Тестирование добавления точки в полином Лагранжа
            Point newPointLagrange = new Point(1000 + i, 2000 + i);
            long startAddLagrange = System.nanoTime();
            lagrange.addPoint(newPointLagrange);
            addPointLagrangeTimes[i] = System.nanoTime() - startAddLagrange;
            
            // Тестирование создания полинома Ньютона
            long startNewton = System.nanoTime();
            NewtonPolynomial newton = new NewtonPolynomial(new ArrayList<>(points));
            newtonCreationTimes[i] = System.nanoTime() - startNewton;
            
            // Тестирование вычисления в точке полинома Ньютона
            long startNewtonEval = System.nanoTime();
            newton.evaluate(EVALUATION_POINT);
            newtonEvalTimes[i] = System.nanoTime() - startNewtonEval;
            
            // Тестирование добавления точки в полином Ньютона
            Point newPointNewton = new Point(2000 + i, 3000 + i);
            long startAddNewton = System.nanoTime();
            newton.addPoint(newPointNewton);
            addPointNewtonTimes[i] = System.nanoTime() - startAddNewton;
            
            // Принудительная сборка мусора для уменьшения влияния на последующие итерации
            if (i % 2 == 0) {
                System.gc();
                try {
                    Thread.sleep(10); // Небольшая пауза для завершения GC
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // Вычисление среднего времени и стандартного отклонения
        double avgLagrangeCreation = getAverageWithoutOutliers(lagrangeCreationTimes);
        double avgNewtonCreation = getAverageWithoutOutliers(newtonCreationTimes);
        double avgLagrangeEval = getAverageWithoutOutliers(lagrangeEvalTimes);
        double avgNewtonEval = getAverageWithoutOutliers(newtonEvalTimes);
        double avgAddPointLagrange = getAverageWithoutOutliers(addPointLagrangeTimes);
        double avgAddPointNewton = getAverageWithoutOutliers(addPointNewtonTimes);
        
        // Вывод результатов
        System.out.println("Время создания Лагранжа: " + formatTime(avgLagrangeCreation));
        System.out.println("Время создания Ньютона: " + formatTime(avgNewtonCreation));
        System.out.println("Отношение Ньютон/Лагранж при создании: " + 
                         String.format("%.2f", avgNewtonCreation / avgLagrangeCreation));
        
        System.out.println("\nВремя вычисления значения Лагранж: " + formatTime(avgLagrangeEval));
        System.out.println("Время вычисления значения Ньютон: " + formatTime(avgNewtonEval));
        System.out.println("Отношение Ньютон/Лагранж при вычислении: " + 
                         String.format("%.2f", avgNewtonEval / avgLagrangeEval));
        
        System.out.println("\nВремя добавления точки (Лагранж): " + formatTime(avgAddPointLagrange));
        System.out.println("Время добавления точки (Ньютон): " + formatTime(avgAddPointNewton));
        System.out.println("Отношение Лагранж/Ньютон при добавлении: " + 
                         String.format("%.2f", avgAddPointLagrange / avgAddPointNewton));
    }
    
    // Форматирование времени в читаемом виде
    private static String formatTime(double nanoTime) {
        if (nanoTime < 1000) {
            return String.format("%.2f нс", nanoTime);
        } else if (nanoTime < 1_000_000) {
            return String.format("%.2f мкс", nanoTime / 1000);
        } else if (nanoTime < 1_000_000_000) {
            return String.format("%.2f мс", nanoTime / 1_000_000);
        } else {
            return String.format("%.2f с", nanoTime / 1_000_000_000);
        }
    }
    
    private static double getAverageWithoutOutliers(long[] times) {
        // Сортируем массив для удаления выбросов
        java.util.Arrays.sort(times);
        
        // Отбрасываем 20% самых быстрых и 20% самых медленных результатов
        int excludeCount = Math.max(1, times.length / 5);
        long sum = 0;
        int count = 0;
        
        for (int i = excludeCount; i < times.length - excludeCount; i++) {
            sum += times[i];
            count++;
        }
        
        return count > 0 ? (double) sum / count : 0;
    }
    
    private static void warmup(List<Point> points) {
        // Прогрев JVM для более точных измерений
        for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
            // Создаем и используем полиномы для прогрева JIT-компилятора
            try {
                LagrangePolynomial lagrange = new LagrangePolynomial(new ArrayList<>(points));
                lagrange.evaluate(EVALUATION_POINT);
                lagrange.addPoint(new Point(1001 + i, 1001 + i));
                
                NewtonPolynomial newton = new NewtonPolynomial(new ArrayList<>(points));
                newton.evaluate(EVALUATION_POINT);
                newton.addPoint(new Point(2001 + i, 2001 + i));
                
                // Сборка мусора
                if (i % 2 == 0) {
                    System.gc();
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception | Error e) {
                System.err.println("Ошибка при прогреве: " + e.getMessage());
            }
        }
        
        // Финальная сборка мусора
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Демонстрационный пример для наглядности
    private static void demonstrationExample() {
        System.out.println("\n=== Демонстрационный пример ===");
        
        Polynomial p1 = new Polynomial(1.0, 2.0, 3.0);
        System.out.println("Пример полинома: " + p1);

        List<Point> initialPoints = List.of(
                new Point(1, 2),
                new Point(2, 3),
                new Point(4, 7)
        );

        long startNewton = System.nanoTime();
        NewtonPolynomial newtonPoly = new NewtonPolynomial(initialPoints);
        long newtonTime = System.nanoTime() - startNewton;
        
        System.out.println("\nПолином Ньютона (создан за " + formatTime(newtonTime) + "):");
        System.out.println(newtonPoly);

        // Вычисляем значение полинома в точке x = 3
        long startEvalNewton = System.nanoTime();
        double valueAt3 = newtonPoly.evaluate(3);
        long evalNewtonTime = System.nanoTime() - startEvalNewton;
        
        System.out.println("Значение полинома при x = 3: " + valueAt3 + 
                          " (вычислено за " + formatTime(evalNewtonTime) + ")");

        // Создаем полином Лагранжа для точек (1,2), (3,4), (5,6)
        List<Point> points2 = List.of(
                new Point(1, 2),
                new Point(3, 4),
                new Point(5, 6)
        );

        long startLagrange = System.nanoTime();
        LagrangePolynomial lp = new LagrangePolynomial(points2);
        long lagrangeTime = System.nanoTime() - startLagrange;
        
        System.out.println("\nПолином Лагранжа (создан за " + formatTime(lagrangeTime) + "):");
        
        long startEvalLagrange2 = System.nanoTime();
        double valueAt2 = lp.evaluate(2);
        long evalLagrangeTime2 = System.nanoTime() - startEvalLagrange2;
        
        long startEvalLagrange4 = System.nanoTime();
        double valueAt4 = lp.evaluate(4);
        long evalLagrangeTime4 = System.nanoTime() - startEvalLagrange4;
        
        System.out.println("Значение в x=2: " + valueAt2 + 
                          " (вычислено за " + formatTime(evalLagrangeTime2) + ")"); // Ожидаем ~3.0
        System.out.println("Значение в x=4: " + valueAt4 + 
                          " (вычислено за " + formatTime(evalLagrangeTime4) + ")"); // Ожидаем ~5.0
        System.out.println("Формула полинома:");
        System.out.println(lp);
        
        // Проверка производительности для большего числа точек
        try {
            System.out.println("\nПроизводительность для больших наборов данных:");
            int largeSize = 1000;
            List<Point> largePoints = generateOptimizedRandomPoints(largeSize);
            
            long startLargeNewton = System.nanoTime();
            NewtonPolynomial largeNewton = new NewtonPolynomial(largePoints);
            long largeNewtonTime = System.nanoTime() - startLargeNewton;
            
            long startLargeLagrange = System.nanoTime();
            LagrangePolynomial largeLagrange = new LagrangePolynomial(largePoints);
            long largeLagrangeTime = System.nanoTime() - startLargeLagrange;
            
            System.out.println("Создание полинома Ньютона (" + largeSize + " точек): " + 
                              formatTime(largeNewtonTime));
            System.out.println("Создание полинома Лагранжа (" + largeSize + " точек): " + 
                              formatTime(largeLagrangeTime));
            System.out.println("Соотношение Ньютон/Лагранж: " + 
                              String.format("%.2f", (double)largeNewtonTime / largeLagrangeTime));
        } catch (Exception e) {
            System.out.println("Ошибка при тестировании больших наборов данных: " + e.getMessage());
        }
    }

    private static List<Point> generateRandomPoints(int count) {
        Random random = new Random(SEED);
        HashSet<Double> usedX = new HashSet<>();
        List<Point> points = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            double x;
            do {
                x = random.nextDouble() * 100;
            } while (usedX.contains(x));
            usedX.add(x);

            double y = random.nextDouble() * 100;
            points.add(new Point(x, y));
        }
        return points;
    }
    
    // Оптимизированная генерация точек - гарантирует уникальность X и не требует проверок
    private static List<Point> generateOptimizedRandomPoints(int count) {
        Random random = new Random(SEED);
        List<Point> points = new ArrayList<>(count);

        // Базовый шаг для гарантии разделения
        double step = 100.0 / count;
        
        for (int i = 0; i < count; i++) {
            // Гарантированно уникальные X с небольшой рандомизацией в пределах шага
            double x = i * step + random.nextDouble() * (step * 0.8);
            double y = random.nextDouble() * 100;
            points.add(new Point(x, y));
        }
        
        return points;
    }
}
